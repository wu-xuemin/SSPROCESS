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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.InstallActualAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.InstallActualData;
import com.example.nan.ssprocess.bean.basic.InstallPlanData;
import com.example.nan.ssprocess.net.Network;
import com.example.nan.ssprocess.util.ShowMessage;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import cn.bingoogolapple.refreshlayout.BGAMoocStyleRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

public class InstallActualActivity extends AppCompatActivity{

    private static final String TAG = "nlg";
    private InstallActualAdapter mInstallActualAdapter;
    private ArrayList<InstallPlanData> mInstallPlanActualList = new ArrayList<>();
    private ArrayList<InstallActualData> mInstallActualList = new ArrayList<>();
    private TextView machineCountTv;
    private TextView mMachineFinishTv;
    private TextView mHeadFinishTv;
    private BGARefreshLayout mRefreshLayout;
    private LinearLayout mHeadFinishLayout;
    private LinearLayout mMachineFinishLayout;
    private ProgressDialog mLoadingProcessDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_actual);
        //获取传递过来的信息
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mInstallPlanActualList = (ArrayList<InstallPlanData>) bundle.getSerializable("mInstallPlanActualList");

        machineCountTv = (TextView) findViewById(R.id.actual_machine_count);
        mMachineFinishTv = (TextView) findViewById(R.id.machine_finish_tv);
        mHeadFinishTv = (TextView) findViewById(R.id.head_finish_tv);
        mHeadFinishLayout = (LinearLayout)findViewById(R.id.head_finish_ll);
        mMachineFinishLayout = (LinearLayout)findViewById(R.id.machine_finish_ll);
        mHeadFinishLayout.setVisibility(View.GONE);

//        TextView headCountTv = findViewById(R.id.actual_head_count);
        machineCountTv.setText(""+mInstallPlanActualList.size());
