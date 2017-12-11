package com.example.nan.ssprocess.fragment;

import android.annotation.SuppressLint;
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

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.ProcessToAdminAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.TaskRecordDataListContent;
import com.example.nan.ssprocess.net.Network;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TabInstallPlanFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TabInstallPlanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabInstallPlanFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    private OnFragmentInteractionListener mListener;
    private static String TAG = "nlgProcessToAdminActivity";
    private ArrayList<TaskRecordDataListContent> mProcessToAdminList = new ArrayList<>();
    private ProcessToAdminAdapter mProcessToAdminAdapter;
    private FetchProcessDataHandler mFetchProcessDataHandler = new FetchProcessDataHandler();

    private SwipeRefreshLayout mSwipeRefresh;
    private Runnable mStopSwipeRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if(mSwipeRefresh.isRefreshing()) {
                mSwipeRefresh.setRefreshing(false);
            }
        }
    };
    public TabInstallPlanFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabInstallPlanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TabInstallPlanFragment newInstance(String param1, String param2) {
        TabInstallPlanFragment fragment = new TabInstallPlanFragment();
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
        // Inflate the layout for this fragment
        View viewContent = inflater.inflate(R.layout.fragment_tab_install_plan, container, false);
        RecyclerView mProcessToAdminRV = (RecyclerView) viewContent.findViewById(R.id.process_to_install_rv);
        LinearLayoutManager manager = new LinearLayoutManager(viewContent.getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mProcessToAdminRV.setLayoutManager(manager);
        mProcessToAdminAdapter = new ProcessToAdminAdapter(mProcessToAdminList);
        mProcessToAdminRV.setAdapter(mProcessToAdminAdapter);

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

        fetchProcessData();
        return viewContent;
    }

    private void fetchProcessData() {
        final String account = SinSimApp.getApp().getAccount();
        final String ip = SinSimApp.getApp().getServerIP();
//        final String ip = "192.168.0.102:8080";
//        final String account = "sss";
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("userAccount", account);
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FETCH_PROCESS_RECORD;
        Network.Instance(SinSimApp.getApp()).fetchProcessModuleData(fetchProcessRecordUrl, mPostValue, mFetchProcessDataHandler);
    }

    @SuppressLint("HandlerLeak")
    private class FetchProcessDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mSwipeRefresh.isRefreshing()) {
                mSwipeRefresh.setRefreshing(false);
            }
            if (msg.what == Network.OK) {
                mProcessToAdminList=(ArrayList<TaskRecordDataListContent>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mProcessToAdminList.size());
                mProcessToAdminAdapter.setProcessList(mProcessToAdminList);
                mProcessToAdminAdapter.notifyDataSetChanged();
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
