package com.example.nan.ssprocess.bean.basic;

/**
 * Created by nan on 2017/12/8.
 */

public class TaskMachineListData {
    private MachineData machineData;
    private String taskName;
    private int status;
    private MachineOrderData machineOrderData;

    public TaskMachineListData(MachineData machineData,String taskName,int status,MachineOrderData machineOrderData){
        this.machineData = machineData;
        this.taskName = taskName;
        this.status = status;
        this.machineOrderData = machineOrderData;
    }

    public MachineData getMachineData() {
        return machineData;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getStatus() {
        return status;
    }

    public MachineOrderData getMachineOrderData() {
        return machineOrderData;
    }

}
