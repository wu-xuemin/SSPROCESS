package com.example.nan.ssprocess.bean;

/**
 * Created by nan on 2017/12/11.
 */

public class TaskRecordDetailDataMachine {
    private TaskRecordDetailDataMachineDetail machine;
    private TaskRecordDetailDataMachineOrderDetail machineOrder;

    public TaskRecordDetailDataMachineDetail getMachine() {
        return machine;
    }

    public TaskRecordDetailDataMachineOrderDetail getMachineOrder() {
        return machineOrder;
    }

    public static class TaskRecordDetailDataMachineDetail{
        private String location;
        private int machineId;
        private int orderId;

        public String getLocation() {
            return location;
        }

        public int getMachineId() {
            return machineId;
        }

        public int getOrderId() {
            return orderId;
        }
    }

    public static class TaskRecordDetailDataMachineOrderDetail{
        private int headNum;
        private int machineType;

        public int getHeadNum() {
            return headNum;
        }

        public int getMachineType() {
            return machineType;
        }
    }
}

