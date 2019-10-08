package com.example.nan.ssprocess.bean.basic;

import java.io.Serializable;

/**
 * Created by nan on 2018/1/4.
 */

public class TaskPlanData implements Serializable {
    private String createTime;
    private int id;
    private int planType;
    private long planTime;
    private String deadline;
    private int taskRecordId;
    private String updateTime;
    private int userId;

    public String getCreateTime() {
        return createTime;
    }

    public int getId() {
        return id;
    }

    public long getPlanTime() {
        return planTime;
    }

    public int getTaskRecordId() {
        return taskRecordId;
    }

    public String getUpdateTime() {
        return updateTime;
    }

//    public int getUserId() {
//        return userId;
//    }

    public int getPlanType() {
        return planType;
    }

    public String getDeadline() {
        return deadline;
    }
}
