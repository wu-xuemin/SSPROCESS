package com.example.nan.ssprocess.activity;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.nan.ssprocess.R;

import java.io.File;
import java.util.ArrayList;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGASortableNinePhotoLayout;

/**
 * @author nan  2017/12/18
 */
/**
 *根据传入的strAbnormalRecordDetail，更新对应多表：
 "machine_id":"", --> machine.machine_id
 "安装是否异常":"", --> task_record.status  task状态，“1”==>未开始， “2”==>进行中，“3”==>安装完成， “4”==>质检完成，“5“===>异常
 "异常类型":"",	--> abnormal_record.abnormal_type
 "异常原因":"", --> abnormal_record.comment
 "异常照片":"", -->abnormal_image.image
 "安装完成":"",  -->   task_record.status或machine.status都可以，反正这两个表都更新
 注意：有外键的字段，需要上传实际存在的外键数据。
 一项update失败的情况下，全部update无效(事务OK)
 */
public class DetailToInstallActivity extends AppCompatActivity implements BGASortableNinePhotoLayout.Delegate {

    private static final String TAG="nlgDetailToInstall";

    private static final int RC_INSTALL_CHOOSE_PHOTO = 1;
    private static final int RC_INSTALL_PHOTO_PREVIEW = 2;

    private BGASortableNinePhotoLayout mInstallAbnormalPhotosSnpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_install);
        Button installInfoUpdateButton = findViewById(R.id.install_info_update_button);

        installInfoUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mInstallAbnormalPhotosSnpl = findViewById(R.id.install_abnormal_add_photos);
        mInstallAbnormalPhotosSnpl.setMaxItemCount(9);
        mInstallAbnormalPhotosSnpl.setPlusEnable(true);
        mInstallAbnormalPhotosSnpl.setDelegate(this);

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
        Log.d(TAG, "choicePhotoWrapper: 6");
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
        if (resultCode == RESULT_OK && requestCode == RC_INSTALL_CHOOSE_PHOTO) {
            mInstallAbnormalPhotosSnpl.addMoreData(BGAPhotoPickerActivity.getSelectedPhotos(data));
        } else {
            Log.d(TAG, "onActivityResult: choose nothing");
        }
    }


}