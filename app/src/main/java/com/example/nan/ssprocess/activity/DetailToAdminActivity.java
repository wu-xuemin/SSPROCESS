package com.example.nan.ssprocess.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.service.MyMqttService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author nan 2017/11/27
 */
public class DetailToAdminActivity extends AppCompatActivity {

    private static final String TAG="nlgDetailToAdmin";
    private String mFilePath = null;// 获取SD卡路径
    private static final int START_CAMERA = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_admin);

        ImageButton takePhotosImageButton=findViewById(R.id.take_photos_ib);
        Button publishButton = findViewById(R.id.publish_button);

        takePhotosImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMqttService.publishMessage("done");
            }
        });

        //保存拍照文件的路径
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/CRH");
        Log.d(TAG, "onCreate: 1"+file);
        if(!file.exists()){
            if(file.mkdir()){
                mFilePath = file.getAbsolutePath();
                Log.d(TAG, "onCreate: 2"+mFilePath);
            }
        }else {
            mFilePath = file.getAbsolutePath();
            Log.d(TAG, "onCreate: 3"+mFilePath);
        }
    }

    // 拍照后存储并显示图片
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 启动系统相机
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        File file  = new File(mFilePath + "/" + format.format(new Date()).toString() + ".png");
        file.setWritable(true);
//        Uri photoUri;
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            photoUri = FileProvider.getUriForFile(this.getApplicationContext(), "com.example.nan.ssprocess.fileprovider", file);
//        } else {
//            photoUri = Uri.fromFile(file); // 传递路径
//        }
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);// 更改系统默认存储路径
        startActivityForResult(intent, START_CAMERA);
    }
}
