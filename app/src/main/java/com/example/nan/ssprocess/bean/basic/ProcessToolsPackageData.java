package com.example.nan.ssprocess.bean.basic;


import java.io.Serializable;

/**
 * Created by Hu Tong on 5/18/2017.
 */

public class ProcessToolsPackageData implements Serializable {
    private int id;
    private String name;

    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }

    public ProcessToolsPackageData(int id, String name) {
        this.name = name;
        this.id = id;
    }
}
