package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.TaskRecordAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.example.nan.ssprocess.service.MyMqttService;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import cn.bingoogolapple.refreshlayout.BGAMoocStyleRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

/**
 * @author nan  2017/11/16
 */
public class ProcessToCheckoutActivity extends AppCompatActivity implements BGARefreshLayout.BGARefreshLayoutDelegate{

    private static String TAG = "nlgProcessToCheckoutActivity";
    private static final int SCAN_QRCODE_START = 1;
    private int mPage;

    private ArrayList<TaskRecordMachineListData> mProcessToCheckoutList = new ArrayList<>();
    private TaskRecordAdapter mTaskRecordAdapter;

    private BGARefreshLayout mRefreshLayout;
    private ProgressDialog mLoadingProcessDialog;
    Intent mqttIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_to_checkout);

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
                intent.putExtra("mTaskRecordMachineListData", mProcessToCheckoutList.get(position));
                startActivity(intent);
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
        fetchProcessData(mPage);
    }

    private void fetchProcessData(int page) {
        final String account = SinSimApp.getApp().getAccount();
        final String ip = SinSimApp.getApp().getServerIP();
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
//        mPostValue.put("userAccount", account);
        /**
         * 新的质检方案，接口也是新的
         */
//        mPostValue.put("recordStatus", SinSimApp.STR_QUALITY_INSPECT_NOT_START);
        mPostValue.put("recordStatus", String.valueOf(SinSimApp.TASK_QUALITY_INSPECT_NOT_STARTED));
        mPostValue.put("page", ""+page);

        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_TO_QA;
        Network.Instance(SinSimApp.getApp()).fetchProcessTaskRecordData(fetchProcessRecordUrl, mPostValue, new FetchProcessDataHandler());
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
                mProcessToCheckoutList=(ArrayList<TaskRecordMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToCheckoutList.size());

                if (mProcessToCheckoutList.size() > 0) {
                    ArrayList<TaskRecordMachineListData> abnormalList = new ArrayList<>();
                    ArrayList<TaskRecordMachineListData> orderChangeList = new ArrayList<>();
                    ArrayList<TaskRecordMachineListData> skipList = new ArrayList<>();
                    ArrayList<TaskRecordMachineListData> normalList = new ArrayList<>();
                    for (int position = 0; position < mProcessToCheckoutList.size(); position++) {
                        if (mProcessToCheckoutList.get(position).getMachineData().getStatus() == SinSimApp.MACHINE_CANCELED) {
                            mProcessToCheckoutList.remove(position);
                        }else if (mProcessToCheckoutList.get(position).getMachineData().getStatus()==SinSimApp.MACHINE_CHANGED
                                ||mProcessToCheckoutList.get(position).getMachineData().getStatus()==SinSimApp.MACHINE_SPLITED) {
                            orderChangeList.add(mProcessToCheckoutList.get(position));
                        } else {
                            switch (mProcessToCheckoutList.get(position).getStatus()) {
                                case SinSimApp.TASK_INITIAL:
                                case SinSimApp.TASK_PLANED:
                                case SinSimApp.TASK_INSTALL_WAITING:
                                case SinSimApp.TASK_INSTALLING:
                                case SinSimApp.TASK_INSTALLED:
                                case SinSimApp.TASK_QUALITY_DOING:
                                case SinSimApp.TASK_QUALITY_DONE:

                                //3期
                                case SinSimApp.TASK_QUALITY_INSPECT_NOT_STARTED:
                                case SinSimApp.TASK_QUALITY_INSPECT_NO_SUCH_ITEM:
                                case SinSimApp.TASK_QUALITY_INSPECT_NG:
                                case SinSimApp.TASK_QUALITY_INSPECT_OK:
                                case SinSimApp.TASK_QUALITY_INSPECT_HAVE_NOT_CHECKED:
                                    normalList.add(mProcessToCheckoutList.get(position));
                                    break;
                                case SinSimApp.TASK_INSTALL_ABNORMAL:
                                case SinSimApp.TASK_QUALITY_ABNORMAL:
                                    abnormalList.add(mProcessToCheckoutList.get(position));
                                    break;
                                case SinSimApp.TASK_SKIP:
                                    skipList.add(mProcessToCheckoutList.get(position));
                                    break;
                                default:
                                    break;

                            }
                        }
                    }
                    Log.d(TAG, "handleMessage: 机器状态排序!");
                    //排序：异常-》改单拆单-》跳过-》正常
                    abnormalList.addAll(orderChangeList);
                    abnormalList.addAll(skipList);
                    abnormalList.addAll(normalList);
                    mProcessToCheckoutList=abnormalList;
                }
                if (mProcessToCheckoutList.size()==0){
                    mTaskRecordAdapter.setProcessList(mProcessToCheckoutList);
                    mTaskRecordAdapter.notifyDataSetChanged();
                    Toast.makeText(ProcessToCheckoutActivity.this, "没有更多了...", Toast.LENGTH_SHORT).show();
                }else {
                    mTaskRecordAdapter.setProcessList(mProcessToCheckoutList);
                    mTaskRecordAdapter.notifyDataSetChanged();
                    Toast.makeText(ProcessToCheckoutActivity.this, "列表已更新！", Toast.LENGTH_SHORT).show();
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(ProcessToCheckoutActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SCAN_QRCODE_START:
                if (resultCode == RESULT_OK)
                {
                    // 取出Intent里的扫码结果去执行机器查找
                    String mMachineNamePlate = data.getStringExtra("mMachineNamePlate");
                    ArrayList<TaskRecordMachineListData> mScanResultList = new ArrayList<>();
                    for (int i=0;i<mProcessToCheckoutList.size();i++){
                        if (mProcessToCheckoutList.get(i).getMachineData().getNameplate().equals(mMachineNamePlate)){
                            mScanResultList.add(mProcessToCheckoutList.get(i));
                        }
                    }
                    if (mScanResultList.isEmpty()||mScanResultList.size()<1){
                        Toast.makeText(ProcessToCheckoutActivity.this, "没有内容!", Toast.LENGTH_LONG).show();
                        return;
                    }else {
                        Intent intent = new Intent(ProcessToCheckoutActivity.this, ScanResultActivity.class);
                        intent.putExtra("mTaskRecordMachineList", (Serializable) mScanResultList);
                        startActivity(intent);
                    }
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
                stopService(mqttIntent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoadingProcessDialog != null) {
            mLoadingProcessDialog.dismiss();
        }
    }
}
