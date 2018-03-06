package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
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
    private LinearLayout installAbnormalLayout;
    private LinearLayout qaNokLayout;

    private ProgressDialog mUploadingProcessDialog;

    private TaskMachineListData mTaskMachineListData=new TaskMachineListData();
    private AbnormalRecordDetailsData mAbnormalRecordDetailsData=new AbnormalRecordDetailsData();

    private BGASortableNinePhotoLayout mInstallAbnormalPhotosSnpl;
    private BGANinePhotoLayout mCurrentClickNpl;

    private final String IP = SinSimApp.getApp().getServerIP();
    private static final int SCAN_QRCODE_START = 1;
    private static final int SCAN_QRCODE_END = 0;
    private static final int RC_INSTALL_CHOOSE_PHOTO = 3;
    private static final int RC_INSTALL_PHOTO_PREVIEW = 4;
    private static final int NORMAL = 3;
    private static final int ABNORMAL = 4;

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
        TextView currentStatusTv=findViewById(R.id.current_status_tv);
        TextView installListTv=findViewById(R.id.intall_list_tv);

        //点击下载装车单
        installListTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //下载装车单
                fetchDownloadListData();
            }
        });

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
        mTaskMachineListData = (TaskMachineListData) intent.getSerializableExtra("mTaskMachineListData");
        Log.d(TAG, "onCreate: position :"+mTaskMachineListData.getMachineData().getLocation());

        //把数据填入相应位置
        orderNumberTv.setText(""+mTaskMachineListData.getMachineData().getOrderId());
        currentStatusTv.setText(SinSimApp.getInstallStatusString(mTaskMachineListData.getStatus()));
        machineNumberTv.setText(mTaskMachineListData.getMachineData().getNameplate());
        locationTv.setText(mTaskMachineListData.getMachineData().getLocation());

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
                if (mTaskMachineListData.getMachineData().getStatus()!=SinSimApp.TASK_INSTALL_WAITING){
                    Toast.makeText(DetailToInstallActivity.this, "正在 "+SinSimApp.getInstallStatusString(mTaskMachineListData.getStatus())+" ，不能开始安装！", Toast.LENGTH_SHORT).show();
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
                if (mTaskMachineListData.getMachineData().getStatus()!=SinSimApp.TASK_INSTALLING){
                    Toast.makeText(DetailToInstallActivity.this, "正在 "+SinSimApp.getInstallStatusString(mTaskMachineListData.getStatus())+" ，不能结束安装！", Toast.LENGTH_SHORT).show();
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
        mPostValue.put("taskRecordId", ""+mTaskMachineListData.getId());
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
                //获取质检结果
                ArrayList<AbnormalRecordDetailsData> mAbnormalRecordList = (ArrayList<AbnormalRecordDetailsData>) msg.obj;
                if (mAbnormalRecordList !=null && !mAbnormalRecordList.isEmpty()) {
                    int updateTime = mAbnormalRecordList.size() - 1;
                    //对比mQualityRecordList.get(update).getCreateTime()取值
                    for (int update = mAbnormalRecordList.size() - 2; update >= 0; update--) {
                        if (mAbnormalRecordList.get(updateTime).getCreateTime() < mAbnormalRecordList.get(update).getCreateTime()) {
                            Log.d(TAG, "handleMessage: " + mAbnormalRecordList.get(update).getCreateTime() + " : " + mAbnormalRecordList.get(update + 1).getCreateTime());
                            updateTime = update;
                        }
                        Log.d(TAG, "handleMessage: updateTime:" + updateTime);
                    }
                    mAbnormalRecordDetailsData = mAbnormalRecordList.get(updateTime);
                    //如果安装异常，填入异常的原因
                    Log.d(TAG, "handleMessage: 流程："+mAbnormalRecordDetailsData.getTaskRecord().getStatus()+" 异常类型："+mAbnormalRecordDetailsData.getAbnormalType());
                    if (mAbnormalRecordDetailsData.getTaskRecord().getStatus()  == SinSimApp.TASK_INSTALL_ABNORMAL) {
                        installAbnormalRb.setChecked(true);
                        failReasonSpinner.setSelection(mAbnormalRecordDetailsData.getAbnormalType(), true);
                        installAbnormalDetailEt.setText(mAbnormalRecordDetailsData.getComment());
                        //加载历史照片地址
                        Log.d(TAG, "handleMessage: photo url: "+mAbnormalRecordDetailsData.getAbnormalImage().getImage());
                        ArrayList<String> installPhotoList=new ArrayList<>(Arrays.asList(URL.HTTP_HEAD+IP+mAbnormalRecordDetailsData.getAbnormalImage().getImage()));
                        mInstallAbnormalPhotosSnpl.addMoreData(installPhotoList);
                    } else {
                        installNormalRb.setChecked(true);
                        failReasonSpinner.setSelection(mAbnormalRecordDetailsData.getAbnormalType(), true);
                        installAbnormalDetailEt.setText("");
                    }
                } else {
                    Log.d(TAG, "handleMessage: 没有安装异常");
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
                if (mQualityRecordList !=null && !mQualityRecordList.isEmpty()) {
                    int updateTime = mQualityRecordList.size() - 1;
                    //根据CreateTime取值
                    for (int update = mQualityRecordList.size() - 2; update >= 0; update--) {
                        if (mQualityRecordList.get(updateTime).getCreateTime() < mQualityRecordList.get(update).getCreateTime()) {
                            Log.d(TAG, "handleMessage: " + mQualityRecordList.get(updateTime).getCreateTime() + " : " + mQualityRecordList.get(update).getCreateTime());
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
        mPostValue.put("order_id", ""+mTaskMachineListData.getMachineData().getOrderId());
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

                    if(mMachineStrId.equals(mTaskMachineListData.getMachineData().getMachineStrId())){
                        Log.d(TAG, "onActivityResult: id 对应");
                        //update status
                        //TODO:更新安装状态

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

                    if(mMachineStrId.equals(mTaskMachineListData.getMachineData().getMachineStrId())){
                        Log.d(TAG, "onActivityResult: id 对应");
                        //update info
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

    /**
     * 更新abnormal信息
     */
    private void updateInstallRecordData() {
        ArrayList<String> imageUrlList = new ArrayList<>();
        Gson gson=new Gson();
        //读取和更新输入信息
        if(installNormalRb.isChecked()){
            mAbnormalRecordDetailsData.getTaskRecord().setStatus(NORMAL);
        }else if(installAbnormalRb.isChecked()){
            mAbnormalRecordDetailsData.getTaskRecord().setStatus(ABNORMAL);
            mAbnormalRecordDetailsData.setAbnormalType((int) failReasonSpinner.getSelectedItemId());
            if(installAbnormalDetailEt.getText()!=null && mInstallAbnormalPhotosSnpl.getData().size()>0){
                //获取安装异常的原因
                mAbnormalRecordDetailsData.setComment(installAbnormalDetailEt.getText().toString());
                //获取图片本地url
                imageUrlList = mInstallAbnormalPhotosSnpl.getData();
                //添加quality_record_image数据库
                AbnormalImageAddData abnormalImageAddData = new AbnormalImageAddData();
                //获取当前时间
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                Date curDate = new Date(System.currentTimeMillis());
                String staCurTime = formatter.format(curDate);
                //更新当前时间
                abnormalImageAddData.setCreateTime(staCurTime);
                abnormalImageAddData.setAbnormalRecordId(mAbnormalRecordDetailsData.getId());
                //上传质检不合格照片
                String imageJson = gson.toJson(abnormalImageAddData);
                Log.d(TAG, "updateInstallRecordData: "+imageJson);
                String uploadQualityRecordImageUrl = URL.HTTP_HEAD + IP + URL.UPLOAD_INSTALL_ABNORMAL_IMAGE;
                Network.Instance(SinSimApp.getApp()).uploadTaskRecordImage(uploadQualityRecordImageUrl, imageUrlList, "abnormalImage", imageJson, new UploadTaskRecordImageHandler());
            } else {
                Toast.makeText(this, "异常原因和异常照片不能为空！", Toast.LENGTH_SHORT).show();
            }
        }
        //上传质检结果
        String mAbnormalRecordDetailsDataToJson = gson.toJson(mAbnormalRecordDetailsData);
        Log.d(TAG, "updateInstallRecordData: gson :"+ mAbnormalRecordDetailsDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("strTaskQualityRecordDetail", mAbnormalRecordDetailsDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + IP + URL.UPDATE_INSTALL_ABNORMAL_RECORD_DETAIL;
        Log.d(TAG, "updateInstallRecordData: "+updateProcessRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, new UpdateProcessDetailDataHandler());

        if( mUploadingProcessDialog == null) {
            mUploadingProcessDialog = new ProgressDialog(DetailToInstallActivity.this);
            mUploadingProcessDialog.setCancelable(false);
            mUploadingProcessDialog.setCanceledOnTouchOutside(false);
            mUploadingProcessDialog.setMessage("上传信息中...");
        }
        mUploadingProcessDialog.show();

    }

    @SuppressLint("HandlerLeak")
    private class UploadTaskRecordImageHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                Toast.makeText(DetailToInstallActivity.this, "照片上传成功！", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "handleMessage: 照片上传成功！");
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

    @SuppressLint("HandlerLeak")
    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mUploadingProcessDialog != null && mUploadingProcessDialog.isShowing()) {
                mUploadingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                Toast.makeText(DetailToInstallActivity.this, "异常信息上传成功！", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "handleMessage: 异常信息上传成功");
                //TODO:发送mqtt消息，更新安装状态，跳转回list界面
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToInstallActivity.this, "异常信息上传失败！"+errorMsg, Toast.LENGTH_SHORT).show();
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
    }
}