package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.QualityRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.QualityRecordImageAddData;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGASortableNinePhotoLayout;

/**
 * @author nan  2017/12/18
 */

public class DetailToCheckoutActivity extends AppCompatActivity implements BGASortableNinePhotoLayout.Delegate{
    private static final String TAG="nlgDetailToCheckout";
    private RadioButton checkedOkRb;
    private RadioButton checkedNokRb;
    private EditText checkoutNokDetailEt;
    private Button installInfoUpdateButton;
    private LinearLayout QaNokLinearLayout;

    private BGASortableNinePhotoLayout mCheckoutNokPhotosSnpl;
    private TaskMachineListData mTaskMachineListData=new TaskMachineListData();
    private QualityRecordDetailsData mQualityRecordDetailsData =new QualityRecordDetailsData();

    private final String IP = SinSimApp.getApp().getServerIP();
    private static final int SCAN_QRCODE_END = 0;
    private static final int RC_CHECKOUT_CHOOSE_PHOTO = 3;
    private static final int RC_CHECKOUT_PHOTO_PREVIEW = 4;
    private static final int PASS = 1;
    private static final int NO_PASS = 0;

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

        TextView locationTv=findViewById(R.id.location_tv);
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

        checkedOkRb=findViewById(R.id.checked_ok_rb);
        checkedNokRb=findViewById(R.id.checked_nok_rb);
        checkoutNokDetailEt=findViewById(R.id.checkout_nok_detail_et);
        installInfoUpdateButton = findViewById(R.id.checkout_upload_button);
        QaNokLinearLayout = findViewById(R.id.qa_nok_ll);

        //获取传递过来的信息
        Intent intent = getIntent();
        mTaskMachineListData = (TaskMachineListData) intent.getSerializableExtra("mTaskMachineListData");
        Log.d(TAG, "onCreate: position :"+mTaskMachineListData.getMachineData().getLocation());

        //把数据填入相应位置
        orderNumberTv.setText(""+mTaskMachineListData.getMachineData().getOrderId());
        currentStatusTv.setText(SinSimApp.getInstallStatusString(mTaskMachineListData.getStatus()));
        machineNumberTv.setText(mTaskMachineListData.getMachineData().getNameplate());
        locationTv.setText(mTaskMachineListData.getMachineData().getLocation());

