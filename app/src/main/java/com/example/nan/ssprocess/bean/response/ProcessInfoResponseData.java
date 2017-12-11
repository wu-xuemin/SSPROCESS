package com.example.nan.ssprocess.bean.response;

import com.example.nan.ssprocess.bean.ResponseData;
import com.example.nan.ssprocess.bean.basic.ProcessInfoData;

import java.util.ArrayList;

/**
 * Created by Hu Tong on 5/19/2017.
 */

public class ProcessInfoResponseData extends ResponseData {
    private ArrayList<ProcessInfoData> process_info_list;

    public ArrayList<ProcessInfoData> getProcessInfoList() {
        return process_info_list;
    }
}
