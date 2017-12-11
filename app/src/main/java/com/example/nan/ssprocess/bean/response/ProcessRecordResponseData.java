package com.example.nan.ssprocess.bean.response;

import com.example.nan.ssprocess.bean.ResponseData;
import com.example.nan.ssprocess.bean.basic.MaterialData;
import com.example.nan.ssprocess.bean.basic.ProcessInfoData;
import com.example.nan.ssprocess.bean.basic.ProcessPersonData;
import com.example.nan.ssprocess.bean.basic.ProcessToolsPackageData;
import com.example.nan.ssprocess.bean.basic.TaskContentRecordData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hu Tong on 9/24/2016.
 */
public class ProcessRecordResponseData extends ResponseData {
    private int id;  //记录记录ID

    private String title;//流程名称

    private String qualification_list = "";//所需资质的列表，TODO：这里以后需要改成数组的形式

    private List<ProcessInfoData> process_info_value = new ArrayList<>();//流程填写的信息

    private List<ProcessToolsPackageData> tools_package = new ArrayList<>();

    private List<MaterialData> materials = new ArrayList<>();

    private int workers_number = 0;//作业人数

    private TaskContentRecordData task_content_list;

    private int process_record_status;//流程的状态

    private int leader_status;//工长是否登录

    private int quality_status;//质检员是都登录

    private List<ProcessPersonData> worker_list = new ArrayList<>();//此次参加工人的列表

    private String create_time = ""; //创建时间

    private String begin_time = "";  //开始时间

    private String finish_time = ""; //结束时间


    public int getProcessId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getProcessStatus() {
        return process_record_status;
    }

    public ArrayList<ProcessToolsPackageData> getToolsPackage() {
        return (ArrayList<ProcessToolsPackageData>)tools_package;
    }

//    public void setProcessStatus(int status) {
//        this.process_record_status = 3;
//    }

    public int getLeaderStatus() {
        return leader_status;
    }

    public int getQualityStatus() {
        return quality_status;
    }

    public ArrayList<ProcessPersonData> getWorkerList() {
        return (ArrayList<ProcessPersonData>)worker_list;
    }

    public TaskContentRecordData getTaskContentList() {
        return task_content_list;
    }

    public ArrayList<ProcessInfoData> getProcessInfoList() {
        return (ArrayList<ProcessInfoData>) process_info_value;
    }
    public ArrayList<MaterialData> getMaterialList() {
        return (ArrayList<MaterialData>) materials;
    }

    public int getWorkersNumber() {
        return workers_number;
    }

}
