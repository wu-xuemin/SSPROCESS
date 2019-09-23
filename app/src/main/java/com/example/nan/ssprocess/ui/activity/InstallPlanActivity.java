package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
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

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.InstallPlanAdapter;
import com.example.nan.ssprocess.bean.basic.InstallPlanData;
import com.google.gson.Gson;

import java.util.ArrayList;

public class InstallPlanActivity extends AppCompatActivity {

    private static final String TAG = "nlg";
    private InstallPlanAdapter mInstallPlanAdapter;
    private ArrayList<InstallPlanData> mInstallPlanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_plan);
        //获取传递过来的信息
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mInstallPlanList = (ArrayList<InstallPlanData>) bundle.getSerializable("mInstallPlanList");
        //列表
        RecyclerView mInstallPlanTV = findViewById(R.id.install_plan_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mInstallPlanTV.setLayoutManager(manager);
        mInstallPlanAdapter = new InstallPlanAdapter(mInstallPlanList);
        mInstallPlanTV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mInstallPlanTV.setAdapter(mInstallPlanAdapter);
        //点击跳转，把所有接收到的数据传递给下一个activity
        mInstallPlanAdapter.setOnItemClickListener(new InstallPlanAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(int position){
                Log.d(TAG, "onItemClick: gson :"+new Gson().toJson(mInstallPlanList.get(position)));
                AlertDialog showCmtDialog = new AlertDialog.Builder(InstallPlanActivity.this).create();
                showCmtDialog.setTitle("备注信息：");
                showCmtDialog.setMessage(mInstallPlanList.get(position).getCmtSend());
                showCmtDialog.show();
            }
        });
    }


}
