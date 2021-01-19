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
import com.example.nan.ssprocess.bean.basic.QualityInspectData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wxm on 2020/12/24.
 */
// extends RecyclerView.Adapter
public class QualityInspectAdapter  extends RecyclerView.Adapter<QualityInspectAdapter.ItemView> implements View.OnClickListener {

    private static String TAG = "QualityRespectlAdapter";
//    private ArrayList<QualityInspectData> mQualityInspectAdapter;
    private OnItemClickListener mOnItemClickListener;//声明自定义的接口

    private List<QualityInspectData> dataList;//数据源
    private Context context;//上下文
    /// 这里，传数据
    public QualityInspectAdapter(List<QualityInspectData> list, Context context ) {
        this.dataList = list;
        this.context = context;
    }
//    public QualityInspectAdapter(ArrayList<QualityInspectData> list) {
//        mQualityInspectAdapter = list;
//    }

    public void updateDataSoruce(List<QualityInspectData> list)
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
        final QualityInspectAdapter.ItemView itemView = (QualityInspectAdapter.ItemView) holder;
        //itemView.setIsRecyclable(false);//禁止复用
        if (dataList !=null && !dataList.isEmpty() && position < dataList.size()) {
            //显示编号，方便查看
            itemView.itemInspectNameTv.setText((position+1) + ". " + dataList.get(position).getInspectName());
            itemView.itemInspectNameTv.setSelected(true);//用于滚动显示
            itemView.itemInspectContentTv.setText(dataList.get(position).getInspectContent());

            holder.itemInspectNameTv.setTag(position);
            holder.itemInspectContentTv.setTag(position);
            holder.radioButtonOK.setTag(position);
            holder.radioButtonNG.setTag(position);
            holder.radioButtonNoSuchOne.setTag(position);
            holder.radioButtonHaveNotChecked.setTag(position );
            holder.checkoutCommentEt.setTag(position);
            holder.checkoutReCheckCommentEt.setTag(position);

            //监听item里的editText。。。
            holder.checkoutCommentEt.addTextChangedListener(new TextSwitcher(holder)  );
            holder.checkoutReCheckCommentEt.addTextChangedListener(new TextSwitcher(holder)  );
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
            itemInspectNameTv.setOnClickListener(QualityInspectAdapter.this);
            itemInspectContentTv.setOnClickListener(QualityInspectAdapter.this);
            radioButtonOK.setOnClickListener(QualityInspectAdapter.this);
            radioButtonNG.setOnClickListener(QualityInspectAdapter.this);
            radioButtonNoSuchOne.setOnClickListener(QualityInspectAdapter.this);
            radioButtonHaveNotChecked.setOnClickListener(QualityInspectAdapter.this);
            checkoutCommentEt.setOnClickListener(QualityInspectAdapter.this);
            checkoutReCheckCommentEt.setOnClickListener(QualityInspectAdapter.this);

        }
    }

    /**
     * 监听备注edit输入框
     */
    public interface CommentEditListener {
        void commentEditInfo(int position, String inputString);
    }

    /**
     * 监听复检备注edit的输入框
     */
    public interface RecheckCommentEditListener {
        void recheckCommentEditInfo(int position, String inputString);
    }

    public void setProcessList(ArrayList<QualityInspectData> list) {
        dataList.clear();
        dataList.addAll(list);
    }

    //自定义EditText的监听类
    class TextSwitcher implements TextWatcher {

        private ItemView mHolder;

        public TextSwitcher(ItemView mHolder) {
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
            CommentEditListener commentEditListener= (CommentEditListener) context;
            RecheckCommentEditListener recheckCommentEditListener= (RecheckCommentEditListener) context;

            if(s!=null){
                commentEditListener.commentEditInfo(Integer.parseInt(mHolder.checkoutCommentEt.getTag().toString()),s.toString());
                recheckCommentEditListener.recheckCommentEditInfo(Integer.parseInt(mHolder.checkoutReCheckCommentEt.getTag().toString()),s.toString());
            }

        }
    }

    /**
     * 点击事件接口
     */
    public interface OnItemClickListener{
//        void onItemClick(int position);
        void onFinishItemClick(int position);
        void onNotFinishItemClick(int position);

        void onItemClick(View v, QualityInspectAdapter.ViewName viewName, int position);
        void onItemLongClick(View v);
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
                // todo
//                case R.id.rv_dx:
//                    mOnItemClickListener.onItemClick(v, DianXianQingceAdapter.ViewName.PRACTISE, position);
//                    break;
//                case R.id.rv_dx_tobeSelect:
//                    mOnItemClickListener.onItemClick(v, DianXianQingceAdapter.ViewName.DX_TO_BE_SELECT, position);
//                    break;
                default:
                    mOnItemClickListener.onItemClick(v, QualityInspectAdapter.ViewName.ITEM, position);

                    break;
            }
        }

    }
}
