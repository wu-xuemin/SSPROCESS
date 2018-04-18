package com.example.nan.ssprocess.bean.basic;

import java.io.Serializable;

public class TaskData implements Serializable {
    private int id;
    private int qualityUserId;

    public int getId() {
        return id;
    }

    public int getQualityUserId() {
        return qualityUserId;
    }
}
