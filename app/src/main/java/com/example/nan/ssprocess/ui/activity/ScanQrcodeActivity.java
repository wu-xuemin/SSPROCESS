package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.util.LinkedHashMap;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

/**
 * @author nan 2017/11/20
 */
public class ScanQrcodeActivity extends AppCompatActivity implements QRCodeView.Delegate{

    private static final String TAG = "nlgScanQrcodeActivity";
    private AlertDialog scanQrResultDialog;
    private ZXingView mQRCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mQRCodeView = (ZXingView) findViewById(R.id.zxingview);
        mQRCodeView.setDelegate(this);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        mQRCodeView.startCamera();
        mQRCodeView.showScanRect();
        Log.d(TAG, "onStart: startCamera");
        mQRCodeView.startSpot();

    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
        if (scanQrResultDialog!=null){
            scanQrResultDialog.dismiss();
        }
    }
    
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(200);
        }
    }

    @Override
    public void onScanQRCodeSuccess(final String result) {
        Log.d(TAG, "result:" + result);
        scanQrResultDialog = new AlertDialog.Builder(ScanQrcodeActivity.this).create();
        scanQrResultDialog.setTitle("扫描结果");
        scanQrResultDialog.setMessage("扫描结果： "+result);
        scanQrResultDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //根据result获取对应taskRecordDetail
                        Intent intent = getIntent();
                        intent.putExtra("mMachineStrId", result);
                        ScanQrcodeActivity.this.setResult(RESULT_OK, intent);
                        ScanQrcodeActivity.this.finish();
//                        final String ip = SinSimApp.getApp().getServerIP();
//                        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
//                        mPostValue.put("taskRecordId", result);
//                        String fetchTaskProcessFromIdUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_DETAIL;
//                        Network.Instance(SinSimApp.getApp()).fetchTaskProcessFromId(fetchTaskProcessFromIdUrl, mPostValue, new FetchTaskProcessFromIdHandler());
                    }
                });
        scanQrResultDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"重新扫描",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //重新扫描
                        mQRCodeView.startSpot();
                    }
                });
        // 显示
        scanQrResultDialog.show();
        //震动
        vibrate();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "open camera fail!");
    }

    @SuppressLint("HandlerLeak")
    private class FetchTaskProcessFromIdHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                Log.d(TAG, "handleMessage: ok...");
                //获取结果
                TaskMachineListData mTaskMachineDetailData = (TaskMachineListData) msg.obj;

                if (mTaskMachineDetailData!=null) {
                    Log.d(TAG, "handleMessage: " + new Gson().toJson(mTaskMachineDetailData));
                    //结果传递回上一个界面
                    Intent intent = getIntent();
                    intent.putExtra("mTaskMachineListData", mTaskMachineDetailData);
                    ScanQrcodeActivity.this.setResult(RESULT_OK, intent);
                    ScanQrcodeActivity.this.finish();
                } else {
                    //二维码不匹配，重新启动扫描
                    Toast.makeText(ScanQrcodeActivity.this,"没有这个机器，请扫正规的二维码！",Toast.LENGTH_SHORT).show();
                    mQRCodeView.startSpot();
                }
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: error..."+errorMsg);
                Toast.makeText(ScanQrcodeActivity.this,"网络错误："+errorMsg,Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}
