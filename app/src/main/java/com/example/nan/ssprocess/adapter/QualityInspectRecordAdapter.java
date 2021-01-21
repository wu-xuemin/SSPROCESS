package com.example.nan.ssprocess.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;

import java.util.List;

/**
 * Created by wxm on 2020/12/24.
 */
// extends RecyclerView.Adapter
public class QualityInspectRecordAdapter extends RecyclerView.Adapter<QualityInspectRecordAdapter.ItemView> implements View.OnClickListener {

    private static String TAG = "QualityInspectRecordAdapter";
    private OnItemClickListener mOnItemClickListener;//声明自定义的接口

//    private List<QualityInspectData> dataList;//数据源
    private List<TaskRecordMachineListData> dataList;//数据源

    private Context context;//上下文
    /// 这里，传数据
    public QualityInspectRecordAdapter(List<TaskRecordMachineListData> list, Context context ) {
        this.dataList = list;
        this.context = context;
    }

    public void updateDataSoruce(List<TaskRecordMachineListData> list)
    {
        this.dataList = list;
        notifyDataSetChanged();
    }

//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_qa_info,parent,false);
//        return new QualityInspectAdapter.ItemView(view);
//
//    }

    @NonNull
    @Override
    public ItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_qa_info, parent, false);
        return new ItemView(view);
    }


    /**
     * 绑定数据
     */
    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
    public void onBindViewHolder(ItemView holder, int position) {
//        final QualityInspectRecordAdapter.ItemView itemView = (QualityInspectRecordAdapter.ItemView) holder;
        //itemView.setIsRecyclable(false);//禁止复用
        if (dataList !=null && !dataList.isEmpty() && position < dataList.size()) {
            //显示编号，方便查看
            holder.itemInspectNameTv.setText((position+1) + ". " + dataList.get(position).getInspectName());
            holder.itemInspectNameTv.setSelected(true);//用于滚动显示
            holder.itemInspectContentTv.setText(dataList.get(position).getInspectContent());

            /**
             * 要把dataList的数据附上，否则item里没有数据显示
             */
            if(Integer.valueOf(dataList.get(position).getRecordStatus()) == SinSimApp.TASK_QUALITY_INSPECT_NOT_STARTED){
                holder.radioButtonOK.setChecked(false);
                holder.radioButtonNG.setChecked(false);
                holder.radioButtonNoSuchOne.setChecked(false);
                holder.radioButtonHaveNotChecked.setChecked(false);
            } else if (Integer.valueOf(dataList.get(position).getRecordStatus()) == SinSimApp.TASK_QUALITY_INSPECT_OK){
                holder.radioButtonOK.setChecked(true);
                holder.radioButtonNG.setChecked(false);
                holder.radioButtonNoSuchOne.setChecked(false);
                holder.radioButtonHaveNotChecked.setChecked(false);
            } else if(Integer.valueOf(dataList.get(position).getRecordStatus()) == SinSimApp.TASK_QUALITY_INSPECT_NG){
                holder.radioButtonOK.setChecked(false);
                holder.radioButtonNG.setChecked(true);
                holder.radioButtonNoSuchOne.setChecked(false);
                holder.radioButtonHaveNotChecked.setChecked(false);
            } else if (Integer.valueOf(dataList.get(position).getRecordStatus()) == SinSimApp.TASK_QUALITY_INSPECT_NO_SUCH_ITEM){
                holder.radioButtonOK.setChecked(false);
                holder.radioButtonNG.setChecked(false);
                holder.radioButtonNoSuchOne.setChecked(false);
                holder.radioButtonHaveNotChecked.setChecked(true);
            }
            holder.checkoutCommentEt.setText(dataList.get(position).getRecordRemark());
            holder.checkoutReCheckCommentEt.setText(dataList.get(position).getReInspect());

            holder.itemInspectNameTv.setTag(position);
            holder.itemInspectContentTv.setTag(position);
            holder.radioButtonOK.setTag(position);
            holder.radioButtonNG.setTag(position);
            holder.radioButtonNoSuchOne.setTag(position);
            holder.radioButtonHaveNotChecked.setTag(position );
            holder.checkoutCommentEt.setTag(position);
            holder.checkoutReCheckCommentEt.setTag(position);

            //监听item里的editText。。。
            holder.checkoutCommentEt.addTextChangedListener(new TextWatcherForRemark(holder)  );
            holder.checkoutReCheckCommentEt.addTextChangedListener(new TextWatcherForReCheck(holder)  );
        }else {
            Log.d(TAG, "onBindViewHolder: 没有获取到list数据");
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == dataList.size()) {
            return 1;
        } else {
            return 0;
        }
    }

    public class ItemView extends RecyclerView.ViewHolder {
//        CardView installPlanLayout;
        TextView itemInspectNameTv;
        TextView itemInspectContentTv;
        RadioButton radioButtonOK;
        RadioButton radioButtonNG;
        RadioButton radioButtonNoSuchOne;
        RadioButton radioButtonHaveNotChecked;
        EditText checkoutCommentEt;
        EditText checkoutReCheckCommentEt;

        ItemView(View itemView) {
            super(itemView);
            itemInspectNameTv = itemView.findViewById(R.id.item_inspect_name);
            itemInspectContentTv = itemView.findViewById(R.id.item_inspect_content);
            radioButtonOK = itemView.findViewById(R.id.item_checked_ok_rb);
            radioButtonNG = itemView.findViewById(R.id.item_checked_ng_rb);
            radioButtonNoSuchOne = itemView.findViewById(R.id.item_no_such_one_rb);
            radioButtonHaveNotChecked = itemView.findViewById(R.id.item_have_not_checked_rb);
            checkoutCommentEt = itemView.findViewById(R.id.checkout_comment_et);
            checkoutReCheckCommentEt = itemView.findViewById(R.id.checkout_re_check_comment_et);


            /**
             * 注意：这里为起点入口，为ItemView添加点击事件, 这里没有加，无法触发item的点击事件！
             */
            itemInspectNameTv.setOnClickListener(QualityInspectRecordAdapter.this);
            itemInspectContentTv.setOnClickListener(QualityInspectRecordAdapter.this);
            radioButtonOK.setOnClickListener(QualityInspectRecordAdapter.this);
            radioButtonNG.setOnClickListener(QualityInspectRecordAdapter.this);
            radioButtonNoSuchOne.setOnClickListener(QualityInspectRecordAdapter.this);
            radioButtonHaveNotChecked.setOnClickListener(QualityInspectRecordAdapter.this);
            checkoutCommentEt.setOnClickListener(QualityInspectRecordAdapter.this);
            checkoutReCheckCommentEt.setOnClickListener(QualityInspectRecordAdapter.this);

        }
    }

    /**
     * 监听备注edit输入框
     */
    public interface RemarkEditListener {
        void remarkEditInfo(int position, String inputString);
    }

    /**
     * 监听复检备注editText的输入框
     */
    public interface RecheckCommentEditListener {
        void recheckCommentEditInfo(int position, String inputString);
    }

    //显示数据 --前面已经传好了
    //上一次的质检结果，已经OK的不用显示，NG和未检的要显示
