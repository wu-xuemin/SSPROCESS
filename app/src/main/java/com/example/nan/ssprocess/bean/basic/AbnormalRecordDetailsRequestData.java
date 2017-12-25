package com.example.nan.ssprocess.bean.basic;

import com.example.nan.ssprocess.bean.basic.AbnormalData;
import com.example.nan.ssprocess.bean.basic.AbnormalImageData;
import com.example.nan.ssprocess.bean.basic.MachineData;
import com.example.nan.ssprocess.bean.basic.TaskRecordData;

/**
 * Created by nan on 2017/12/22.
 */

public class AbnormalRecordDetailsRequestData {
    private AbnormalData abnormal;
    private AbnormalImageData abnormalImage;
    private int abnormalType;
    private String comment;
    private int id;
    private MachineData machine;
    private String solution;
    private int solutionUser;
    private int submitUser;
    private TaskRecordData taskRecord;
    private int taskRecordId;


    public AbnormalImageData getAbnormalImage() {
        return abnormalImage;
    }

    public AbnormalData getAbnormal() {
        return abnormal;
    }

    public int getAbnormalType() {
        return abnormalType;
    }

    public String getComment() {
        return comment;
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
}
