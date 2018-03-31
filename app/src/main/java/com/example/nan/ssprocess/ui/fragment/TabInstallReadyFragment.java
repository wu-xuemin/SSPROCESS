package com.example.nan.ssprocess.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.ui.activity.DetailToInstallActivity;
import com.example.nan.ssprocess.adapter.TaskRecordAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.example.nan.ssprocess.ui.activity.ScanQrcodeActivity;
import com.example.nan.ssprocess.ui.activity.ScanResultActivity;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import cn.bingoogolapple.refreshlayout.BGAMoocStyleRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

import static android.app.Activity.RESULT_OK;


public class TabInstallReadyFragment extends Fragment implements BGARefreshLayout.BGARefreshLayoutDelegate{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static String TAG = "nlgTabInstallReadyFragment";
    private ArrayList<TaskRecordMachineListData> mProcessToInstallReadyList = new ArrayList<>();
    private TaskRecordAdapter mTaskRecordAdapter;
    private FetchProcessDataHandler mFetchProcessDataHandler = new FetchProcessDataHandler();
    private ProgressDialog mLoadingProcessDialog;
    private int mPage;
    private BGARefreshLayout mRefreshLayout;

    private static final int SCAN_QRCODE_START = 1;

    public TabInstallReadyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabInstallReadyFragment.
     */
    public static TabInstallReadyFragment newInstance(String param1, String param2) {
        TabInstallReadyFragment fragment = new TabInstallReadyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewContent = inflater.inflate(R.layout.fragment_tab_install_ready, container, false);

        //列表
        RecyclerView mProcessToAdminRV = viewContent.findViewById(R.id.process_to_install_rv);
        LinearLayoutManager manager = new LinearLayoutManager(viewContent.getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mProcessToAdminRV.setLayoutManager(manager);
        mTaskRecordAdapter = new TaskRecordAdapter(mProcessToInstallReadyList);
        mProcessToAdminRV.addItemDecoration(new DividerItemDecoration(viewContent.getContext(),DividerItemDecoration.VERTICAL));
        mProcessToAdminRV.setAdapter(mTaskRecordAdapter);
        //点击跳转，把所有接收到的数据传递给下一个activity
        mTaskRecordAdapter.setOnItemClickListener(new TaskRecordAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(int position){
                Log.d(TAG, "onItemClick: gson :"+new Gson().toJson(mProcessToInstallReadyList.get(position)));
                Intent intent=new Intent(getActivity(),DetailToInstallActivity.class);
                intent.putExtra("mTaskRecordMachineListData", mProcessToInstallReadyList.get(position));
                startActivity(intent);
            }
        });

        //点击扫码
        Button scanQrcodeBotton = viewContent.findViewById(R.id.unplaned_install_scan_qrcode_button);
        scanQrcodeBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),ScanQrcodeActivity.class);
                startActivityForResult(intent,SCAN_QRCODE_START);
            }
        });

        mRefreshLayout = viewContent.findViewById(R.id.refreshLayout);
        mRefreshLayout.setDelegate(this);
        mPage=0;
        // 设置下拉刷新和上拉加载更多的风格     参数1：应用程序上下文，参数2：是否具有上拉加载更多功能
        BGAMoocStyleRefreshViewHolder moocStyleRefreshViewHolder = new BGAMoocStyleRefreshViewHolder(viewContent.getContext(), true);
        moocStyleRefreshViewHolder.setOriginalImage(R.drawable.bga_refresh_moooc);
        moocStyleRefreshViewHolder.setUltimateColor(R.color.colorAccent);
        mRefreshLayout.setRefreshViewHolder(moocStyleRefreshViewHolder);

        return viewContent;
    }
    @Override
    public void onStart() {
        super.onStart();
        //第一次进入刷新页面， 加载loading页面
        if( mLoadingProcessDialog == null) {
            mLoadingProcessDialog = new ProgressDialog(getActivity());
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
        mPostValue.put("userAccount", account);
        mPostValue.put("page", ""+page);
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_TO_UNPLANNED_INSTALL;
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
                mProcessToInstallReadyList=(ArrayList<TaskRecordMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToInstallReadyList.size());
                if (mProcessToInstallReadyList.size()==0){
                    Toast.makeText(getContext(),"没有更多了...",Toast.LENGTH_SHORT).show();
                } else {
                    mTaskRecordAdapter.setProcessList(mProcessToInstallReadyList);
                    mTaskRecordAdapter.notifyDataSetChanged();
                }
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(getContext(),"更新失败!"+errorMsg,Toast.LENGTH_SHORT).show();
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
                    String mMachineNamePlate = data.getStringExtra("mMachineNamePlate");
                    final String ip = SinSimApp.getApp().getServerIP();
                    LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                    String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_BY_SCAN_QRCORD_TO_UNPLANNED_INSTALL;
                    mPostValue.put("page", ""+mPage);
                    mPostValue.put("namePlate", ""+mMachineNamePlate);
                    mPostValue.put("account", ""+SinSimApp.getApp().getAccount());
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
                ArrayList<TaskRecordMachineListData> mScanResultList=(ArrayList<TaskRecordMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mScanResultList.size());
                if (mScanResultList.size()==0){
                    Toast.makeText(getContext(), "没有内容!", Toast.LENGTH_LONG).show();
                } else {
                    if (mScanResultList.get(0).getMachineData().getStatus()==SinSimApp.MACHINE_CHANGED
                            ||mScanResultList.get(0).getMachineData().getStatus()==SinSimApp.MACHINE_SPLITED
                            ||mScanResultList.get(0).getMachineData().getStatus()==SinSimApp.MACHINE_CANCELED) {
                        Toast.makeText(getContext(), "正在改单/拆单中!", Toast.LENGTH_LONG).show();
                    }else {
                        Intent intent = new Intent(getContext(), ScanResultActivity.class);
                        intent.putExtra("mTaskRecordMachineList", (Serializable) mScanResultList);
                        startActivity(intent);
                    }
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(getContext(), "网络错误！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mLoadingProcessDialog != null) {
            mLoadingProcessDialog.dismiss();
        }
    }
}
