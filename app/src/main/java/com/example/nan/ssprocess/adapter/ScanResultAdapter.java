package com.example.nan.ssprocess.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;

import java.util.ArrayList;

/**
 * Created by nan on 2018/2/13.
 */

public class ScanResultAdapter extends RecyclerView.Adapter {

    private static String TAG = "nlgScanResultAdapter";
    private ArrayList<TaskMachineListData> mProcessList;
    private ScanResultAdapter.OnItemClickListener itemClickListener = null;

    public ScanResultAdapter(ArrayList<TaskMachineListData> list) {
        mProcessList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_of_matchine,parent,false);
        return new ScanResultAdapter.ItemView(view);

    }

    /**
     * 绑定数据
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final ScanResultAdapter.ItemView itemView = (ScanResultAdapter.ItemView) holder;
        //itemView.setIsRecyclable(false);//禁止复用
        if (mProcessList!=null && !mProcessList.isEmpty() && position < mProcessList.size()) {
            Log.d(TAG, "onBindViewHolder: 有数据"+position);
            itemView.machineIdTv.setText("" + mProcessList.get(position).getMachineData().getMachineStrId());
            itemView.processNameTv.setText(mProcessList.get(position).getTaskName());
            switch (mProcessList.get(position).getStatus()){
                case SinSimApp.TASK_INITIAL:
                    itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_INITIAL));
                    itemView.processStateTv.setTextColor(Color.YELLOW);
                    break;
                case SinSimApp.TASK_PLANED:
                    itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_PLANED));
                    itemView.processStateTv.setTextColor(Color.YELLOW);
                    break;
                case SinSimApp.TASK_INSTALL_WAITING:
                    itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_INSTALL_WAITING));
                    itemView.processStateTv.setTextColor(Color.GREEN);
                    break;
                case SinSimApp.TASK_INSTALLING:
                    itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_INSTALLING));
                    itemView.processStateTv.setTextColor(Color.BLUE);
                    break;
                case SinSimApp.TASK_INSTALLED:
                    itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_INSTALLED));
                    itemView.processStateTv.setTextColor(Color.GREEN);
                    break;
                case SinSimApp.TASK_QUALITY_DOING:
                    itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_QUALITY_DOING));
                    itemView.processStateTv.setTextColor(Color.BLUE);
                    break;
                case SinSimApp.TASK_QUALITY_DONE:
                    itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_QUALITY_DONE));
                    itemView.processStateTv.setTextColor(Color.GREEN);
                    break;
                case SinSimApp.TASK_INSTALL_ABNORMAL:
                    itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_INSTALL_ABNORMAL));
                    itemView.processStateTv.setTextColor(Color.RED);
                    break;
                case SinSimApp.TASK_QUALITY_ABNORMAL:
                    itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_QUALITY_ABNORMAL));
                    itemView.processStateTv.setTextColor(Color.RED);
                    break;
                default:
                    break;

            }

            itemView.itemLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(position);
                    }
                }
            });
        }else {
            Log.d(TAG, "onBindViewHolder: 没有获取到list数据");
        }
    }

    @Override
    public int getItemCount() {
        return mProcessList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mProcessList.size()) {
            return 1;
        } else {
            return 0;
        }
    }

    public class ItemView extends RecyclerView.ViewHolder {
        LinearLayout itemLinearLayout;
        TextView machineIdTv;
        TextView processNameTv;
        TextView processStateTv;

        ItemView(View itemView) {
            super(itemView);
            itemLinearLayout=itemView.findViewById(R.id.item_scan_result_layout);
            machineIdTv = itemView.findViewById(R.id.process_machine_id_tv);
            processNameTv = itemView.findViewById(R.id.process_name_tv);
            processStateTv = itemView.findViewById(R.id.process_state_tv);
        }
    }
    public void setProcessList(ArrayList<TaskMachineListData> list) {
        mProcessList.clear();
        mProcessList.addAll(list);
    }

    /**
     * 点击事件接口
     */
    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    /**
     * 设置点击事件的方法
     */
    public void setOnItemClickListener(ScanResultAdapter.OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
}
