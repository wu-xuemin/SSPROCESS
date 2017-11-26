package com.example.nan.ssprocess.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.bean.basic.ProcessModuleResponseData;
import com.example.nan.ssprocess.bean.basic.ProcessModuleResponseData;


import java.util.ArrayList;

/**
 * Created by Young on 2017/11/26.
 */

public class ProcessToAdminAdapter extends RecyclerView.Adapter{
    private ArrayList<ProcessModuleResponseData> mProcessList;
    private OnItemClickListener itemClickListener = null;
    public ProcessToAdminAdapter(ArrayList<ProcessModuleResponseData> list) {
        mProcessList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_of_process,parent,false);
        return new ItemView(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ItemView itemView = (ItemView) holder;
        //itemView.setIsRecyclable(false);//禁止复用
        itemView.idTextView.setText(mProcessList.get(position).getMachineId());
        itemView.idTextView.setOnClickListener(new View.OnClickListener() {
                                                     @Override
                                                     public void onClick(View v) {
                                                         if(itemClickListener != null) {
                                                             itemClickListener.onItemClick(position);
                                                         }
                                                     }
                                                 }
        );
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ItemView extends RecyclerView.ViewHolder {
        TextView idTextView;
        public ItemView(View itemView) {
            super(itemView);
            idTextView = (TextView) (itemView.findViewById(R.id.process_machine_id_tv));
        }
    }
    public void setProcessList(ArrayList<ProcessModuleResponseData> list) {
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
