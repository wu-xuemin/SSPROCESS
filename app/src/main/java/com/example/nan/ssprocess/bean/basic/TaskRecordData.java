package com.example.nan.ssprocess.bean.basic;

/**
 * Created by nan on 2017/12/22.
 */

public class TaskRecordData {
    private String installBeginTime;
    private String installEndTime;
    private int id;
    private String leader;
    private int nodeKey;
    private int processRecordId;
    private int status;//task状态
    private String qualityBeginTime;
    private String qualityEndTime;
    private String taskName;
    private String workerList;

    public String getInstallBeginTime() {
        return installBeginTime;
    }

    public String getInstallEndTime() {
        return installEndTime;
    }

    public int getId() {
        return id;
    }

    public String getLeader() {
        return leader;
    }

    public int getNodeKey() {
        return nodeKey;
    }

    public int getProcessRecordId() {
        return processRecordId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getWorkerList() {
        return workerList;
    }

    public String getQualityBeginTime() {
        return qualityBeginTime;
    }

    public String getQualityEndTime() {
        return qualityEndTime;
    }
}
