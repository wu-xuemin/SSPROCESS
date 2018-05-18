package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.SearchView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.TaskRecordAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.example.nan.ssprocess.service.MyMqttService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import cn.bingoogolapple.refreshlayout.BGAMoocStyleRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;


/**
 * @author nan  2017/11/16
 */
public class ProcessToAdminActivity extends AppCompatActivity implements BGARefreshLayout.BGARefreshLayoutDelegate{

    private Intent mqttIntent;
    private static String TAG = "nlgProcessToAdminActivity";
    private ArrayList<TaskRecordMachineListData> mProcessToAdminList = new ArrayList<>();
    private TaskRecordMachineListData mScanResultListData=new TaskRecordMachineListData();
    private TaskRecordAdapter mProcessToAdminAdapter;
    private BGARefreshLayout mRefreshLayout;

    private AlertDialog mLocationSettingDialog =null;
    private ProgressDialog mLoadingProcessDialog;
    private ProgressDialog mUpdatingProcessDialog;

    private final String IP = SinSimApp.getApp().getServerIP();
    private int mPage;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_to_admin);

        //启动MQTT服务
        mqttIntent = new Intent(this, MyMqttService.class);
        startService(mqttIntent);

        mRefreshLayout = findViewById(R.id.refreshLayout);
        mRefreshLayout.setDelegate(this);
        mPage=0;
        // 设置下拉刷新和上拉加载更多的风格     参数1：应用程序上下文，参数2：是否具有上拉加载更多功能
        BGAMoocStyleRefreshViewHolder moocStyleRefreshViewHolder = new BGAMoocStyleRefreshViewHolder(this, true);
        moocStyleRefreshViewHolder.setOriginalImage(R.drawable.bga_refresh_moooc);
        moocStyleRefreshViewHolder.setUltimateColor(R.color.colorAccent);
        mRefreshLayout.setRefreshViewHolder(moocStyleRefreshViewHolder);

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
                intent.putExtra("mTaskRecordMachineListData", mProcessToAdminList.get(position));
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
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
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        String fetchProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FETCH_TASK_RECORD_TO_ADMIN;
        mPostValue.put("page", ""+page);
        Network.Instance(SinSimApp.getApp()).fetchProcessTaskRecordData(fetchProcessRecordUrl, mPostValue, new FetchProcessDataHandler());
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        Log.d(TAG, "onBGARefreshLayoutBeginRefreshing: 下划刷新");
        mPage=0;
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
                mProcessToAdminList=(ArrayList<TaskRecordMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToAdminList.size());
                int iListSize=mProcessToAdminList.size();
                for(int position=iListSize-1;position>=0;position--) {
                    if (mProcessToAdminList.get(position).getMachineData().getStatus() == SinSimApp.MACHINE_CANCELED) {
                        mProcessToAdminList.remove(position);
                    }
                }
                if (mProcessToAdminList.size()==0){
                    mProcessToAdminAdapter.setProcessList(mProcessToAdminList);
                    mProcessToAdminAdapter.notifyDataSetChanged();
                    Toast.makeText(ProcessToAdminActivity.this, "没有更多了...", Toast.LENGTH_SHORT).show();
                } else {
                    mProcessToAdminAdapter.setProcessList(mProcessToAdminList);
                    mProcessToAdminAdapter.notifyDataSetChanged();
                    Log.d(TAG, "handleMessage: 列表已更新!");
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(ProcessToAdminActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 更新上传机器location
     */
    private void updateProcessDetailData() {
        //更新location状态
        mScanResultListData.getMachineData().setLocation(location);
        Gson gson=new Gson();
        String machineDataToJson = gson.toJson(mScanResultListData.getMachineData());
        Log.d(TAG, "onItemClick: gson :"+ machineDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("machine", machineDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + IP + URL.UPDATE_MACHINE_LOCATION;
        Log.d(TAG, "updateProcessDetailData: "+updateProcessRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, new UpdateProcessDetailDataHandler());
    }

    @SuppressLint("HandlerLeak")
    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if(mUpdatingProcessDialog != null && mUpdatingProcessDialog.isShowing()) {
                mUpdatingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                Toast.makeText(ProcessToAdminActivity.this, "上传位置成功！", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(ProcessToAdminActivity.this, "上传失败："+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        //搜索框
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        // 显示“开始搜索”的按钮
        searchView.setSubmitButtonEnabled(true);
        // 提示内容右边提供一个将提示内容放到搜索框的按钮
        searchView.setQueryRefinementEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                stopService(mqttIntent);
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
        if(mUpdatingProcessDialog != null) {
            mUpdatingProcessDialog.dismiss();
        }
        if(mLocationSettingDialog != null) {
            mLocationSettingDialog.dismiss();
        }
    }
}
