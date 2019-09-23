package com.example.nan.ssprocess.bean.basic;

import java.io.Serializable;

/**
 * Created by nan on 2017/12/22.
 */

public class InstallPlanData implements Serializable {
    private int id;
    private String groupName;
    private String headNum;
    private String location;
    private String nameplate;
    private String orderNum;
    private String type;
    private String cmtSend;


    public int getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getHeadNum() {
        return headNum;
    }

    public String getLocation() {
        return location;
    }

    public String getNameplate() {
        return nameplate;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public String getType() {
        return type;
    }

    public String getCmtSend() {
        return cmtSend;
    }
}
