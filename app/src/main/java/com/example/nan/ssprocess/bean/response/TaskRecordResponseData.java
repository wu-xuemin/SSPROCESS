package com.example.nan.ssprocess.bean.response;

import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nan
 * @date 2017/12/21
 */

public class TaskRecordResponseData {

    private List<TaskRecordMachineListData> list = new ArrayList<>();

    public List<TaskRecordMachineListData> getList() {
        return list;
    }
}
