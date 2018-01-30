package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.TaskRecordAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;


/**
 * @author nan  2017/11/16
 */
public class ProcessToAdminActivity extends AppCompatActivity implements BGARefreshLayout.BGARefreshLayoutDelegate{

    private static String TAG = "nlgProcessToAdminActivity";
    private ArrayList<TaskMachineListData> mProcessToAdminList = new ArrayList<>();
    private TaskRecordAdapter mProcessToAdminAdapter;
    private FetchProcessDataHandler mFetchProcessDataHandler = new FetchProcessDataHandler();
    private int mPage;

    private ProgressDialog mLoadingProcessDialog;
    private SwipeRefreshLayout mSwipeRefresh;
    private BGARefreshLayout mRefreshLayout;

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

        mRefreshLayout = findViewById(R.id.refreshLayout);
        mRefreshLayout.setDelegate(this);

        //点击扫码
        Button scanQrcodeBotton = findViewById(R.id.admin_scan_qrcode_button);
        scanQrcodeBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProcessToAdminActivity.this,ScanQrcodeActivity.class);
                startActivity(intent);
            }
        });

        //列表
        final RecyclerView mProcessToAdminRV = findViewById(R.id.process_to_admin_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mProcessToAdminRV.setLayoutManager(manager);
        mProcessToAdminRV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mProcessToAdminAdapter = new TaskRecordAdapter(mProcessToAdminList);
        mProcessToAdminRV.setAdapter(mProcessToAdminAdapter);
        //点击跳转，把所有接收到的数据传递给下一个activity
        mProcessToAdminAdapter.setOnItemClickListener(new TaskRecordAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(int position){
                Log.d(TAG, "onItemClick: gson :"+new Gson().toJson(mProcessToAdminList.get(position)));
                Intent intent=new Intent(ProcessToAdminActivity.this,DetailToAdminActivity.class);
//                Intent intent=new Intent(ProcessToAdminActivity.this,DetailToCheckoutActivity.class);
                intent.putExtra("mTaskMachineListData", mProcessToAdminList.get(position));
                startActivity(intent);
            }
        });

        //下拉刷新
        mPage=0;
        mSwipeRefresh = findViewById(R.id.admin_swipe_refresh);
        int[] colors = getResources().getIntArray(R.array.google_colors);
        mSwipeRefresh.setColorSchemeColors(colors);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //超时停止刷新
                mSwipeRefresh.postDelayed(mStopSwipeRefreshRunnable, 5000);
                fetchProcessData(mPage);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //第一次进入刷新页面， 加载loading页面
        if( mLoadingProcessDialog == null) {
            mLoadingProcessDialog = new ProgressDialog(ProcessToAdminActivity.this);
            mLoadingProcessDialog.setCancelable(false);
            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
            mLoadingProcessDialog.setMessage("获取信息中...");
        }
        mLoadingProcessDialog.show();
        fetchProcessData(mPage);
    }

    private void fetchProcessData(int page) {
        final String ip = SinSimApp.getApp().getServerIP();
        final String account = SinSimApp.getApp().getAccount();
//        final String ip = "192.168.0.102:8080";
//        final String account = "sss";
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_TO_ADMIN;
        mPostValue.put("userAccount", account);
        mPostValue.put("page", ""+page);
        Network.Instance(SinSimApp.getApp()).fetchProcessTaskRecordData(fetchProcessRecordUrl, mPostValue, mFetchProcessDataHandler);
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {

    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        mPage=mPage+1;
        fetchProcessData(mPage);
        return false;
    }

    @SuppressLint("HandlerLeak")
    private class FetchProcessDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mLoadingProcessDialog != null && mLoadingProcessDialog.isShowing()) {
                mLoadingProcessDialog.dismiss();
            }
            if(mSwipeRefresh.isRefreshing()) {
                mSwipeRefresh.setRefreshing(false);
            }
            if (msg.what == Network.OK) {
                mProcessToAdminList=(ArrayList<TaskMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToAdminList.size());
                mProcessToAdminAdapter.setProcessList(mProcessToAdminList);
                mProcessToAdminAdapter.notifyDataSetChanged();
                Toast.makeText(ProcessToAdminActivity.this, "列表已更新！", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(ProcessToAdminActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                SinSimApp.getApp().setLogOut();
                Intent it = new Intent();
                it.setClass(ProcessToAdminActivity.this, LoginActivity.class);
                startActivity(it);
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
