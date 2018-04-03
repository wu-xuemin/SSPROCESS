package com.example.nan.ssprocess.bean.basic;

import com.example.nan.ssprocess.bean.basic.QualityRecordImageData;
import com.example.nan.ssprocess.bean.basic.TaskRecordData;

/**
 * Created by nan on 2017/12/22.
 */

public class QualityRecordDetailsData {
    private String comment;//不合格描述
    private long createTime;
    private long solveTime;
    private int id;
    private String submitUser;
    private QualityRecordImageData qualityRecordImage;
    private TaskRecordData taskRecord;
    private int taskRecordId;
    private String solution;

    public QualityRecordDetailsData(){}

    public QualityRecordDetailsData(String submitUser, int taskRecordId, long createTime){
        this.createTime=createTime;
        this.submitUser=submitUser;
        this.id=0;
        this.taskRecordId=taskRecordId;
    }

    public String getComment() {
        return comment;
    }

    public long getCreateTime() {
        return createTime;
    }

    public int getId() {
        return id;
    }

    public String getSubmitUser() {
        return submitUser;
    }

    public QualityRecordImageData getQualityRecordImage() {
        return qualityRecordImage;
    }

    public TaskRecordData getTaskRecord() {
        return taskRecord;
    }

    public int getTaskRecordId() {
        return taskRecordId;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getSolveTime() {
        return solveTime;
    }

    public String getSolution() {
        return solution;
    }
}
