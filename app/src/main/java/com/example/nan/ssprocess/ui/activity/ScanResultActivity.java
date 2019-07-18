package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.ScanResultAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.TaskNodeData;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.example.nan.ssprocess.util.ShowMessage;
import com.google.gson.Gson;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author nan
 */
public class ScanResultActivity extends AppCompatActivity {

    private static String TAG = "nlgScanResultActivity";
    private ScanResultAdapter mScanResultAdapter;
    private TaskRecordMachineListData mTaskRecordMachineListData;
    private int iTaskRecordMachineListDataStatusTemp;
    private AlertDialog mInstallDialog=null;
    private AlertDialog mQaDialog=null;
    private ProgressDialog mUpdatingProcessDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        //返回前页按钮
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //获取传递过来的信息
        Intent intent = getIntent();
        final ArrayList<TaskNodeData> currentTaskList = (ArrayList<TaskNodeData>) intent.getSerializableExtra("currentTaskList");
        final ArrayList<TaskRecordMachineListData> mScanResultList = (ArrayList<TaskRecordMachineListData>) intent.getSerializableExtra("mScanResultList");
        final String mMachineNamePlate = intent.getStringExtra("mMachineNamePlate");

        TextView nameplateTv = findViewById(R.id.nameplate_tv);
        nameplateTv.setText(mMachineNamePlate);

