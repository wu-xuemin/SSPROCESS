package com.example.nan.ssprocess.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.ProcessToAdminAdapter;
import com.example.nan.ssprocess.bean.basic.LoginRequestData;
import com.example.nan.ssprocess.bean.basic.ProcessModuleListData;
import com.example.nan.ssprocess.bean.basic.ProcessModuleResponseData;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.net.Network;

import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * @author nan  2017/11/16
 */
public class ProcessToAdminActivity extends AppCompatActivity {

    private ProcessToAdminActivity mProcessToAdminActivity;
    private ArrayList<ProcessModuleListData> mProcessToAdminList = new ArrayList<>();
    private ProcessToAdminAdapter mProcessToAdminAdapter;
    private FetchProcessDataHandler mFetchProcessDataHandler = new FetchProcessDataHandler();

    private SwipeRefreshLayout mSwipeRefresh;
    private Runnable mStopSwipeRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if(mSwipeRefresh.isRefreshing()) {
                mSwipeRefresh.setRefreshing(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_to_admin);

        Button scanQrcodeBotton = (Button) findViewById(R.id.admin_scan_qrcode_button);
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

        //下拉刷新
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.admin_swipe_refresh);
        int[] colors = getResources().getIntArray(R.array.google_colors);
        mSwipeRefresh.setColorSchemeColors(colors);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //超时停止刷新
                mSwipeRefresh.postDelayed(mStopSwipeRefreshRunnable, 5000);
                fetchProcessData();
            }

        });
//        //第一次进入刷新页面， 加载loading页面
//        if( mLoadingProcessDialog == null) {
//            mLoadingProcessDialog = new ProgressDialog(ChooseProcessActivity.this);
//            mLoadingProcessDialog.setCancelable(false);
//            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
//            mLoadingProcessDialog.setMessage("获取流程模板中...");
//        }
//        mLoadingProcessDialog.show();
    }

    private void fetchProcessData() {
        final String account = SinSimApp.getApp().getAccount();
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("userAccount", account);
        String fetchProcessRecordUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.FETCH_PROCESS_RECORD;
        Network.Instance(SinSimApp.getApp()).fetchProcessRecordData(fetchProcessRecordUrl, mPostValue, mFetchProcessDataHandler);
    }

    private class FetchProcessDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                Toast.makeText(ProcessToAdminActivity.this, "获取正在进行中流程成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProcessToAdminActivity.this, "更新流程信息失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
