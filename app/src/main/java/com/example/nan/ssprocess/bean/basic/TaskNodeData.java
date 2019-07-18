package com.example.nan.ssprocess.bean.basic;

import java.io.Serializable;

public class TaskNodeData implements Serializable {
    private String text;
    private String taskStatus;

    public String getText() {
        return text;
    }

    public String getTaskStatus() {
        return taskStatus;
    }
}
