package com.example.nan.ssprocess.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.net.Network;
import com.example.nan.ssprocess.util.ShowMessage;

import java.util.LinkedHashMap;
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
            {Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Network mNetwork;
    private FetchProcessRecordStatusHandler mFetchProcessRecordStatusHandler;
    private Handler mTimeoutHandler;
    private Runnable mTimeOutRunnable;

    public static String IMEI = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_FULLSCREEN | localLayoutParams.flags);
        setContentView(R.layout.activity_splash);

        mNetwork = Network.Instance(SinSimApp.getApp());
        mFetchProcessRecordStatusHandler = new FetchProcessRecordStatusHandler();
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
        boolean  isLogin  = SinSimApp.getApp().isLogined();
//        boolean  isLogin  = true;
        if(isLogin) {
            final String account = SinSimApp.getApp().getAccount();
            final String password = SinSimApp.getApp().getPassword();
            //(1)检查账号密码是否存在
            if(account.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "用户未登录！", Toast.LENGTH_LONG).show();
                jumpToLoginAct();
            } else {
                //(2)检查网络连接是否正常
                if(!mNetwork.isNetworkConnected()) {
                    Toast.makeText(this, "网络无法连接，请检查！", Toast.LENGTH_LONG).show();
                    jumpToLoginAct();
                } else {

                    @SuppressLint("StaticFieldLeak")
                    final AsyncTask task = new AsyncTask() {
                        @Override
                        protected void onCancelled() {
                            super.onCancelled();
                        }

                        @Override
                        protected Object doInBackground(Object[] params) {
                            //检查账号密码是否正确，正确的话返回流程的状态
                            LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                            Log.d(TAG, "doInBackground: "+SinSimApp.getApp().getServerIP()+account+password+IMEI);
                            mPostValue.put("account", account);
                            mPostValue.put("password", password);
                            mPostValue.put("mobile", IMEI);
                            String fetchProcessRecordURL = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.USER_LOGIN;
                            mNetwork.fetchProcessRecordStatusData(fetchProcessRecordURL, mPostValue, mFetchProcessRecordStatusHandler);
                            return null;
                        }
                    };
                    task.execute();

                    //联系服务器，获取未结束的流程
                    // 5秒内返回，从handler中移除runnable，检查返回状态[error:{'账号密码错误'，'其他错误'; success:{'无未结束作业流程', '作业流程创建中', '作业流程已开始'}}]
                    mTimeoutHandler = new Handler();
                    mTimeOutRunnable = new Runnable() {
                        @Override
                        public void run() {
                            //if timeout, finish async task, notify user the network error
                            // and jump to login activity
                            task.cancel(true);
                            Toast.makeText(SplashActivity.this, "网络连接错误，请检查无线连接是否在同一个局域网，以及服务端的IP设置是否正确！",
                                    Toast.LENGTH_LONG).show();
                            jumpToLoginAct();
                        }
                    };
                    mTimeoutHandler.postDelayed(mTimeOutRunnable,8000);
                }
            }
        } else {
            jumpToLoginAct();
        }

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
    private class FetchProcessRecordStatusHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            //网络请求返回，移除超时的Runnable
            mTimeoutHandler.removeCallbacks(mTimeOutRunnable);
            if (msg.what == Network.OK) {
                onFetchProcessDataSuccess((String)msg.obj);
            } else {
                String errorMsg = (String)msg.obj;
                onFetchProcessDataFailed(errorMsg);
            }
        }
    }

    private void onFetchProcessDataSuccess(final String processStatus) {
        //Toast.makeText(this,"Fetch Successful!", Toast.LENGTH_SHORT).show();

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
                if(SinSimApp.getApp().getRole() == 2) {
                    Intent it = new Intent();
                    it.setClass(SplashActivity.this, ProcessToAdminActivity.class);
                    startActivity(it);
                    finish();

                }else if(SinSimApp.getApp().getRole() == 11){
                    //进行中，流程记录未结束
                    Intent it2 = new Intent();
                    it2.setClass(SplashActivity.this, ProcessToCheckoutActivity.class);
                    startActivity(it2);
                    finish();
                }else if(SinSimApp.getApp().getRole() == 3){
                    Intent it3 = new Intent();
                    it3.setClass(SplashActivity.this, ProcessToInstallActivity.class);
                    startActivity(it3);
                }
                else {
                    Toast.makeText(SplashActivity.this,"您无权限操作!", Toast.LENGTH_SHORT).show();
                    jumpToLoginAct();
                }
            }
        });
        logoText.startAnimation(animation);
    }

    private void onFetchProcessDataFailed(String errorMsg) {
        Toast.makeText(this,errorMsg, Toast.LENGTH_SHORT).show();
        jumpToLoginAct();
    }

    private void checkIMEI() {
        IMEI = getIMEI();
        Log.d(TAG, "checkIMEI: "+IMEI);
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
