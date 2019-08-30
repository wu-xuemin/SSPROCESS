package com.example.nan.ssprocess.bean.basic;

import java.io.Serializable;

/**
 * Created by nan on 2017/12/8.
 */

public class TaskRecordMachineListData implements Serializable {
    private int id;//task record id;
    private String taskName;//流程名字
    private int status;//流程状态
    private String installBeginTime;
    private String installEndTime;
    private String leader;
    private int nodeKey;
    private int processRecordId;
    private String qualityBeginTime;
    private String qualityEndTime;
    private MachineData machine;
    private MachineOrderData machineOrder;
    private TaskPlanData taskPlan;
    private TaskData task;
    private String workerList;
    private String cmtFeedback;

    public int getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getStatus() {
        return status;
    }

    public MachineData getMachineData() {
        return machine;
    }

    public MachineOrderData getMachineOrderData() {
        return machineOrder;
    }

    public TaskPlanData getTaskPlan() {
        return taskPlan;
    }

    public String getInstallBeginTime() {
        return installBeginTime;
    }

    public void setInstallBeginTime(String installBeginTime) {
        this.installBeginTime = installBeginTime;
    }

    public String getInstallEndTime() {
        return installEndTime;
    }

    public void setInstallEndTime(String installEndTime) {
        this.installEndTime = installEndTime;
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

    public String getQualityBeginTime() {
        return qualityBeginTime;
    }

    public void setQualityBeginTime(String qualityBeginTime) {
        this.qualityBeginTime = qualityBeginTime;
    }

    public String getQualityEndTime() {
        return qualityEndTime;
    }

    public void setQualityEndTime(String qualityEndTime) {
        this.qualityEndTime = qualityEndTime;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getWorkerList() {
        return workerList;
    }

    public void setWorkerList(String workerList) {
        this.workerList = workerList;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public TaskData getTaskData() {
        return task;
    }

    public String getCmtFeedback() {
        return cmtFeedback;
    }

    public void setCmtFeedback(String cmtFeedback) {
        this.cmtFeedback = cmtFeedback;
    }
}
