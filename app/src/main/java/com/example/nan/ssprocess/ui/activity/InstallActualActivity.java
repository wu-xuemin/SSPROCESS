package com.example.nan.ssprocess.ui.activity;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.InstallActualAdapter;
import com.example.nan.ssprocess.adapter.InstallPlanAdapter;
import com.example.nan.ssprocess.bean.basic.InstallPlanData;
import com.google.gson.Gson;

import java.util.ArrayList;

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
                AlertDialog showCmtDialog = new AlertDialog.Builder(InstallActualActivity.this).create();
                showCmtDialog.setTitle("备注信息：");
                showCmtDialog.setMessage(mInstallPlanActualList.get(position).getCmtSend());
                showCmtDialog.show();
            }
        });
    }
}
