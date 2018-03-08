package com.example.nan.ssprocess.bean.basic;

import com.example.nan.ssprocess.bean.basic.AbnormalData;
import com.example.nan.ssprocess.bean.basic.AbnormalImageData;
import com.example.nan.ssprocess.bean.basic.MachineData;
import com.example.nan.ssprocess.bean.basic.TaskRecordData;

/**
 * Created by nan on 2017/12/22.
 */

public class AbnormalRecordDetailsData {
    private AbnormalData abnormal;
    private AbnormalImageData abnormalImage;
    private int abnormalType;//故障类型
    private String comment;//故障原因
    private int id;
    private MachineData machine;
    private String solution;
    private int solutionUser;
    private int submitUser;
    private TaskRecordData taskRecord;
    private int taskRecordId;
    private long createTime;//更新时间

    public AbnormalRecordDetailsData(){}

    public AbnormalRecordDetailsData(int submitUser,int taskRecordId,long createTime){
        this.abnormalType=0;
        this.comment="";
        this.id=0;
        this.solution="";
        this.solutionUser=1;
        this.submitUser=submitUser;
        this.taskRecordId=taskRecordId;
        this.createTime=createTime;
    }
    public AbnormalImageData getAbnormalImage() {
        return abnormalImage;
    }

    public AbnormalData getAbnormal() {
        return abnormal;
    }

    public int getAbnormalType() {
        return abnormalType;
    }

    public void setAbnormalType(int abnormalType) {
        this.abnormalType = abnormalType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public MachineData getMachine() {
        return machine;
    }

    public String getSolution() {
        return solution;
    }

    public int getSolutionUser() {
        return solutionUser;
    }

    public int getSubmitUser() {
        return submitUser;
    }

    public TaskRecordData getTaskRecord() {
        return taskRecord;
    }

    public int getTaskRecordId() {
        return taskRecordId;
    }

    public long getCreateTime() {
        return createTime;
    }
}
