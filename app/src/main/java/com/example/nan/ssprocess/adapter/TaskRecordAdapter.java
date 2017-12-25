package com.example.nan.ssprocess.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;


import java.util.ArrayList;

/**
 * Created by Young on 2017/11/26.
 */

public class TaskRecordAdapter extends RecyclerView.Adapter {

    private ArrayList<TaskMachineListData> mProcessList;
    private OnItemClickListener itemClickListener = null;

    public TaskRecordAdapter(ArrayList<TaskMachineListData> list) {
        mProcessList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_of_process,parent,false);
        return new ItemView(view);
    }

    //绑定数据
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final ItemView itemView = (ItemView) holder;
        //itemView.setIsRecyclable(false);//禁止复用
        itemView.machineIdTv.setText(""+mProcessList.get(position).getMachineData().getMachineId());
        itemView.processNameTv.setText(mProcessList.get(position).getTaskName());
        itemView.processStateTv.setText(""+mProcessList.get(position).getStatus());
        itemView.contractDateTv.setText(mProcessList.get(position).getMachineOrderData().getContractShipDate());
        itemView.planDateTv.setText(mProcessList.get(position).getMachineOrderData().getPlanShipDate());
        itemView.itemLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(itemClickListener != null) {
                    itemClickListener.onItemClick(position);
                }
            }
        });

        }

    @Override
    public int getItemCount() {
        return mProcessList.size();
    }

    public class ItemView extends RecyclerView.ViewHolder {
        LinearLayout itemLinearLayout;
        TextView machineIdTv;
        TextView processNameTv;
        TextView processStateTv;
        TextView contractDateTv;
        TextView planDateTv;

        ItemView(View itemView) {
            super(itemView);
            itemLinearLayout=itemView.findViewById(R.id.item_linear_layout);
            machineIdTv = (TextView) itemView.findViewById(R.id.process_machine_id_tv);
            processNameTv = (TextView) itemView.findViewById(R.id.process_name_tv);
            processStateTv = (TextView) itemView.findViewById(R.id.process_state_tv);
            contractDateTv = (TextView) itemView.findViewById(R.id.process_begin_date_tv);
            planDateTv = (TextView) itemView.findViewById(R.id.process_end_date_tv);
        }
    }
    public void setProcessList(ArrayList<TaskMachineListData> list) {
        mProcessList.clear();
        mProcessList.addAll(list);
    }
    //点击事件接口
    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    //设置点击事件的方法
    public void setOnItemClickListener(OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
}
