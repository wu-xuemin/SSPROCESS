package com.example.nan.ssprocess.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.InstallPlanAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.InstallPlanData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

public class InstallPlanActivity extends AppCompatActivity {

    private static final String TAG = "nlg";
    private InstallPlanAdapter mInstallPlanAdapter;
    private ArrayList<InstallPlanData> mInstallPlanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_plan);

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
            }
        });

        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("installGroupName", ""+SinSimApp.getApp().getGroupName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// HH:mm:ss
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,1);
        String tomorrow = simpleDateFormat.format(c.getTime());
        Log.d(TAG, "onCreate: "+tomorrow);
        mPostValue.put("queryStartTime", tomorrow);
        mPostValue.put("queryFinishTime", tomorrow);

        String fetchInstallPlanUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.FATCH_INSTALL_PLAN;
        //Network.Instance(SinSimApp.getApp()).fetchInstallPlan(fetchInstallPlanUrl, mPostValue, new FetchInstallPlanHandler());

    }
}
