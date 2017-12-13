package com.example.nan.ssprocess.bean;


/**
 * Created by nan on 2017/5/25.
 */

public class LoginResponseData {
    private String account;
    private int id;
    private String name;
    private int valid;
    private LoginGroup group;
    private LoginRole role;
    private String password;


    public String getFullName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }

    public int getValid() {
        return valid;
    }
    public String getPassword() {
        return password;
    }

    public LoginGroup getGroup() {
        return group;
    }

    public LoginRole getRole() {
        return role;
    }

}
