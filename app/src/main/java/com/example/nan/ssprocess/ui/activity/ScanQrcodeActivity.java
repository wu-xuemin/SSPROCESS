package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
    private FetchTaskProcessFromIdHandler mFetchTaskProcessFromIdHandler = new FetchTaskProcessFromIdHandler();

    private ZXingView mQRCodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);

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
    }
    
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(200);
        }
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.d(TAG, "result:" + result);
        //根据result获取对应taskRecordDetail
        final String ip = SinSimApp.getApp().getServerIP();
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecordId", result);
        String fetchTaskProcessFromIdUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchTaskProcessFromId(fetchTaskProcessFromIdUrl, mPostValue, mFetchTaskProcessFromIdHandler);

        vibrate();
        mQRCodeView.startSpot();
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
                    Toast.makeText(ScanQrcodeActivity.this,"没有这个机器，请扫正规的二维码！",Toast.LENGTH_SHORT).show();
                }
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: error..."+errorMsg);
                Toast.makeText(ScanQrcodeActivity.this,"扫码有误："+errorMsg,Toast.LENGTH_SHORT).show();
            }
        }
    }
}
