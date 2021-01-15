package com.example.nan.ssprocess.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.bean.basic.InstallPlanData;
import com.example.nan.ssprocess.bean.basic.QualityInspectData;

import java.util.ArrayList;

/**
 * Created by wxm on 2020/12/24.
 */

public class QualityInspectAdapter extends RecyclerView.Adapter {

    private static String TAG = "QualityRespectlAdapter";
    private ArrayList<QualityInspectData> mQualityInspectAdapter;
    private QualityInspectAdapter.OnItemClickListener itemClickListener = null;

    public QualityInspectAdapter(ArrayList<QualityInspectData> list) {
        mQualityInspectAdapter = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_qa_info,parent,false);
        return new QualityInspectAdapter.ItemView(view);

    }

    /**
     * 绑定数据
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final QualityInspectAdapter.ItemView itemView = (QualityInspectAdapter.ItemView) holder;
        //itemView.setIsRecyclable(false);//禁止复用
        if (mQualityInspectAdapter !=null && !mQualityInspectAdapter.isEmpty() && position < mQualityInspectAdapter.size()) {
            itemView.itemInspectNameTv.setText(mQualityInspectAdapter.get(position).getInspectName());
            itemView.itemInspectNameTv.setSelected(true);//用于滚动显示
            itemView.itemInspectContentTv.setText(mQualityInspectAdapter.get(position).getInspectContent());
        }else {
            Log.d(TAG, "onBindViewHolder: 没有获取到list数据");
        }
    }

    @Override
    public int getItemCount() {
        return mQualityInspectAdapter.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mQualityInspectAdapter.size()) {
            return 1;
        } else {
            return 0;
        }
    }

    public class ItemView extends RecyclerView.ViewHolder {
//        CardView installPlanLayout;
        TextView itemInspectNameTv;
        TextView itemInspectContentTv;

        ItemView(View itemView) {
            super(itemView);
            itemInspectNameTv = itemView.findViewById(R.id.item_inspect_name);
            itemInspectContentTv = itemView.findViewById(R.id.item_inspect_content);
        }
    }
    public void setProcessList(ArrayList<QualityInspectData> list) {
        mQualityInspectAdapter.clear();
        mQualityInspectAdapter.addAll(list);
    }

    /**
     * 点击事件接口
     */
    public interface OnItemClickListener{
        void onItemClick(int position);
        void onFinishItemClick(int position);
        void onNotFinishItemClick(int position);
    }

    /**
     * 设置点击事件的方法
     */
    public void setOnItemClickListener(QualityInspectAdapter.OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
}
