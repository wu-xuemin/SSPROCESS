package com.example.nan.ssprocess.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.service.MyMqttService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author nan 2017/11/27
 */
public class DetailToAdminActivity extends AppCompatActivity {

    private static final String TAG="nlgDetailToAdmin";
    private String mFilePath = null;// 获取SD卡路径
    private static final int START_CAMERA = 10001;
    private ImageView takePhotosImageView;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_admin);

        Button publishButton = findViewById(R.id.publish_button);
        takePhotosImageView=findViewById(R.id.take_photos_iv);


        takePhotosImageView.setOnClickListener(new View.OnClickListener() {
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
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/SINSIM");
        Log.d(TAG, "onCreate: 1"+file);
        if(!file.exists()){
            if(file.mkdir()){
                mFilePath = file.getAbsolutePath();
            }
        }else {
            mFilePath = file.getAbsolutePath();
        }
    }

    // 拍照后存储并显示图片
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 启动系统相机
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        File file  = new File(mFilePath + "/" + format.format(new Date()).toString() + ".png");

        try {
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        file.setWritable(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            photoUri = FileProvider.getUriForFile(this.getApplicationContext(), "com.example.nan.ssprocess.fileprovider", file);
        } else {
            photoUri = Uri.fromFile(file); // 传递路径
        }
        Log.d(TAG, "openCamera: url: "+photoUri);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);// 更改系统默认存储路径
        startActivityForResult(intent, START_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // 如果返回数据
            if (requestCode == START_CAMERA) { // 判断请求码是否为REQUEST_CAMERA,如果是代表是这个页面传过去的，需要进行获取
                Toast.makeText(this, "已保存至以下目录：" + mFilePath, Toast.LENGTH_SHORT).show();
                Bitmap bitmap = null;
                try {
                    //在imageView中显示
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));
                    takePhotosImageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "onActivityResult: error:"+e);
                    e.printStackTrace();
                }
            }
        } else {
            ToastUtils.showLong("请在设置的应用中勾选存储空间权限，再次尝试！");
        }
    }
}
