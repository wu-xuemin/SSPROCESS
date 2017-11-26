package com.example.nan.ssprocess.bean.basic;


import java.io.Serializable;

/**
 * Created by Hu Tong on 5/18/2017.
 */

public class MaterialData implements Serializable {
    private int id;
    private String name;
    private String size;
    private int number;
    private String package_name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public int getNumber() {
        return number;
    }

    public String getPackageName() {
        return package_name;
    }

    public MaterialData(int id, String name, String style, int content) {
        this.id = id;
        this.name = name;
        this.size = style;
        this.number = content;
    }

    //    public void setId(int id) {
//        this.id = id;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
}
