package com.example.nan.ssprocess.bean.response;

import com.example.nan.ssprocess.bean.basic.TaskMachineListData;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nan
 * @date 2017/12/21
 */

public class TaskRecordResponseData {

    private List<TaskMachineListData> list = new ArrayList<>();

    public List<TaskMachineListData> getList() {
        return list;
    }
}
