package com.example.nan.ssprocess.bean;

/**
 * Created by nan on 2017/12/8.
 */

public class TaskRecordDataListContent {
    private int id;
    private String taskName;
    private int status;
    private String beginTime;
    private String endTime;
    public TaskRecordDataListContent(int id,String taskName,int status,String beginTime,String endTime){
        this.id = id;
        this.taskName = taskName;
        this.status = status;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getStatus() {
        return status;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
