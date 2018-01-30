package com.example.nan.ssprocess.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.ui.activity.DetailToInstallActivity;
import com.example.nan.ssprocess.adapter.TaskRecordAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static android.app.Activity.RESULT_OK;


public class TabInstallReadyFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static String TAG = "nlgTabInstallReadyFragment";
    private ArrayList<TaskMachineListData> mProcessToInstallReadyList = new ArrayList<>();
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
                intent.putExtra("mTaskMachineListData", mProcessToInstallReadyList.get(position));
                startActivity(intent);
            }
        });

        //下拉刷新
        mSwipeRefresh = (SwipeRefreshLayout) viewContent.findViewById(R.id.install_swipe_refresh);
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

        //第一次进入刷新页面， 加载loading页面
        if( mLoadingProcessDialog == null) {
            mLoadingProcessDialog = new ProgressDialog(getActivity());
            mLoadingProcessDialog.setCancelable(false);
            mLoadingProcessDialog.setCanceledOnTouchOutside(false);
            mLoadingProcessDialog.setMessage("获取信息中...");
        }
        mLoadingProcessDialog.show();

        fetchProcessData();
        return viewContent;
    }

    private void fetchProcessData() {
        final String account = SinSimApp.getApp().getAccount();
        final String ip = SinSimApp.getApp().getServerIP();
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("userAccount", account);
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_TO_INSTALL;
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
                mProcessToInstallReadyList=(ArrayList<TaskMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToInstallReadyList.size());
                mTaskRecordAdapter.setProcessList(mProcessToInstallReadyList);
                mTaskRecordAdapter.notifyDataSetChanged();
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
            }
        }
    }

}
