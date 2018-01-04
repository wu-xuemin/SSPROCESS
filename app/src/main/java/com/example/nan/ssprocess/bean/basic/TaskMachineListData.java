package com.example.nan.ssprocess.bean.basic;

import java.io.Serializable;

/**
 * Created by nan on 2017/12/8.
 */

public class TaskMachineListData implements Serializable {
    private int id;//task record id;
    private MachineData machine;
    private String taskName;//流程名字
    private int status;//流程状态
    private MachineOrderData machineOrder;
    private TaskPlanData taskPlan;

    public int getId() {
        return id;
    }

    public MachineData getMachineData() {
        return machine;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getStatus() {
        return status;
    }

    public MachineOrderData getMachineOrderData() {
        return machineOrder;
    }

    public TaskPlanData getTaskPlan() {
        return taskPlan;
    }
}
