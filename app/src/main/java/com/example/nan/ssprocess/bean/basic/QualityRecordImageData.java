package com.example.nan.ssprocess.bean.basic;

/**
 * Created by nan on 2017/12/22.
 */

public class QualityRecordImageData {
    private String createTime;
    private int id;
    private String image;
    private int taskQualityRecordId;

    public String getCreateTime() {
        return createTime;
    }

    public int getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public int getTaskQualityRecordId() {
        return taskQualityRecordId;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
