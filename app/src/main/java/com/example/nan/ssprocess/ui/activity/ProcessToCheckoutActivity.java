package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
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
import com.example.nan.ssprocess.service.MyMqttService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author nan  2017/11/16
 */
public class ProcessToCheckoutActivity extends AppCompatActivity {

    private static String TAG = "nlgProcessToCheckoutActivity";
    private static final int SCAN_QRCODE_START = 1;

    private ArrayList<TaskMachineListData> mProcessToCheckoutList = new ArrayList<>();
    private TaskRecordAdapter mTaskRecordAdapter;
    private FetchProcessDataHandler mFetchProcessDataHandler = new FetchProcessDataHandler();

    private ProgressDialog mLoadingProcessDialog;
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
        setContentView(R.layout.activity_process_to_checkout);

        // 启动MQTT服务
        Intent startIntent = new Intent(this, MyMqttService.class);
        startService(startIntent);

        //点击扫码
        Button scanQrcodeBotton = findViewById(R.id.checkout_scan_qrcode_button);
        scanQrcodeBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProcessToCheckoutActivity.this,ScanQrcodeActivity.class);
                startActivityForResult(intent,SCAN_QRCODE_START);
            }
        });

        //列表
        RecyclerView mProcessToAdminRV = findViewById(R.id.process_to_checkout_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mProcessToAdminRV.setLayoutManager(manager);
        mTaskRecordAdapter = new TaskRecordAdapter(mProcessToCheckoutList);
        mProcessToAdminRV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mProcessToAdminRV.setAdapter(mTaskRecordAdapter);
        //点击跳转，把所有接收到的数据传递给下一个activity
        mTaskRecordAdapter.setOnItemClickListener(new TaskRecordAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(int position){
                Log.d(TAG, "onItemClick: position :"+position);
                Log.d(TAG, "onItemClick: gson :"+new Gson().toJson(mProcessToCheckoutList.get(position)));
                Intent intent=new Intent(ProcessToCheckoutActivity.this,DetailToCheckoutActivity.class);
                intent.putExtra("mTaskMachineListData", mProcessToCheckoutList.get(position));
                startActivity(intent);
            }
        });

        //下拉刷新
        mSwipeRefresh = findViewById(R.id.checkout_swipe_refresh);
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        //第一次进入刷新页面， 加载loading页面
        if( mLoadingProcessDialog == null) {
            mLoadingProcessDialog = new ProgressDialog(ProcessToCheckoutActivity.this);
            mLoadingProcessDialog.setCancelable(false);
            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
            mLoadingProcessDialog.setMessage("获取信息中...");
        }
        mLoadingProcessDialog.show();
        fetchProcessData();
    }

    private void fetchProcessData() {
        final String account = SinSimApp.getApp().getAccount();
        final String ip = SinSimApp.getApp().getServerIP();
//        final String ip = "192.168.0.102:8080";
//        final String account = "sss";
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("userAccount", account);
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_TO_QA;
        Network.Instance(SinSimApp.getApp()).fetchProcessTaskRecordData(fetchProcessRecordUrl, mPostValue, mFetchProcessDataHandler);
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
                mProcessToCheckoutList=(ArrayList<TaskMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToCheckoutList.size());
                mTaskRecordAdapter.setProcessList(mProcessToCheckoutList);
                mTaskRecordAdapter.notifyDataSetChanged();
                Toast.makeText(ProcessToCheckoutActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(ProcessToCheckoutActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SCAN_QRCODE_START:
                // 当resultCode为RESULT_OK时，也就是处理特定的结果
                if (resultCode == RESULT_OK)
                {
                    // 取出Intent里的Extras数据传递给跳转的activity
                    TaskMachineListData mTaskMachineListData=new TaskMachineListData();
                    mTaskMachineListData=(TaskMachineListData)data.getSerializableExtra("mTaskMachineListData");
                    //TODO:添加判断逻辑

                    Intent intent=new Intent(ProcessToCheckoutActivity.this,DetailToCheckoutActivity.class);
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
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                SinSimApp.getApp().setLogOut();
                Intent it = new Intent();
                it.setClass(ProcessToCheckoutActivity.this, LoginActivity.class);
                startActivity(it);
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
