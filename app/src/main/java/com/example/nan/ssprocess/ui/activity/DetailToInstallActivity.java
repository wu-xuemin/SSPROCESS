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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.example.nan.ssprocess.bean.basic.AbnormalData;
import com.example.nan.ssprocess.bean.basic.AbnormalRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.QualityRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;
import com.example.nan.ssprocess.bean.basic.UserData;
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
    private TextView chooseInstallerTv;
    private LinearLayout installAbnormalLayout;
    private LinearLayout qaNokLayout;

    private ProgressDialog mUploadingProcessDialog;
    private AlertDialog mInstallDialog=null;
    private AlertDialog mInstallerListDialog=null;
    private ProgressDialog mUpdatingProcessDialog;

    private TaskRecordMachineListData mTaskRecordMachineListData;
    private int iTaskRecordMachineListDataStatusTemp;

    private BGASortableNinePhotoLayout mInstallAbnormalPhotosSnpl;
    private BGANinePhotoLayout mCurrentClickNpl;

    private UpdateProcessDetailDataHandler mUpdateProcessDetailDataHandler=new UpdateProcessDetailDataHandler();

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<AbnormalData> mAbnormalTypeList;
    private ArrayList<Integer> checkedNameList = new ArrayList<>();
    private String checkedName;

    private final String IP = SinSimApp.getApp().getServerIP();
    private static final int SCAN_QRCODE_START = 1;
    private static final int SCAN_QRCODE_END = 0;
    private static final int RC_INSTALL_CHOOSE_PHOTO = 3;
    private static final int RC_INSTALL_PHOTO_PREVIEW = 4;

