package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.AbnormalRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.QualityRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.bean.response.ResponseData;
import com.example.nan.ssprocess.net.DownloadService;
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
    private TaskMachineListData mTaskMachineListData=new TaskMachineListData();
    private TextView locationTv;
    private TextView abnormalReasonTv;
    private TextView abnormalDetailTv;
    private TextView nokReasonTv;
    private TextView nokDetailTv;
    private LinearLayout qaNokLayout;
    private LinearLayout instalAbnormalLayout;
    private AlertDialog mLocationSettngDialog=null;

    private ArrayList<String> mInstallFileList = new ArrayList<>();
    private ArrayList<AbnormalRecordDetailsData> mAbnormalRecordList = new ArrayList<>();
    private AbnormalRecordDetailsData mAbnormalRecordDetailsData=new AbnormalRecordDetailsData();

    private ArrayList<QualityRecordDetailsData> mQualityRecordList=new ArrayList<>();
    private QualityRecordDetailsData mQualityRecordDetailsData =new QualityRecordDetailsData();

    private ArrayList<String> installPhotoList;
    private ArrayList<String> checkoutPhotoList;
    private BGANinePhotoLayout mCurrentClickNpl;

    private final String ip = SinSimApp.getApp().getServerIP();

    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }
    };

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
        TextView intallListTv=findViewById(R.id.intall_list_tv);

        Intent intent1 = new Intent(this, DownloadService.class);
        startService(intent1); // 启动服务
        bindService(intent1, connection, BIND_AUTO_CREATE); // 绑定服务
        //点击下载装车单
        intallListTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //下载装车单
                fetchDownloadListData();
            }
        });

        abnormalReasonTv=findViewById(R.id.abnormal_reason_tv);
        abnormalDetailTv=findViewById(R.id.abnormal_detail_tv);
        instalAbnormalLayout=findViewById(R.id.abnormal_detail_layout);
        nokReasonTv=findViewById(R.id.nok_reason_tv);
        nokDetailTv=findViewById(R.id.nok_detail_tv);
        qaNokLayout=findViewById(R.id.checked_nok_layout);

        //获取传递过来的信息
        Intent intent = getIntent();
        mTaskMachineListData = (TaskMachineListData) intent.getSerializableExtra("mTaskMachineListData");
        Log.d(TAG, "onCreate: localtion:"+mTaskMachineListData.getMachineData().getLocation());

        //把数据填入相应位置
        orderNumberTv.setText(""+mTaskMachineListData.getMachineData().getOrderId());
        currentStatusTv.setText(SinSimApp.getInstallStatusString(mTaskMachineListData.getStatus()));
        machineNumberTv.setText(mTaskMachineListData.getMachineData().getMachineStrId());
        locationTv.setTextColor(Color.BLUE);
        if (mTaskMachineListData.getMachineData().getLocation().isEmpty()){
            locationTv.setText("点击上传位置");
        }else {
            locationTv.setText(mTaskMachineListData.getMachineData().getLocation());
        }
        locationTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layout = (LinearLayout) View.inflate(DetailToAdminActivity.this, R.layout.dialog_location_seting, null);
                final EditText dialogLocationEt = layout.findViewById(R.id.dialog_location_et);
                mLocationSettngDialog = new AlertDialog.Builder(DetailToAdminActivity.this).create();
                mLocationSettngDialog.setTitle("输入机器的位置：");
                mLocationSettngDialog.setView(layout);
                mLocationSettngDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                mLocationSettngDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //获取dialog的输入信息，并上传到服务器
                        if (TextUtils.isEmpty(dialogLocationEt.getText())) {
                            Toast.makeText(DetailToAdminActivity.this,"地址不能为空，请确认后重新输入！",Toast.LENGTH_SHORT).show();
                        }else {
                            locationTv.setText(dialogLocationEt.getText().toString());
                            updateProcessDetailData();
                        }
                    }
                });
                mLocationSettngDialog.show();
            }
        });

        fetchTaskRecordDetailData();
    }

    /**
     * 获取装车单的文件名
     */
    private void fetchDownloadListData() {
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("order_id", ""+mTaskMachineListData.getMachineData().getOrderId());
        String fetchInstallFileListUrl = URL.HTTP_HEAD + ip + URL.FETCH_DOWNLOADING_FILELIST;
        Network.Instance(SinSimApp.getApp()).fetchInstallFileList(fetchInstallFileListUrl, mPostValue, new FetchInstallFileListHandler());
    }

    @SuppressLint("HandlerLeak")
    private class FetchInstallFileListHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                mInstallFileList=(ArrayList<String>)msg.obj;
                for (String installFile:mInstallFileList){
                    String url = URL.HTTP_HEAD + ip + URL.DOWNLOAD_DIR + installFile;
                    //开始下载装车单
                    downloadBinder.startDownload(url);
                }
                Toast.makeText(DetailToAdminActivity.this, "开始下载装车单！", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToAdminActivity.this, "下载失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 获取安装和质检信息
     */
    private void fetchTaskRecordDetailData() {
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecordId", ""+mTaskMachineListData.getId());
        String fetchQaProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FATCH_TASK_QUALITY_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessQARecordData(fetchQaProcessRecordUrl, mPostValue, new FetchQaRecordDataHandler());

        String fetchInstallProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FATCH_INSTALL_ABNORMAL_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessInstallRecordData(fetchInstallProcessRecordUrl, mPostValue, new FetchInstallRecordDataHandler());
    }

    @SuppressLint("HandlerLeak")
    private class FetchQaRecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取质检结果
                mQualityRecordList=(ArrayList<QualityRecordDetailsData>)msg.obj;
                if (mQualityRecordList!=null && !mQualityRecordList.isEmpty()) {
                    int updateTime = mQualityRecordList.size() - 1;
                    //根据CreateTime取值
                    for (int update = mQualityRecordList.size() - 2; update >= 0; update--) {
                        if (mQualityRecordList.get(updateTime).getCreateTime() < mQualityRecordList.get(update).getCreateTime()) {
                            Log.d(TAG, "handleMessage: " + mQualityRecordList.get(updateTime).getCreateTime() + " : " + mQualityRecordList.get(update).getCreateTime());
                            updateTime = update;
                        }
                        Log.d(TAG, "handleMessage: updateTime1:" + updateTime);
                    }
                    mQualityRecordDetailsData = mQualityRecordList.get(updateTime);
                    if (mQualityRecordDetailsData.getStatus() == SinSimApp.TASK_QUALITY_ABNORMAL) {
                        nokReasonTv.setText("不合格");
                        qaNokLayout.setVisibility(View.VISIBLE);
                        nokDetailTv.setText(mQualityRecordDetailsData.getComment());
                        //TODO:照片地址
                        //九宫格显示照片
                        checkoutPhotoList=new ArrayList<>(Arrays.asList("http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered11.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered12.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered13.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered14.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered15.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered16.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered17.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered18.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered19.png"));
                        BGANinePhotoLayout checkoutNinePhotoLayout = findViewById(R.id.checkout_nok_photos);
                        checkoutNinePhotoLayout.setDelegate(DetailToAdminActivity.this);
                        checkoutNinePhotoLayout.setData(checkoutPhotoList);
                    } else if (mQualityRecordDetailsData.getStatus() == SinSimApp.TASK_QUALITY_DONE){
                        nokReasonTv.setText("合格");
                        qaNokLayout.setVisibility(View.GONE);
                    } else {
                        nokReasonTv.setText("暂无");
                        qaNokLayout.setVisibility(View.GONE);
                    }
                } else {
                    qaNokLayout.setVisibility(View.GONE);
                    Toast.makeText(DetailToAdminActivity.this,"尚未质检",Toast.LENGTH_SHORT).show();
                }
            } else {
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
                //获取质检结果
                mAbnormalRecordList=(ArrayList<AbnormalRecordDetailsData>)msg.obj;
                if (mAbnormalRecordList!=null && !mAbnormalRecordList.isEmpty()) {
                    int updateTime = mAbnormalRecordList.size()-1;
                    //对比mQualityRecordList.get(update).getCreateTime()取值
                    for (int update = mAbnormalRecordList.size()-2; update >= 0; update--) {
                        if (mAbnormalRecordList.get(updateTime).getCreateTime() < mAbnormalRecordList.get(update).getCreateTime()) {
                            Log.d(TAG, "handleMessage: " + mAbnormalRecordList.get(update).getCreateTime() + " : " + mAbnormalRecordList.get(update + 1).getCreateTime());
                            updateTime = update;
                        }
                        Log.d(TAG, "handleMessage: updateTime1:" + updateTime);
                    }
                    mAbnormalRecordDetailsData = mAbnormalRecordList.get(updateTime);
                    //异常，填入异常原因
                    if (mAbnormalRecordDetailsData.getTaskRecord().getStatus() == SinSimApp.TASK_INSTALL_ABNORMAL) {
                        abnormalReasonTv.setText("异常");
                        instalAbnormalLayout.setVisibility(View.VISIBLE);
                        abnormalDetailTv.setText(mAbnormalRecordDetailsData.getComment());
                        //TODO:照片地址
                        //九宫格显示照片
                        installPhotoList=new ArrayList<>(Arrays.asList("http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered1.png"));
                        BGANinePhotoLayout installNinePhotoLayout = findViewById(R.id.install_abnormal_photos);
                        installNinePhotoLayout.setDelegate(DetailToAdminActivity.this);
                        installNinePhotoLayout.setData(installPhotoList);
                    } else if (mAbnormalRecordDetailsData.getTaskRecord().getStatus() == SinSimApp.TASK_INSTALLED){
                        abnormalReasonTv.setText("正常");
                        instalAbnormalLayout.setVisibility(View.GONE);
                    } else {
                        abnormalReasonTv.setText("暂无");
                        instalAbnormalLayout.setVisibility(View.GONE);
                    }
                } else {
                    instalAbnormalLayout.setVisibility(View.GONE);
                    Log.d(TAG, "handleMessage: 没有安装异常");
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(DetailToAdminActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProcessDetailData() {
        //更新loaction状态
        mTaskMachineListData.getMachineData().setLocation(locationTv.getText().toString());
        Gson gson=new Gson();
        String machineDataToJson = gson.toJson(mTaskMachineListData.getMachineData());
        Log.d(TAG, "onItemClick: gson :"+ machineDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("machine", machineDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + ip + URL.UPDATE_MACHINE_LOCATION;
        Log.d(TAG, "updateProcessDetailData: "+updateProcessRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, new UpdateProcessDetailDataHandler());
    }

    @SuppressLint("HandlerLeak")
    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                Toast.makeText(DetailToAdminActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToAdminActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
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
        if (mLocationSettngDialog!=null) {
            mLocationSettngDialog.dismiss();
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
