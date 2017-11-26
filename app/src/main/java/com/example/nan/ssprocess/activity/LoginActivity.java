package com.example.nan.ssprocess.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.example.nan.ssprocess.service.MyMqttService;

import java.util.LinkedHashMap;

/**
 * @author nan 2017/11/14
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "nlgLoginActivity";

    private EditText mAccountText;
    private EditText mPasswordText;
    private Button mLoginButton;

    private AlertDialog mIPSettngDialog = null;

    private ProgressDialog mLoadingProcessDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

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
        Log.d(TAG, "Login");

        mLoginButton.setEnabled(false);

        if( mLoadingProcessDialog == null) {
            mLoadingProcessDialog = new ProgressDialog(LoginActivity.this);
            mLoadingProcessDialog.setCancelable(false);
            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
            mLoadingProcessDialog.setMessage("登录中...");
        }
        mLoadingProcessDialog.show();
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("account", mAccountText.getText().toString());
        mPostValue.put("password", mPasswordText.getText().toString());
        mPostValue.put("mobile", "1");
//        if(TextUtils.isEmpty(SinSimApp.getApp().getServerIP())){
//            if(mLoadingProcessDialog.isShowing()) {
//                mLoadingProcessDialog.dismiss();
//            }
//            mLoginButton.setEnabled(true);
//            Toast.makeText(this, "服务端IP为空，请设置IP地址", Toast.LENGTH_SHORT).show();
//        } else {
//            String loginUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.LOCATION + URL.USER_LOGIN;
//            mNetwork.fetchLoginData(loginUrl, mPostValue, mLoginHandler);
//        }
        Intent intent = new Intent(LoginActivity.this,ProcessToAdminActivity.class);
        startActivity(intent);

        Intent startIntent = new Intent(LoginActivity.this, MyMqttService.class);
        // 启动服务
        startService(startIntent);

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ip_settings:
                //Toast.makeText(LoginActivity.this, "IP设置", Toast.LENGTH_SHORT).show();
                LinearLayout layout = (LinearLayout) View.inflate(LoginActivity.this, R.layout.dialog_ip_setting, null);
                final EditText editText = (EditText)layout.findViewById(R.id.ip_value);
                //读取保存的IP地址
//                String temp = SinSimApp.getApp().readValue(SinSimApp.PersistentValueType.SERVICE_IP, "");
                if(mIPSettngDialog == null) {
                    mIPSettngDialog = new AlertDialog.Builder(LoginActivity.this).create();
                }
                mIPSettngDialog.setTitle("服务端IP设置");
                mIPSettngDialog.setView(layout);
//                if(!TextUtils.isEmpty(temp)) {
//                    editText.setText(temp);
//                }
                mIPSettngDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                mIPSettngDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        try {
//                            SinSimApp.getApp().writePreferenceValue(SinSimApp.PersistentValueType.SERVICE_IP, editText.getText().toString());
//                            SinSimApp.getApp().commitValues();
//                            SinSimApp.getApp().setServerIP(editText.getText().toString());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
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
