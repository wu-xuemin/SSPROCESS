package com.example.nan.ssprocess.bean.basic;

import java.util.ArrayList;

/**
 * Created by Hu Tong on 5/17/2017.
 */

public class TaskContentRecordData {

    private String CLASS = "go.GraphLinksModel";//因为无法使用class作为变量名，所以的encode回去时需要替换CLASS成class

    private String linkFromPortIdProperty = "";

    private String linkToPortIdProperty = "";

    private ArrayList<NodeData> nodeDataArray = new ArrayList<>();

    private ArrayList<LinkData> linkDataArray = new ArrayList<>();

    public ArrayList<NodeData> getNodeDataArray() {
        return nodeDataArray;
    }

    public ArrayList<LinkData> getLinkDataArray() {
        return linkDataArray;
    }
}
