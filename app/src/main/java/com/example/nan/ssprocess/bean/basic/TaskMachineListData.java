package com.example.nan.ssprocess.bean.basic;

/**
 * Created by nan on 2017/12/8.
 */

public class TaskMachineListData {
    private MachineData machine;
    private String taskName;
    private int status;
    private MachineOrderData machineOrder;

    public TaskMachineListData(MachineData machineData,String taskName,int status,MachineOrderData machineOrderData){
        this.machine = machineData;
        this.taskName = taskName;
        this.status = status;
        this.machineOrder = machineOrderData;
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

}
