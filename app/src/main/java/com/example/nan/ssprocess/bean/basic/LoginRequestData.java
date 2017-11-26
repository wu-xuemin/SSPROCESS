package com.example.nan.ssprocess.bean.basic;

/**
 * Created by Hu Tong on 2017/5/24.
 */

public class LoginRequestData {
    private int mobile = 1;//区别移动端和浏览器
    private String name;
    private String password;

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
