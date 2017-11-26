package com.example.nan.ssprocess.bean.basic;


import java.io.Serializable;

/**
 * Created by Hu Tong on 5/18/2017.
 */

public class ChooseProcessData implements Serializable {
    private int id;
    private String name;

    public ChooseProcessData(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
