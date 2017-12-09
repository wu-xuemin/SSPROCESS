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
import android.util.Log;
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
import java.util.List;


/**
 * @author nan  2017/11/16
 */
public class ProcessToAdminActivity extends AppCompatActivity implements UpdateOperationStatusListener{

    private static String TAG = "nlgProcessToAdminActivity";
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
        fetchProcessData();

    }

    private void fetchProcessData() {
        final String account = SinSimApp.getApp().getAccount();
        final String ip = SinSimApp.getApp().getServerIP();
//        final String ip = "192.168.0.102:8080";
//        final String account = "sss";
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("userAccount", account);
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FETCH_PROCESS_RECORD;
        Network.Instance(SinSimApp.getApp()).fetchProcessModuleData(fetchProcessRecordUrl, mPostValue, mFetchProcessDataHandler);
    }

    @Override
    public void onUpdateOperationStatus(final boolean success, String errorMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!success) {
                    Toast.makeText(SinSimApp.getApp().getApplicationContext(), "操作状态更新失败！",Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(ChooseProcessActivity.this, "操作状态更新成功！",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class FetchProcessDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mSwipeRefresh.isRefreshing()) {
                mSwipeRefresh.setRefreshing(false);
            }
            if (msg.what == Network.OK) {
                mProcessToAdminList=(ArrayList<ProcessModuleListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToAdminList.size());
                mProcessToAdminAdapter.setProcessList(mProcessToAdminList);
                mProcessToAdminAdapter.notifyDataSetChanged();
                Toast.makeText(ProcessToAdminActivity.this, "获取正在进行中流程成功！", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(ProcessToAdminActivity.this, "更新流程信息失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
