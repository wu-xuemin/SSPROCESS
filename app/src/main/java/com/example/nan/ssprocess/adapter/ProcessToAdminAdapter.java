package com.example.nan.ssprocess.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.bean.basic.ProcessModuleListData;
import com.example.nan.ssprocess.bean.basic.ProcessModuleResponseData;


import java.util.ArrayList;

/**
 * Created by Young on 2017/11/26.
 */

public class ProcessToAdminAdapter extends RecyclerView.Adapter<ProcessToAdminAdapter.ViewHolder>{

    private ArrayList<ProcessModuleListData> mProcessList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View processView;
        TextView machineIdTv;
        TextView processNameTv;
        TextView processStateTv;
        TextView beginDateTv;
        TextView endDateTv;

        public ViewHolder(View itemView) {
            super(itemView);
            processView = itemView;
            machineIdTv = (TextView) itemView.findViewById(R.id.process_machine_id_tv);
            processNameTv = (TextView) itemView.findViewById(R.id.process_name_tv);
            processStateTv = (TextView) itemView.findViewById(R.id.process_state_tv);
            beginDateTv = (TextView) itemView.findViewById(R.id.process_begin_date_tv);
            endDateTv = (TextView) itemView.findViewById(R.id.process_end_date_tv);

        }
    }
    public ProcessToAdminAdapter(ArrayList<ProcessModuleListData> list) {
        mProcessList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_of_process,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProcessToAdminAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

//    public FruitAdapter(List<Fruit> fruitList) {
//        mFruitList = fruitList;
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fruit_item, parent, false);
//        final ViewHolder holder = new ViewHolder(view);
//        holder.fruitView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int position = holder.getAdapterPosition();
//                Fruit fruit = mFruitList.get(position);
//                Toast.makeText(v.getContext(), "you clicked view " + fruit.getName(), Toast.LENGTH_SHORT).show();
//            }
//        });
//        holder.fruitImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int position = holder.getAdapterPosition();
//                Fruit fruit = mFruitList.get(position);
//                Toast.makeText(v.getContext(), "you clicked image " + fruit.getName(), Toast.LENGTH_SHORT).show();
//            }
//        });
//        return holder;
//    }
//
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        Fruit fruit = mFruitList.get(position);
//        holder.fruitImage.setImageResource(fruit.getImageId());
//        holder.fruitName.setText(fruit.getName());
//    }
//
//    @Override
//    public int getItemCount() {
//        return mFruitList.size();
//    }


}
