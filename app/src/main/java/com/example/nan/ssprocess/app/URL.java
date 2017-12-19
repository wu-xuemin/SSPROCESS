package com.example.nan.ssprocess.app;

/**
 * Created by Hu Tong on 7/15/2016.
 */
public class URL {

	//public static final String IP = "192.168.1.116";
    public static final String HTTP_HEAD = "http://";
//    public static final String LOCATION = "/shcrhservice/index.php/home";

    public static final String USER_LOGIN = "/user/requestLogin";
    public static final String USER_LOGOUT = "/user/logout";
    public static final String PROCESS_MODULE_LIST = "/process/getRecords";
    public static final String PROCESS_RECORD_ADD = "/ProcessRecord/addProcessRecord";
    public static final String FETCH_TASK_RECORD_TO_ADMIN = "/task/record/selectAllTaskRecordDetail";
    public static final String FETCH_TASK_RECORD_TO_QA = "/task/record/selectAllQaTaskRecordDetailByUserAccount";
    public static final String FETCH_TASK_RECORD_TO_INSTALL = "/task/record/selectAllInstallTaskRecordDetailByUserAccount";
    public static final String FETCH_PROCESS_TASK_PLANS_RECORD = "/selectTaskPlans";
    public static final String UPDATE_OPERATION_STATUS = "/OperationStatus/modifyData";
    public static final String FETCH_TOOLS_PACKAGE = "/ToolsPackage/getRecords";
    public static final String FETCH_GUIDANCE = "/TaskContent/getGuidanceByName";
    public static final String UPDATE_PROCESS_RECORD = "/ProcessRecord/updateProcessRecord";
    public static final String PRINT_BC_TABLE = "/ProcessRecord/printBCKeyreplacementRecord";
    public static final String PRINT_TUOCHE_INSTALL_TABLE = "/ProcessRecord/printCRH2TuoCheLunLunDuiAnZhuangRecord";
    public static final String PRINT_TUOCHE_UNINSTALL_TABLE = "/ProcessRecord/printCRH2TuoCheLunLunDuiChaiXieRecord";
    public static final String PRINT_DONGCHE_INSTALL_TABLE = "/ProcessRecord/printCRH2DongCheLunLunDuiAnZhuangRecord";
    public static final String PRINT_DONGCHE_UNINSTALL_TABLE = "/ProcessRecord/printCRH2DongCheLunLunDuiChaiXieRecord";

}
