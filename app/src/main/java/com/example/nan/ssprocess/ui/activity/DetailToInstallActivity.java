package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.AbnormalImageAddData;
import com.example.nan.ssprocess.bean.basic.AbnormalRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
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
public class DetailToInstallActivity extends AppCompatActivity implements BGASortableNinePhotoLayout.Delegate {

    private static final String TAG="nlgDetailToInstall";
    private RadioButton installNormalRb;
    private RadioButton installAbnormalRb;
    private Spinner failReasonSpinner;
    private EditText installAbnormalDetailEt;
    private Button begainInstallButton;
    private Button installInfoUpdateButton;

    private TaskMachineListData mTaskMachineListData=new TaskMachineListData();
    private ArrayList<AbnormalRecordDetailsData> mAbnormalRecordList = new ArrayList<>();
    private AbnormalRecordDetailsData mAbnormalRecordDetailsData=new AbnormalRecordDetailsData();
    private FetchInstallRecordDataHandler mFetchInstallRecordDataHandler = new FetchInstallRecordDataHandler();
    private UpdateProcessDetailDataHandler mUpdateProcessDetailDataHandler=new UpdateProcessDetailDataHandler();
    private UploadTaskRecordImageHandler mUploadTaskRecordImageHandler=new UploadTaskRecordImageHandler();


    private static final int SCAN_QRCODE_START = 1;
    private static final int SCAN_QRCODE_END = 0;
    private static final int RC_INSTALL_CHOOSE_PHOTO = 3;
    private static final int RC_INSTALL_PHOTO_PREVIEW = 4;
	private static final int NORMAL = 3;
    private static final int ABNORMAL = 4;

