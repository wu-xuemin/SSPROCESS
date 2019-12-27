package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.AttendanceData;
import com.example.nan.ssprocess.bean.basic.InstallPlanData;
import com.example.nan.ssprocess.bean.basic.UserData;
import com.example.nan.ssprocess.net.Network;
import com.example.nan.ssprocess.service.MyMqttService;
import com.example.nan.ssprocess.util.ShowMessage;
import com.google.gson.Gson;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "nlg";
    private Intent mqttIntent;

    private ProgressDialog mLoadingProcessDialog;
    private LinkedHashMap<String, String> mPostValue;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// HH:mm:ss
    private Date date;
    final String IP = SinSimApp.getApp().getServerIP();

    private AttendanceData mAttendanceData;
    private boolean mAttendanceFlag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        //启动MQTT服务
        mqttIntent = new Intent(this, MyMqttService.class);
        startService(mqttIntent);
    }

    public void onNextPlan(View view) {
        mPostValue = new LinkedHashMap<>();
        mPostValue.put("installGroupName", ""+ SinSimApp.getApp().getGroupName());
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,1);
        String tomorrow = simpleDateFormat.format(c.getTime());
        Log.d(TAG, "onCreate: "+tomorrow);
        mPostValue.put("queryStartTime", tomorrow);

        String fetchInstallPlanUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.FATCH_INSTALL_PLAN;
        Network.Instance(SinSimApp.getApp()).fetchInstallPlan(fetchInstallPlanUrl, mPostValue, new FetchInstallPlanHandler());

        if( mLoadingProcessDialog == null) {
            mLoadingProcessDialog = new ProgressDialog(MenuActivity.this);
            mLoadingProcessDialog.setCancelable(false);
            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
            mLoadingProcessDialog.setMessage("获取信息中...");
        }
        mLoadingProcessDialog.show();
    }

    public void onTodayFinished(View view) {
        mPostValue = new LinkedHashMap<>();
        mPostValue.put("installGroupName", ""+SinSimApp.getApp().getGroupName());
        date = new Date(System.currentTimeMillis());
        mPostValue.put("isNotFinished", "true");
        mPostValue.put("queryFinishTime", simpleDateFormat.format(date));

        String fetchInstallPlanUrl2 = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.FATCH_INSTALL_PLAN;
        Network.Instance(SinSimApp.getApp()).fetchInstallPlan(fetchInstallPlanUrl2, mPostValue, new FetchInstallActualHandler());

        if( mLoadingProcessDialog == null) {
            mLoadingProcessDialog = new ProgressDialog(MenuActivity.this);
            mLoadingProcessDialog.setCancelable(false);
            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
            mLoadingProcessDialog.setMessage("获取信息中...");
        }
        mLoadingProcessDialog.show();
    }

    public void onAttendance(View view) {
        mPostValue = new LinkedHashMap<>();
        mPostValue.put("installGroupName", ""+SinSimApp.getApp().getGroupName());
        date = new Date(System.currentTimeMillis());
        mPostValue.put("queryStartTime", simpleDateFormat.format(date));
        mPostValue.put("queryFinishTime", simpleDateFormat.format(date));
        String fetchAttendanceUrl = URL.HTTP_HEAD + IP + URL.FATCH_ATTENDANCE;
        Network.Instance(SinSimApp.getApp()).fetchAttendance(fetchAttendanceUrl, mPostValue, new FetchAttendanceHandler());
    }

    @SuppressLint("HandlerLeak")
    private class FetchInstallPlanHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mLoadingProcessDialog != null && mLoadingProcessDialog.isShowing()) {
                mLoadingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                ArrayList<InstallPlanData> mInstallPlanList = (ArrayList<InstallPlanData>) msg.obj;
                Log.d(TAG, "handleMessage: "+(new Gson().toJson(mInstallPlanList)));

                if (mInstallPlanList.size()<1){
                    ShowMessage.showDialog(MenuActivity.this,"尚未安排明日计划！");
                }else {
                    Intent intent=new Intent(MenuActivity.this,InstallPlanActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("mInstallPlanList", (Serializable) mInstallPlanList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        }
    }
    @SuppressLint("HandlerLeak")
    private class FetchInstallActualHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mLoadingProcessDialog != null && mLoadingProcessDialog.isShowing()) {
                mLoadingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                ArrayList<InstallPlanData> mInstallPlanActualList = (ArrayList<InstallPlanData>) msg.obj;
                Log.d(TAG, "handleMessage: "+(new Gson().toJson(mInstallPlanActualList)));

                if (mInstallPlanActualList.size()<1){
                    ShowMessage.showDialog(MenuActivity.this,"没有待完成计划！");
                }else {
                    Intent intent=new Intent(MenuActivity.this,InstallActualActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("mInstallPlanActualList", (Serializable) mInstallPlanActualList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class FetchAttendanceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == Network.OK) {
                Log.d(TAG, "handleMessage: "+(new Gson().toJson(msg.obj)));
                ArrayList<AttendanceData> attendanceDataArrayList = (ArrayList<AttendanceData>) msg.obj;
                if (attendanceDataArrayList.size()>0){
                    mAttendanceData = attendanceDataArrayList.get(0);
                    mAttendanceFlag = true;
                    AlertDialog continueDialog = new AlertDialog.Builder(MenuActivity.this).create();
                    continueDialog.setTitle("考勤");
                    continueDialog.setMessage("今天传过考勤了");
                    continueDialog.setButton(AlertDialog.BUTTON_POSITIVE,"重新上传", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                            mPostValue.put("id", "" + SinSimApp.getApp().getAppUserId());
                            String fetchInstallerListUrl = URL.HTTP_HEAD + IP + URL.FATCH_GROUP_BY_USERID;
                            Network.Instance(SinSimApp.getApp()).fetchInstallerList(fetchInstallerListUrl, mPostValue, new FetchInstallerGroupHandler());
                        }
                    });
                    continueDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"取消", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    continueDialog.show();
//                    ShowMessage.showDialog(MenuActivity.this,"今天传过考勤了！");
                }else {
                    mAttendanceFlag = false;
                    LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                    mPostValue.put("id", "" + SinSimApp.getApp().getAppUserId());
                    String fetchInstallerListUrl = URL.HTTP_HEAD + IP + URL.FATCH_GROUP_BY_USERID;
                    Network.Instance(SinSimApp.getApp()).fetchInstallerList(fetchInstallerListUrl, mPostValue, new FetchInstallerGroupHandler());
                }
            }
        }
    }
    @SuppressLint("HandlerLeak")
    private class FetchInstallerGroupHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                final ArrayList<UserData> mInstallerList = (ArrayList<UserData>) msg.obj;
                Log.d(TAG, "安装组人数: "+mInstallerList.size());

                AlertDialog attendanceSettingDialog = null;
                LinearLayout layout = (LinearLayout) View.inflate(MenuActivity.this, R.layout.dialog_attendance_setting, null);
                TextView textViewTp = (TextView)layout.findViewById(R.id.total_population);
                final EditText editTextWp = (EditText)layout.findViewById(R.id.work_population);
                final EditText editTextOp = (EditText)layout.findViewById(R.id.overtime_population);
                final EditText editTextLp = (EditText)layout.findViewById(R.id.leave_population);
                final EditText editTextTwp = (EditText)layout.findViewById(R.id.tomorrow_work_population);
                final AttendanceData attendanceData = new AttendanceData();
                attendanceSettingDialog = new AlertDialog.Builder(MenuActivity.this).create();
                attendanceSettingDialog.setTitle("考勤信息");
                attendanceSettingDialog.setView(layout);
                textViewTp.setText(String.valueOf(mInstallerList.size()));
                if (mAttendanceFlag){
                    editTextWp.setText(mAttendanceData.getAttendanceMember());
                    editTextOp.setText(mAttendanceData.getOvertimeMember());
                    editTextLp.setText(mAttendanceData.getAbsenceMember());
                    editTextTwp.setText(mAttendanceData.getAttendanceTomorrow());
                }
                attendanceSettingDialog.setButton(AlertDialog.BUTTON_POSITIVE, "上传", new DialogInterface.OnClickListener() {
                    class CreateAttendenceHandler extends Handler {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if (msg.what == Network.OK) {
                                ShowMessage.showToast(MenuActivity.this,"考勤上传成功！",ShowMessage.MessageDuring.SHORT);
                            }else {
                                ShowMessage.showDialog(MenuActivity.this,"出错！请检查网络！");
                            }
                        }
                    }

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (editTextWp.getText().toString().length()<1
                                    || editTextOp.getText().toString().length()<1
                                    || editTextLp.getText().toString().length()<1
                                    || editTextTwp.getText().toString().length()<1
                            ){
                                ShowMessage.showDialog(MenuActivity.this,"出错！请填写完整再上传！");
                            } else if (Integer.parseInt(editTextWp.getText().toString()) > mInstallerList.size()
                                    || Integer.parseInt(editTextOp.getText().toString()) > mInstallerList.size()
                                    || Integer.parseInt(editTextLp.getText().toString()) > mInstallerList.size()
                                    || Integer.parseInt(editTextTwp.getText().toString()) > mInstallerList.size()
                            ){
                                ShowMessage.showDialog(MenuActivity.this,"出错！人数不能大于实际人数！");
                            }else {
                                attendanceData.setAttendanceMember(editTextWp.getText().toString());
                                attendanceData.setOvertimeMember(editTextOp.getText().toString());
                                attendanceData.setAbsenceMember(editTextLp.getText().toString());
                                attendanceData.setAttendanceTomorrow(editTextTwp.getText().toString());
                                attendanceData.setUserId(SinSimApp.getApp().getAppUserId());
                                attendanceData.setInstallGroupId(SinSimApp.getApp().getGroupId());

                                LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                                mPostValue.put("attendance", new Gson().toJson(attendanceData));

                                if (mAttendanceFlag) {
                                    String updateAttendenceUrl = URL.HTTP_HEAD + IP + URL.UPDATE_ATTENDANCE;
                                    Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateAttendenceUrl, mPostValue, new CreateAttendenceHandler());
                                }else {
                                    String createAttendenceUrl = URL.HTTP_HEAD + IP + URL.CREATE_ATTENDANCE;
                                    Network.Instance(SinSimApp.getApp()).updateProcessRecordData(createAttendenceUrl, mPostValue, new CreateAttendenceHandler());
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                attendanceSettingDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"取消", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                attendanceSettingDialog.show();
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "FetchInstallerGroupHandler handleMessage: "+errorMsg);
                ShowMessage.showToast(MenuActivity.this,"网络错误！"+errorMsg, ShowMessage.MessageDuring.SHORT);
            }
        }
    }

    public void onExit(View view) {
        if(mLoadingProcessDialog != null && mLoadingProcessDialog.isShowing()) {
            mLoadingProcessDialog.dismiss();
        }
        stopService(mqttIntent);
        SinSimApp.getApp().setLogOut();
        Intent it = new Intent();
        it.setClass(MenuActivity.this, LoginActivity.class);
        startActivity(it);
        finish();
    }

    @Override
    protected void onDestroy() {
        if(mLoadingProcessDialog != null && mLoadingProcessDialog.isShowing()) {
            mLoadingProcessDialog.dismiss();
        }
        super.onDestroy();
    }
}
