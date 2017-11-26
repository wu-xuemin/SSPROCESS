package com.example.nan.ssprocess.bean.basic;

/**
 * Created by Hu Tong on 2017/5/25.
 */

public class LoginResponseData {
    private String account;

    private String name;

    private String password;

    private int role_id;

    /**
     * 在登陆完成后检查当前进行中的流程状态
     * 未开始：1，进行中：2，结束：3，取消：4
     */
    private int process_status=3;

    public String getFullName() {
        return name;
    }

    public int getRoleId() {
        return role_id;
    }

    public String getPassword() {
        return password;
    }

    public String getAccount() {
        return account;
    }

    public int getProcessStatus() {
        return process_status;
    }
}
