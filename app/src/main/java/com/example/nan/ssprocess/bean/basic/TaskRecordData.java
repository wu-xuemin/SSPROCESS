package com.example.nan.ssprocess.bean.basic;

/**
 * Created by nan on 2017/12/22.
 */

public class TaskRecordData {
    private String beginTime;
    private String endTime;
    private int id;
    private String leader;
    private int nodeKey;
    private int processRecordId;
    private int status;
    private String taskName;
    private String workerList;

    public String getBeginTime() {
        return beginTime;
    }

    public String getEndTime() {
        return endTime;
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

    public String getTaskName() {
        return taskName;
    }

    public String getWorkerList() {
        return workerList;
    }

}
