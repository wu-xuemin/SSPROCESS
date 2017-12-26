package com.example.nan.ssprocess.bean.basic;

import com.example.nan.ssprocess.bean.basic.QualityRecordImageData;
import com.example.nan.ssprocess.bean.basic.TaskRecordData;

/**
 * Created by nan on 2017/12/22.
 */

public class QualityRecordDetailsData {
    private String comment;
    private Long createTime;
    private int id;
    private String name;
    private QualityRecordImageData qualityRecordImage;
    private int status;
    private TaskRecordData taskRecord;
    private int taskRecordId;

    public String getComment() {
        return comment;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public QualityRecordImageData getQualityRecordImage() {
        return qualityRecordImage;
    }

    public int getStatus() {
        return status;
    }

    public TaskRecordData getTaskRecord() {
        return taskRecord;
    }

    public int getTaskRecordId() {
        return taskRecordId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
