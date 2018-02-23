package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
import android.support.v7.widget.SearchView;
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

import cn.bingoogolapple.refreshlayout.BGAMoocStyleRefreshViewHolder;
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
    private BGARefreshLayout mRefreshLayout;

    private ProgressDialog mLoadingProcessDialog;
    private static final int SCAN_QRCODE_START = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_to_admin);

        mRefreshLayout = findViewById(R.id.refreshLayout);
        mRefreshLayout.setDelegate(this);
        mPage=0;
        // 设置下拉刷新和上拉加载更多的风格     参数1：应用程序上下文，参数2：是否具有上拉加载更多功能
        BGAMoocStyleRefreshViewHolder moocStyleRefreshViewHolder = new BGAMoocStyleRefreshViewHolder(this, true);
        moocStyleRefreshViewHolder.setOriginalImage(R.drawable.bga_refresh_moooc);
        moocStyleRefreshViewHolder.setUltimateColor(R.color.colorAccent);
        mRefreshLayout.setRefreshViewHolder(moocStyleRefreshViewHolder);

        //点击扫码
        Button scanQrcodeBotton = findViewById(R.id.admin_scan_qrcode_button);
        scanQrcodeBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProcessToAdminActivity.this,ScanQrcodeActivity.class);
                startActivityForResult(intent,SCAN_QRCODE_START);
            }
        });

        //列表
        RecyclerView mProcessToAdminRV = findViewById(R.id.process_to_admin_rv);
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
        Log.d(TAG, "onBGARefreshLayoutBeginRefreshing: 下划刷新");
        fetchProcessData(mPage);
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        Log.d(TAG, "onBGARefreshLayoutBeginLoadingMore: 上划刷新");
        mPage=mPage+1;
        fetchProcessData(mPage);
        return true;
    }

    @SuppressLint("HandlerLeak")
    private class FetchProcessDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mLoadingProcessDialog != null && mLoadingProcessDialog.isShowing()) {
                mLoadingProcessDialog.dismiss();
            }
            mRefreshLayout.endRefreshing();
            mRefreshLayout.endLoadingMore();

            if (msg.what == Network.OK) {
                mProcessToAdminList=(ArrayList<TaskMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToAdminList.size());
                if (mProcessToAdminList.size()==0){
                    Toast.makeText(ProcessToAdminActivity.this, "没有更多了...", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO: 合并同一机器不同流程
                    mProcessToAdminAdapter.setProcessList(mProcessToAdminList);
                    mProcessToAdminAdapter.notifyDataSetChanged();
                    Toast.makeText(ProcessToAdminActivity.this, "列表已更新！", Toast.LENGTH_SHORT).show();
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(ProcessToAdminActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SCAN_QRCODE_START:
                // 当requestCode、resultCode同时为0时，也就是处理特定的结果
                if (resultCode == RESULT_OK)
                {
                    // 取出Intent里的Extras数据传递给跳转的activity
                    TaskMachineListData mTaskMachineListData = new TaskMachineListData();
                    mTaskMachineListData=(TaskMachineListData)data.getSerializableExtra("mTaskMachineListData");
                    Intent intent=new Intent(this,DetailToInstallActivity.class);
                    intent.putExtra("mTaskMachineListData", mTaskMachineListData);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);    // 显示“开始搜索”的按钮
        searchView.setQueryRefinementEnabled(true); // 提示内容右边提供一个将提示内容放到搜索框的按钮
        return true;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoadingProcessDialog != null) {
            mLoadingProcessDialog.dismiss();
        }
    }
}
