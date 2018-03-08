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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.AbnormalImageAddData;
import com.example.nan.ssprocess.bean.basic.AbnormalRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.QualityRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerPreviewActivity;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGANinePhotoLayout;
import cn.bingoogolapple.photopicker.widget.BGASortableNinePhotoLayout;

/**
 * @author nan  2017/12/18
 */
public class DetailToInstallActivity extends AppCompatActivity implements BGASortableNinePhotoLayout.Delegate,BGANinePhotoLayout.Delegate {

    private static final String TAG="nlgDetailToInstall";
    private RadioButton installNormalRb;
    private RadioButton installAbnormalRb;
    private Spinner failReasonSpinner;
    private EditText installAbnormalDetailEt;
    private Button begainInstallButton;
    private Button installInfoUpdateButton;
    private TextView nokReasonTv;
    private TextView nokDetailTv;
    private TextView currentStatusTv;
    private LinearLayout installAbnormalLayout;
    private LinearLayout qaNokLayout;

    private ProgressDialog mUploadingProcessDialog;
    private AlertDialog mInstallDialog=null;
    private ProgressDialog mUpdatingProcessDialog;

    private TaskRecordMachineListData mTaskRecordMachineListData;
    private int iTaskRecordMachineListDataStatusTemp;

    private BGASortableNinePhotoLayout mInstallAbnormalPhotosSnpl;
    private BGANinePhotoLayout mCurrentClickNpl;

    private UpdateProcessDetailDataHandler mUpdateProcessDetailDataHandler=new UpdateProcessDetailDataHandler();

