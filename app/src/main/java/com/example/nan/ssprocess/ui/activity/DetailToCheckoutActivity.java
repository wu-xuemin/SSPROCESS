package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.adapter.QualityInspectRecordAdapter;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.QualityRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGASortableNinePhotoLayout;

/**
 * @author nan  2017/12/18
 */

public class DetailToCheckoutActivity extends AppCompatActivity implements BGASortableNinePhotoLayout.Delegate,
        QualityInspectRecordAdapter.RemarkEditListener,
        QualityInspectRecordAdapter.RecheckCommentEditListener{
    private static final String TAG="nlgDetailToCheckout";
    private TextView locationTv;
    private TextView currentStatusTv;

    private ProgressDialog mUploadingProcessDialog;
    private AlertDialog mQaDialog = null;
    private ProgressDialog mUpdatingProcessDialog;
    private AlertDialog mLocationSettingDialog = null;
    private AlertDialog mUpdateQualityInspectRecordDialog = null;


    private TaskRecordMachineListData mTaskRecordMachineListData;
    private int iTaskRecordMachineListDataStatusTemp;
    private UpdateProcessDetailDataHandler mUpdateProcessDetailDataHandler = new UpdateProcessDetailDataHandler();

    private final String IP = SinSimApp.getApp().getServerIP();
    private static final int SCAN_QRCODE_START = 1;
    private static final int SCAN_QRCODE_END = 0;
    private static final int RC_CHECKOUT_CHOOSE_PHOTO = 3;
    private static final int RC_CHECKOUT_PHOTO_PREVIEW = 4;

    private QualityInspectRecordAdapter mQualityInspectRecordAdapter;
    private RecyclerView mQualityInspectRV;
    //   先获取 taskRecordMachineListDataArrayList， 然后从中获取 mQualityInspectList
    /**
     * TaskRecordMachineListData 包含了 质检
     */
    private ArrayList<TaskRecordMachineListData> taskRecordMachineListDataArrayList = new ArrayList<>();
    /**
     * 被用户填写了，待上传的质检
     */
    private ArrayList<TaskRecordMachineListData> mQualityInspectRecordTobeUploadList = new ArrayList<>();

    /**
     * 各条质检项的质检结果
     */
    private Button buttonUploadQualityInspectRecord;

    final String account = SinSimApp.getApp().getAccount();

//    @SuppressLint("SimpleDateFormat")
//    SimpleDateFormat formatter = new SimpleDateFormat("yy/MM/dd");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_checkout);

        //返回前页按钮
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        locationTv=findViewById(R.id.location_tv);
        TextView orderNumberTv=findViewById(R.id.order_number_tv);
        TextView machineNumberTv=findViewById(R.id.machine_number_tv);
        currentStatusTv=findViewById(R.id.current_status_tv);
        TextView installListTv=findViewById(R.id.intall_list_tv);

        //点击下载装车单
        installListTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //下载装车单
                fetchDownloadListData();
            }
        });

        buttonUploadQualityInspectRecord = findViewById(R.id.button_upload_quality_inspect_record);
        buttonUploadQualityInspectRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 要求有弹框确认是否提交
                 */
                mUpdateQualityInspectRecordDialog = new AlertDialog.Builder(DetailToCheckoutActivity.this).create();
                mUpdateQualityInspectRecordDialog.setMessage("是否提交质检？");
                mUpdateQualityInspectRecordDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                mUpdateQualityInspectRecordDialog.setButton(AlertDialog.BUTTON_POSITIVE, "是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateQualityInspectData();
                    }
                });
                mUpdateQualityInspectRecordDialog.show();
            }
        });

        //获取传递过来的信息
        /**
         * mTaskRecordMachineListData是由前个页面ProcessToCheckoutActivity传过来，
         * 里面的质检条目（可能数量很多）则在质检页面自己去获取。
         */
        Intent intent = getIntent();
        mTaskRecordMachineListData = (TaskRecordMachineListData) intent.getSerializableExtra("mTaskRecordMachineListData");
        Log.d(TAG, "onCreate: position :"+mTaskRecordMachineListData.getMachineData().getLocation());

        //把数据填入相应位置
        orderNumberTv.setText(""+mTaskRecordMachineListData.getMachineOrderData().getOrderNum());
        currentStatusTv.setText(SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus()));
        machineNumberTv.setText(mTaskRecordMachineListData.getMachineData().getNameplate());

        //locationTv.setText(mTaskRecordMachineListData.getMachineData().getLocation());
        if (mTaskRecordMachineListData.getMachineData().getLocation().isEmpty()){
            locationTv.setText("点击上传");
        }else {
            locationTv.setText(mTaskRecordMachineListData.getMachineData().getLocation());
        }
        locationTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layout = (LinearLayout) View.inflate(DetailToCheckoutActivity.this, R.layout.dialog_location_seting, null);
                final EditText dialogLocationEt = layout.findViewById(R.id.dialog_location_et);
                mLocationSettingDialog = new AlertDialog.Builder(DetailToCheckoutActivity.this).create();
                mLocationSettingDialog.setTitle("输入机器的位置：");
                mLocationSettingDialog.setView(layout);
                mLocationSettingDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                mLocationSettingDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //获取dialog的输入信息，并上传到服务器
                        if (TextUtils.isEmpty(dialogLocationEt.getText())) {
                            Toast.makeText(DetailToCheckoutActivity.this,"地址不能为空，请确认后重新输入！",Toast.LENGTH_SHORT).show();
                        }else {
                            locationTv.setText(dialogLocationEt.getText().toString());
                            if( mUpdatingProcessDialog == null) {
                                mUpdatingProcessDialog = new ProgressDialog(DetailToCheckoutActivity.this);
                                mUpdatingProcessDialog.setCancelable(false);
                                mUpdatingProcessDialog.setCanceledOnTouchOutside(false);
                                mUpdatingProcessDialog.setMessage("上传信息中...");
                            }
                            mUpdatingProcessDialog.show();
                            updateLocationData();
                        }
                    }
                });
                mLocationSettingDialog.show();
            }
        });


        if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_INSTALLED) {
//            beginQaButton.setVisibility(View.VISIBLE);
//            endQaButton.setVisibility(View.GONE);
//            checkedOkRb.setEnabled(false);
//            checkedNokRb.setEnabled(false);
        }else if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_QUALITY_DOING) {
//            beginQaButton.setVisibility(View.GONE);
//            endQaButton.setVisibility(View.VISIBLE);
//            checkedOkRb.setEnabled(true);
//            checkedNokRb.setEnabled(true);
        }else if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_QUALITY_ABNORMAL){
//            beginQaButton.setVisibility(View.GONE);
//            endQaButton.setVisibility(View.GONE);
//            checkedOkRb.setEnabled(false);
//            checkedNokRb.setEnabled(true);
        }else {
//            beginQaButton.setVisibility(View.GONE);
//            endQaButton.setVisibility(View.GONE);
//            checkedOkRb.setEnabled(true);
//            checkedNokRb.setEnabled(false);
        }

        //获取历史质检数据 --
