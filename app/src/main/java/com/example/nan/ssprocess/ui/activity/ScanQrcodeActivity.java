package com.example.nan.ssprocess.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.nan.ssprocess.R;

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
