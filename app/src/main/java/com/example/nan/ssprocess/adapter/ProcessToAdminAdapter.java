package com.example.nan.ssprocess.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.bean.basic.TaskRecordDataList;
import com.example.nan.ssprocess.bean.basic.TaskRecordDataListContent;


import java.util.ArrayList;

/**
 * Created by Young on 2017/11/26.
 */

public class ProcessToAdminAdapter extends RecyclerView.Adapter{

    private ArrayList<TaskRecordDataListContent> mProcessList;
    private OnItemClickListener itemClickListener = null;

    public ProcessToAdminAdapter(ArrayList<TaskRecordDataListContent> list) {
        mProcessList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_of_process,parent,false);
        return new ItemView(view);
    }

    //绑定数据
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ItemView itemView = (ItemView) holder;
        //itemView.setIsRecyclable(false);//禁止复用
        itemView.machineIdTv.setText(""+mProcessList.get(position).getId());
        itemView.processNameTv.setText(mProcessList.get(position).getTaskName());
        itemView.processStateTv.setText(""+mProcessList.get(position).getStatus());
        itemView.beginDateTv.setText(mProcessList.get(position).getBeginTime());
        itemView.endDateTv.setText(mProcessList.get(position).getEndTime());

//        itemView.machineIdTv.setOnClickListener(new View.OnClickListener() {
//                                                     @Override
//                                                     public void onClick(View v) {
//                                                         if(itemClickListener != null) {
//                                                             itemClickListener.onItemClick(position);
//                                                         }
//                                                     }
//                                                 }
//        );
    }

    @Override
    public int getItemCount() {
        return mProcessList.size();
    }

    public class ItemView extends RecyclerView.ViewHolder {
        TextView machineIdTv;
        TextView processNameTv;
        TextView processStateTv;
        TextView beginDateTv;
        TextView endDateTv;
        public ItemView(View itemView) {
            super(itemView);
            machineIdTv = (TextView) itemView.findViewById(R.id.process_machine_id_tv);
            processNameTv = (TextView) itemView.findViewById(R.id.process_name_tv);
            processStateTv = (TextView) itemView.findViewById(R.id.process_state_tv);
            beginDateTv = (TextView) itemView.findViewById(R.id.process_begin_date_tv);
            endDateTv = (TextView) itemView.findViewById(R.id.process_end_date_tv);
        }
    }
    public void setProcessList(ArrayList<TaskRecordDataListContent> list) {
        mProcessList.clear();
        mProcessList.addAll(list);
    }
    //点击事件接口
    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    //设置点击事件的方法
    public void setItemClickListener(OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
}