//        fetchQARecordData();
//        fetchQARecordData3Qi();

        //九宫格拍照
//        mCheckoutNokPhotosSnpl = findViewById(R.id.checkout_nok_add_photos);
//        mCheckoutNokPhotosSnpl.setMaxItemCount(3);
//        mCheckoutNokPhotosSnpl.setPlusEnable(true);
//        mCheckoutNokPhotosSnpl.setDelegate(this);

        mQualityInspectRV = (RecyclerView) findViewById(R.id.checkout_list_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mQualityInspectRV.setLayoutManager(manager);

        mQualityInspectRecordAdapter = new QualityInspectRecordAdapter(taskRecordMachineListDataArrayList, DetailToCheckoutActivity.this);//, RelateNewDxActivity.this, Constant.REQUEST_CODE_LUJING_CANDIDATE_WIRES);
        mQualityInspectRV.addItemDecoration(new DividerItemDecoration(DetailToCheckoutActivity.this, DividerItemDecoration.VERTICAL));
        mQualityInspectRV.setAdapter(mQualityInspectRecordAdapter);
        mQualityInspectRecordAdapter.setOnItemClickListener(MyItemClickListener);
    }

    public void onStartQa(View view) {
        if (mTaskRecordMachineListData.getStatus()!=SinSimApp.TASK_INSTALLED){
            Toast.makeText(DetailToCheckoutActivity.this, "正在 "+SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus())+" ，不能开始质检！", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(DetailToCheckoutActivity.this, ScanQrcodeActivity.class);
            startActivityForResult(intent, SCAN_QRCODE_START);
        }
    }

    public void onStopQa(View view) {
        if (mTaskRecordMachineListData.getStatus()!=SinSimApp.TASK_QUALITY_DOING){
            Toast.makeText(DetailToCheckoutActivity.this, "正在 "+SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus())+" ，不能结束质检！", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(DetailToCheckoutActivity.this, ScanQrcodeActivity.class);
            startActivityForResult(intent, SCAN_QRCODE_END);
        }
    }
    private void updateLocationData() {
        //更新loaction状态
        mTaskRecordMachineListData.getMachineData().setLocation(locationTv.getText().toString());
        Gson gson=new Gson();
        String machineDataToJson = gson.toJson(mTaskRecordMachineListData.getMachineData());
        Log.d(TAG, "onItemClick: gson :"+ machineDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("machine", machineDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + IP + URL.UPDATE_MACHINE_LOCATION;
        Log.d(TAG, "updateProcessDetailData: "+updateProcessRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, new UpdateLocationDataHandler());
    }

    @SuppressLint("HandlerLeak")
    private class UpdateLocationDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mUpdatingProcessDialog != null && mUpdatingProcessDialog.isShowing()) {
                mUpdatingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                Toast.makeText(DetailToCheckoutActivity.this, "上传位置成功！", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToCheckoutActivity.this, "上传失败："+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class UpdateQualityInspectRecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mUpdatingProcessDialog != null && mUpdatingProcessDialog.isShowing()) {
                mUpdatingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                Toast.makeText(DetailToCheckoutActivity.this, "上传质检信息成功！", Toast.LENGTH_SHORT).show();
                DetailToCheckoutActivity.this.finish();
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToCheckoutActivity.this, "上传失败："+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void fetchQARecordData() {
        Log.i(TAG, "获取历史质检数据");
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecordId", ""+mTaskRecordMachineListData.getId());
        String fetchProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FATCH_TASK_QUALITY_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessQARecordData(fetchProcessRecordUrl, mPostValue, new FetchQaRecordDataHandler());
    }

//    private void fetchQARecordData3Qi() {
//        Log.i(TAG, "获取历史质检数据"); ????不需要，因为第一次时都获取了。
//        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
//        mPostValue.put("taskRecordId", ""+mTaskRecordMachineListData.getId());
//        String fetchProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FATCH_TASK_QUALITY_RECORD_DETAIL;
//        Network.Instance(SinSimApp.getApp()).fetchProcessQARecordData(fetchProcessRecordUrl, mPostValue, new FetchQaRecordDataHandler());
//    }

    //更新新质检数据
    private void updateQualityInspectData() {

        Gson gson=new Gson();
        String mQualityInspectListDataToJson = gson.toJson(mQualityInspectRecordTobeUploadList);
        Log.d(TAG, "onItemClick: gson :"+ mQualityInspectListDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("mQualityInspectRecordTobeUploadList", mQualityInspectListDataToJson);
        String updateQualityInspectRecordUrl = URL.HTTP_HEAD + IP + URL.UPDATE_QUALITY_INSPECT_RECORD_LIST;
        Log.d(TAG, "updateQualityInspectData: "+updateQualityInspectRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateQualityInspectRecordUrl, mPostValue, new UpdateQualityInspectRecordDataHandler());
    }

    /**
     * 质检各个item 里的控件点击监听事件
     */
    private QualityInspectRecordAdapter.OnItemClickListener MyItemClickListener = new QualityInspectRecordAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(View v, QualityInspectRecordAdapter.ViewName viewName, int position) {
            //viewName可区分item及item内部控件
            mQualityInspectRecordTobeUploadList.get(position).setInspectPerson(account);
//            mQualityInspectRecordTobeUploadList.get(position).setUpdateTime(formatter.format(new Date()));
            Date now = new Date();
            mQualityInspectRecordTobeUploadList.get(position).setUpdateTime(String.valueOf((now.getTime())));
            switch (v.getId()){

                case R.id.item_checked_ok_rb:
                    /**
                     * 因为要和APP其他状态共用一些代码，所以这里不要用字符串，用数字
                     */
                    mQualityInspectRecordTobeUploadList.get(position).setRecordStatus(String.valueOf(SinSimApp.TASK_QUALITY_INSPECT_OK));
                    break;
                case R.id.item_checked_ng_rb:
                    mQualityInspectRecordTobeUploadList.get(position).setRecordStatus((String.valueOf(SinSimApp.TASK_QUALITY_INSPECT_NG)));
                    break;
                case R.id.item_no_such_one_rb:
                    mQualityInspectRecordTobeUploadList.get(position).setRecordStatus(String.valueOf(SinSimApp.TASK_QUALITY_INSPECT_NO_SUCH_ITEM));
                    break;
                case R.id.item_have_not_checked_rb:
                    mQualityInspectRecordTobeUploadList.get(position).setRecordStatus(String.valueOf(SinSimApp.TASK_QUALITY_INSPECT_HAVE_NOT_CHECKED));
                    break;

                case R.id.checkout_comment_et:
                    /**
                     * 在这里做的话，只有点击了输入框才能赋值，应该改成自动赋值。
                     */
//                    EditText editTextComment = (EditText) v;
//                    if(editTextComment != null) {
//                        Toast.makeText(DetailToCheckoutActivity.this, " 备注 " + (position + 1) + ", " + editTextComment.getText(), Toast.LENGTH_SHORT).show();
//                        mQualityInspectRecordTobeUploadList.get(position).setRecordRemark(editTextComment.getText().toString());
//                    } else {
//                        Log.w(TAG, "找不到checkout_comment_et");
//                    }
                    break;
                case R.id.checkout_re_check_comment_et:
                    break;
                default:
                    break;
            }
        }

    };

    @SuppressLint("HandlerLeak")
    private class FetchQaRecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取质检结果
                if (mTaskRecordMachineListData.getStatus()== SinSimApp.TASK_QUALITY_ABNORMAL) {
                    ArrayList<QualityRecordDetailsData> mQualityRecordList = (ArrayList<QualityRecordDetailsData>) msg.obj;
                    if (mQualityRecordList.size() > 0) {
                        int updateTime = mQualityRecordList.size() - 1;
                        //对比更新时间取值
                        for (int update = mQualityRecordList.size() - 2; update >= 0; update--) {
                            if (mQualityRecordList.get(updateTime).getId() < mQualityRecordList.get(update).getId()) {
                                updateTime = update;
                            }
                            Log.d(TAG, "handleMessage: updateTime1:" + updateTime);
                        }
                        QualityRecordDetailsData mQualityRecordDetailsData = mQualityRecordList.get(updateTime);
                        Log.d(TAG, "handleMessage: get Json = " + new Gson().toJson(mQualityRecordDetailsData));
                        //加载历史照片
//                        checkedNokRb.setChecked(true);
//                        checkedNokRb.setEnabled(true);
//                        checkedOkRb.setEnabled(false);
//                        checkoutNokDetailEt.setText(mQualityRecordDetailsData.getComment());
//                        checkoutNokDetailEt.setFocusable(false);
//                        checkoutNokDetailEt.setFocusableInTouchMode(false);
                        //加载历史照片地址
                        String picsName=mQualityRecordDetailsData.getQualityRecordImage().getImage();
                        picsName=picsName.substring(1,picsName.indexOf("]"));
                        Log.d(TAG, "照片地址："+picsName);
                        if (picsName.isEmpty()) {
                            Log.d(TAG, "安装异常照片: 无拍照地址");
                        } else {
                            String[] picName = picsName.split(",");
                            String picUrl;
                            ArrayList<String> checkoutPhotoList = new ArrayList<>();
                            if (picName.length == 1) {
                                picUrl = URL.HTTP_HEAD + IP.substring(0, IP.indexOf(":")) + URL.QA_PIC_DIR + picsName.substring(picsName.lastIndexOf("/"));
                                checkoutPhotoList.add(picUrl);
                            } else {
                                for (String aPicName : picName) {
                                    picUrl = URL.HTTP_HEAD + IP.substring(0, IP.indexOf(":")) + URL.QA_PIC_DIR + aPicName.substring(aPicName.lastIndexOf("/"));
                                    Log.d(TAG, "handleMessage: 异常照片地址：" + picUrl);
                                    checkoutPhotoList.add(picUrl);
                                }
                            }
//                            mCheckoutNokPhotosSnpl.addMoreData(checkoutPhotoList);
//                            mCheckoutNokPhotosSnpl.setEditable(false);
                        }
                    } else {
                        Log.d(TAG, "handleMessage: 尚未质检");
                    }
                }else {
//                    checkedOkRb.setChecked(true);
//                    checkoutNokDetailEt.setText("");
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(DetailToCheckoutActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateQARecordData() {
//        Gson gson=new Gson();
//        //获取当前时间
//        @SuppressLint("SimpleDateFormat")
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//        Date curDate = new Date(System.currentTimeMillis());
//        String strCurTime = formatter.format(curDate);
//        long lCurTime=System.currentTimeMillis();
//
//        //读取和更新输入信息
//        if(checkedOkRb.isChecked()){
//            iTaskRecordMachineListDataStatusTemp=mTaskRecordMachineListData.getStatus();
//            mTaskRecordMachineListData.setQualityEndTime(strCurTime);
//            updateProcessDetailData(SinSimApp.TASK_QUALITY_DONE);
//        } else {
//            QualityRecordDetailsData qualityRecordDetailsData=new QualityRecordDetailsData(SinSimApp.getApp().getFullName(),mTaskRecordMachineListData.getId(),lCurTime);
//            String qualityRecordDetailsDataToJson = gson.toJson(qualityRecordDetailsData);
//            Log.d(TAG, "updateInstallRecordData: gson :"+ qualityRecordDetailsDataToJson);
//            if(checkoutNokDetailEt.getText()!=null && mCheckoutNokPhotosSnpl.getData().size() > 0){
//                //获取质检不合格原因
//                qualityRecordDetailsData.setComment(checkoutNokDetailEt.getText().toString());
//                //上传质检结果
//                String mQualityRecordDetailsDataToJson = gson.toJson(qualityRecordDetailsData);
//                Log.d(TAG, "updateQARecordData: taskQualityRecord:"+ mQualityRecordDetailsDataToJson);
//                LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
//                mPostValue.put("taskQualityRecord", mQualityRecordDetailsDataToJson);
//
//                iTaskRecordMachineListDataStatusTemp=mTaskRecordMachineListData.getStatus();
//                mTaskRecordMachineListData.setStatus(SinSimApp.TASK_QUALITY_ABNORMAL);
//                String mTaskRecordDataToJson = gson.toJson(mTaskRecordMachineListData);
//                Log.d(TAG, "updateQARecordData: taskRecord:"+mTaskRecordDataToJson);
//                mPostValue.put("taskRecord", mTaskRecordDataToJson);
//
//                //获取图片本地url
//                ArrayList<String> imageUrlList = mCheckoutNokPhotosSnpl.getData();
//                Log.d(TAG, "异常图片: "+imageUrlList.size());
//
//                String uploadQaAbnormalDetailUrl = URL.HTTP_HEAD + IP + URL.UPLOAD_INSTALL_QA_DETAIL;
//                Network.Instance(SinSimApp.getApp()).uploadTaskRecordImage(uploadQaAbnormalDetailUrl, imageUrlList, mPostValue, new UploadTaskRecordImageHandler());
//            } else {
//                Toast.makeText(DetailToCheckoutActivity.this,"请拍照并输入质检不合格原因！",Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }
//        if( mUploadingProcessDialog == null) {
//            mUploadingProcessDialog = new ProgressDialog(DetailToCheckoutActivity.this);
//            mUploadingProcessDialog.setCancelable(false);
//            mUploadingProcessDialog.setCanceledOnTouchOutside(false);
//            mUploadingProcessDialog.setMessage("上传信息中...");
//        }
//        mUploadingProcessDialog.show();
    }

    @SuppressLint("HandlerLeak")
    private class UploadTaskRecordImageHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mUploadingProcessDialog != null && mUploadingProcessDialog.isShowing()) {
                mUploadingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                Toast.makeText(DetailToCheckoutActivity.this, "上传质检不合格信息成功！", Toast.LENGTH_SHORT).show();
                DetailToCheckoutActivity.this.finish();
                Log.d(TAG, "handleMessage: 上传质检不合格信息成功！");
            } else {
                mTaskRecordMachineListData.setStatus(iTaskRecordMachineListDataStatusTemp);
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToCheckoutActivity.this, "上传图片失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 获取装车单的文件名
     */
    private void fetchDownloadListData() {
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("order_id", ""+mTaskRecordMachineListData.getMachineData().getOrderId());
        String fetchInstallFileListUrl = URL.HTTP_HEAD + IP + URL.FETCH_DOWNLOADING_FILELIST;
        Network.Instance(SinSimApp.getApp()).fetchInstallFileList(fetchInstallFileListUrl, mPostValue, new FetchInstallFileListHandler());
    }

    @SuppressLint("HandlerLeak")
    private class FetchInstallFileListHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                ArrayList<String> mInstallFileList = (ArrayList<String>) msg.obj;
                Intent intent=new Intent(DetailToCheckoutActivity.this,InstallListActivity.class);
                intent.putExtra("mInstallFileList", mInstallFileList);
                startActivity(intent);
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "FetchInstallFileListHandler handleMessage: "+errorMsg);
                Toast.makeText(DetailToCheckoutActivity.this, "网络错误！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClickAddNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, ArrayList<String> models) {
        choicePhotoWrapper();
    }

    @Override
    public void onClickDeleteNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
//        mCheckoutNokPhotosSnpl.removeItem(position);
    }

    @Override
    public void onClickNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        Intent photoPickerPreviewIntent = new BGAPhotoPickerPreviewActivity.IntentBuilder(DetailToCheckoutActivity.this)
                .previewPhotos(models) // 当前预览的图片路径集合
                .selectedPhotos(models) // 当前已选中的图片路径集合
//                .maxChooseCount(mCheckoutNokPhotosSnpl.getMaxItemCount()) // 图片选择张数的最大值
                .currentPosition(position) // 当前预览图片的索引
                .isFromTakePhoto(false) // 是否是拍完照后跳转过来
                .build();
        startActivityForResult(photoPickerPreviewIntent, RC_CHECKOUT_PHOTO_PREVIEW);
    }

    @Override
    public void onNinePhotoItemExchanged(BGASortableNinePhotoLayout sortableNinePhotoLayout, int fromPosition, int toPosition, ArrayList<String> models) {

    }

    private void choicePhotoWrapper() {
        // 拍照后照片的存放目录，改成你自己拍照后要存放照片的目录。如果不传递该参数的话就没有拍照功能
        File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");
        Intent photoPickerIntent = new BGAPhotoPickerActivity.IntentBuilder(DetailToCheckoutActivity.this)
                .cameraFileDir(takePhotoDir) // 拍照后照片的存放目录，改成你自己拍照后要存放照片的目录。
//                .maxChooseCount(mCheckoutNokPhotosSnpl.getMaxItemCount() - mCheckoutNokPhotosSnpl.getItemCount()) // 图片选择张数的最大值
                .selectedPhotos(null) // 当前已选中的图片路径集合
                .pauseOnScroll(false) // 滚动列表时是否暂停加载图片
                .build();
        startActivityForResult(photoPickerIntent, RC_CHECKOUT_CHOOSE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SCAN_QRCODE_START:
                if(resultCode == RESULT_OK) {
                    //检验二维码信息是否对应
                    String mMachineNamePlate = data.getStringExtra("mMachineNamePlate");
                    if(mMachineNamePlate.equals(mTaskRecordMachineListData.getMachineData().getNameplate())){
                        //update status
                        if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_INSTALLED) {
                            mQaDialog = new AlertDialog.Builder(DetailToCheckoutActivity.this).create();
                            mQaDialog.setMessage("是否现在开始质检？");
                            mQaDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            mQaDialog.setButton(AlertDialog.BUTTON_POSITIVE, "是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //改状态
                                    if( mUpdatingProcessDialog == null) {
                                        mUpdatingProcessDialog = new ProgressDialog(DetailToCheckoutActivity.this);
                                        mUpdatingProcessDialog.setCancelable(false);
                                        mUpdatingProcessDialog.setCanceledOnTouchOutside(false);
                                        mUpdatingProcessDialog.setMessage("正在开始...");
                                    }
                                    mUpdatingProcessDialog.show();
                                    //获取当前时间
                                    @SuppressLint("SimpleDateFormat")
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                                    Date curDate = new Date(System.currentTimeMillis());
                                    String staCurTime = formatter.format(curDate);
                                    mTaskRecordMachineListData.setQualityBeginTime(staCurTime);
                                    iTaskRecordMachineListDataStatusTemp=mTaskRecordMachineListData.getStatus();
                                    updateProcessDetailData(SinSimApp.TASK_QUALITY_DOING);
                                }
                            });
                            mQaDialog.show();
                        } else {
                            Toast.makeText(this, "失败，当前状态无法开始！", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d(TAG, "onActivityResult: 二维码信息不对应");
                        Toast.makeText(this, "二维码信息不对应！", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "onActivityResult: scan QRcode fail");
                }
                break;
            case SCAN_QRCODE_END:
                if(resultCode == RESULT_OK) {
                    //检验二维码信息是否对应
                    String mMachineNamePlate = data.getStringExtra("mMachineNamePlate");
                    if(mMachineNamePlate.equals(mTaskRecordMachineListData.getMachineData().getNameplate())){
                        Log.d(TAG, "onActivityResult: id 对应");
                        updateQARecordData();
                    } else {
                        Log.d(TAG, "onActivityResult: 二维码信息不对应");
                        Toast.makeText(this, "二维码信息不对应！", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "onActivityResult: scan QRcode fail");
                }
                break;
            case RC_CHECKOUT_CHOOSE_PHOTO:
                if(resultCode == RESULT_OK) {
//                    mCheckoutNokPhotosSnpl.addMoreData(BGAPhotoPickerActivity.getSelectedPhotos(data));
                } else {
                    Log.d(TAG, "onActivityResult: choose  nothing");
                }
                break;
            case RC_CHECKOUT_PHOTO_PREVIEW:
//                mCheckoutNokPhotosSnpl.setData(BGAPhotoPickerPreviewActivity.getSelectedPhotos(data));
            default:
                break;
        }
    }
    private void updateProcessDetailData(int status) {
        //更新loaction状态
        mTaskRecordMachineListData.setStatus(status);
        Gson gson=new Gson();
        String taskRecordDataToJson = gson.toJson(mTaskRecordMachineListData);
        Log.d(TAG, "onItemClick: gson :"+ taskRecordDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecord", taskRecordDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.UPDATE_TASK_RECORD_STATUS;
        Log.d(TAG, "updateProcessDetailData: "+updateProcessRecordUrl+mPostValue.get("taskRecord"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, mUpdateProcessDetailDataHandler);
    }

    @SuppressLint("HandlerLeak")
    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mUpdatingProcessDialog != null && mUpdatingProcessDialog.isShowing()) {
                mUpdatingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                currentStatusTv.setText(SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus()));
                if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_INSTALLED) {
//                    beginQaButton.setVisibility(View.VISIBLE);
//                    endQaButton.setVisibility(View.GONE);
                }else if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_QUALITY_DOING) {
//                    beginQaButton.setVisibility(View.GONE);
//                    endQaButton.setVisibility(View.VISIBLE);
//                    checkedNokRb.setEnabled(true);
//                    checkedOkRb.setEnabled(true);
                }else if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_QUALITY_DONE) {
//                    beginQaButton.setVisibility(View.GONE);
//                    endQaButton.setVisibility(View.VISIBLE);
                    Toast.makeText(DetailToCheckoutActivity.this, "质检信息上传成功！", Toast.LENGTH_SHORT).show();
                    DetailToCheckoutActivity.this.finish();
                }else {
//                    beginQaButton.setVisibility(View.GONE);
//                    endQaButton.setVisibility(View.GONE);
                }
            } else {
                mTaskRecordMachineListData.setStatus(iTaskRecordMachineListDataStatusTemp);
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg+mTaskRecordMachineListData.getStatus());
                Toast.makeText(DetailToCheckoutActivity.this, "失败，网络错误，请检查网络！", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchQualityInspectData(0);
    }
    private void fetchQualityInspectData(int page) {
        final String ip = SinSimApp.getApp().getServerIP();
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();


        mPostValue.put("nameplate", String.valueOf(mTaskRecordMachineListData.getMachineData().getNameplate()));
        /**
         * 已经质检OK、不需要再看了的，就不再显示； 未检或者NG的才显示
         */
        mPostValue.put("recordStatus", String.valueOf(SinSimApp.TASK_QUALITY_INSPECT_NOT_STARTED)
                +"," + String.valueOf(SinSimApp.TASK_QUALITY_INSPECT_NG) );
        mPostValue.put("page", ""+page);

        String fetchQualityInspectUrl = URL.HTTP_HEAD + ip + URL.FETCH_TASK_RECORD_TO_QA;
        //获取质检数据
        Network.Instance(SinSimApp.getApp()).fetchProcessTaskRecordData(fetchQualityInspectUrl, mPostValue, new FetchQualityInspectDataHandler());
    }

    @SuppressLint("HandlerLeak")
    private class FetchQualityInspectDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                taskRecordMachineListDataArrayList =(ArrayList<TaskRecordMachineListData>)msg.obj;
                Log.d(TAG, "handleMessage: size: "+ taskRecordMachineListDataArrayList.size());