        checkedOkRb.setChecked(true);
        QaNokLinearLayout.setVisibility(View.GONE);
        checkedOkRb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                QaNokLinearLayout.setVisibility(View.VISIBLE);
            }
        });
        checkedNokRb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                QaNokLinearLayout.setVisibility(View.GONE);
            }
        });

        //获取历史质检数据
        fetchQARecordData();

        //点击上传质检结果
        installInfoUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DetailToCheckoutActivity.this,ScanQrcodeActivity.class);
                startActivityForResult(intent,SCAN_QRCODE_END);
            }
        });

        //九宫格拍照
        mCheckoutNokPhotosSnpl = findViewById(R.id.checkout_nok_add_photos);
        mCheckoutNokPhotosSnpl.setMaxItemCount(9);
        mCheckoutNokPhotosSnpl.setPlusEnable(true);
        mCheckoutNokPhotosSnpl.setDelegate(this);
    }

    private void fetchQARecordData() {
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecordId", ""+mTaskMachineListData.getId());
        String fetchProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FATCH_TASK_QUALITY_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessQARecordData(fetchProcessRecordUrl, mPostValue, new FetchQaRecordDataHandler());
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
                    //对比更新时间取值
                    for (int update = mQualityRecordList.size() - 2; update >= 0; update--) {
                        if (mQualityRecordList.get(updateTime).getCreateTime() < mQualityRecordList.get(update).getCreateTime()) {
                            Log.d(TAG, "handleMessage: " + mQualityRecordList.get(updateTime).getCreateTime() + " : " + mQualityRecordList.get(update).getCreateTime());
                            updateTime = update;
                        }
                        Log.d(TAG, "handleMessage: updateTime1:" + updateTime);
                    }
                    mQualityRecordDetailsData = mQualityRecordList.get(updateTime);
                    Log.d(TAG, "handleMessage: get Json = "+new Gson().toJson(mQualityRecordDetailsData));
                    if (mQualityRecordDetailsData.getStatus() == SinSimApp.TASK_QUALITY_ABNORMAL) {
                        //加载历史照片
                        checkedNokRb.setChecked(true);
                        checkoutNokDetailEt.setText(mQualityRecordDetailsData.getComment());
                        //加载历史照片地址
                        Log.d(TAG, "handleMessage: photo url: "+mQualityRecordDetailsData.getQualityRecordImage().getImage());
                        ArrayList<String> installPhotoList=new ArrayList<>(Arrays.asList(mQualityRecordDetailsData.getQualityRecordImage().getImage()));
                        mCheckoutNokPhotosSnpl.addMoreData(installPhotoList);
                    } else {
                        checkedOkRb.setChecked(true);
                        checkoutNokDetailEt.setText("");
                    }
                } else {
                    Log.d(TAG, "handleMessage: 尚未质检");
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(DetailToCheckoutActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateQARecordData() {
        ArrayList<String> imageUrlList;
        Gson gson=new Gson();

        //读取和更新输入信息
        if(checkedOkRb.isChecked()){
            mQualityRecordDetailsData.setStatus(PASS);
        } else {
            mQualityRecordDetailsData.setStatus(NO_PASS);
            if(checkoutNokDetailEt.getText()!=null && mCheckoutNokPhotosSnpl.getData().size() > 0){
                //获取质检不合格原因
                mQualityRecordDetailsData.setComment(checkoutNokDetailEt.getText().toString());
                //获取图片本地url
                imageUrlList = mCheckoutNokPhotosSnpl.getData();
                //添加quality_record_image数据库
                QualityRecordImageAddData qualityRecordImageAddData = new QualityRecordImageAddData();
                //获取当前时间
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                Date curDate = new Date(System.currentTimeMillis());
                String strCurTima = formatter.format(curDate);
                //更新当前时间
                qualityRecordImageAddData.setCreateTime(strCurTima);
                qualityRecordImageAddData.setTaskQualityRecordId(mQualityRecordDetailsData.getId());
                //更新质检不合格照片
                String imageJson = gson.toJson(qualityRecordImageAddData);
                Log.d(TAG, "updateQARecordData: "+imageJson);
                String uploadQualityRecordImageUrl = URL.HTTP_HEAD + IP + URL.UPLOAD_QUALITY_RECORD_IMAGE;
                Network.Instance(SinSimApp.getApp()).uploadTaskRecordImage(uploadQualityRecordImageUrl, imageUrlList, "qualityRecordImage", imageJson, new UploadTaskRecordImageHandler());
            } else {
                Toast.makeText(DetailToCheckoutActivity.this,"请拍照并输入质检不合格原因！",Toast.LENGTH_SHORT).show();
            }
        }
        //上传质检结果
        String mQualityRecordDetailsDataToJson = gson.toJson(mQualityRecordDetailsData);
        Log.d(TAG, "updateQARecordData: mQualityRecordDetailsDataToJson:"+ mQualityRecordDetailsDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("strTaskQualityRecordDetail", mQualityRecordDetailsDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + IP + URL.UPDATE_TASK_QUALITY_RECORD_DETAIL;
        Log.d(TAG, "updateQARecordData: "+updateProcessRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, new UpdateProcessDetailDataHandler());
    }

    @SuppressLint("HandlerLeak")
    private class UploadTaskRecordImageHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                Toast.makeText(DetailToCheckoutActivity.this, "上传图片成功！", Toast.LENGTH_SHORT).show();
                //TODO:是否弹窗
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToCheckoutActivity.this, "上传图片失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                Toast.makeText(DetailToCheckoutActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
                installInfoUpdateButton.setEnabled(false);
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToCheckoutActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
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
        mCheckoutNokPhotosSnpl.removeItem(position);
    }

    @Override
    public void onClickNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        Intent photoPickerPreviewIntent = new BGAPhotoPickerPreviewActivity.IntentBuilder(DetailToCheckoutActivity.this)
                .previewPhotos(models) // 当前预览的图片路径集合
                .selectedPhotos(models) // 当前已选中的图片路径集合
                .maxChooseCount(mCheckoutNokPhotosSnpl.getMaxItemCount()) // 图片选择张数的最大值
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
                .maxChooseCount(mCheckoutNokPhotosSnpl.getMaxItemCount() - mCheckoutNokPhotosSnpl.getItemCount()) // 图片选择张数的最大值
                .selectedPhotos(null) // 当前已选中的图片路径集合
                .pauseOnScroll(false) // 滚动列表时是否暂停加载图片
                .build();
        startActivityForResult(photoPickerIntent, RC_CHECKOUT_CHOOSE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SCAN_QRCODE_END:
                if(resultCode == RESULT_OK) {
                    //检验二维码信息是否对应
                    String mMachineStrId = data.getStringExtra("mMachineStrId");
                    if(mMachineStrId.equals(mTaskMachineListData.getMachineData().getMachineStrId())){
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
                    mCheckoutNokPhotosSnpl.addMoreData(BGAPhotoPickerActivity.getSelectedPhotos(data));
                } else {
                    Log.d(TAG, "onActivityResult: choose  nothing");
                }
                break;
            case RC_CHECKOUT_PHOTO_PREVIEW:
                mCheckoutNokPhotosSnpl.setData(BGAPhotoPickerPreviewActivity.getSelectedPhotos(data));
            default:
                break;
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
}
