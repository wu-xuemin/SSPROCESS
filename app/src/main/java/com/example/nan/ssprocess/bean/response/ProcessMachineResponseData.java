package com.example.nan.ssprocess.bean.response;

import com.example.nan.ssprocess.bean.basic.MachineProcessData;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nan
 * @date 2017/12/21
 */

public class ProcessMachineResponseData {

    private List<MachineProcessData> list = new ArrayList<>();

    public List<MachineProcessData> getList() {
        return list;
    }
}
