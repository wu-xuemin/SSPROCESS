package com.example.nan.ssprocess.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.activity.DetailToAdminActivity;
import com.example.nan.ssprocess.activity.ProcessToAdminActivity;
import com.example.nan.ssprocess.activity.ProcessToCheckoutActivity;
import com.example.nan.ssprocess.activity.ProcessToInstallActivity;
import com.example.nan.ssprocess.activity.ScanQrcodeActivity;
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

    private static String TAG = "nlgProcessToAdminActivity";
    private ArrayList<TaskMachineListData> mProcessToInstallList = new ArrayList<>();
    private TaskRecordAdapter mTaskRecordAdapter;
    private FetchProcessDataHandler mFetchProcessDataHandler = new FetchProcessDataHandler();
    private ProgressDialog mLoadingProcessDialog;
    private static final int SCAN_QRCODE_START = 1;

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

        //点击扫码
        Button scanQrcodeBotton = viewContent.findViewById(R.id.admin_scan_qrcode_button);
        scanQrcodeBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(),ScanQrcodeActivity.class);
                startActivityForResult(intent,SCAN_QRCODE_START);
            }
        });

        //列表
        RecyclerView mProcessToAdminRV = viewContent.findViewById(R.id.process_to_install_rv);
        LinearLayoutManager manager = new LinearLayoutManager(viewContent.getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mProcessToAdminRV.setLayoutManager(manager);
        mTaskRecordAdapter = new TaskRecordAdapter(mProcessToInstallList);
        mProcessToAdminRV.setAdapter(mTaskRecordAdapter);
        //点击跳转，把所有接收到的数据传递给下一个activity
        mTaskRecordAdapter.setOnItemClickListener(new TaskRecordAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(int position){
                Log.d(TAG, "onItemClick: gson :"+new Gson().toJson(mProcessToInstallList.get(position)));
                Intent intent=new Intent(getActivity(),DetailToAdminActivity.class);
                intent.putExtra("mTaskMachineListData", mProcessToInstallList.get(position));
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
                    Intent intent=new Intent(getActivity(),DetailToAdminActivity.class);
                    intent.putExtra("mTaskMachineListData", mTaskMachineListData);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    private void fetchProcessData() {
        final String account = SinSimApp.getApp().getAccount();
        final String ip = SinSimApp.getApp().getServerIP();
//        final String ip = "192.168.0.102:8080";
//        final String account = "sss";
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("userAccount", account);
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_TO_INSTALL;
        Network.Instance(SinSimApp.getApp()).fetchProcessTaskRecordData(fetchProcessRecordUrl, mPostValue, mFetchProcessDataHandler);
    }

    @SuppressLint("HandlerLeak")
    private class FetchProcessDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mSwipeRefresh.isRefreshing()) {
                mSwipeRefresh.setRefreshing(false);
            }
            if (msg.what == Network.OK) {
                mProcessToInstallList=(ArrayList<TaskMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToInstallList.size());
                mTaskRecordAdapter.setProcessList(mProcessToInstallList);
                mTaskRecordAdapter.notifyDataSetChanged();
            } else {
                String errorMsg = (String)msg.obj;
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
