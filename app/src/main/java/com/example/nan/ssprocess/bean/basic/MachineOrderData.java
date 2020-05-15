package com.example.nan.ssprocess.bean.basic;

import java.io.Serializable;

/**
 * Created by nan on 2017/12/18.
 */

public class MachineOrderData implements Serializable {
    private int id;
    private String orderNum;
    private String headNum;
    private String needleNum;
    private int machineType;//类型
    private String contractShipDate;//合同日期
    private long planShipDate;//计划日期

    public int getMachineType() {
        return machineType;
    }

    public String getContractShipDate() {
        return contractShipDate;
    }

    public long getPlanShipDate() {
        return planShipDate;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public int getId() {
        return id;
    }

    public String getHeadNum() {
        return headNum;
    }

    public String getNeedleNum() {
        return needleNum;
    }
}
