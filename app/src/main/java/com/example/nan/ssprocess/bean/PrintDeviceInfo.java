package com.example.nan.ssprocess.bean;

import android.bluetooth.BluetoothDevice;

/**
 * 打印机信息类
 * @author zhangbin
 * @date 2020-08-13
 */
public class PrintDeviceInfo {
    /**
     * 打印机名称
     */
    private String mDeviceName;
    /**
     * 打印机mac地址
     */
    private String mDeviceAddress;
    /**
     * 打印机状态 未配对/已配对/已连接
     */
    private int mDeviceStatus = BluetoothDevice.BOND_BONDED;

    public PrintDeviceInfo(String deviceName, String deviceAddress, int deviceStatus) {
        mDeviceName = deviceName;
        mDeviceAddress = deviceAddress;
        mDeviceStatus = deviceStatus;
    }


    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        mDeviceAddress = deviceAddress;
    }

    public int getDeviceStatus() {
        return mDeviceStatus;
    }

    public void setDeviceStatus(int deviceStatus) {
        mDeviceStatus = deviceStatus;
    }






}
