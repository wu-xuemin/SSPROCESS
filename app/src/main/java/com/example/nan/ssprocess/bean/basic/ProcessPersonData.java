package com.example.nan.ssprocess.bean.basic;


import java.io.Serializable;

/**
 * Created by Hu Tong on 5/18/2017.
 */

public class ProcessPersonData implements Serializable {
    //private int id; //序号
    private String name; //名字
    private String role_name; //角色名
    private int number;  //编号
    //private boolean on_line = false;

    public ProcessPersonData(String name, String role_name, int number) {
        this.name = name;
        this.role_name = role_name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public String getRoleName() {
        return role_name;
    }

    public int getNumber() {
        return number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoleName(String role_name) {
        this.role_name = role_name;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