    private BGASortableNinePhotoLayout mInstallAbnormalPhotosSnpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_install);

        EditText locationEt = findViewById(R.id.location_et);
        TextView orderNumberTv=findViewById(R.id.order_number_tv);
        TextView machineNumberTv=findViewById(R.id.machine_number_tv);
        TextView needleCountTv=findViewById(R.id.needle_count_tv);
        TextView typeTv=findViewById(R.id.type_tv);
        TextView intallListTv=findViewById(R.id.intall_list_tv);

        installNormalRb=findViewById(R.id.normal_rb);
        installAbnormalRb=findViewById(R.id.abnormal_rb);
        failReasonSpinner=findViewById(R.id.fail_reason_spinner);
        installAbnormalDetailEt=findViewById(R.id.abnormal_detail_et);
        begainInstallButton = findViewById(R.id.begin_install_button);
        installInfoUpdateButton = findViewById(R.id.install_info_update_button);

        //获取传递过来的信息
        Intent intent = getIntent();
        mTaskMachineListData = (TaskMachineListData) intent.getSerializableExtra("mTaskMachineListData");
        Log.d(TAG, "onCreate: position :"+mTaskMachineListData.getMachineData().getLocation());

        //把数据填入相应位置
        orderNumberTv.setText(""+mTaskMachineListData.getMachineData().getOrderId());
        needleCountTv.setText(""+mTaskMachineListData.getMachineOrderData().getHeadNum());
        machineNumberTv.setText(mTaskMachineListData.getMachineData().getMachineStrId());
        typeTv.setText(""+mTaskMachineListData.getMachineOrderData().getMachineType());
        locationEt.setText(mTaskMachineListData.getMachineData().getLocation());
        locationEt.setFocusable(false);
        locationEt.setEnabled(false);

        //点击返回
        ImageView previousIv = findViewById(R.id.close_machine_detail);
        previousIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //安装状态
        switch (mTaskMachineListData.getMachineData().getStatus()){
            case 1:
                begainInstallButton.setText("扫码开始");
                begainInstallButton.setEnabled(true);
                begainInstallButton.setClickable(true);
                begainInstallButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(DetailToInstallActivity.this,ScanQrcodeActivity.class);
                        startActivityForResult(intent,SCAN_QRCODE_START);
                    }
                });
                installInfoUpdateButton.setClickable(false);
                installInfoUpdateButton.setEnabled(false);
                break;
            case 2:
                begainInstallButton.setText("安装中");
                begainInstallButton.setEnabled(false);
                begainInstallButton.setClickable(false);
                installInfoUpdateButton.setClickable(true);
                installInfoUpdateButton.setEnabled(true);
                installInfoUpdateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(DetailToInstallActivity.this,ScanQrcodeActivity.class);
                        startActivityForResult(intent,SCAN_QRCODE_END);
                    }
                });
                break;
            case 3:
                begainInstallButton.setText("扫码开始");
                begainInstallButton.setEnabled(false);
                begainInstallButton.setClickable(false);
                installInfoUpdateButton.setClickable(false);
                installInfoUpdateButton.setEnabled(false);
                break;
            case 4:
                begainInstallButton.setText("重新开始");
                begainInstallButton.setEnabled(true);
                begainInstallButton.setClickable(true);
                begainInstallButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(DetailToInstallActivity.this,ScanQrcodeActivity.class);
                        startActivityForResult(intent,SCAN_QRCODE_START);
                    }
                });
                installInfoUpdateButton.setClickable(false);
                installInfoUpdateButton.setEnabled(false);
                break;
            default:
                break;
        }

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
        final String account = SinSimApp.getApp().getAccount();
        final String ip = SinSimApp.getApp().getServerIP();
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecordId", ""+mTaskMachineListData.getId());
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FATCH_INSTALL_ABNORMAL_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessInstallRecordData(fetchProcessRecordUrl, mPostValue, mFetchInstallRecordDataHandler);
    }

    @SuppressLint("HandlerLeak")
    private class FetchInstallRecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取质检结果
                mAbnormalRecordList=(ArrayList<AbnormalRecordDetailsData>)msg.obj;
                if (mAbnormalRecordList!=null && !mAbnormalRecordList.isEmpty()) {
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
                    //如果异常，填入异常原因
                    Log.d(TAG, "handleMessage: 流程："+mAbnormalRecordDetailsData.getTaskRecord().getStatus()+" 异常类型："+mAbnormalRecordDetailsData.getAbnormalType());
                    if (mAbnormalRecordDetailsData.getTaskRecord().getStatus() == 4) {
                        installAbnormalRb.setChecked(true);
                        failReasonSpinner.setSelection(mAbnormalRecordDetailsData.getAbnormalType(), true);
                        installAbnormalDetailEt.setText(mAbnormalRecordDetailsData.getComment());
                        //TODO:照片地址
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

    /**
     * 更新abnormal信息
     */
    private void updateInstallRecordData() {
        final String ip = SinSimApp.getApp().getServerIP();
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
                String strCurTima = formatter.format(curDate);
                //更新当前时间
                abnormalImageAddData.setCreateTime(strCurTima);
                abnormalImageAddData.setAbnormalRecordId(mAbnormalRecordDetailsData.getId());
                //更新质检不合格照片
                String imageJson = gson.toJson(abnormalImageAddData);
                Log.d(TAG, "updateInstallRecordData: "+imageJson);
                String uploadQualityRecordImageUrl = URL.HTTP_HEAD + ip + URL.UPLOAD_INSTALL_ABNORMAL_IMAGE;
                Network.Instance(SinSimApp.getApp()).uploadTaskRecordImage(uploadQualityRecordImageUrl, imageUrlList, "abnormalImage", imageJson, mUploadTaskRecordImageHandler);
            }
        }

        String mAbnormalRecordDetailsDataToJson = gson.toJson(mAbnormalRecordDetailsData);
        Log.d(TAG, "updateInstallRecordData: gson :"+ mAbnormalRecordDetailsDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("strTaskQualityRecordDetail", mAbnormalRecordDetailsDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + ip + URL.UPDATE_INSTALL_ABNORMAL_RECORD_DETAIL;
        Log.d(TAG, "updateInstallRecordData: "+updateProcessRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, mUpdateProcessDetailDataHandler);
    }

    @SuppressLint("HandlerLeak")
    private class UploadTaskRecordImageHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                Toast.makeText(DetailToInstallActivity.this, "上传图片成功！", Toast.LENGTH_SHORT).show();
                installInfoUpdateButton.setText("重新上传");
                //TODO:是否弹窗
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToInstallActivity.this, "上传图片失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                Toast.makeText(DetailToInstallActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();

                //TODO:发送mqtt消息，更新安装状态，跳转回list界面
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToInstallActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
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
                    TaskMachineListData taskMachineListDataId=new TaskMachineListData();
                    taskMachineListDataId = (TaskMachineListData) data.getSerializableExtra("mTaskMachineListData");
                    if(taskMachineListDataId.getId()==mTaskMachineListData.getId()){
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
                    TaskMachineListData taskMachineListDataId=new TaskMachineListData();
                    taskMachineListDataId = (TaskMachineListData) data.getSerializableExtra("mTaskMachineListData");
                    if(taskMachineListDataId.getId()==mTaskMachineListData.getId()){
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
}