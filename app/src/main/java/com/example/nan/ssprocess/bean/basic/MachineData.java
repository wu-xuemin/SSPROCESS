package com.example.nan.ssprocess.bean.basic;

import java.io.Serializable;

/**
 * Created by nan on 2017/12/18.
 */

public class MachineData implements Serializable{
    private String location;
    private String machineId;
    private int orderId;

    private int status;
    private String createTime;
    private int id;
    private String installedTime;
    private String nameplate;
    private String shipTime;
    private String updateTime;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMachineId() {
        return machineId;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getStatus() {
        return status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public int getId() {
        return id;
    }

    public String getInstalledTime() {
        return installedTime;
    }

    public String getNameplate() {
        return nameplate;
    }

    public String getShipTime() {
        return shipTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

}
