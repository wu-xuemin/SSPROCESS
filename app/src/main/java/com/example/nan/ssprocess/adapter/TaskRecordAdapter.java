package com.example.nan.ssprocess.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Young on 2017/11/26.
 */

public class TaskRecordAdapter extends RecyclerView.Adapter {

    private static String TAG = "nlgTaskRecordAdapter";
    private ArrayList<TaskRecordMachineListData> mProcessList;
    private OnItemClickListener itemClickListener = null;

    public TaskRecordAdapter(ArrayList<TaskRecordMachineListData> list) {
        mProcessList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_of_process,parent,false);
        return new ItemView(view);

    }

    /**
     * 绑定数据
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final ItemView itemView = (ItemView) holder;
        //itemView.setIsRecyclable(false);//禁止复用
        if (mProcessList!=null && !mProcessList.isEmpty() && position < mProcessList.size()) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date planShipDate = new Date(mProcessList.get(position).getMachineOrderData().getPlanShipDate());
            if(mProcessList.get(position).getMachineOrderData().getPlanShipDate() == 0) {
                itemView.planShipDateTv.setVisibility(View.INVISIBLE);
            }else {
                itemView.planShipDateTv.setText(formatter.format(planShipDate));
            }
            Date planDate = new Date(mProcessList.get(position).getTaskPlan().getPlanTime());
            if(mProcessList.get(position).getTaskPlan().getPlanTime() == 0) {
                itemView.planDateTv.setVisibility(View.INVISIBLE);
            } else {
                itemView.planDateTv.setText(formatter.format(planDate));
            }
            itemView.machineIdTv.setText("" + mProcessList.get(position).getMachineData().getNameplate());
            itemView.processNameTv.setText(mProcessList.get(position).getTaskName());
            if (mProcessList.get(position).getMachineData().getStatus()==SinSimApp.MACHINE_CHANGED){
                itemView.processStateTv.setText("改单中");
                itemView.taskStatusIv.setImageResource(R.mipmap.change);
            }else if(mProcessList.get(position).getMachineData().getStatus()==SinSimApp.MACHINE_SPLITED) {
                itemView.processStateTv.setText("拆单中");
                itemView.taskStatusIv.setImageResource(R.mipmap.split);
            } else if(mProcessList.get(position).getMachineData().getStatus()==SinSimApp.MACHINE_CANCELED) {
                itemView.processStateTv.setText("已取消");
                itemView.taskStatusIv.setImageResource(R.mipmap.cancel);
            } else {
                switch (mProcessList.get(position).getStatus()) {
                    case SinSimApp.TASK_INITIAL:
                        itemView.taskStatusIv.setImageResource(R.mipmap.initial);
                        itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_INITIAL));
                        break;
                    case SinSimApp.TASK_PLANED:
                        itemView.taskStatusIv.setImageResource(R.mipmap.initial);
                        itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_PLANED));
                        break;
                    case SinSimApp.TASK_INSTALL_WAITING:
                        itemView.taskStatusIv.setImageResource(R.mipmap.install);
                        itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_INSTALL_WAITING));
                        break;
                    case SinSimApp.TASK_INSTALLING:
                        itemView.taskStatusIv.setImageResource(R.mipmap.install);
                        itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_INSTALLING));
                        break;
                    case SinSimApp.TASK_INSTALLED:
                        itemView.taskStatusIv.setImageResource(R.mipmap.quality);
                        itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_INSTALLED));
                        break;
                    case SinSimApp.TASK_QUALITY_DOING:
                        itemView.taskStatusIv.setImageResource(R.mipmap.quality);
                        itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_QUALITY_DOING));
                        break;
                    case SinSimApp.TASK_QUALITY_DONE:
                        itemView.taskStatusIv.setImageResource(R.mipmap.quality);
                        itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_QUALITY_DONE));
                        break;
                    case SinSimApp.TASK_INSTALL_ABNORMAL:
                        itemView.taskStatusIv.setImageResource(R.mipmap.abnormal);
                        itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_INSTALL_ABNORMAL));
                        break;
                    case SinSimApp.TASK_QUALITY_ABNORMAL:
                        itemView.taskStatusIv.setImageResource(R.mipmap.abnormal);
                        itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_QUALITY_ABNORMAL));
                        break;
                    case SinSimApp.TASK_SKIP:
                        itemView.taskStatusIv.setImageResource(R.mipmap.jump);
                        itemView.processStateTv.setText(SinSimApp.getInstallStatusString(SinSimApp.TASK_SKIP));
                        break;
                    default:
                        break;

                }
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
        CardView itemLinearLayout;
        ImageView taskStatusIv;
        TextView machineIdTv;
        TextView processNameTv;
        TextView processStateTv;
        TextView planShipDateTv;
        TextView planDateTv;

        ItemView(View itemView) {
            super(itemView);
            itemLinearLayout=itemView.findViewById(R.id.item_linear_layout);
            taskStatusIv=itemView.findViewById(R.id.task_status_iv);
            machineIdTv = (TextView) itemView.findViewById(R.id.process_machine_id_tv);
            processNameTv = (TextView) itemView.findViewById(R.id.process_name_tv);
            processStateTv = (TextView) itemView.findViewById(R.id.process_state_tv);
            planShipDateTv = (TextView) itemView.findViewById(R.id.process_begin_date_tv);
            planDateTv = (TextView) itemView.findViewById(R.id.process_end_date_tv);
        }
    }
    public void setProcessList(ArrayList<TaskRecordMachineListData> list) {
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
    public void setOnItemClickListener(OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
}
