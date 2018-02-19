package com.example.nan.ssprocess.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.ScanResultAdapter;
import com.example.nan.ssprocess.adapter.TaskRecordAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * @author nan
 */
public class ScanResultActivity extends AppCompatActivity {

    private static String TAG = "nlgScanResultActivity";
    private ArrayList<TaskMachineListData> mScanResultList = new ArrayList<>();
    private ScanResultAdapter mScanResultAdapter;

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

        //列表
        RecyclerView mScanResultRv = findViewById(R.id.scan_result_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mScanResultRv.setLayoutManager(manager);
        mScanResultRv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mScanResultAdapter = new ScanResultAdapter(mScanResultList);
        mScanResultRv.setAdapter(mScanResultAdapter);
        //点击跳转，把所有接收到的数据传递给下一个activity
        mScanResultAdapter.setOnItemClickListener(new ScanResultAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(int position){
                Log.d(TAG, "onItemClick: gson :"+new Gson().toJson(mScanResultList.get(position)));
                Intent intent = new Intent();
                switch (SinSimApp.getApp().getRole()){
                    case SinSimApp.LOGIN_FOR_ADMIN:
                        intent.setClass(ScanResultActivity.this,DetailToAdminActivity.class);
                        intent.putExtra("mTaskMachineListData", mScanResultList.get(position));
                        startActivity(intent);
                        finish();
                        break;
                    case SinSimApp.LOGIN_FOR_INSTALL:
                        intent.setClass(ScanResultActivity.this,DetailToInstallActivity.class);
                        intent.putExtra("mTaskMachineListData", mScanResultList.get(position));
                        startActivity(intent);
                        finish();
                        break;
                    case SinSimApp.LOGIN_FOR_QA:
                        intent.setClass(ScanResultActivity.this,DetailToCheckoutActivity.class);
                        intent.putExtra("mTaskMachineListData", mScanResultList.get(position));
                        startActivity(intent);
                        finish();
                        break;
                    default:
                        Toast.makeText(ScanResultActivity.this,"请检查登入账号!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
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
}