//
                if (taskRecordMachineListDataArrayList.size() > 0) {
                    ArrayList<TaskRecordMachineListData> abnormalList = new ArrayList<>();
                    ArrayList<TaskRecordMachineListData> orderChangeList = new ArrayList<>();
                    ArrayList<TaskRecordMachineListData> skipList = new ArrayList<>();
                    ArrayList<TaskRecordMachineListData> normalList = new ArrayList<>();
                    for (int position = 0; position < taskRecordMachineListDataArrayList.size(); position++) {
                        if (taskRecordMachineListDataArrayList.get(position).getMachineData().getStatus() == SinSimApp.MACHINE_CANCELED) {
                            taskRecordMachineListDataArrayList.remove(position);
                        }else if (taskRecordMachineListDataArrayList.get(position).getMachineData().getStatus()==SinSimApp.MACHINE_CHANGED
                                || taskRecordMachineListDataArrayList.get(position).getMachineData().getStatus()==SinSimApp.MACHINE_SPLITED) {
                            orderChangeList.add(taskRecordMachineListDataArrayList.get(position));
                        } else {
                            switch (taskRecordMachineListDataArrayList.get(position).getStatus()) {
                                case SinSimApp.TASK_INITIAL:
                                case SinSimApp.TASK_PLANED:
                                case SinSimApp.TASK_INSTALL_WAITING:
                                case SinSimApp.TASK_INSTALLING:
                                case SinSimApp.TASK_INSTALLED:
                                case SinSimApp.TASK_QUALITY_DOING:
                                case SinSimApp.TASK_QUALITY_DONE:

                                    //3期
                                case SinSimApp.TASK_QUALITY_INSPECT_NOT_STARTED:
                                case SinSimApp.TASK_QUALITY_INSPECT_NO_SUCH_ITEM:
                                case SinSimApp.TASK_QUALITY_INSPECT_NG:
                                case SinSimApp.TASK_QUALITY_INSPECT_OK:
                                case SinSimApp.TASK_QUALITY_INSPECT_HAVE_NOT_CHECKED:
                                    normalList.add(taskRecordMachineListDataArrayList.get(position));
                                    break;
                                case SinSimApp.TASK_INSTALL_ABNORMAL:
                                case SinSimApp.TASK_QUALITY_ABNORMAL:
                                    abnormalList.add(taskRecordMachineListDataArrayList.get(position));
                                    break;
                                case SinSimApp.TASK_SKIP:
                                    skipList.add(taskRecordMachineListDataArrayList.get(position));
                                    break;
                                default:
                                    break;

                            }
                        }
                    }
                    Log.d(TAG, "handleMessage: 机器状态排序!");
                    //排序：异常-》改单拆单-》跳过-》正常
                    abnormalList.addAll(orderChangeList);
                    abnormalList.addAll(skipList);
                    abnormalList.addAll(normalList);
                    taskRecordMachineListDataArrayList =abnormalList;
                }

                for(int t=0; t<taskRecordMachineListDataArrayList.size(); t++){
                    mQualityInspectRecordTobeUploadList.add(taskRecordMachineListDataArrayList.get(t));
                }
                if (mQualityInspectRecordAdapter == null)
                {
                    mQualityInspectRecordAdapter = new QualityInspectRecordAdapter(taskRecordMachineListDataArrayList, DetailToCheckoutActivity.this);//, RelateNewDxActivity.this, Constant.REQUEST_CODE_LUJING_CANDIDATE_WIRES);
                    mQualityInspectRV.addItemDecoration(new DividerItemDecoration(DetailToCheckoutActivity.this, DividerItemDecoration.VERTICAL));
                    mQualityInspectRV.setAdapter(mQualityInspectRecordAdapter);
                    mQualityInspectRecordAdapter.setOnItemClickListener(MyItemClickListener);
                }

                mQualityInspectRecordAdapter.updateDataSoruce(taskRecordMachineListDataArrayList);
                mQualityInspectRecordAdapter.notifyDataSetChanged();
                if (taskRecordMachineListDataArrayList.size()==0){
                    Toast.makeText(DetailToCheckoutActivity.this, "没有更多了...", Toast.LENGTH_SHORT).show();
                }else {
                    Log.i(TAG, "0's RecordStatus:" + taskRecordMachineListDataArrayList.get(0).getRecordStatus());
                    Log.i(TAG, "0's RecordRemark:" + taskRecordMachineListDataArrayList.get(0).getRecordRemark());
                    Toast.makeText(DetailToCheckoutActivity.this, "列表已更新！", Toast.LENGTH_SHORT).show();
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(DetailToCheckoutActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void remarkEditInfo(int position, String inputString) {
        //回调处理edittext内容，使用map的好处在于：position确定的情况下，string改变，只会动态改变string内容
        Log.i(TAG, "备注， position: " + position + ", string: " + inputString);
        /**
         * 把监控获取的输入框信息更新给mQualityInspectRecordTobeUploadList，用于上传。
         */
        mQualityInspectRecordTobeUploadList.get(position ).setRecordRemark(inputString);
        mQualityInspectRecordTobeUploadList.get(position).setInspectPerson(account);
//        mQualityInspectRecordTobeUploadList.get(position).setUpdateTime(formatter.format(new Date()));
        Date now = new Date();
        mQualityInspectRecordTobeUploadList.get(position).setUpdateTime(String.valueOf((now.getTime())));
    }

    @Override
    public void recheckCommentEditInfo(int position, String inputString) {
        //回调处理edittext内容，使用map的好处在于：position确定的情况下，string改变，只会动态改变string内容
        Log.i(TAG, "复检备注，position: " + position + ", string: " + inputString);
        /**
         * 把监控获取的输入框信息更新给mQualityInspectRecordTobeUploadList，用于上传。
         */
        mQualityInspectRecordTobeUploadList.get(position ).setReInspect(inputString);
        mQualityInspectRecordTobeUploadList.get(position).setInspectPerson(account);
        mQualityInspectRecordTobeUploadList.get(position).setInspectPerson(account);
        Date now = new Date();
        mQualityInspectRecordTobeUploadList.get(position).setUpdateTime(String.valueOf((now.getTime())));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mUploadingProcessDialog != null) {
            mUploadingProcessDialog.dismiss();
        }
        if(mQaDialog != null) {
            mQaDialog.dismiss();
        }
        if(mUpdatingProcessDialog != null) {
            mUpdatingProcessDialog.dismiss();
        }
        if(mLocationSettingDialog != null) {
            mLocationSettingDialog.dismiss();
        }
    }
}
