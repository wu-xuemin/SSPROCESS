package com.example.nan.ssprocess.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.net.Network;
import com.example.nan.ssprocess.util.ShowMessage;
import com.example.nan.ssprocess.bean.LoginResponseData;

import java.util.LinkedHashMap;

/**
 * @author nan 2017/11/14
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "nlgLoginActivity";

    private EditText mAccountText;
    private EditText mPasswordText;
    private Button mLoginButton;
    private ProgressDialog mLoadingProcessDialog;
    private Network mNetwork;
    private LoginHandler mLoginHandler;
    private SinSimApp mApp;
    private AlertDialog mIPSettngDialog = null;
    private String mPassword=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mApp = (SinSimApp) getApplication();
        mNetwork = Network.Instance(getApplication());
        mLoginHandler = new LoginHandler();

        mLoginButton = (Button) findViewById(R.id.btn_login);
        mPasswordText = (EditText) findViewById(R.id.input_password);
        mAccountText = (EditText) findViewById(R.id.input_account);

        mLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {

        mLoginButton.setEnabled(false);

        if( mLoadingProcessDialog == null) {
            mLoadingProcessDialog = new ProgressDialog(LoginActivity.this);
            mLoadingProcessDialog.setCancelable(false);
            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
            mLoadingProcessDialog.setMessage("登录中...");
        }
        mLoadingProcessDialog.show();
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPassword=mPasswordText.getText().toString();
        mPostValue.put("account", mAccountText.getText().toString());
        mPostValue.put("password", mPassword);
        mPostValue.put("mobile", SplashActivity.IMEI);
        Log.d(TAG, "login: IMEI: "+SplashActivity.IMEI);
        Log.d(TAG, "login: IP: "+SinSimApp.getApp().getServerIP());
        if(TextUtils.isEmpty(SinSimApp.getApp().getServerIP())){
            if(mLoadingProcessDialog.isShowing()) {
                mLoadingProcessDialog.dismiss();
            }
            mLoginButton.setEnabled(true);
            Toast.makeText(this, "服务端IP为空，请设置IP地址", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "login: 服务端IP为空，请设置IP地址");
        } else {
            String loginUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.USER_LOGIN;
            Log.d(TAG, "login: url: "+loginUrl);
            mNetwork.fetchLoginData(loginUrl, mPostValue, mLoginHandler);
        }

//        Intent intent = new Intent(LoginActivity.this,ProcessToInstallActivity.class);
        Intent intent = new Intent(LoginActivity.this,DetailToInstallActivity.class);
        startActivity(intent);
//
//        // 启动服务
//
//        Intent startIntent = new Intent(LoginActivity.this, MyMqttService.class);
//        startService(startIntent);
//
//        finish();
    }

    class LoginHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if(mLoadingProcessDialog != null && mLoadingProcessDialog.isShowing()) {
                mLoadingProcessDialog.dismiss();
            }

            if (msg.what == Network.OK) {
                onLoginSuccess((LoginResponseData)msg.obj);
            } else {
                String errorMsg = (String)msg.obj;
                onLoginFailed(errorMsg);
            }
        }
    }

    public void onLoginSuccess(LoginResponseData data) {
        ShowMessage.showToast(LoginActivity.this, "登录成功!", ShowMessage.MessageDuring.SHORT);
        mLoginButton.setEnabled(true);
        if( data != null) {
            //Store to memory and preference
            mApp.setIsLogined(true, data.getAccount(), data.getFullName(), mPassword, data.getRole().getId());
            //TODO:
            /**
             * 在登陆完成后检查人员role进入不同界面
             * 生产部管理员：2，安装组长：3，质检员：11
             */
            Log.d(TAG, "onLoginSuccess: role id "+SinSimApp.getApp().getRole());

            if(SinSimApp.getApp().getRole() == 2) {
                Intent it = new Intent();
                it.setClass(LoginActivity.this, ProcessToAdminActivity.class);
                startActivity(it);
                finish();

            }else if(SinSimApp.getApp().getRole() == 11){
                //进行中，流程记录未结束
                Intent it2 = new Intent();
                it2.setClass(LoginActivity.this, ProcessToCheckoutActivity.class);
                startActivity(it2);
                finish();
            }else if(SinSimApp.getApp().getRole() == 3){
                Intent it3 = new Intent();
                it3.setClass(LoginActivity.this, ProcessToInstallActivity.class);
                startActivity(it3);
            }
            else {
                Toast.makeText(LoginActivity.this,"您无权限操作!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onLoginFailed(String msg) {
        if( msg != null) {
            ShowMessage.showDialog(LoginActivity.this, msg);
        }
        mLoginButton.setEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ip_settings:
                //Toast.makeText(LoginActivity.this, "IP设置", Toast.LENGTH_SHORT).show();
                LinearLayout layout = (LinearLayout) View.inflate(LoginActivity.this, R.layout.dialog_ip_setting, null);
                final EditText editText = (EditText)layout.findViewById(R.id.ip_value);
                //读取保存的IP地址
                String temp = SinSimApp.getApp().getServerIP();
                Log.d(TAG, "onOptionsItemSelected: "+temp);
                mIPSettngDialog = new AlertDialog.Builder(LoginActivity.this).create();
                mIPSettngDialog.setTitle("服务端IP设置");
                mIPSettngDialog.setView(layout);
                if(!TextUtils.isEmpty(temp)) {
                    editText.setText(temp);
                }
                mIPSettngDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                mIPSettngDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Log.d(TAG, "onClick: 输入ip："+editText.getText().toString());
                            SinSimApp.getApp().setServerIP(editText.getText().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                mIPSettngDialog.show();
                break;
            case R.id.password_settings:
                Toast.makeText(LoginActivity.this, "密码更改", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home,menu);
        return super.onCreateOptionsMenu(menu);
    }
}
