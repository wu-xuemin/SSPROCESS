package com.example.nan.ssprocess.app;

/**
 * Created by nan on 12/15/2016.
 */
public class URL {

    public static final String HTTP_HEAD = "http://";
    public static final String TCP_HEAD = "tcp://";
    public static final String MQTT_PORT = ":1883";
    public static final String DOWNLOAD_DIR = "/oll/";
    public static final String INSTALL_PIC_DIR = "/abnormal";
    public static final String QA_PIC_DIR = "/quality";
    public static final String USER_LOGIN = "/user/requestLogin";
    public static final String FETCH_TASK_RECORD_DETAIL = "/task/record/selectTaskRecordDetail";
    public static final String FETCH_TASK_RECORD_TO_ADMIN = "/task/record/selectAllTaskRecordDetail";
    public static final String FETCH_TASK_RECORD_BY_SEARCH_TO_ADMIN = "/machine/selectMachinesByNameplate";
    public static final String FETCH_DOWNLOADING_FILELIST = "/order/loading/list/selectOrderLoadingFileNameByOrderId";
    public static final String FETCH_TASK_RECORD_BY_SCAN_QRCORD_TO_ADMIN = "/task/record/selectTaskRecordByNamePlate";
    public static final String FETCH_TASK_RECORD_TO_QA = "/task/record/selectAllQaTaskRecordDetailByUserAccount";
    public static final String FETCH_TASK_RECORD_BY_SCAN_QRCORD_TO_QA = "/task/record/selectQATaskRecordDetailByAccountAndNamePlate";
    public static final String FETCH_TASK_RECORD_TO_INSTALL = "/task/record/selectAllInstallTaskRecordDetailByUserAccount";
    public static final String FETCH_TASK_RECORD_TO_UNPLANNED_INSTALL = "/task/record/selectUnplannedTaskRecordByAccount";
    public static final String FETCH_TASK_RECORD_BY_SCAN_QRCORD_TO_INSTALL = "/task/record/selectTaskRecordByNamePlateAndAccount";
    public static final String FETCH_TASK_RECORD_BY_SCAN_QRCORD_TO_UNPLANNED_INSTALL = "/task/record/selectUnPlannedTaskRecordByNamePlateAndAccount";
    public static final String UPDATE_TASK_RECORD_STATUS = "/task/record/updateTaskInfo";
    public static final String UPDATE_MACHINE_LOCATION = "/machine/update";
    public static final String FATCH_INSTALL_ABNORMAL_TYPE_LIST = "/abnormal/list";
    public static final String FATCH_INSTALL_ABNORMAL_RECORD_DETAIL = "/abnormal/record/selectAbnormalRecordDetails";
    public static final String FATCH_INSTALL_ABNORMAL_RECORD_LIST = "/abnormal/record/list";
    public static final String UPDATE_INSTALL_ABNORMAL_RECORD_DETAIL = "/abnormal/record/add";
    public static final String UPLOAD_INSTALL_ABNORMAL_IMAGE = "/abnormal/image/add";
    public static final String FATCH_TASK_QUALITY_RECORD_DETAIL = "/task/quality/record/selectTaskQualityRecordDetails";
    public static final String FATCH_TASK_QUALITY_RECORD_LIST = "/task/quality/record/list";
    public static final String UPDATE_TASK_QUALITY_RECORD_DETAIL = "/task/quality/record/add";
    public static final String UPLOAD_QUALITY_RECORD_IMAGE = "/quality/record/image/add";
    public static final String FATCH_GROUP_BY_USERID = "/user/selectAllInstallGroupByUserId";

}
