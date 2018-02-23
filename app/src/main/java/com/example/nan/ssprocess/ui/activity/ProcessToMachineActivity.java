package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

/**
 * @author nan
 */
public class ProcessToMachineActivity extends AppCompatActivity {

    private static String TAG = "nlgProcessToMachineActivity";
    private ArrayList<TaskMachineListData> mProcessToMachineList = new ArrayList<>();
    private TaskRecordAdapter mProcessToMachineAdapter;
    private FetchProcessDataHandler mFetchProcessDataHandler = new FetchProcessDataHandler();
    private int mPage;
    private String mSearchContent;

    private ProgressDialog mLoadingProcessDialog;
    private AlertDialog mSearchResultDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_to_machine);
        //返回前页按钮
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mSearchContent = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "onCreate: "+mSearchContent);
        }

        //列表
        RecyclerView mProcessToMachineRV = findViewById(R.id.process_to_machine_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mProcessToMachineRV.setLayoutManager(manager);
        mProcessToMachineRV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mProcessToMachineAdapter = new TaskRecordAdapter(mProcessToMachineList);
        mProcessToMachineRV.setAdapter(mProcessToMachineAdapter);
        //点击跳转，把所有接收到的数据传递给下一个activity
        mProcessToMachineAdapter.setOnItemClickListener(new TaskRecordAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(int position){
                Log.d(TAG, "onItemClick: gson :"+new Gson().toJson(mProcessToMachineList.get(position)));
                Intent intent=new Intent(ProcessToMachineActivity.this,DetailToAdminActivity.class);
                intent.putExtra("mTaskMachineListData", mProcessToMachineList.get(position));
                startActivity(intent);
                finish();
            }
        });

        //第一次进入刷新页面， 加载loading页面
        if( mLoadingProcessDialog == null) {
            mLoadingProcessDialog = new ProgressDialog(ProcessToMachineActivity.this);
            mLoadingProcessDialog.setCancelable(false);
            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
            mLoadingProcessDialog.setMessage("获取信息中...");
        }
        mLoadingProcessDialog.show();
        mPage=0;
        fetchProcessData(mPage);
    }

    private void fetchProcessData(int page) {
        final String ip = SinSimApp.getApp().getServerIP();
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_BY_SEARCH_TO_ADMIN;
        mPostValue.put("namePlate", mSearchContent);
        mPostValue.put("page", ""+page);
        Network.Instance(SinSimApp.getApp()).fetchProcessTaskRecordData(fetchProcessRecordUrl, mPostValue, mFetchProcessDataHandler);
    }
    
    @SuppressLint("HandlerLeak")
    private class FetchProcessDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mLoadingProcessDialog != null && mLoadingProcessDialog.isShowing()) {
                mLoadingProcessDialog.dismiss();
            }

            if (msg.what == Network.OK) {
                mProcessToMachineList=(ArrayList<TaskMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToMachineList.size());
                if (mProcessToMachineList.size()==0){
                    mSearchResultDialog = new AlertDialog.Builder(ProcessToMachineActivity.this).create();
                    mSearchResultDialog.setMessage("搜索结果为空！");
                    mSearchResultDialog.setButton(AlertDialog.BUTTON_POSITIVE, "重新搜索",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //关闭当前activity
                                    finish();
                                }
                            });
                    mSearchResultDialog.show();   
                } else {
                    mProcessToMachineAdapter.setProcessList(mProcessToMachineList);
                    mProcessToMachineAdapter.notifyDataSetChanged();
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(ProcessToMachineActivity.this, "搜索失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoadingProcessDialog != null) {
            mLoadingProcessDialog.dismiss();
        }
        if(mSearchResultDialog != null) {
            mSearchResultDialog.dismiss();
        }
    }
}
