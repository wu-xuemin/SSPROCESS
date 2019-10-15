package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
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

public class InstallActualActivity extends AppCompatActivity implements BGARefreshLayout.BGARefreshLayoutDelegate{

    private static final String TAG = "nlg";
    private InstallActualAdapter mInstallActualAdapter;
    private ArrayList<InstallPlanData> mInstallPlanActualList = new ArrayList<>();
    TextView machineCountTv;
    private BGARefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_actual);
        //获取传递过来的信息
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mInstallPlanActualList = (ArrayList<InstallPlanData>) bundle.getSerializable("mInstallPlanActualList");

        machineCountTv = findViewById(R.id.actual_machine_count);
//        TextView headCountTv = findViewById(R.id.actual_head_count);
        machineCountTv.setText(""+mInstallPlanActualList.size());
//        int headCountSum = 0;
//
//        for (int i =0;i<mInstallPlanActualList.size();i++) {
//            headCountSum += Integer.valueOf(mInstallPlanActualList.get(i).getHeadNum())-mInstallPlanActualList.get(i).getHeadCountDone();
//        }
//        headCountTv.setText(""+headCountSum);
        //列表
        RecyclerView mInstallActualRV = findViewById(R.id.install_actual_rv);
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

                LinearLayout hanxianLL = layout.findViewById(R.id.hanxian_ll);
                if (SinSimApp.getApp().getGroupName().equals("焊线组")) {
                    hanxianLL.setVisibility(View.VISIBLE);
                }else {
                    hanxianLL.setVisibility(View.GONE);
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
            }
        });

        mRefreshLayout = findViewById(R.id.refreshLayout);
        mRefreshLayout.setDelegate(this);
        // 设置下拉刷新和上拉加载更多的风格     参数1：应用程序上下文，参数2：是否具有上拉加载更多功能
        BGAMoocStyleRefreshViewHolder moocStyleRefreshViewHolder = new BGAMoocStyleRefreshViewHolder(this, true);
        moocStyleRefreshViewHolder.setOriginalImage(R.drawable.bga_refresh_moooc);
        moocStyleRefreshViewHolder.setUltimateColor(R.color.colorAccent);
        mRefreshLayout.setRefreshViewHolder(moocStyleRefreshViewHolder);
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        Log.d(TAG, "onBGARefreshLayoutBeginRefreshing: 下划刷新");
        LinkedHashMap<String, String> mPostValue2 = new LinkedHashMap<>();
        mPostValue2.put("installGroupName", ""+SinSimApp.getApp().getGroupName());
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");// HH:mm:ss
        Date date2 = new Date(System.currentTimeMillis());
        mPostValue2.put("isNotFinished", "true");
        mPostValue2.put("queryFinishTime", simpleDateFormat2.format(date2));

        String fetchInstallPlanUrl2 = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.FATCH_INSTALL_PLAN;
        Network.Instance(SinSimApp.getApp()).fetchInstallPlan(fetchInstallPlanUrl2, mPostValue2, new FetchInstallActualHandler());
    }

    @SuppressLint("HandlerLeak")
    private class FetchInstallActualHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mRefreshLayout.endRefreshing();
            mRefreshLayout.endLoadingMore();
            if (msg.what == Network.OK) {
                ArrayList<InstallPlanData> installPlanActualList = (ArrayList<InstallPlanData>) msg.obj;
                mInstallPlanActualList.clear();
                mInstallPlanActualList.addAll(installPlanActualList);//直接赋值会被指向其他的地址，所以需要用list操作来实现更新
                Log.d(TAG, "handleMessage: "+(new Gson().toJson(mInstallPlanActualList)));
                machineCountTv.setText(""+mInstallPlanActualList.size());
                //更新adapter
                mInstallActualAdapter.notifyDataSetChanged();
            } else {
                ShowMessage.showDialog(InstallActualActivity.this,"网络出错了！");
            }
        }
    }
    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
    }
}
