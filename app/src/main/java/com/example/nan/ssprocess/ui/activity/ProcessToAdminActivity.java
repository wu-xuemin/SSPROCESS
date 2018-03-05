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
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.io.Serializable;
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
    private ArrayList<TaskMachineListData> mScanResultList = new ArrayList<>();
    private TaskMachineListData mScanResultListData=new TaskMachineListData();
    private TaskRecordAdapter mProcessToAdminAdapter;
    private int mPage;
    private BGARefreshLayout mRefreshLayout;

    private AlertDialog mLocationSettngDialog=null;
    private ProgressDialog mLoadingProcessDialog;
    private ProgressDialog mUpdateingProcessDialog;
    private static final int SCAN_QRCODE_START = 1;
    private final String IP = SinSimApp.getApp().getServerIP();

    private String location;

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
                mProcessToAdminList=(ArrayList<TaskMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToAdminList.size());
                if (mProcessToAdminList.size()==0){
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SCAN_QRCODE_START:
                if (resultCode == RESULT_OK)
                {
                    // 取出Intent里的扫码结果去执行机器查找
                    String mMachineStrId = data.getStringExtra("mMachineStrId");
                    LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                    String fetchProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FETCH_TASK_RECORD_BY_SCAN_QRCORD_TO_ADMIN;
                    mPostValue.put("page", ""+mPage);
                    mPostValue.put("machineStrId", ""+mMachineStrId);
                    Network.Instance(SinSimApp.getApp()).fetchProcessTaskRecordData(fetchProcessRecordUrl, mPostValue, new FetchProcessListDataHandler());
                }
                break;
            default:
                break;
        }
    }
    @SuppressLint("HandlerLeak")
    private class FetchProcessListDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                mScanResultList=(ArrayList<TaskMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mScanResultList.size());
                //结果为空就报错，有结果则取第一条信息位依据
                if (mScanResultList.size()==0){
                    Toast.makeText(ProcessToAdminActivity.this, "该机器编号没有内容!", Toast.LENGTH_LONG).show();
                } else {
                    //弹窗上传机器位置
                    LinearLayout layout = (LinearLayout) View.inflate(ProcessToAdminActivity.this, R.layout.dialog_location_seting, null);
                    final EditText dialogLocationEt = layout.findViewById(R.id.dialog_location_et);
                    mLocationSettngDialog = new AlertDialog.Builder(ProcessToAdminActivity.this).create();
                    mLocationSettngDialog.setView(layout);
                    //获取原有location信息
                    mScanResultListData=mScanResultList.get(0);
                    location = mScanResultListData.getMachineData().getLocation();
                    if (location.isEmpty()|| "".equals(location)){
                        dialogLocationEt.setText("");
                    } else {
                        dialogLocationEt.setText(location);
                    }
                    mLocationSettngDialog.setTitle("请输入 "+mScanResultListData.getMachineData().getNameplate()+" 的位置：");
                    mLocationSettngDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    mLocationSettngDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //获取dialog的输入信息，并上传到服务器
                            if (TextUtils.isEmpty(dialogLocationEt.getText())) {
                                Toast.makeText(ProcessToAdminActivity.this,"地址不能为空，请确认后重新输入！",Toast.LENGTH_SHORT).show();
                            }else {
                                location=dialogLocationEt.getText().toString();
                                if( mUpdateingProcessDialog == null) {
                                    mUpdateingProcessDialog = new ProgressDialog(ProcessToAdminActivity.this);
                                    mUpdateingProcessDialog.setCancelable(false);
                                    mUpdateingProcessDialog.setCanceledOnTouchOutside(false);
                                    mUpdateingProcessDialog.setMessage("上传信息中...");
                                }
                                mUpdateingProcessDialog.show();
                                updateProcessDetailData();
                            }
                        }
                    });
                    mLocationSettngDialog.show();
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(ProcessToAdminActivity.this, "网络错误！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 更新上传机器location
     */
    private void updateProcessDetailData() {
        //更新loaction状态
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

            if(mUpdateingProcessDialog != null && mUpdateingProcessDialog.isShowing()) {
                mUpdateingProcessDialog.dismiss();
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
        if(mLocationSettngDialog != null) {
            mLocationSettngDialog.dismiss();
        }
    }
}
