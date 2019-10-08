package com.example.nan.ssprocess.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.bean.basic.InstallPlanData;

import java.util.ArrayList;

/**
 * Created by nan on 2018/2/13.
 */

public class InstallActualAdapter extends RecyclerView.Adapter {

    private static String TAG = "nlgInstallPlanAdapter";
    private ArrayList<InstallPlanData> mInstallPlanAdapter;
    private InstallActualAdapter.OnItemClickListener itemClickListener = null;

    public InstallActualAdapter(ArrayList<InstallPlanData> list) {
        mInstallPlanAdapter = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_install_actual,parent,false);
        return new InstallActualAdapter.ItemView(view);

    }

    /**
     * 绑定数据
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final InstallActualAdapter.ItemView itemView = (InstallActualAdapter.ItemView) holder;
        //itemView.setIsRecyclable(false);//禁止复用
        if (mInstallPlanAdapter!=null && !mInstallPlanAdapter.isEmpty() && position < mInstallPlanAdapter.size()) {
            itemView.orderNumberTv.setText(mInstallPlanAdapter.get(position).getOrderNum());
            itemView.nameplateTv.setText(mInstallPlanAdapter.get(position).getNameplate());
            itemView.headNumberTv.setText(mInstallPlanAdapter.get(position).getHeadNum());
            //itemView.headCountDoneTv.setText(mInstallPlanAdapter.get(position).getLocation());//TODO

            itemView.installPlanLayout.setOnClickListener(new View.OnClickListener() {
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
        return mInstallPlanAdapter.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mInstallPlanAdapter.size()) {
            return 1;
        } else {
            return 0;
        }
    }

    public class ItemView extends RecyclerView.ViewHolder {
        CardView installPlanLayout;
        TextView orderNumberTv;
        TextView nameplateTv;
        TextView headNumberTv;
        TextView headCountDoneTv;

        ItemView(View itemView) {
            super(itemView);
            installPlanLayout = itemView.findViewById(R.id.item_install_actual_layout);
            orderNumberTv = itemView.findViewById(R.id.order_number_tv);
            nameplateTv = itemView.findViewById(R.id.nameplate_tv);
            headNumberTv = itemView.findViewById(R.id.head_number_tv);
            headCountDoneTv = itemView.findViewById(R.id.head_count_done_tv);
        }
    }
    public void setProcessList(ArrayList<InstallPlanData> list) {
        mInstallPlanAdapter.clear();
        mInstallPlanAdapter.addAll(list);
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
    public void setOnItemClickListener(InstallActualAdapter.OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
}
