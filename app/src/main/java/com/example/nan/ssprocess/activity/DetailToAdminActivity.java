package com.example.nan.ssprocess.activity;

import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.ResponseData;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.example.nan.ssprocess.service.MyMqttService;

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
    private EditText locationEt;

    private UpdateProcessDetailDataHandler mUpdateProcessDetailDataHandler;
    private ArrayList<String> installPhotoList;
    private ArrayList<String> checkoutPhotoList;
    private BGANinePhotoLayout mCurrentClickNpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_admin);

        Button publishButton = findViewById(R.id.publish_button);
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProcessDetailData();
            }
        });

        Intent intent = getIntent();
        // 获取到传递过来的姓名
        String machineID = intent.getStringExtra("machineID");
        // 获取到传递过来的图片
//        Bitmap bitmap = intent.getParcelableExtra("pic");
        Log.d(TAG, "onCreate: machineID "+machineID);

        locationEt=findViewById(R.id.location_et);


        ImageView previousIv = findViewById(R.id.machine_service_detail_back);
        previousIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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
        final String account = SinSimApp.getApp().getAccount();
        final String ip = SinSimApp.getApp().getServerIP();
//        final String ip = "192.168.0.102:8080";
//        final String account = "sss";
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("machine", "{\"createTime\":1512192287000,\"id\":3,\"installedTime\":1512097889000,\"location\":\""+locationEt.getText()+"\",\"machineId\":\"3\",\"nameplate\":\"nnnme\",\"orderId\":4,\"shipTime\":1512616296000,\"status\":2,\"updateTime\":1512011479000}");
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.UPDATE_MACHINE_LOCATION;
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(fetchProcessRecordUrl, mPostValue, mUpdateProcessDetailDataHandler);
    }

    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                mResponseData=(ResponseData)msg.obj;
                Log.d(TAG, "handleMessage: size: "+mResponseData.getMessage());
                Toast.makeText(DetailToAdminActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (String)msg.obj;
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
