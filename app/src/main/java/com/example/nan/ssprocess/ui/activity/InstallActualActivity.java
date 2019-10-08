package com.example.nan.ssprocess.ui.activity;

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

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class InstallActualActivity extends AppCompatActivity {

    private static final String TAG = "nlg";
    private InstallActualAdapter mInstallActualAdapter;
    private ArrayList<InstallPlanData> mInstallPlanActualList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_actual);
        //获取传递过来的信息
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mInstallPlanActualList = (ArrayList<InstallPlanData>) bundle.getSerializable("mInstallPlanActualList");

        TextView machineCountTv = findViewById(R.id.machine_count);
        TextView headCountTv = findViewById(R.id.head_count);
//        machineCountTv.setText(""+mInstallPlanActualList.size());
//        int headCount = 0;
//        for (int i =0;i<mInstallPlanActualList.size();i++) {
//            headCount += Integer.valueOf(mInstallPlanActualList.get(i).getHeadNum());
//        }
//        headCountTv.setText(""+headCount);
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
            public void onItemClick(int position){
                Log.d(TAG, "onItemClick: gson :"+new Gson().toJson(mInstallPlanActualList.get(position)));
                
                AlertDialog installActualDialog = null;
                LinearLayout layout = (LinearLayout) View.inflate(InstallActualActivity.this, R.layout.dialog_install_actual, null);
                final EditText headCountDoneEt = (EditText)layout.findViewById(R.id.head_count_done);
                final EditText cmtFeedbackEt = (EditText)layout.findViewById(R.id.cmt_feedback);

                LinearLayout hanxianLL = layout.findViewById(R.id.hanxian_ll);
                hanxianLL.setVisibility(View.GONE);

                TextView headCountTv = (TextView)layout.findViewById(R.id.head_count_tv);
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
                    class CreateInstallPlanActualHandler extends Handler {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if (msg.what == Network.OK) {
                                ShowMessage.showToast(InstallActualActivity.this,"上传成功！",ShowMessage.MessageDuring.SHORT);
                                //TODO:完成头数量传到主页
                            }else {
                                ShowMessage.showDialog(InstallActualActivity.this,"出错！请检查网络！");
                            }
                        }
                    }
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            InstallActualData installActualData = new InstallActualData();
                            installActualData.setHeadCountDone(headCountDoneEt.getText().toString());
                            installActualData.setCmtFeedback(cmtFeedbackEt.getText().toString());
                            LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                            mPostValue.put("installPlanActual", ""+new Gson().toJson(installActualData));

                            String createInstallPlanActualUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.CREATE_INSTALL_PLAN_ACTUAL;
                            Network.Instance(SinSimApp.getApp()).updateProcessRecordData(createInstallPlanActualUrl, mPostValue, new CreateInstallPlanActualHandler());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                installActualDialog.show();
            }
        });
    }

}
