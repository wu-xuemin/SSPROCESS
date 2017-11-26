package com.example.nan.ssprocess.bean.basic;


import java.io.Serializable;

/**
 * Created by Hu Tong on 5/18/2017.
 */

public class ProcessInfoData implements Serializable {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public ProcessInfoData(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