//    private String[] items={};


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
        chooseInstallerTv=findViewById(R.id.choose_installer_tv);
        installAbnormalLayout=findViewById(R.id.install_abnormal_ll);
        //质检信息默认隐藏，只有在质检异常的状态显示
        qaNokLayout=findViewById(R.id.checked_nok_layout);
        qaNokLayout.setVisibility(View.GONE);

        //获取传递过来的信息
        Intent intent = getIntent();
        mTaskRecordMachineListData = (TaskRecordMachineListData) intent.getSerializableExtra("mTaskRecordMachineListData");
        Log.d(TAG, "onCreate: position :"+mTaskRecordMachineListData.getMachineData().getLocation());

        //把数据填入相应位置
        orderNumberTv.setText(""+mTaskRecordMachineListData.getMachineOrderData().getOrderNum());
        currentStatusTv.setText(SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus()));
        machineNumberTv.setText(mTaskRecordMachineListData.getMachineData().getNameplate());
        locationTv.setText(mTaskRecordMachineListData.getMachineData().getLocation());
        if(mTaskRecordMachineListData.getWorkerList() == null || "".equals(mTaskRecordMachineListData.getWorkerList())){
            Log.d(TAG, "没有安装人员");
        }else {
            chooseInstallerTv.setText(mTaskRecordMachineListData.getWorkerList());
        }

        //点击下载装车单
        installListTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDownloadListData();
            }
        });

        //点击选择安装工人
        chooseInstallerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTaskRecordMachineListData.getStatus() == SinSimApp.TASK_INSTALLING) {
                    fetchInstallerListData();
                } else {
                    Toast toast =Toast.makeText(DetailToInstallActivity.this, "请在安装结束前进行安装人员选择！", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
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
        if (mTaskRecordMachineListData.getMachineData().getStatus()==SinSimApp.MACHINE_CHANGED
                ||mTaskRecordMachineListData.getMachineData().getStatus()==SinSimApp.MACHINE_SPLITED
                ||mTaskRecordMachineListData.getMachineData().getStatus()==SinSimApp.MACHINE_CANCELED) {
            begainInstallButton.setVisibility(View.GONE);
            installInfoUpdateButton.setVisibility(View.GONE);
            Toast toast = Toast.makeText(DetailToInstallActivity.this, "正在改单/拆单，不能安装！", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }else {
            if (mTaskRecordMachineListData.getStatus() == SinSimApp.TASK_INSTALL_WAITING) {
                begainInstallButton.setVisibility(View.VISIBLE);
                installInfoUpdateButton.setVisibility(View.GONE);
                installAbnormalRb.setEnabled(false);
                installNormalRb.setEnabled(false);
            } else if (mTaskRecordMachineListData.getStatus() == SinSimApp.TASK_INSTALLING) {
                begainInstallButton.setVisibility(View.GONE);
                installInfoUpdateButton.setVisibility(View.VISIBLE);
                installAbnormalRb.setEnabled(true);
                installNormalRb.setEnabled(true);
                //异常解决返回安装中的状态时，不从数据库获取信息
                chooseInstallerTv.setText("选择人员");
            } else {
                begainInstallButton.setVisibility(View.GONE);
                installInfoUpdateButton.setVisibility(View.GONE);
                installAbnormalRb.setEnabled(false);
                installNormalRb.setEnabled(true);
            }
        }

        fetchInstallRecordData();

        //九宫格拍照
        mInstallAbnormalPhotosSnpl = findViewById(R.id.install_abnormal_add_photos);
        mInstallAbnormalPhotosSnpl.setMaxItemCount(3);
        mInstallAbnormalPhotosSnpl.setPlusEnable(true);
        mInstallAbnormalPhotosSnpl.setDelegate(this);

    }

    public void onStartInstall(View view) {
        if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_INSTALL_WAITING) {
            Intent intent = new Intent(DetailToInstallActivity.this, ScanQrcodeActivity.class);
            startActivityForResult(intent, SCAN_QRCODE_START);
        }else{
            Toast.makeText(DetailToInstallActivity.this, "正在 "+SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus())+" ，不能开始安装！", Toast.LENGTH_SHORT).show();
        }
    }

    public void onStopInstall(View view) {
        if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_INSTALLING) {
            Intent intent = new Intent(DetailToInstallActivity.this, ScanQrcodeActivity.class);
            startActivityForResult(intent, SCAN_QRCODE_END);
        }else {
            Toast.makeText(DetailToInstallActivity.this, "正在 "+SinSimApp.getInstallStatusString(mTaskRecordMachineListData.getStatus())+" ，不能结束安装！", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 根据taskRecordId获取当前abnormal信息
     */
    private void fetchInstallRecordData() {
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecordId", ""+mTaskRecordMachineListData.getId());

        String fetchAbnormalTypeUrl = URL.HTTP_HEAD + IP + URL.FATCH_INSTALL_ABNORMAL_TYPE_LIST;
        Network.Instance(SinSimApp.getApp()).fetchAbnormalTypeList(fetchAbnormalTypeUrl, mPostValue, new FetchAbnormalTypeListHandler());

        String fetchProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FATCH_INSTALL_ABNORMAL_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessInstallRecordData(fetchProcessRecordUrl, mPostValue, new FetchInstallRecordDataHandler());

        String fetchQaProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FATCH_TASK_QUALITY_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessQARecordData(fetchQaProcessRecordUrl, mPostValue, new FetchQaRecordDataHandler());
    }

    @SuppressLint("HandlerLeak")
    private class FetchAbnormalTypeListHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取异常类型
                mAbnormalTypeList = (ArrayList<AbnormalData>) msg.obj;
                if (mAbnormalTypeList.size() > 0) {
                    //数据
                    List<String> dataList = new ArrayList<String>();
                    for (int i = 0; i < mAbnormalTypeList.size(); i++) {
                        dataList.add(mAbnormalTypeList.get(i).getAbnormalName());
                    }
                    //适配器
                    arrayAdapter = new ArrayAdapter<String>(DetailToInstallActivity.this, android.R.layout.simple_spinner_item, dataList);
                    //设置样式
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //加载适配器
                    failReasonSpinner.setAdapter(arrayAdapter);
                }else {
                    Log.d(TAG, "获取异常类型为空");
                }
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(DetailToInstallActivity.this, "获取异常类型失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class FetchInstallRecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取安装结果
                ArrayList<AbnormalRecordDetailsData> mAbnormalRecordList = (ArrayList<AbnormalRecordDetailsData>) msg.obj;
                Log.d(TAG, "安装异常信息: "+new Gson().toJson(mAbnormalRecordList));
                Log.d(TAG, "安装异常: mAbnormalRecordList.size:"+mAbnormalRecordList.size());
                if (mAbnormalRecordList.size()>0) {
                    int updateTime = mAbnormalRecordList.size() - 1;
                    for (int update = mAbnormalRecordList.size() - 2; update >= 0; update--) {
                        if (mAbnormalRecordList.get(updateTime).getId() < mAbnormalRecordList.get(update).getId()) {
                            Log.d(TAG, "安装异常: " + mAbnormalRecordList.get(update).getCreateTime() + " : " + mAbnormalRecordList.get(update + 1).getCreateTime());
                            updateTime = update;
                        }
                        Log.d(TAG, "安装异常: updateTime:" + updateTime);
                    }
                    AbnormalRecordDetailsData abnormalRecordDetailsData = mAbnormalRecordList.get(updateTime);
                    Log.d(TAG, "安装异常信息: "+new Gson().toJson(abnormalRecordDetailsData));
                    //如果安装异常，填入异常的原因
                    Log.d(TAG, "安装异常: 流程："+abnormalRecordDetailsData.getTaskRecord().getStatus()+" 异常类型："+abnormalRecordDetailsData.getAbnormalType());
                    if (mTaskRecordMachineListData.getStatus()  == SinSimApp.TASK_INSTALL_ABNORMAL) {
                        installAbnormalRb.setChecked(true);
                        installAbnormalRb.setEnabled(true);
                        installNormalRb.setEnabled(false);
                        if(arrayAdapter.isEmpty()){
                            Log.d(TAG, "安装异常信息: 空");
                        }else {
                            //根据该选项获取位置
                            int position = arrayAdapter.getPosition(abnormalRecordDetailsData.getAbnormal().getAbnormalName());
                            failReasonSpinner.setSelection(position);
                        }
                        failReasonSpinner.setEnabled(false);
                        installAbnormalDetailEt.setText(abnormalRecordDetailsData.getComment());
                        installAbnormalDetailEt.setFocusable(false);
                        installAbnormalDetailEt.setFocusableInTouchMode(false);
                        //加载历史照片地址
                        String picsName=abnormalRecordDetailsData.getAbnormalImage().getImage();
                        picsName=picsName.substring(1,picsName.indexOf("]"));
                        Log.d(TAG, "照片地址："+picsName);
                        if (picsName.isEmpty()) {
                            Log.d(TAG, "安装异常照片: 无拍照地址");
                        } else {
                            String[] picName = picsName.split(",");
                            String picUrl;
                            ArrayList<String> installPhotoList = new ArrayList<>();
                            Log.d(TAG, "安装异常: pic长度：" + picName.length);
                            if (picName.length == 1) {
                                picUrl = URL.HTTP_HEAD + IP.substring(0, IP.indexOf(":")) + URL.INSTALL_PIC_DIR + picsName.substring(picsName.lastIndexOf("/"));
                                installPhotoList.add(picUrl);
                            } else {
                                for (String aPicName : picName) {
                                    picUrl = URL.HTTP_HEAD + IP.substring(0, IP.indexOf(":")) + URL.INSTALL_PIC_DIR + aPicName.substring(aPicName.lastIndexOf("/"));
                                    Log.d(TAG, "安装异常: 异常照片地址：" + picUrl);
                                    installPhotoList.add(picUrl);
                                }
                            }
                            mInstallAbnormalPhotosSnpl.addMoreData(installPhotoList);
                            mInstallAbnormalPhotosSnpl.setEditable(false);
                        }
                    } else {
                        installNormalRb.setChecked(true);
                        installAbnormalDetailEt.setText("");
                    }
                } else {
                    Log.d(TAG, "安装异常: 没有安装异常的信息");
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
                if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_QUALITY_ABNORMAL) {
                    ArrayList<QualityRecordDetailsData> mQualityRecordList = (ArrayList<QualityRecordDetailsData>) msg.obj;
                    if (mQualityRecordList.size() > 0) {
                        int updateTime = mQualityRecordList.size() - 1;
                        //根据id取值
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
                            Log.d(TAG, "质检异常照片: 无");
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
                            checkoutNinePhotoLayout.setDelegate(DetailToInstallActivity.this);
                            checkoutNinePhotoLayout.setData(checkoutPhotoList);
                        }
                    } else {
                        nokReasonTv.setText("不合格");
                        qaNokLayout.setVisibility(View.GONE);
                    }
                }else if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_QUALITY_DONE){
                    nokReasonTv.setText("合格");
                    qaNokLayout.setVisibility(View.GONE);
                }else {
                    nokReasonTv.setText("暂无");
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
     /**
     * 获取安装组员名单
     */
    private void fetchInstallerListData() {
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("id", ""+SinSimApp.getApp().getUserId());
        String fetchInstallerListUrl = URL.HTTP_HEAD + IP + URL.FATCH_GROUP_BY_USERID;
        Network.Instance(SinSimApp.getApp()).fetchInstallerList(fetchInstallerListUrl, mPostValue, new FetchInstallerListHandler());
    }

    @SuppressLint("HandlerLeak")
    private class FetchInstallerListHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                ArrayList<UserData> mInstallerList = (ArrayList<UserData>) msg.obj;
                Log.d(TAG, "安装组人数: "+mInstallerList.size());
                String[] items={};
                boolean[] checkedItemsArray = new boolean[mInstallerList.size()];
                for (int i=0;i<mInstallerList.size();i++){
                    items = Arrays.copyOf(items, items.length+1);
                    items[items.length-1] = mInstallerList.get(i).getName();
                    //初始化是否选择的boolean数组
                    checkedItemsArray[i] = false;
                }
                for (int i = 0; i < checkedNameList.size(); i++) {
                    for (int j = 0; j < checkedItemsArray.length; j++) {
                        if(checkedNameList.get(i) == j) {
                            checkedItemsArray[j] = true;
                        }
                    }
                }

                // 创建一个AlertDialog建造者
                AlertDialog.Builder alertDialogBuilder= new AlertDialog.Builder(DetailToInstallActivity.this);
                // 设置标题
                alertDialogBuilder.setTitle("安装人员：");
                // 参数介绍
                // 第一个参数：弹出框的信息集合，一般为字符串集合
                // 第二个参数：被默认选中的，一个布尔类型的数组
                // 第三个参数：勾选事件监听
                final String[] finalItems = items;

                alertDialogBuilder.setMultiChoiceItems(items, checkedItemsArray, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        // dialog：不常使用，弹出框接口
                        // which：勾选或取消的是第几个
                        // isChecked：是否勾选
                        if (isChecked) {
                            // 选中
                            Log.d(TAG, "onClick: "+which);
                            checkedNameList.add(which);
                        }else {
                            // 取消选中
                            for (int i=0;i<checkedNameList.size();i++){
                                if (which==checkedNameList.get(i)){
                                    checkedNameList.remove(i);
                                    break;
                                }
                            }
                        }

                    }
                });
                alertDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        checkedName="";
                        for (int i=0;i<checkedNameList.size();i++){
                            if(i != checkedNameList.size()-1) {
                                checkedName += finalItems[checkedNameList.get(i)]+", ";
                            } else {
                                checkedName += finalItems[checkedNameList.get(i)];
                            }
                        }
                        chooseInstallerTv.setText(checkedName);
                        // 关闭提示框
                        mInstallerListDialog.dismiss();
                    }
                });
                alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // 关闭提示框
                        mInstallerListDialog.dismiss();
                    }
                });
                mInstallerListDialog = alertDialogBuilder.create();
                mInstallerListDialog.show();

        } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "FetchInstalerListHandler handleMessage: "+errorMsg);
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
                    String mMachineNamePlate = data.getStringExtra("mMachineNamePlate");

                    if(mMachineNamePlate.equals(mTaskRecordMachineListData.getMachineData().getNameplate())){
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
                    String mMachineNamePlate = data.getStringExtra("mMachineNamePlate");

                    if(mMachineNamePlate.equals(mTaskRecordMachineListData.getMachineData().getNameplate())){
                        Log.d(TAG, "onActivityResult: id 对应");
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

                if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_INSTALL_WAITING){
                    begainInstallButton.setVisibility(View.VISIBLE);
                    installInfoUpdateButton.setVisibility(View.GONE);
                    installAbnormalRb.setEnabled(false);
                    installNormalRb.setEnabled(false);
                } else if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_INSTALLING){
                    begainInstallButton.setVisibility(View.GONE);
                    installInfoUpdateButton.setVisibility(View.VISIBLE);
                    installAbnormalRb.setEnabled(true);
                    installNormalRb.setEnabled(true);
                } else if (mTaskRecordMachineListData.getStatus()==SinSimApp.TASK_INSTALLED){
                    Toast.makeText(DetailToInstallActivity.this, "该工序已安装完成！", Toast.LENGTH_SHORT).show();
                    DetailToInstallActivity.this.finish();
                } else {
                    begainInstallButton.setVisibility(View.GONE);
                    installInfoUpdateButton.setVisibility(View.GONE);
                }
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
        mTaskRecordMachineListData.setLeader(SinSimApp.getApp().getFullName());
        if ("".equals(checkedName) || checkedName==null){
            Toast toast = Toast.makeText(DetailToInstallActivity.this, "请勾选安装人员！", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }else {
            mTaskRecordMachineListData.setWorkerList(checkedName);
        }

        //读取和更新输入信息
        if(installNormalRb.isChecked()){
            mTaskRecordMachineListData.setInstallEndTime(strCurTime);
            iTaskRecordMachineListDataStatusTemp=mTaskRecordMachineListData.getStatus();
            if (mTaskRecordMachineListData.getTaskData().getQualityUserId()>0) {
                updateProcessDetailData(SinSimApp.TASK_INSTALLED);
            } else {
                mTaskRecordMachineListData.setQualityBeginTime(strCurTime);
                mTaskRecordMachineListData.setQualityEndTime(strCurTime);
                updateProcessDetailData(SinSimApp.TASK_QUALITY_DONE);
            }
        }else if(installAbnormalRb.isChecked()){
            AbnormalRecordDetailsData abnormalRecordAddData=new AbnormalRecordDetailsData(SinSimApp.getApp().getUserId(),mTaskRecordMachineListData.getId(),lCurTime);
            String bnormalRecordDetailsDataToJson = gson.toJson(abnormalRecordAddData);
            Log.d(TAG, "updateInstallRecordData: gson :"+ bnormalRecordDetailsDataToJson);
            if(installAbnormalDetailEt.getText()!=null && mInstallAbnormalPhotosSnpl.getData().size()>0){
                //获取安装异常的原因
                abnormalRecordAddData.setComment(installAbnormalDetailEt.getText().toString());
                //异常类型的获取
                for (int i=0;i<mAbnormalTypeList.size();i++){
                    if (failReasonSpinner.getSelectedItem().equals(mAbnormalTypeList.get(i).getAbnormalName())){
                        abnormalRecordAddData.setAbnormalType(mAbnormalTypeList.get(i).getId());
                    }
                }
                //上传安装结果
                String nAbnormalRecordDetailsDataToJson = gson.toJson(abnormalRecordAddData);
                Log.d(TAG, "updateInstallRecordData: abnormalRecord : "+ nAbnormalRecordDetailsDataToJson);
                LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
                mPostValue.put("abnormalRecord", nAbnormalRecordDetailsDataToJson);

                iTaskRecordMachineListDataStatusTemp=mTaskRecordMachineListData.getStatus();
                mTaskRecordMachineListData.setStatus(SinSimApp.TASK_INSTALL_ABNORMAL);
                String taskRecordDataToJson =gson.toJson(mTaskRecordMachineListData);
                Log.d(TAG, "updateInstallRecordData: taskRecord: "+taskRecordDataToJson);
                mPostValue.put("taskRecord", taskRecordDataToJson);

                //获取图片本地url
                ArrayList<String> imageUrlList = mInstallAbnormalPhotosSnpl.getData();
                Log.d(TAG, "异常图片: "+imageUrlList.size());
                String uploadInstallAbnormalDetailUrl = URL.HTTP_HEAD + IP + URL.UPLOAD_INSTALL_ABNORMAL_DETAIL;
                Network.Instance(SinSimApp.getApp()).uploadTaskRecordImage(uploadInstallAbnormalDetailUrl, imageUrlList, mPostValue, new UploadTaskRecordImageHandler());

            } else {
                Toast.makeText(this, "异常原因和异常照片不能为空！", Toast.LENGTH_SHORT).show();
                return;
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
    private class UploadTaskRecordImageHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if(mUploadingProcessDialog != null && mUploadingProcessDialog.isShowing()) {
                mUploadingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                Toast.makeText(DetailToInstallActivity.this, "异常信息和照片上传成功！", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "handleMessage: 异常信息和照片上传成功！");
                DetailToInstallActivity.this.finish();
            } else {
                mTaskRecordMachineListData.setStatus(iTaskRecordMachineListDataStatusTemp);
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "UploadTaskRecordImageHandler: "+errorMsg);
                Toast.makeText(DetailToInstallActivity.this, "上传失败！请重新上传", Toast.LENGTH_SHORT).show();
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