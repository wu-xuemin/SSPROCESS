package com.example.nan.ssprocess.bean.basic;

import java.io.Serializable;

/**
 * Created by nan on 2017/12/8.
 * 新QA的数据也在这里
 */

public class TaskRecordMachineListData implements Serializable {
    private int id;//task record id;
    private String taskName;//流程名字
    private int status;//流程状态
    private String recordStatus; //质检状态，3期新质检
    private String recordRemark; //对该条质检的备注，3期新质检
    private String reInspect;   //复检结果，3期新质检
    private String inspectPerson;   //质检人，3期新质检
    private String updateTime;   //质检更新时间，3期新质检

    private String inspectName;
    // 在 qualityInspect里已经包含了inspectContent，不需要了; 而虽然也包含了inspectName，但是inspectName是在QualityInspectRecord里就存在的。
//    private String inspectContent;

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getInspectPerson() {
        return inspectPerson;
    }

    public void setInspectPerson(String inspectPerson) {
        this.inspectPerson = inspectPerson;
    }

    public String getInspectName() {
        return inspectName;
    }

    public void setInspectName(String inspectName) {
        this.inspectName = inspectName;
    }

//    public String getInspectContent() {
//        return inspectContent;
//    }
//
//    public void setInspectContent(String inspectContent) {
//        this.inspectContent = inspectContent;
//    }

    public String getRecordRemark() {
        return recordRemark;
    }

    public void setRecordRemark(String recordRemark) {
        this.recordRemark = recordRemark;
    }

    public String getReInspect() {
        return reInspect;
    }

    public void setReInspect(String reInspect) {
        this.reInspect = reInspect;
    }

    public String getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(String recordStatus) {
        this.recordStatus = recordStatus;
    }

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

//    //新QA的数据也在这里
//    private String inspectName;
//    private String inspectContent;
//
//    public String getInspectName() {
//        return inspectName;
//    }
//
//    public void setInspectName(String inspectName) {
//        this.inspectName = inspectName;
//    }
//
//    public String getInspectContent() {
//        return inspectContent;
//    }
//
//    public void setInspectContent(String inspectContent) {
//        this.inspectContent = inspectContent;
//    }
    private QualityInspectData qualityInspect;

    public QualityInspectData getQualityInspect() {
        return qualityInspect;
    }

    public void setQualityInspect(QualityInspectData qualityInspect) {
        this.qualityInspect = qualityInspect;
    }

    /**
     * 因为 质检组 也要看安装进度信息，所以需要安装进度信息 --暂不需要
     */
//    private TaskRecordData taskRecordData;
//
//    public TaskRecordData getTaskRecordData() {
//        return taskRecordData;
//    }
//
//    public void setTaskRecordData(TaskRecordData taskRecordData) {
//        this.taskRecordData = taskRecordData;
//    }
}
