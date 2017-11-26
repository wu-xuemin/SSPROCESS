package com.example.nan.ssprocess.bean.basic;

/**
 * Created by Hu Tong on 5/17/2017.
 * 后台录入的作业内容的基础数据，记载以后不能更改
 */

public class TaskContentData {


    private int id;

    //作业内容名称
    private String name;

    //作业指导（图文）
    private String guide;

    //紧固扭矩值
    private int tight_value;

    //螺栓数
    private int bolt_value;

    //是否被删除
    private int is_valid;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTightValue() {
        return tight_value;
    }

    public int getBoltValue() {
        return bolt_value;
    }

    public int getValid() {
        return is_valid;
    }

    public String getGuide() {
        return guide;
    }

    public TaskContentData(int id, String name, int tight_value, int bolt_value, int valid) {
        super();
        this.id = id;
        this.name =  name;
        this.tight_value = tight_value;
        this.bolt_value = bolt_value;
        this.is_valid = valid;
    }

}
