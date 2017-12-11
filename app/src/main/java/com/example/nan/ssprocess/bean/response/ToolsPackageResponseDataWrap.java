package com.example.nan.ssprocess.bean.response;

import com.example.nan.ssprocess.bean.ResponseData;
import com.example.nan.ssprocess.bean.basic.ProcessToolsPackageData;

import java.util.ArrayList;

/**
 * Created by Hu Tong on 9/24/2016.
 */
public class ToolsPackageResponseDataWrap extends ResponseData {

    private ArrayList<ProcessToolsPackageData> data;

    public ArrayList<ProcessToolsPackageData> getData() {
        return data;
    }

}
