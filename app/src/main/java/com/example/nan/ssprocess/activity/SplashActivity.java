package com.example.nan.ssprocess.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * @author nan 2017/11/15
 */
public class SplashActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "nlgSplashActivity";
    private static final int REQUEST_SOME_PERMISSIONS = 111;
    private static final String[] APP_NEEDS_PERMISSIONS =
            {Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA};
    private static String IMEI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_FULLSCREEN | localLayoutParams.flags);
        setContentView(R.layout.activity_splash);
        //申请权限
        requestSomePermissions();
    }

    @AfterPermissionGranted(REQUEST_SOME_PERMISSIONS)
    public void requestSomePermissions(){
        if(!EasyPermissions.hasPermissions(this, APP_NEEDS_PERMISSIONS)){
            EasyPermissions.requestPermissions(
                    this,
                    "app需要这些权限才能正常运行",
                    REQUEST_SOME_PERMISSIONS,
                    APP_NEEDS_PERMISSIONS);
        }else {
            init();
        }

    }

    private void init(){
        //检查机器IMEI
        checkIMEI();

        //检查preference中的isLogin状态

        jumpToLoginAct();

    }

    private void checkIMEI() {
        IMEI = getIMEI();
        Log.d(TAG, "checkIMEI: "+IMEI);
        //数据库读取已登记设备进行比对
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private String getIMEI() {
        String idIMEI = null;
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            idIMEI = telephonyManager.getDeviceId();
        }else {
            Log.d(TAG, "getIMEI: have some error");
        }
        return idIMEI;
    }

    private void jumpToLoginAct() {
        final TextView logoText = (TextView)findViewById(R.id.crh_text);
        // 设置加载动画透明度渐变从（0.1不显示-1.0完全显示）
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.1f);
        // 设置动画时间5s
        animation.setDuration(2000);
        // 将组件与动画关联
        logoText.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            // 动画开始时执行
            @Override
            public void onAnimationStart(Animation animation) {

            }
            // 动画重复时执行
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
            // 动画结束时执行
            @Override
            public void onAnimationEnd(Animation animation) {
                logoText.setVisibility(View.INVISIBLE);
                Intent it = new Intent();
                it.setClass(SplashActivity.this, LoginActivity.class);
                it.putExtra("act", "SplashActivity");
                startActivity(it);
                finish();
            }
        });
        logoText.startAnimation(animation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }else {
            requestSomePermissions();
        }
    }
}
