package com.example.nan.ssprocess.bean.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hu Tong on 5/28/2017.
 */

public class NodeData {

    private String category = "";

    private String text ="";

    private int key;

    private String loc = "";

    private List<ProcessPersonData> worker_list = new ArrayList<>();

    private int task_status = 1;//作业内容状态: 1. 未开始（灰色）2.进行中（黄色）3. 工长已确认（蓝色）4. 质检已确认【结束】（绿色）

    private String begin_time; //作业开始时间

    private String end_time; //作业结束时间

    private String tight_value;//预警/紧固/复合扭矩值

    private int bolt_value = 0;

    private String bolt_style;

    private int skip = 0;

    private int photo_control_num = -1;

    private String signal_out = null;

    private String signal_in = null;

    private int signal_out_value = 0;

    private int signal_in_value = 0;

    public String getCategory() {
        return category;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getKey() {
        return key;
    }

    public String getLoc() {
        return loc;
    }

    public List<ProcessPersonData> getWorker_list() {
        return worker_list;
    }

    public int getTask_status() {
        return task_status;
    }

    public void setTask_status(int status) {
        this.task_status = status;
    }

    public void setBegin_time(String begin_time) {
        this.begin_time = begin_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getBegin_time() {
        return begin_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public String getTight_value() {
        return tight_value;
    }

    public int getBolt_value() {
        return bolt_value;
    }
    public String getBolt_style() {
        return bolt_style;
    }

    public int getPhotoControlNum() {
        return photo_control_num;
    }

    public int getSkip() {
        return skip;
    }

    public String getSignalOut() {
        return signal_out;
    }

    public void setSignalOut(String signal_out) {
        this.signal_out = signal_out;
    }

    public String getSignalIn() {
        return signal_in;
    }

    public void setSignalIn(String signal_in) {
        this.signal_in = signal_in;
    }

    public int getSignalOutValue() {
        return signal_out_value;
    }

    public void setSignalOutValue(int signal_out_value) {
        this.signal_out_value = signal_out_value;
    }

    public int getSignalInValue() {
        return signal_in_value;
    }

    public void setSignalInValue(int signal_in_value) {
        this.signal_in_value = signal_in_value;
    }
}