    private final String IP = SinSimApp.getApp().getServerIP();
    private static final int SCAN_QRCODE_START = 1;
    private static final int SCAN_QRCODE_END = 0;
    private static final int RC_INSTALL_CHOOSE_PHOTO = 3;
    private static final int RC_INSTALL_PHOTO_PREVIEW = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_install);

        //返回前页按钮
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView locationTv = findViewById(R.id.location_tv);
        TextView orderNumberTv=findViewById(R.id.order_number_tv);
        TextView machineNumberTv=findViewById(R.id.machine_number_tv);
        currentStatusTv=findViewById(R.id.current_status_tv);
        TextView installListTv=findViewById(R.id.intall_list_tv);

        installNormalRb=findViewById(R.id.normal_rb);
        installAbnormalRb=findViewById(R.id.abnormal_rb);
        failReasonSpinner=findViewById(R.id.fail_reason_spinner);
        installAbnormalDetailEt=findViewById(R.id.abnormal_detail_et);
        begainInstallButton = findViewById(R.id.begin_install_button);
        installInfoUpdateButton = findViewById(R.id.install_info_update_button);
        nokReasonTv=findViewById(R.id.nok_reason_tv);
        nokDetailTv=findViewById(R.id.nok_detail_tv);
        installAbnormalLayout=findViewById(R.id.install_abnormal_ll);
        qaNokLayout=findViewById(R.id.checked_nok_layout);

        //获取传递过来的信息
        Intent intent = getIntent();
        mTaskRecordMachineListData = (TaskRecordMachineListData) intent.getSerializableExtra("mTaskRecordMachineListData");
        Log.d(TAG, "onCreate: position :"+mTaskRecordMachineListData.getMachineData().getLocation());

        //把数据填入相应位置
        orderNumberTv.setText(""+mTaskRecordMachineListData.getMachineData().getOrderId());
        currentStatusTv.setText(SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus()));
        machineNumberTv.setText(mTaskRecordMachineListData.getMachineData().getNameplate());
        locationTv.setText(mTaskRecordMachineListData.getMachineData().getLocation());

        //点击下载装车单
        installListTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDownloadListData();
            }
        });

        installNormalRb.setChecked(true);
        installAbnormalLayout.setVisibility(View.GONE);
        installNormalRb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                installAbnormalLayout.setVisibility(View.VISIBLE);
            }
        });
        installAbnormalRb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                installAbnormalLayout.setVisibility(View.GONE);
            }
        });

        //开始安装
        begainInstallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTaskRecordMachineListData.getStatus()!=SinSimApp.TASK_INSTALL_WAITING){
                    Toast.makeText(DetailToInstallActivity.this, "正在 "+SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus())+" ，不能开始安装！", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(DetailToInstallActivity.this, ScanQrcodeActivity.class);
                    startActivityForResult(intent, SCAN_QRCODE_START);
                }
            }
        });
        //结束安装
        installInfoUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTaskRecordMachineListData.getStatus()!=SinSimApp.TASK_INSTALLING){
                    Toast.makeText(DetailToInstallActivity.this, "正在 "+SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus())+" ，不能结束安装！", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(DetailToInstallActivity.this, ScanQrcodeActivity.class);
                    startActivityForResult(intent, SCAN_QRCODE_END);
                }
            }
        });

        fetchInstallRecordData();

        //九宫格拍照
        mInstallAbnormalPhotosSnpl = findViewById(R.id.install_abnormal_add_photos);
        mInstallAbnormalPhotosSnpl.setMaxItemCount(9);
        mInstallAbnormalPhotosSnpl.setPlusEnable(true);
        mInstallAbnormalPhotosSnpl.setDelegate(this);

    }

    /**
     * 根据taskRecordId获取当前abnormal信息
     */
    private void fetchInstallRecordData() {
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecordId", ""+mTaskRecordMachineListData.getId());
        String fetchProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FATCH_INSTALL_ABNORMAL_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessInstallRecordData(fetchProcessRecordUrl, mPostValue, new FetchInstallRecordDataHandler());

        String fetchQaProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FATCH_TASK_QUALITY_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessQARecordData(fetchQaProcessRecordUrl, mPostValue, new FetchQaRecordDataHandler());
    }


    @SuppressLint("HandlerLeak")
    private class FetchInstallRecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取安装结果
                ArrayList<AbnormalRecordDetailsData> mAbnormalRecordList = (ArrayList<AbnormalRecordDetailsData>) msg.obj;
                Log.d(TAG, "handleMessage: mAbnormalRecordList.size:"+mAbnormalRecordList.size());
                if (mAbnormalRecordList.size()>0) {
                    int updateTime = mAbnormalRecordList.size() - 1;
                    for (int update = mAbnormalRecordList.size() - 2; update >= 0; update--) {
                        if (mAbnormalRecordList.get(updateTime).getId() < mAbnormalRecordList.get(update).getId()) {
                            Log.d(TAG, "handleMessage: " + mAbnormalRecordList.get(update).getCreateTime() + " : " + mAbnormalRecordList.get(update + 1).getCreateTime());
                            updateTime = update;
                        }
                        Log.d(TAG, "handleMessage: updateTime:" + updateTime);
                    }
                    AbnormalRecordDetailsData abnormalRecordDetailsData = mAbnormalRecordList.get(updateTime);
                    //如果安装异常，填入异常的原因
                    Log.d(TAG, "handleMessage: 流程："+abnormalRecordDetailsData.getTaskRecord().getStatus()+" 异常类型："+abnormalRecordDetailsData.getAbnormalType());
                    if (abnormalRecordDetailsData.getTaskRecord().getStatus()  == SinSimApp.TASK_INSTALL_ABNORMAL) {
                        installAbnormalRb.setChecked(true);
                        failReasonSpinner.setSelection(abnormalRecordDetailsData.getAbnormalType());
                        installAbnormalDetailEt.setText(abnormalRecordDetailsData.getComment());
                        //加载历史照片地址
                        Log.d(TAG, "handleMessage: photo url: "+abnormalRecordDetailsData.getAbnormalImage().getImage());
                        ArrayList<String> installPhotoList=new ArrayList<>(Arrays.asList("http://192.168.0.103:8080/images/abnormal/A0M094156575_null_Abnormal_2018-03-08-11-48-58.jpg", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered11.png"));
                        mInstallAbnormalPhotosSnpl.addMoreData(installPhotoList);
                    } else {
                        installNormalRb.setChecked(true);
                        failReasonSpinner.setSelection(0);
                        installAbnormalDetailEt.setText("");
                    }
                } else {
                    Log.d(TAG, "handleMessage: 没有安装异常的信息");
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(DetailToInstallActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class FetchQaRecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取质检结果
                ArrayList<QualityRecordDetailsData> mQualityRecordList = (ArrayList<QualityRecordDetailsData>) msg.obj;
                if (mQualityRecordList.size()>0) {
                    int updateTime = mQualityRecordList.size() - 1;
                    //根据id取值
                    for (int update = mQualityRecordList.size() - 2; update >= 0; update--) {
                        if (mQualityRecordList.get(updateTime).getId() < mQualityRecordList.get(update).getId()) {
                            updateTime = update;
                        }
                        Log.d(TAG, "handleMessage: updateTime1:" + updateTime);
                    }
                    QualityRecordDetailsData mQualityRecordDetailsData = mQualityRecordList.get(updateTime);
                    if (mQualityRecordDetailsData.getStatus() == SinSimApp.TASK_QUALITY_ABNORMAL) {
                        nokReasonTv.setText("不合格");
                        qaNokLayout.setVisibility(View.VISIBLE);
                        nokDetailTv.setText(mQualityRecordDetailsData.getComment());
                        //照片地址
                        ArrayList<String> checkoutPhotoList = new ArrayList<>(Arrays.asList(URL.HTTP_HEAD + IP + mQualityRecordDetailsData.getQualityRecordImage().getImage(), "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered11.png"));
                        BGANinePhotoLayout checkoutNinePhotoLayout = findViewById(R.id.checkout_nok_photos);
                        checkoutNinePhotoLayout.setDelegate(DetailToInstallActivity.this);
                        checkoutNinePhotoLayout.setData(checkoutPhotoList);
                    } else if (mQualityRecordDetailsData.getStatus() == SinSimApp.TASK_QUALITY_DONE){
                        nokReasonTv.setText("合格");
                        qaNokLayout.setVisibility(View.GONE);
                    } else {
                        nokReasonTv.setText("暂无");
                        qaNokLayout.setVisibility(View.GONE);
                    }
                } else {
                    nokReasonTv.setText("尚未质检");
                    qaNokLayout.setVisibility(View.GONE);
                }
            } else {
                nokReasonTv.setText("暂无");
                qaNokLayout.setVisibility(View.GONE);
                String errorMsg = (String)msg.obj;
                Toast.makeText(DetailToInstallActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
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
                Intent intent=new Intent(DetailToInstallActivity.this,InstallListActivity.class);
                intent.putExtra("mInstallFileList", mInstallFileList);
                startActivity(intent);
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "FetchInstallFileListHandler handleMessage: "+errorMsg);
                Toast.makeText(DetailToInstallActivity.this, "网络错误！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onClickNinePhotoItem(BGANinePhotoLayout ninePhotoLayout, View view, int position, String model, List<String> models) {
        mCurrentClickNpl = ninePhotoLayout;
        photoPreviewWrapper();
    }

    private void photoPreviewWrapper() {
        if (mCurrentClickNpl == null) {
            return;
        }
        File downloadDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerDownload");
        BGAPhotoPreviewActivity.IntentBuilder photoPreviewIntentBuilder = new BGAPhotoPreviewActivity.IntentBuilder(this)
                .saveImgDir(downloadDir); // 保存图片的目录，如果传 null，则没有保存图片功能
        if (mCurrentClickNpl.getItemCount() == 1) {
            // 预览单张图片
            photoPreviewIntentBuilder.previewPhoto(mCurrentClickNpl.getCurrentClickItem());
        } else if (mCurrentClickNpl.getItemCount() > 1) {
            // 预览多张图片
            photoPreviewIntentBuilder.previewPhotos(mCurrentClickNpl.getData())
                    .currentPosition(mCurrentClickNpl.getCurrentClickItemPosition()); // 当前预览图片的索引
        }
        startActivity(photoPreviewIntentBuilder.build());
    }

    @Override
    public void onClickAddNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, ArrayList<String> models) {
        choicePhotoWrapper();
    }

    @Override
    public void onClickDeleteNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        mInstallAbnormalPhotosSnpl.removeItem(position);
    }

    @Override
    public void onClickNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        Intent photoPickerPreviewIntent = new BGAPhotoPickerPreviewActivity.IntentBuilder(DetailToInstallActivity.this)
                .previewPhotos(models) // 当前预览的图片路径集合
                .selectedPhotos(models) // 当前已选中的图片路径集合
                .maxChooseCount(mInstallAbnormalPhotosSnpl.getMaxItemCount()) // 图片选择张数的最大值
                .currentPosition(position) // 当前预览图片的索引
                .isFromTakePhoto(false) // 是否是拍完照后跳转过来
                .build();
        startActivityForResult(photoPickerPreviewIntent, RC_INSTALL_PHOTO_PREVIEW);
    }

    @Override
    public void onNinePhotoItemExchanged(BGASortableNinePhotoLayout sortableNinePhotoLayout, int fromPosition, int toPosition, ArrayList<String> models) {

    }

    private void choicePhotoWrapper() {
        // 拍照后照片的存放目录，改成你自己拍照后要存放照片的目录。如果不传递该参数的话就没有拍照功能
        File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");
        Intent photoPickerIntent = new BGAPhotoPickerActivity.IntentBuilder(DetailToInstallActivity.this)
                .cameraFileDir(takePhotoDir) // 拍照后照片的存放目录，改成你自己拍照后要存放照片的目录。
                .maxChooseCount(mInstallAbnormalPhotosSnpl.getMaxItemCount() - mInstallAbnormalPhotosSnpl.getItemCount()) // 图片选择张数的最大值
                .selectedPhotos(null) // 当前已选中的图片路径集合
                .pauseOnScroll(false) // 滚动列表时是否暂停加载图片
                .build();
        startActivityForResult(photoPickerIntent, RC_INSTALL_CHOOSE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SCAN_QRCODE_START:
                if(resultCode == RESULT_OK) {
                    // 检验二维码信息是否对应
                    String mMachineStrId = data.getStringExtra("mMachineStrId");

                    if(mMachineStrId.equals(mTaskRecordMachineListData.getMachineData().getMachineStrId())){
                        Log.d(TAG, "onActivityResult: id 对应");
                        //update status
                        if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_INSTALL_WAITING) {
                            mInstallDialog = new AlertDialog.Builder(DetailToInstallActivity.this).create();
                            mInstallDialog.setMessage("是否现在开始安装？");
                            mInstallDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            mInstallDialog.setButton(AlertDialog.BUTTON_POSITIVE, "是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //改状态
                                    if( mUpdatingProcessDialog == null) {
                                        mUpdatingProcessDialog = new ProgressDialog(DetailToInstallActivity.this);
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
                                    mTaskRecordMachineListData.setInstallBeginTime(staCurTime);
                                    iTaskRecordMachineListDataStatusTemp=mTaskRecordMachineListData.getStatus();
                                    updateProcessDetailData(SinSimApp.TASK_INSTALLING);
                                }
                            });
                            mInstallDialog.show();
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
                    // 检验二维码信息是否对应
                    String mMachineStrId = data.getStringExtra("mMachineStrId");

                    if(mMachineStrId.equals(mTaskRecordMachineListData.getMachineData().getMachineStrId())){
                        Log.d(TAG, "onActivityResult: id 对应");
                        //update info
                        if( mUpdatingProcessDialog == null) {
                            mUpdatingProcessDialog = new ProgressDialog(DetailToInstallActivity.this);
                            mUpdatingProcessDialog.setCancelable(false);
                            mUpdatingProcessDialog.setCanceledOnTouchOutside(false);
                            mUpdatingProcessDialog.setMessage("正在结束...");
                        }
                        mUpdatingProcessDialog.show();
                        updateInstallRecordData();
                    } else {
                        Log.d(TAG, "onActivityResult: 二维码信息不对应");
                        Toast.makeText(this, "二维码信息不对应！", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "onActivityResult: scan QRcode fail");
                }

                break;
            case RC_INSTALL_CHOOSE_PHOTO:
                if(resultCode == RESULT_OK) {
                    mInstallAbnormalPhotosSnpl.addMoreData(BGAPhotoPickerActivity.getSelectedPhotos(data));
                } else {
                    Log.d(TAG, "onActivityResult: choose  nothing");
                }
                break;
            case RC_INSTALL_PHOTO_PREVIEW:
                mInstallAbnormalPhotosSnpl.setData(BGAPhotoPickerPreviewActivity.getSelectedPhotos(data));
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
            if(mUploadingProcessDialog != null && mUploadingProcessDialog.isShowing()) {
                mUploadingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                currentStatusTv.setText(SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus()));
            } else {
                mTaskRecordMachineListData.setStatus(iTaskRecordMachineListDataStatusTemp);
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg+mTaskRecordMachineListData.getStatus());
                Toast.makeText(DetailToInstallActivity.this, "失败，网络错误，请检查网络！", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * 更新abnormal信息
     */
    private void updateInstallRecordData() {
        Gson gson=new Gson();
        //获取当前时间
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        String strCurTime = formatter.format(curDate);
        long lCurTime=System.currentTimeMillis();
        mTaskRecordMachineListData.setInstallEndTime(strCurTime);

        //读取和更新输入信息
        if(installNormalRb.isChecked()){
            iTaskRecordMachineListDataStatusTemp=mTaskRecordMachineListData.getStatus();
            updateProcessDetailData(SinSimApp.TASK_INSTALLED);
        }else if(installAbnormalRb.isChecked()){
            AbnormalRecordDetailsData abnormalRecordAddData=new AbnormalRecordDetailsData(SinSimApp.getApp().getUserId(),mTaskRecordMachineListData.getId(),lCurTime);
            String bnormalRecordDetailsDataToJson = gson.toJson(abnormalRecordAddData);
            Log.d(TAG, "updateInstallRecordData: gson :"+ bnormalRecordDetailsDataToJson);
            if(installAbnormalDetailEt.getText()!=null && mInstallAbnormalPhotosSnpl.getData().size()>0){
                //获取安装异常的原因
                abnormalRecordAddData.setComment(installAbnormalDetailEt.getText().toString());
                abnormalRecordAddData.setAbnormalType( failReasonSpinner.getSelectedItemPosition()+1);
                //上传安装结果
                String nAbnormalRecordDetailsDataToJson = gson.toJson(abnormalRecordAddData);
                Log.d(TAG, "updateInstallRecordData: gson :"+ nAbnormalRecordDetailsDataToJson);
                LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                mPostValue.put("abnormalRecord", nAbnormalRecordDetailsDataToJson);
                String updateProcessRecordUrl = URL.HTTP_HEAD + IP + URL.UPDATE_INSTALL_ABNORMAL_RECORD_DETAIL;
                Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, new UpdateProcessResultDataHandler());

            } else {
                Toast.makeText(this, "异常原因和异常照片不能为空！", Toast.LENGTH_SHORT).show();
            }
        }

        if( mUploadingProcessDialog == null) {
            mUploadingProcessDialog = new ProgressDialog(DetailToInstallActivity.this);
            mUploadingProcessDialog.setCancelable(false);
            mUploadingProcessDialog.setCanceledOnTouchOutside(false);
            mUploadingProcessDialog.setMessage("上传信息中...");
        }
        mUploadingProcessDialog.show();

    }
    @SuppressLint("HandlerLeak")
    private class UpdateProcessResultDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                Toast.makeText(DetailToInstallActivity.this, "异常信息上传成功！", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "handleMessage: 异常信息上传成功");
                LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                mPostValue.put("taskRecordId", ""+mTaskRecordMachineListData.getId());
                String fetchProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FATCH_INSTALL_ABNORMAL_RECORD_LIST;
                Network.Instance(SinSimApp.getApp()).fetchProcessInstallRecordData(fetchProcessRecordUrl, mPostValue, new FetchAbnormalRecordDataHandler());
                //TODO:发送mqtt消息，更新安装状态，跳转回list界面
            } else {
                if(mUploadingProcessDialog != null && mUploadingProcessDialog.isShowing()) {
                    mUploadingProcessDialog.dismiss();
                }
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToInstallActivity.this, "异常信息上传失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class FetchAbnormalRecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取安装结果
                ArrayList<AbnormalRecordDetailsData> mAbnormalRecordList = (ArrayList<AbnormalRecordDetailsData>) msg.obj;
                Log.d(TAG, "handleMessage: 质检异常lise size："+mAbnormalRecordList.size());
                if ( mAbnormalRecordList.size()>0) {
                    int updateTime = mAbnormalRecordList.size() - 1;
                    for (int update = mAbnormalRecordList.size() - 2; update >= 0; update--) {
                        if (mAbnormalRecordList.get(updateTime).getId() < mAbnormalRecordList.get(update).getId()) {
                            updateTime = update;
                        }
                        Log.d(TAG, "handleMessage: updateTime:" + updateTime);
                    }
                    AbnormalRecordDetailsData abnormalRecordDetailsData = mAbnormalRecordList.get(updateTime);

                    //获取图片本地url
                    ArrayList<String> imageUrlList = mInstallAbnormalPhotosSnpl.getData();
                    //添加quality_record_image数据库
                    AbnormalImageAddData abnormalImageAddData = new AbnormalImageAddData();
                    //更新当前时间
                    abnormalImageAddData.setCreateTime(abnormalRecordDetailsData.getCreateTime());
                    abnormalImageAddData.setAbnormalRecordId(abnormalRecordDetailsData.getId());
                    //上传质检不合格照片
                    Gson gson=new Gson();
                    String imageJson = gson.toJson(abnormalImageAddData);
                    Log.d(TAG, "updateInstallRecordData: "+imageJson);
                    String uploadQualityRecordImageUrl = URL.HTTP_HEAD + IP + URL.UPLOAD_INSTALL_ABNORMAL_IMAGE;
                    Network.Instance(SinSimApp.getApp()).uploadTaskRecordImage(uploadQualityRecordImageUrl, imageUrlList, "abnormalImage", imageJson, new UploadTaskRecordImageHandler());

                } else {
                    if(mUploadingProcessDialog != null && mUploadingProcessDialog.isShowing()) {
                        mUploadingProcessDialog.dismiss();
                    }
                    Log.d(TAG, "handleMessage: 添加安装异常的信息失败");
                }
            } else {
                if(mUploadingProcessDialog != null && mUploadingProcessDialog.isShowing()) {
                    mUploadingProcessDialog.dismiss();
                }
                String errorMsg = (String)msg.obj;
                Toast.makeText(DetailToInstallActivity.this, "上传信息失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @SuppressLint("HandlerLeak")
    private class UploadTaskRecordImageHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                Toast.makeText(DetailToInstallActivity.this, "照片上传成功！", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "handleMessage: 照片上传成功！");
                iTaskRecordMachineListDataStatusTemp=mTaskRecordMachineListData.getStatus();
                updateProcessDetailData(SinSimApp.TASK_INSTALL_ABNORMAL);
            } else {
                if(mUploadingProcessDialog != null && mUploadingProcessDialog.isShowing()) {
                    mUploadingProcessDialog.dismiss();
                }
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToInstallActivity.this, "异常照片上传失败！请重新上传", Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        super.onDestroy();
        if(mUploadingProcessDialog != null) {
            mUploadingProcessDialog.dismiss();
        }
        if(mInstallDialog != null) {
            mInstallDialog.dismiss();
        }
        if(mUpdatingProcessDialog != null) {
            mUpdatingProcessDialog.dismiss();
        }
    }
}