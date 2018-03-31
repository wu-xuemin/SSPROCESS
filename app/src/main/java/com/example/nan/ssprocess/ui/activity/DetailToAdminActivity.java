package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.AbnormalRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.QualityRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGANinePhotoLayout;

/**
 * @author nan 2017/11/27
 */

public class DetailToAdminActivity extends AppCompatActivity implements BGANinePhotoLayout.Delegate {

    private static final String TAG="nlgDetailToAdmin";
    private TaskRecordMachineListData mTaskRecordMachineListData=new TaskRecordMachineListData();
    private TextView locationTv;
    private TextView abnormalReasonTv;
    private TextView abnormalDetailTv;
    private TextView nokReasonTv;
    private TextView nokDetailTv;
    private LinearLayout qaNokLayout;
    private LinearLayout installAbnormalLayout;
    private AlertDialog mLocationSettingDialog =null;
    private ProgressDialog mUpdatingProcessDialog;

    private BGANinePhotoLayout mCurrentClickNpl;

    private final String IP = SinSimApp.getApp().getServerIP();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_admin);

        //返回前页按钮
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        locationTv=findViewById(R.id.location_tv);
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

        abnormalReasonTv=findViewById(R.id.abnormal_reason_tv);
        abnormalDetailTv=findViewById(R.id.abnormal_detail_tv);
        installAbnormalLayout=findViewById(R.id.abnormal_detail_layout);
        nokReasonTv=findViewById(R.id.nok_reason_tv);
        nokDetailTv=findViewById(R.id.nok_detail_tv);
        qaNokLayout=findViewById(R.id.checked_nok_layout);

        //获取传递过来的信息
        Intent intent = getIntent();
        mTaskRecordMachineListData = (TaskRecordMachineListData) intent.getSerializableExtra("mTaskRecordMachineListData");
        Log.d(TAG, "onCreate: location:"+mTaskRecordMachineListData.getMachineData().getLocation());

        //把数据填入相应位置
        orderNumberTv.setText(""+mTaskRecordMachineListData.getMachineOrderData().getOrderNum());
        currentStatusTv.setText(SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus()));
        machineNumberTv.setText(mTaskRecordMachineListData.getMachineData().getNameplate());
        ///locationTv.setTextColor(Color.BLUE);
        if (mTaskRecordMachineListData.getMachineData().getLocation().isEmpty()){
            locationTv.setText("点击上传");
        }else {
            locationTv.setText(mTaskRecordMachineListData.getMachineData().getLocation());
        }
        locationTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layout = (LinearLayout) View.inflate(DetailToAdminActivity.this, R.layout.dialog_location_seting, null);
                final EditText dialogLocationEt = layout.findViewById(R.id.dialog_location_et);
                mLocationSettingDialog = new AlertDialog.Builder(DetailToAdminActivity.this).create();
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
                            Toast.makeText(DetailToAdminActivity.this,"地址不能为空，请确认后重新输入！",Toast.LENGTH_SHORT).show();
                        }else {
                            locationTv.setText(dialogLocationEt.getText().toString());
                            if( mUpdatingProcessDialog == null) {
                                mUpdatingProcessDialog = new ProgressDialog(DetailToAdminActivity.this);
                                mUpdatingProcessDialog.setCancelable(false);
                                mUpdatingProcessDialog.setCanceledOnTouchOutside(false);
                                mUpdatingProcessDialog.setMessage("上传信息中...");
                            }
                            mUpdatingProcessDialog.show();
                            updateProcessDetailData();
                        }
                    }
                });
                mLocationSettingDialog.show();
            }
        });

        fetchTaskRecordDetailData();
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
                Intent intent=new Intent(DetailToAdminActivity.this,InstallListActivity.class);
                intent.putExtra("mInstallFileList", mInstallFileList);
                startActivity(intent);
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "FetchInstallFileListHandler handleMessage: "+errorMsg);
                Toast.makeText(DetailToAdminActivity.this, "网络错误！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * 获取安装和质检信息
     */
    private void fetchTaskRecordDetailData() {
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecordId", ""+mTaskRecordMachineListData.getId());
        String fetchQaProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FATCH_TASK_QUALITY_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessQARecordData(fetchQaProcessRecordUrl, mPostValue, new FetchQaRecordDataHandler());

        String fetchInstallProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FATCH_INSTALL_ABNORMAL_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessInstallRecordData(fetchInstallProcessRecordUrl, mPostValue, new FetchInstallRecordDataHandler());
    }

    @SuppressLint("HandlerLeak")
    private class FetchQaRecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取质检结果
                if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_QUALITY_ABNORMAL) {
                    ArrayList<QualityRecordDetailsData> mQualityRecordList = (ArrayList<QualityRecordDetailsData>) msg.obj;
                    if (mQualityRecordList.size() > 0) {
                        int updateTime = mQualityRecordList.size() - 1;
                        //根据id取值，因为id是递增的，id越大数据越新
                        for (int update = mQualityRecordList.size() - 2; update >= 0; update--) {
                            if (mQualityRecordList.get(updateTime).getId() < mQualityRecordList.get(update).getId()) {
                                updateTime = update;
                            }
                            Log.d(TAG, "handleMessage: updateTime1:" + updateTime);
                        }
                        QualityRecordDetailsData mQualityRecordDetailsData = mQualityRecordList.get(updateTime);
                        nokReasonTv.setText("不合格");
                        qaNokLayout.setVisibility(View.VISIBLE);
                        nokDetailTv.setText(mQualityRecordDetailsData.getComment());
                        String picsName=mQualityRecordDetailsData.getQualityRecordImage().getImage();
                        picsName=picsName.substring(1,picsName.indexOf("]"));
                        Log.d(TAG, "照片地址："+picsName);
                        if (picsName.isEmpty()) {
                            Log.d(TAG, "质检异常照片: 无拍照地址");
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
                            BGANinePhotoLayout checkoutNinePhotoLayout = findViewById(R.id.checkout_nok_photos);
                            checkoutNinePhotoLayout.setDelegate(DetailToAdminActivity.this);
                            checkoutNinePhotoLayout.setData(checkoutPhotoList);
                        }
                    } else {
                        nokReasonTv.setText("不合格");
                        qaNokLayout.setVisibility(View.GONE);
                    }
                } else if (mTaskRecordMachineListData.getStatus() == SinSimApp.TASK_QUALITY_DONE) {
                    nokReasonTv.setText("合格");
                    qaNokLayout.setVisibility(View.GONE);
                } else {
                    nokReasonTv.setText("暂无");
                    qaNokLayout.setVisibility(View.GONE);
                }
            } else {
                nokReasonTv.setText("暂无");
                qaNokLayout.setVisibility(View.GONE);
                String errorMsg = (String)msg.obj;
                Toast.makeText(DetailToAdminActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class FetchInstallRecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取安装结果
                if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_INSTALL_ABNORMAL) {
                    ArrayList<AbnormalRecordDetailsData> mAbnormalRecordList = (ArrayList<AbnormalRecordDetailsData>) msg.obj;
                    if (mAbnormalRecordList.size() > 0) {
                        int updateTime = mAbnormalRecordList.size() - 1;
                        for (int update = mAbnormalRecordList.size() - 2; update >= 0; update--) {
                            if (mAbnormalRecordList.get(updateTime).getId() < mAbnormalRecordList.get(update).getId()) {
                                updateTime = update;
                            }
                            Log.d(TAG, "handleMessage: updateTime1:" + updateTime);
                        }
                        AbnormalRecordDetailsData mAbnormalRecordDetailsData = mAbnormalRecordList.get(updateTime);
                        //异常，填入异常原因
                        abnormalReasonTv.setText("异常");
                        installAbnormalLayout.setVisibility(View.VISIBLE);
                        abnormalDetailTv.setText(mAbnormalRecordDetailsData.getComment());

                        String picsName=mAbnormalRecordDetailsData.getAbnormalImage().getImage();
                        picsName=picsName.substring(1,picsName.indexOf("]"));
                        Log.d(TAG, "照片地址："+picsName);
                        if (picsName.isEmpty()) {
                            Log.d(TAG, "安装异常照片: 无拍照地址");
                        } else {
                            String[] picName = picsName.split(",");
                            String picUrl;
                            ArrayList<String> installPhotoList = new ArrayList<>();
                            if (picName.length == 1) {
                                picUrl = URL.HTTP_HEAD + IP.substring(0, IP.indexOf(":")) + URL.INSTALL_PIC_DIR + picsName.substring(picsName.lastIndexOf("/"));
                                installPhotoList.add(picUrl);
                            } else {
                                for (String aPicName : picName) {
                                    picUrl = URL.HTTP_HEAD + IP.substring(0, IP.indexOf(":")) + URL.INSTALL_PIC_DIR + aPicName.substring(aPicName.lastIndexOf("/"));
                                    Log.d(TAG, "handleMessage: 异常照片地址：" + picUrl);
                                    installPhotoList.add(picUrl);
                                }
                            }
                            //九宫格显示照片
                            BGANinePhotoLayout installNinePhotoLayout = findViewById(R.id.install_abnormal_photos);
                            installNinePhotoLayout.setDelegate(DetailToAdminActivity.this);
                            installNinePhotoLayout.setData(installPhotoList);
                        }
                    } else {
                        abnormalReasonTv.setText("异常");
                        installAbnormalLayout.setVisibility(View.GONE);
                        Log.d(TAG, "handleMessage: 没有上传安装异常");
                    }
                } else if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_INSTALLED){
                    abnormalReasonTv.setText("正常");
                    installAbnormalLayout.setVisibility(View.GONE);
                }else {
                    abnormalReasonTv.setText("暂无");
                    installAbnormalLayout.setVisibility(View.GONE);
                }
            } else {
                abnormalReasonTv.setText("暂无");
                installAbnormalLayout.setVisibility(View.GONE);
                String errorMsg = (String)msg.obj;
                Toast.makeText(DetailToAdminActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProcessDetailData() {
        //更新loaction状态
        mTaskRecordMachineListData.getMachineData().setLocation(locationTv.getText().toString());
        Gson gson=new Gson();
        String machineDataToJson = gson.toJson(mTaskRecordMachineListData.getMachineData());
        Log.d(TAG, "onItemClick: gson :"+ machineDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("machine", machineDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + IP + URL.UPDATE_MACHINE_LOCATION;
        Log.d(TAG, "updateProcessDetailData: "+updateProcessRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, new UpdateProcessDetailDataHandler());
    }

    @SuppressLint("HandlerLeak")
    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mUpdatingProcessDialog != null && mUpdatingProcessDialog.isShowing()) {
                mUpdatingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                Toast.makeText(DetailToAdminActivity.this, "上传位置成功！", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToAdminActivity.this, "上传失败："+errorMsg, Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        super.onDestroy();
        if(mUpdatingProcessDialog != null) {
            mUpdatingProcessDialog.dismiss();
        }
        if (mLocationSettingDialog !=null) {
            mLocationSettingDialog.dismiss();
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