        //列表
        RecyclerView mScanResultRv = findViewById(R.id.scan_result_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mScanResultRv.setLayoutManager(manager);
        mScanResultRv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mScanResultAdapter = new ScanResultAdapter(currentTaskList);
        mScanResultRv.setAdapter(mScanResultAdapter);
        //点击跳转，把所有接收到的数据传递给下一个activity
        mScanResultAdapter.setOnItemClickListener(new ScanResultAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(final int position) {
                if (mScanResultList.isEmpty() || mScanResultList.size() < 1) {
                    ShowMessage.showToast(ScanResultActivity.this, "无权操作该工序！", ShowMessage.MessageDuring.SHORT);
                } else {
                    boolean isTaskOwner = false;
                    for (int index=0;index<mScanResultList.size();index++){
                        if (currentTaskList.get(position).getText().equals(mScanResultList.get(index).getTaskName())) {
                            mTaskRecordMachineListData = mScanResultList.get(index);
                            isTaskOwner = true;
                            break;
                        }
                    }
                    if (!isTaskOwner){
                        ShowMessage.showToast(ScanResultActivity.this, "无权操作该工序！", ShowMessage.MessageDuring.SHORT);
                    }else {
                        Log.d(TAG, "onItemClick: gson :" + new Gson().toJson(mTaskRecordMachineListData));
                        switch (SinSimApp.getApp().getRole()) {
                            case SinSimApp.LOGIN_FOR_INSTALL:
                                if (!(mTaskRecordMachineListData.getStatus() == SinSimApp.TASK_INSTALL_WAITING)) {
                                    Intent intent = new Intent();
                                    intent.setClass(ScanResultActivity.this, DetailToInstallActivity.class);
                                    intent.putExtra("mTaskRecordMachineListData", mScanResultList.get(position));
                                    startActivity(intent);
                                    finish();
                                } else {//状态是安装中则直接开始安装
                                    mInstallDialog = new AlertDialog.Builder(ScanResultActivity.this).create();
                                    mInstallDialog.setMessage("是否现在开始安装？");
                                    mInstallDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    mInstallDialog.setButton(AlertDialog.BUTTON_POSITIVE, "是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //先改状态再执行跳转
                                            if (mUpdatingProcessDialog == null) {
                                                mUpdatingProcessDialog = new ProgressDialog(ScanResultActivity.this);
                                                mUpdatingProcessDialog.setCancelable(false);
                                                mUpdatingProcessDialog.setCanceledOnTouchOutside(false);
                                                mUpdatingProcessDialog.setMessage("正在开始...");
                                            }
                                            mUpdatingProcessDialog.show();
                                            //获取当前时间
                                            @SuppressLint("SimpleDateFormat")
                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                                            Date curDate = new Date(System.currentTimeMillis());
                                            String staCurTime = formatter.format(curDate);
                                            mTaskRecordMachineListData.setInstallBeginTime(staCurTime);
                                            iTaskRecordMachineListDataStatusTemp = mTaskRecordMachineListData.getStatus();
                                            updateProcessDetailData(SinSimApp.TASK_INSTALLING);

                                            // 扫码完写入时间和结果到本地
                                            String path = Environment.getExternalStorageDirectory().getPath() + "/Xiaomi";
                                            String name = "/ScanResultRecorder.txt";
                                            String strFilePath = path + name;
                                            try {
                                                File filePath = null;
                                                filePath = new File(strFilePath);
                                                if (!filePath.exists()) {
                                                    filePath.getParentFile().mkdirs();
                                                    filePath.createNewFile();
                                                }
                                                String scanResultRecord = curDate + ":  " + mTaskRecordMachineListData.getMachineData().getNameplate() + mTaskRecordMachineListData.getTaskName() + " [start]！\r\n";
                                                RandomAccessFile raf = new RandomAccessFile(filePath, "rwd");
                                                raf.seek(filePath.length());
                                                raf.write(scanResultRecord.getBytes());
                                                raf.close();
                                            } catch (Exception e) {
                                                Log.i("error:", e + "");
                                            }
                                        }
                                    });
                                    mInstallDialog.show();
                                }
                                break;
                            case SinSimApp.LOGIN_FOR_QA:
                                if (mTaskRecordMachineListData.getStatus() == SinSimApp.TASK_INSTALLED) {
                                    mQaDialog = new AlertDialog.Builder(ScanResultActivity.this).create();
                                    mQaDialog.setMessage("是否现在开始质检？");
                                    mQaDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    mQaDialog.setButton(AlertDialog.BUTTON_POSITIVE, "是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //先改状态再执行跳转
                                            if (mUpdatingProcessDialog == null) {
                                                mUpdatingProcessDialog = new ProgressDialog(ScanResultActivity.this);
                                                mUpdatingProcessDialog.setCancelable(false);
                                                mUpdatingProcessDialog.setCanceledOnTouchOutside(false);
                                                mUpdatingProcessDialog.setMessage("正在开始...");
                                            }
                                            mUpdatingProcessDialog.show();
                                            //获取当前时间
                                            @SuppressLint("SimpleDateFormat")
                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                                            Date curDate = new Date(System.currentTimeMillis());
                                            String staCurTime = formatter.format(curDate);
                                            mTaskRecordMachineListData.setQualityBeginTime(staCurTime);
                                            iTaskRecordMachineListDataStatusTemp = mTaskRecordMachineListData.getStatus();
                                            updateProcessDetailData(SinSimApp.TASK_QUALITY_DOING);
                                        }
                                    });
                                    mQaDialog.show();
                                } else {
                                    Intent intent1 = new Intent();
                                    intent1.setClass(ScanResultActivity.this, DetailToCheckoutActivity.class);
                                    intent1.putExtra("mTaskRecordMachineListData", mTaskRecordMachineListData);
                                    startActivity(intent1);
                                    finish();
                                }
                                break;
                            default:
                                Toast.makeText(ScanResultActivity.this, "账号错误，请检查登入账号!", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }
            }
        });
    }

    private void updateProcessDetailData(int status) {
        //更新loaction状态
        mTaskRecordMachineListData.setStatus(status);
        Gson gson=new Gson();
        String taskRecordDataToJson = gson.toJson(mTaskRecordMachineListData);
        Log.d(TAG, "onItemClick: gson :"+ taskRecordDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecord", taskRecordDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.UPDATE_TASK_RECORD_STATUS;
        Log.d(TAG, "updateProcessDetailData: "+updateProcessRecordUrl+mPostValue.get("taskRecord"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, new UpdateProcessDetailDataHandler());
    }

    @SuppressLint("HandlerLeak")
    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mUpdatingProcessDialog != null && mUpdatingProcessDialog.isShowing()) {
                mUpdatingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                if (SinSimApp.getApp().getRole()==SinSimApp.LOGIN_FOR_INSTALL) {
                    Intent intent = new Intent();
                    intent.setClass(ScanResultActivity.this, DetailToInstallActivity.class);
                    intent.putExtra("mTaskRecordMachineListData", mTaskRecordMachineListData);
                    startActivity(intent);
                    finish();
                    Toast.makeText(ScanResultActivity.this, "请开始安装！", Toast.LENGTH_SHORT).show();
                } else if (SinSimApp.getApp().getRole()==SinSimApp.LOGIN_FOR_QA){
                    Intent intent = new Intent();
                    intent.setClass(ScanResultActivity.this, DetailToCheckoutActivity.class);
                    intent.putExtra("mTaskRecordMachineListData", mTaskRecordMachineListData);
                    startActivity(intent);
                    finish();
                    Toast.makeText(ScanResultActivity.this, "请开始质检！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ScanResultActivity.this,"账号错误，请检查登入账号!", Toast.LENGTH_SHORT).show();
                }

                // 扫码完写入时间和结果到本地
                String path = Environment.getExternalStorageDirectory().getPath() + "/Xiaomi";
                String name = "/ScanResultRecorder.txt";
                String strFilePath = path + name;
                try{
                    File filePath=null;
                    filePath = new File(strFilePath);
                    if (!filePath.exists()) {
                        filePath.getParentFile().mkdirs();
                        filePath.createNewFile();
                    }
                    String scanResultRecord = mTaskRecordMachineListData.getMachineData().getNameplate() + mTaskRecordMachineListData.getTaskName() + " [start OK]！\r\n";
                    RandomAccessFile raf = new RandomAccessFile(filePath, "rwd");
                    raf.seek(filePath.length());
                    raf.write(scanResultRecord.getBytes());
                    raf.close();
                } catch (Exception e) {
                    Log.i("error:", e+"");
                }
            } else {
                mTaskRecordMachineListData.setStatus(iTaskRecordMachineListDataStatusTemp);
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(ScanResultActivity.this, "网络错误，无法开始，请检查网络！", Toast.LENGTH_SHORT).show();

                // 扫码完写入时间和结果到本地
                String path = Environment.getExternalStorageDirectory().getPath() + "/Xiaomi";
                String name = "/ScanResultRecorder.txt";
                String strFilePath = path + name;
                try{
                    File filePath=null;
                    filePath = new File(strFilePath);
                    if (!filePath.exists()) {
                        filePath.getParentFile().mkdirs();
                        filePath.createNewFile();
                    }
                    String scanResultRecord = mTaskRecordMachineListData.getMachineData().getNameplate() + mTaskRecordMachineListData.getTaskName() + " [start FAIL]！\r\n";
                    RandomAccessFile raf = new RandomAccessFile(filePath, "rwd");
                    raf.seek(filePath.length());
                    raf.write(scanResultRecord.getBytes());
                    raf.close();
                } catch (Exception e) {
                    Log.i("error:", e+"");
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mInstallDialog != null) {
            mInstallDialog.dismiss();
        }
        if(mUpdatingProcessDialog != null) {
            mUpdatingProcessDialog.dismiss();
        }
        if(mQaDialog != null) {
            mQaDialog.dismiss();
        }
    }
}