//    public void setQualityInspectRecordList(ArrayList<TaskRecordMachineListData> list) {
//        dataList.clear();
//        dataList.addAll(list);
//    }

    //自定义EditText的监听类--for remark备注
    class TextWatcherForRemark implements TextWatcher {

        private ItemView mHolder;

        public TextWatcherForRemark(ItemView mHolder) {
            this.mHolder = mHolder;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            //用户输入完毕后，处理输入数据，回调给主界面处理
            RemarkEditListener remarkEditListener= (RemarkEditListener) context;

            if(s!=null){
                remarkEditListener.remarkEditInfo(Integer.parseInt(mHolder.checkoutCommentEt.getTag().toString()),s.toString());
            }

        }
    }
    //自定义EditText的监听类--for recheck 复检的备注，
    class TextWatcherForReCheck implements TextWatcher {

        private ItemView mHolder;

        public TextWatcherForReCheck(ItemView mHolder) {
            this.mHolder = mHolder;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            //用户输入完毕后，处理输入数据，回调给主界面处理
            RecheckCommentEditListener recheckCommentEditListener= (RecheckCommentEditListener) context;
            if(s!=null){
                recheckCommentEditListener.recheckCommentEditInfo(Integer.parseInt(mHolder.checkoutReCheckCommentEt.getTag().toString()),s.toString());
            }

        }
    }
    /**
     * 点击事件接口
     */
    public interface OnItemClickListener{
////        void onItemClick(int position);
//        void onFinishItemClick(int position);
//        void onNotFinishItemClick(int position);

        void onItemClick(View v, QualityInspectRecordAdapter.ViewName viewName, int position);
    }

    //=======================以下为item中的button控件点击事件处理===================================
    //item里面有多个控件可以点击（item+item内部控件）
    public enum ViewName {
        ITEM,
        PRACTISE,
        DX_TO_BE_SELECT
    }


    /**
     * 设置点击事件的方法
     */
    public void setOnItemClickListener(OnItemClickListener itemClickListener){
        this.mOnItemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();      //getTag()获取数据
        if (mOnItemClickListener != null) {
            switch (v.getId()){
                default:
                    mOnItemClickListener.onItemClick(v, QualityInspectRecordAdapter.ViewName.ITEM, position);

                    break;
            }
        }

    }

}
