package com.example.nan.ssprocess.bean.basic;

/**
 * Created by nan on 2017/12/18.
 */

public class MachineOrderData {
    private int headNum;//头数
    private int machineType;//类型
    private String contractShipDate;//合同日期
    private String planShipDate;//计划日期

    public int getHeadNum() {
        return headNum;
    }

    public int getMachineType() {
        return machineType;
    }

    public String getContractShipDate() {
        return contractShipDate;
    }

    public String getPlanShipDate() {
        return planShipDate;
    }
}
