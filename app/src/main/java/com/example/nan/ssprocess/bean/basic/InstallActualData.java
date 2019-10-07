package com.example.nan.ssprocess.bean.basic;

import java.io.Serializable;

/**
 * Created by nan on 2017/12/22.
 */

public class InstallActualData implements Serializable {
    private int id;
    private int headCountDone;
    private String cmtFeedback;
    private String pcWireNum;
    private String kouxianNum;
    private String lightWireNum;
    private String warnSignalNum;
    private String deviceSignalNum;
    private String warnPowerNum;
    private String devicePowerNum;
    private String deviceBuxiuNum;
    private String deviceSwitchNum;


    public int getId() {
        return id;
    }

    public int getHeadCountDone() {
        return headCountDone;
    }

    public void setHeadCountDone(int headCountDone) {
        this.headCountDone = headCountDone;
    }

    public String getCmtFeedback() {
        return cmtFeedback;
    }

    public void setCmtFeedback(String cmtFeedback) {
        this.cmtFeedback = cmtFeedback;
    }

    public String getPcWireNum() {
        return pcWireNum;
    }

    public void setPcWireNum(String pcWireNum) {
        this.pcWireNum = pcWireNum;
    }

    public String getKouxianNum() {
        return kouxianNum;
    }

    public void setKouxianNum(String kouxianNum) {
        this.kouxianNum = kouxianNum;
    }

    public String getLightWireNum() {
        return lightWireNum;
    }

    public void setLightWireNum(String lightWireNum) {
        this.lightWireNum = lightWireNum;
    }

    public String getWarnSignalNum() {
        return warnSignalNum;
    }

    public void setWarnSignalNum(String warnSignalNum) {
        this.warnSignalNum = warnSignalNum;
    }

    public String getDeviceSignalNum() {
        return deviceSignalNum;
    }

    public void setDeviceSignalNum(String deviceSignalNum) {
        this.deviceSignalNum = deviceSignalNum;
    }

    public String getWarnPowerNum() {
        return warnPowerNum;
    }

    public void setWarnPowerNum(String warnPowerNum) {
        this.warnPowerNum = warnPowerNum;
    }

    public String getDevicePowerNum() {
        return devicePowerNum;
    }

    public void setDevicePowerNum(String devicePowerNum) {
        this.devicePowerNum = devicePowerNum;
    }

    public String getDeviceBuxiuNum() {
        return deviceBuxiuNum;
    }

    public void setDeviceBuxiuNum(String deviceBuxiuNum) {
        this.deviceBuxiuNum = deviceBuxiuNum;
    }

    public String getDeviceSwitchNum() {
        return deviceSwitchNum;
    }

    public void setDeviceSwitchNum(String deviceSwitchNum) {
        this.deviceSwitchNum = deviceSwitchNum;
    }
}