//        int headCountSum = 0;
//
//        for (int i =0;i<mInstallPlanActualList.size();i++) {
//            headCountSum += Integer.valueOf(mInstallPlanActualList.get(i).getHeadNum())-mInstallPlanActualList.get(i).getHeadCountDone();
//        }
//        headCountTv.setText(""+headCountSum);
        //列表
        RecyclerView mInstallActualRV = (RecyclerView) findViewById(R.id.install_actual_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mInstallActualRV.setLayoutManager(manager);
        mInstallActualAdapter = new InstallActualAdapter(mInstallPlanActualList);
        mInstallActualRV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mInstallActualRV.setAdapter(mInstallActualAdapter);
        //点击弹窗
        mInstallActualAdapter.setOnItemClickListener(new InstallActualAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(final int position){
                /*
                Log.d(TAG, "onItemClick: gson :"+new Gson().toJson(mInstallPlanActualList.get(position)));

                AlertDialog installActualDialog = null;
                LinearLayout layout = (LinearLayout) View.inflate(InstallActualActivity.this, R.layout.dialog_install_actual, null);
                final EditText headCountDoneEt = (EditText)layout.findViewById(R.id.head_count_done);
                final EditText cmtFeedbackEt = (EditText)layout.findViewById(R.id.cmt_feedback);
                final EditText pcWireEt = (EditText)layout.findViewById(R.id.pc_wire_num);
                final EditText kouxianEt = (EditText)layout.findViewById(R.id.kouxian_num);
                final EditText lightWireEt = (EditText)layout.findViewById(R.id.light_wire_num);
                final EditText warnSignalEt = (EditText)layout.findViewById(R.id.warn_signal_num);
                final EditText deviceSignalEt = (EditText)layout.findViewById(R.id.device_signal_num);
                final EditText warnPowerEt = (EditText)layout.findViewById(R.id.warn_power_num);
                final EditText devicePowerEt = (EditText)layout.findViewById(R.id.device_power_num);
                final EditText deviceBuxiuEt = (EditText)layout.findViewById(R.id.device_buxiu_num);
                final EditText deviceSwitchEt = (EditText)layout.findViewById(R.id.device_switch_num);

                LinearLayout headNumberLL = layout.findViewById(R.id.actual_head_number_ll);
                LinearLayout hanxianLL = layout.findViewById(R.id.hanxian_ll);
                hanxianLL.setVisibility(View.GONE);//焊线组信息需求变更

                if (SinSimApp.getApp().getGroupName().equals("焊线组")) {
                    headNumberLL.setVisibility(View.GONE);
                }else {
                    headNumberLL.setVisibility(View.VISIBLE);
                }
                TextView headCountTv = (TextView)layout.findViewById(R.id.head_count_tv);
//                final int headCount = Integer.parseInt(mInstallPlanActualList.get(position).getHeadNum()) - mInstallPlanActualList.get(position).getHeadCountDone();
//                headCountTv.setText(""+headCount);
                headCountTv.setText(mInstallPlanActualList.get(position).getHeadNum());

                installActualDialog = new AlertDialog.Builder(InstallActualActivity.this).create();
                installActualDialog.setTitle("完成情况");
                installActualDialog.setView(layout);
                installActualDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                installActualDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @SuppressLint("HandlerLeak")
                    class CreateInstallPlanActualHandler extends Handler {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if (msg.what == Network.OK) {
                                ShowMessage.showToast(InstallActualActivity.this,"上传成功！",ShowMessage.MessageDuring.SHORT);
                                mInstallPlanActualList.get(position).setHeadCountDone(Integer.parseInt(headCountDoneEt.getText().toString()) + mInstallPlanActualList.get(position).getHeadCountDone());
                                //更新adapter
                                mInstallActualAdapter.notifyDataSetChanged();
                            }else {
                                ShowMessage.showDialog(InstallActualActivity.this,msg.obj.toString());
                            }
                        }
                    }
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (headCountDoneEt.getText().toString().length()<1){
                                ShowMessage.showDialog(InstallActualActivity.this,"出错！请填写完整再上传！");
                            }else {
                                InstallActualData installActualData = new InstallActualData();
                                installActualData.setHeadCountDone(headCountDoneEt.getText().toString());
                                installActualData.setCmtFeedback(cmtFeedbackEt.getText().toString());
                                installActualData.setInstallPlanId(mInstallPlanActualList.get(position).getId());
                                if (SinSimApp.getApp().getGroupName().equals("焊线组")) {


                                    installActualData.setPcWireNum(pcWireEt.getText().toString());
                                    installActualData.setKouxianNum(kouxianEt.getText().toString());
                                    installActualData.setLightWireNum(lightWireEt.getText().toString());
                                    installActualData.setWarnSignalNum(warnSignalEt.getText().toString());
                                    installActualData.setDeviceSignalNum(deviceSignalEt.getText().toString());
                                    installActualData.setWarnPowerNum(warnPowerEt.getText().toString());
                                    installActualData.setDevicePowerNum(devicePowerEt.getText().toString());
                                    installActualData.setDeviceBuxiuNum(deviceBuxiuEt.getText().toString());
                                    installActualData.setDeviceSwitchNum(deviceSwitchEt.getText().toString());

                                }
                                LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                                mPostValue.put("installPlanActual", "" + new Gson().toJson(installActualData));
                                Log.d(TAG, "onClick: " + new Gson().toJson(installActualData));

                                String createInstallPlanActualUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.CREATE_INSTALL_PLAN_ACTUAL;
                                Network.Instance(SinSimApp.getApp()).updateProcessRecordData(createInstallPlanActualUrl, mPostValue, new CreateInstallPlanActualHandler());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                installActualDialog.show();
                */
            }

            @Override
            public void onFinishItemClick(int position) {
                InstallActualData installActualData = new InstallActualData();
                installActualData.setInstallPlanId(mInstallPlanActualList.get(position).getId());
                String strHead = mInstallPlanActualList.get(position).getHeadNum();
                if (strHead.contains("+")){
                    String split[] = strHead.split("\\+");
                    strHead = String.valueOf(Integer.parseInt(split[0]) + Integer.parseInt(split[1]));
                }
                installActualData.setHeadCountDone(Integer.parseInt(strHead));
                if (mInstallActualList.size()<1) {
                    mInstallActualList.add(installActualData);
                } else {
                    boolean flag = true;
                    for (int i = 0; i < mInstallActualList.size();i++){
                        if (installActualData.getInstallPlanId() == mInstallActualList.get(i).getInstallPlanId()){
                            flag = false;
                            break;
                        }
                    }
                    if (flag){
                        mInstallActualList.add(installActualData);
                    }
                }
                mInstallPlanActualList.get(position).setHeadCountDone(installActualData.getHeadCountDone());
                mInstallPlanActualList.get(position).setHasFinished(true);
                mMachineFinishTv.setText(String.valueOf(mInstallActualList.size()));
                mInstallActualAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNotFinishItemClick(final int position) {
                Log.d(TAG, "onNotFinishItemClick: 未完成");
                Log.d(TAG, "onNotFinishItemClick: "+new Gson().toJson(mInstallPlanActualList.get(position)));

                LinearLayout layout = (LinearLayout) View.inflate(InstallActualActivity.this, R.layout.dialog_install_actual, null);
                final EditText headCountDoneEt = (EditText)layout.findViewById(R.id.head_count_done);
                final EditText cmtFeedbackEt = (EditText)layout.findViewById(R.id.cmt_feedback);

                LinearLayout headNumberLL = layout.findViewById(R.id.actual_head_number_ll);
//                LinearLayout hanxianLL = layout.findViewById(R.id.hanxian_ll);
//                hanxianLL.setVisibility(View.GONE);//焊线组信息需求变更

                if (SinSimApp.getApp().getGroupName().equals("焊线组")
                        || SinSimApp.getApp().getGroupName().equals("驱动组")
                        || SinSimApp.getApp().getGroupName().equals("台板组")
                        || SinSimApp.getApp().getGroupName().equals("线架组")) {
                    headNumberLL.setVisibility(View.GONE);
                }else {
                    headNumberLL.setVisibility(View.VISIBLE);
                }
                TextView headCountTv = (TextView)layout.findViewById(R.id.head_count_tv);
                headCountTv.setText(mInstallPlanActualList.get(position).getHeadNum());

                AlertDialog installActualDialog = new AlertDialog.Builder(InstallActualActivity.this).create();
                installActualDialog.setTitle("完成情况");
                installActualDialog.setView(layout);
                installActualDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                installActualDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InstallActualData installActualData = new InstallActualData();
                        if (headCountDoneEt.getText().toString().length()<1){
                            if (!(SinSimApp.getApp().getGroupName().equals("焊线组")
                                    || SinSimApp.getApp().getGroupName().equals("驱动组")
                                    || SinSimApp.getApp().getGroupName().equals("台板组")
                                    || SinSimApp.getApp().getGroupName().equals("线架组"))) {
                                ShowMessage.showDialog(InstallActualActivity.this, "出错！请填写完整！");
                            }
                        }else {
                            if (SinSimApp.getApp().getGroupName().equals("焊线组")
                                    || SinSimApp.getApp().getGroupName().equals("驱动组")
                                    || SinSimApp.getApp().getGroupName().equals("台板组")
                                    || SinSimApp.getApp().getGroupName().equals("线架组")) {
                                installActualData.setHeadCountDone(0);
                            }else {
                                installActualData.setHeadCountDone(Integer.parseInt(headCountDoneEt.getText().toString()));
                            }
                            installActualData.setCmtFeedback(cmtFeedbackEt.getText().toString());
                            installActualData.setInstallPlanId(mInstallPlanActualList.get(position).getId());
                        }

                        if (mInstallActualList.size()<1) {
                            mInstallActualList.add(installActualData);
                        } else {
                            boolean flag = true;
                            for (int i = 0; i < mInstallActualList.size();i++){
                                if (installActualData.getInstallPlanId() == mInstallActualList.get(i).getInstallPlanId()){
                                    flag = false;
                                    mInstallActualList.get(i).setHeadCountDone(installActualData.getHeadCountDone());
                                    mInstallActualList.get(i).setCmtFeedback(installActualData.getCmtFeedback());
                                    break;
                                }
                            }
                            if (flag){
                                mInstallActualList.add(installActualData);
                            }
                        }
                        mInstallPlanActualList.get(position).setHeadCountDone(installActualData.getHeadCountDone());
                        mInstallPlanActualList.get(position).setHasFinished(false);
                        mMachineFinishTv.setText(String.valueOf(mInstallActualList.size()));
                        mInstallActualAdapter.notifyDataSetChanged();
                    }
                });
                installActualDialog.show();
            }
        });

    }

    /**
     * 上传数据
     * @param view
     */
    public void onUpload(View view) {
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("installPlanActualListInfo", "" + new Gson().toJson(mInstallActualList));
        Log.d(TAG, "onClick: " + new Gson().toJson(mInstallActualList));

        String createInstallPlanActualUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.CREATE_INSTALL_PLAN_ACTUAL;
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(createInstallPlanActualUrl, mPostValue, new CreateInstallPlanActualHandler());
        if( mLoadingProcessDialog == null) {
            mLoadingProcessDialog = new ProgressDialog(InstallActualActivity.this);
            mLoadingProcessDialog.setCancelable(false);
            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
            mLoadingProcessDialog.setMessage("获取信息中...");
        }
        mLoadingProcessDialog.show();
    }
    @SuppressLint("HandlerLeak")
    class CreateInstallPlanActualHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mLoadingProcessDialog != null && mLoadingProcessDialog.isShowing()) {
                mLoadingProcessDialog.dismiss();
            }
            Log.d(TAG, "handleMessage: "+msg.what);
            if (msg.what == Network.OK) {
//                ShowMessage.showToast(InstallActualActivity.this,"上传成功！",ShowMessage.MessageDuring.SHORT);
                AlertDialog installActualDialog = new AlertDialog.Builder(InstallActualActivity.this).create();
                installActualDialog.setTitle("上传成功");
                installActualDialog.setMessage("上传成功，返回上一页!");
                installActualDialog.setCancelable(false);
                installActualDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                installActualDialog.show();
            }else {
                ShowMessage.showDialog(InstallActualActivity.this,"上传失败");
            }
        }
    }
}
