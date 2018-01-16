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
import android.widget.TextView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.QualityRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGASortableNinePhotoLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author nan  2017/12/18
 */

public class DetailToCheckoutActivity extends AppCompatActivity implements BGASortableNinePhotoLayout.Delegate{
    private static final String TAG="nlgDetailToCheckout";
    private RadioButton checkedOkRb;
    private RadioButton checkedNokRb;
    private EditText checkoutNokDetailEt;

    private BGASortableNinePhotoLayout mCheckoutNokPhotosSnpl;
    private TaskMachineListData mTaskMachineListData=new TaskMachineListData();
    private ArrayList<QualityRecordDetailsData> mQualityRecordList=new ArrayList<>();
    private QualityRecordDetailsData mQualityRecordDetailsData =new QualityRecordDetailsData();
    private FetchQARecordDataHandler mFetchQARecordDataHandler = new FetchQARecordDataHandler();
    private UpdateProcessDetailDataHandler mUpdateProcessDetailDataHandler=new UpdateProcessDetailDataHandler();

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private final OkHttpClient client = new OkHttpClient();

    private static final int SCAN_QRCODE_END = 0;
    private static final int RC_CHECKOUT_CHOOSE_PHOTO = 3;
    private static final int RC_CHECKOUT_PHOTO_PREVIEW = 4;
    private static final int PASS = 1;
    private static final int NO_PASS = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_checkout);

        EditText locationEt=findViewById(R.id.location_et);
        TextView orderNumberTv=findViewById(R.id.order_number_tv);
        TextView machineNumberTv=findViewById(R.id.machine_number_tv);
        TextView needleCountTv=findViewById(R.id.needle_count_tv);
        TextView typeTv=findViewById(R.id.type_tv);
        TextView intallListTv=findViewById(R.id.intall_list_tv);

        checkedOkRb=findViewById(R.id.checked_ok_rb);
        checkedNokRb=findViewById(R.id.checked_nok_rb);
        checkoutNokDetailEt=findViewById(R.id.checkout_nok_detail_et);

        //获取传递过来的信息
        Intent intent = getIntent();
        mTaskMachineListData = (TaskMachineListData) intent.getSerializableExtra("mTaskMachineListData");
        Log.d(TAG, "onCreate: position :"+mTaskMachineListData.getMachineData().getLocation());

        //把数据填入相应位置
        orderNumberTv.setText(""+mTaskMachineListData.getMachineData().getOrderId());
        needleCountTv.setText(""+mTaskMachineListData.getMachineOrderData().getHeadNum());
        machineNumberTv.setText(mTaskMachineListData.getMachineData().getMachineId());
        typeTv.setText(""+mTaskMachineListData.getMachineOrderData().getMachineType());
        locationEt.setText(mTaskMachineListData.getMachineData().getLocation());
        locationEt.setFocusable(false);
        locationEt.setEnabled(false);

        //获取质检数据
        fetchQARecordData();

        //点击返回
        ImageView previousIv = findViewById(R.id.close_machine_detail);
        previousIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //点击上传质检结果
        Button installInfoUpdateButton = findViewById(R.id.checkout_upload_button);
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
        final String account = SinSimApp.getApp().getAccount();
        final String ip = SinSimApp.getApp().getServerIP();
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecordId", ""+mTaskMachineListData.getId());
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FATCH_TASK_QUALITY_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessQARecordData(fetchProcessRecordUrl, mPostValue, mFetchQARecordDataHandler);
    }

    @SuppressLint("HandlerLeak")
    private class FetchQARecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取质检结果
                mQualityRecordList=(ArrayList<QualityRecordDetailsData>)msg.obj;
                if (mQualityRecordList!=null && !mQualityRecordList.isEmpty()) {
                    int updateTime = mQualityRecordList.size() - 1;
                    //对比mQualityRecordList.get(update).getCreateTime()取值
                    for (int update = mQualityRecordList.size() - 2; update >= 0; update--) {
                        if (mQualityRecordList.get(updateTime).getCreateTime() < mQualityRecordList.get(update).getCreateTime()) {
                            Log.d(TAG, "handleMessage: " + mQualityRecordList.get(updateTime).getCreateTime() + " : " + mQualityRecordList.get(update).getCreateTime());
                            updateTime = update;
                        }
                        Log.d(TAG, "handleMessage: updateTime1:" + updateTime);
                    }
                    mQualityRecordDetailsData = mQualityRecordList.get(updateTime);
                    if (mQualityRecordDetailsData.getStatus() == 0) {
                        checkedNokRb.setChecked(true);
                        checkoutNokDetailEt.setText(mQualityRecordDetailsData.getComment());
                        //TODO:照片地址
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
        final String ip = SinSimApp.getApp().getServerIP();
        ArrayList<String> imageUrl = new ArrayList<>();
        //读取和更新输入信息
        if(checkedOkRb.isChecked()){
            mQualityRecordDetailsData.setStatus(PASS);
        }else if(checkedNokRb.isChecked()){
            mQualityRecordDetailsData.setStatus(NO_PASS);
            if(checkoutNokDetailEt.getText()!=null && mCheckoutNokPhotosSnpl.getData().size() > 0){
                mQualityRecordDetailsData.setComment(checkoutNokDetailEt.getText().toString());
                imageUrl = mCheckoutNokPhotosSnpl.getData();
            } else {
                Toast.makeText(DetailToCheckoutActivity.this,"请拍照并输入质检不合格原因",Toast.LENGTH_SHORT).show();
            }
        }
        //上传检验失败信息
        //mCheckoutNokPhotosSnpl.getData();
        Gson gson=new Gson();
        String mQualityRecordDetailsDataToJson = gson.toJson(mQualityRecordDetailsData);
        Log.d(TAG, "updateQARecordData: gson :"+ mQualityRecordDetailsDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("strTaskQualityRecordDetail", mQualityRecordDetailsDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + ip + URL.UPDATE_TASK_QUALITY_RECORD_DETAIL;
        Log.d(TAG, "updateQARecordData: "+updateProcessRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, mUpdateProcessDetailDataHandler);
    }

    @SuppressLint("HandlerLeak")
    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                Toast.makeText(DetailToCheckoutActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
                //TODO:跳转回list界面
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToCheckoutActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
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
                    // 检验二维码信息是否对应
                    TaskMachineListData taskMachineListDataId=new TaskMachineListData();
                    taskMachineListDataId = (TaskMachineListData) data.getSerializableExtra("mTaskMachineListData");
                    if(taskMachineListDataId.getId()==mTaskMachineListData.getId()){
                        Log.d(TAG, "onActivityResult: id 对应");
                        //update info
                        uploadImg();
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

    private void uploadImg() {
        // mImgUrls为存放图片的url集合
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        Log.d(TAG, "uploadImg: "+mCheckoutNokPhotosSnpl.getData().size());
        for (int i = 0; i <mCheckoutNokPhotosSnpl.getData().size() ; i++) {
            Log.d(TAG, "uploadImg: "+mCheckoutNokPhotosSnpl.getData().get(i));
            File f=new File(mCheckoutNokPhotosSnpl.getData().get(i));
            if (f!=null) {
                builder.addFormDataPart("file1", f.getName(), RequestBody.create(MEDIA_TYPE_PNG, f));
            }
        }
        //添加其它信息
        Log.d(TAG, "uploadImg: qualityRecordImage url : "+new Gson().toJson(mQualityRecordDetailsData.getQualityRecordImage()));
        builder.addFormDataPart("qualityRecordImage",new Gson().toJson(mQualityRecordDetailsData.getQualityRecordImage()));

        String updateImgUrl = URL.HTTP_HEAD + SinSimApp.getApp().getServerIP() + URL.UPLOAD_QUALITY_RECORD_IMAGE;

        MultipartBody requestBody = builder.build();
        //构建请求
        Request request = new Request.Builder()
                .url(updateImgUrl)//地址
                .post(requestBody)//添加请求体
                .build();

        //异步
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println("上传失败:e.getLocalizedMessage() = " + e.getLocalizedMessage());
                Log.d(TAG, "onFailure: 图片上传失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                System.out.println("上传照片成功：response = " + response.body().string());
                Log.d(TAG, "onResponse: 图片上传成功");
            }
        });
    }
}
