package com.example.nan.ssprocess.activity;

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.bean.response.ResponseData;
import com.example.nan.ssprocess.bean.basic.MachineData;
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
/*"machine_id":"", --> machine.machine_id
"location":"",-->machine.location
* */
public class DetailToAdminActivity extends AppCompatActivity implements BGANinePhotoLayout.Delegate {

    private static final String TAG="nlgDetailToAdmin";
    private ResponseData mResponseData = new ResponseData();
    private TaskMachineListData taskMachineListData;
    private EditText locationEt;

    private UpdateProcessDetailDataHandler mUpdateProcessDetailDataHandler=new UpdateProcessDetailDataHandler();
    private ArrayList<String> installPhotoList;
    private ArrayList<String> checkoutPhotoList;
    private BGANinePhotoLayout mCurrentClickNpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_admin);

        locationEt=findViewById(R.id.location_et);
        TextView orderNumberTv=findViewById(R.id.order_number_tv);
        TextView machineNumberTv=findViewById(R.id.machine_number_tv);
        TextView needleCountTv=findViewById(R.id.needle_count_tv);
        TextView typeTv=findViewById(R.id.type_tv);
        TextView intallListTv=findViewById(R.id.intall_list_tv);


        //获取传递过来的信息
        Intent intent = getIntent();
        taskMachineListData = (TaskMachineListData) intent.getSerializableExtra("taskMachineListData");
        Log.d(TAG, "onItemClick: position :"+taskMachineListData.getMachineData().getLocation());

        //把数据填入相应位置
        orderNumberTv.setText(""+taskMachineListData.getMachineData().getOrderId());
        needleCountTv.setText(""+taskMachineListData.getMachineOrderData().getHeadNum());
        machineNumberTv.setText(taskMachineListData.getMachineData().getMachineId());
        typeTv.setText(taskMachineListData.getMachineOrderData().getMachineType());
        locationEt.setText(taskMachineListData.getMachineData().getLocation());

        //点击返回
        ImageView previousIv = findViewById(R.id.close_machine_detail);
        previousIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //点击上传位置信息
        Button publishButton = findViewById(R.id.update_location_button);
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProcessDetailData();
            }
        });

        //九宫格显示照片
        installPhotoList=new ArrayList<>(Arrays.asList("http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered1.png"));
        BGANinePhotoLayout installNinePhotoLayout = findViewById(R.id.install_abnormal_photos);
        installNinePhotoLayout.setDelegate(this);
        installNinePhotoLayout.setData(installPhotoList);
        checkoutPhotoList=new ArrayList<>(Arrays.asList("http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered11.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered12.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered13.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered14.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered15.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered16.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered17.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered18.png", "http://7xk9dj.com1.z0.glb.clouddn.com/refreshlayout/images/staggered19.png"));
        BGANinePhotoLayout checkoutNinePhotoLayout = findViewById(R.id.checkout_nok_photos);
        checkoutNinePhotoLayout.setDelegate(this);
        checkoutNinePhotoLayout.setData(checkoutPhotoList);

    }

    private void updateProcessDetailData() {
        final String ip = SinSimApp.getApp().getServerIP();
//        final String ip = "192.168.0.102:8080";
//        final String account = "sss";
        taskMachineListData.getMachineData().setLocation(locationEt.getText().toString());
        Gson gson=new Gson();
        String machineDataToJson = gson.toJson(taskMachineListData);
        Log.d(TAG, "onItemClick: gson :"+ machineDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("machine", machineDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + ip + URL.UPDATE_MACHINE_LOCATION;
        Log.d(TAG, "updateProcessDetailData: "+updateProcessRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, mUpdateProcessDetailDataHandler);
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
}
