package com.example.nan.ssprocess.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.ProcessToAdminAdapter;
import com.example.nan.ssprocess.bean.basic.LoginRequestData;

import java.util.ArrayList;

import com.example.nan.ssprocess.bean.basic.ProcessModuleResponseData;

/**
 * @author nan  2017/11/16
 */
public class ProcessToAdminActivity extends AppCompatActivity {

    private ProcessToAdminActivity mProcessToAdminActivity;
    private ArrayList<ProcessModuleResponseData> mProcessToAdminList = new ArrayList<>();
    private ProcessToAdminAdapter mProcessToAdminAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_to_admin);
        Button scanQrcodeBotton = (Button) findViewById(R.id.scan_qrcode_button);
        scanQrcodeBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProcessToAdminActivity.this,ScanQrcodeActivity.class);
                startActivity(intent);
            }
        });


        RecyclerView mProcessToAdminRV = (RecyclerView) findViewById(R.id.process_to_admin_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mProcessToAdminRV.setLayoutManager(manager);
        mProcessToAdminAdapter = new ProcessToAdminAdapter(mProcessToAdminList);
        mProcessToAdminRV.setAdapter(mProcessToAdminAdapter);

//        //下拉刷新
//        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
//        int[] colors = getResources().getIntArray(R.array.google_colors);
//        mSwipeRefresh.setColorSchemeColors(colors);
//        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                //超时停止刷新
//                mSwipeRefresh.postDelayed(mStopSwipeRefreshRunnable, 5000);
//                TODO:开始网络请求，获取最新的流程资料
//                fetchData();
//            }
//
//        });
//        //第一次进入刷新页面， 加载loading页面
//        if( mLoadingProcessDialog == null) {
//            mLoadingProcessDialog = new ProgressDialog(ChooseProcessActivity.this);
//            mLoadingProcessDialog.setCancelable(false);
//            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
//            mLoadingProcessDialog.setMessage("获取流程模板中...");
//        }
//        mLoadingProcessDialog.show();
    }
}
