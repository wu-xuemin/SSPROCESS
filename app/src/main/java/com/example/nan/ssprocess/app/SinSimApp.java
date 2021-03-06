package com.example.nan.ssprocess.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.blankj.utilcode.util.CacheUtils;
import com.blankj.utilcode.util.Utils;
import com.example.nan.ssprocess.util.LogUtils;

import okhttp3.OkHttpClient;


/**
 * @author nan
 */
public class SinSimApp extends Application {

    private static final boolean DEBUG_LOG = true;
    private static final String TAG = "SinSimApp";

    /**
     * Shared preferences file name, where we store persistent values.
     */
    static final String SHARED_PREFS_FILENAME = "SinSim";

    public static final int LOGIN_REQUEST_CODE = 20000;
    public static final int LOGIN_RESULT_SUCCESS_CODE = 20001;
    public static final int LOGIN_FOR_ADMIN = 2;    //生产部管理员
    public static final int LOGIN_FOR_QA = 11;      //质检员
    public static final int LOGIN_FOR_QA_LEADER = 16;   //质检组长
    public static final int LOGIN_FOR_INSTALL = 3;      //安装组长

    public static final String FROM_NOTIFICATION = "1";

    /**
     * 机器状态:
     * "0" --> 签核完成,机器创建的初始化状态
     * "1" --> 配置完成，待计划
     * "2" --> 已加入计划生产
     * "3" --> 安装中
     * "4" --> 安装完成
     * "5" --> 改单
     * "6" --> 拆单
     * "7" --> 取消
     */
    public static final int MACHINE_INITIAL = 0;
    public static final int MACHINE_CONFIGURED = 1;
    public static final int MACHINE_PLANING = 2;
    public static final int MACHINE_INSTALLING = 3;
    public static final int MACHINE_INSTALLED = 4;
    public static final int MACHINE_CHANGED = 5;
    public static final int MACHINE_SPLITED = 6;
    public static final int MACHINE_CANCELED = 7;

    /**
     * Task(工序)安装状态
     * "0" --> 初始化状态
     * "1" --> 已计划
     * "2" --> 待安装
     * "3" --> 开始安装
     * "4" --> 安装完成
     * "5" --> 质检中
     * "6" --> 质检完成
     * "7" --> 安装异常
     * "8" --> 质检异常
     * "9" --> 跳过
     *
     * "10" --> 待质检 （同4？）
     *  3期新质检
     * "10" --> 未开始质检
     * "11" --> 无此检验条目
     * "12" --> 质检不合格
     * "13" --> 质检合格
     * "14" --> 未检
     */
    public static final int TASK_INITIAL = 0;
    public static final int TASK_PLANED = 1;
    public static final int TASK_INSTALL_WAITING = 2;
//    安装流程简化，
//    由原先的先点击“扫码开始”--安装好了，再点击“扫码完成”
//    改为： 只需要扫码一次。扫码一次就好了，让安装人员减少了一个扫码开始的步骤。 不再由 安装中TASK_INSTALLING 这个状态
    public static final int TASK_INSTALLING = 3; // 这个可以不要了
    public static final int TASK_INSTALLED = 4;
    public static final int TASK_QUALITY_DOING = 5;
    public static final int TASK_QUALITY_DONE = 6;
    public static final int TASK_INSTALL_ABNORMAL = 7;
    public static final int TASK_QUALITY_ABNORMAL = 8;
    public static final int TASK_SKIP = 9;
    //3期质检：
    public static final int TASK_QUALITY_INSPECT_NOT_STARTED = 10;
    public static final int TASK_QUALITY_INSPECT_NO_SUCH_ITEM = 11;
    public static final int TASK_QUALITY_INSPECT_NG = 12;
    public static final int TASK_QUALITY_INSPECT_OK = 13;
    public static final int TASK_QUALITY_INSPECT_HAVE_NOT_CHECKED = 14;

    /**
     * 二期的质检和原先不同，在定了 机器位置，则就要发消息给质检人员。
     * 所以质检的状态，不和工序的状态共用。
     * 包括：
     * 该条质检未开始，无此检验条目，质检合格，质检不合格。
     * （没有质检中,因为是对一条质检内容而言,对机器有多条质检，才有质检中这样的状态）,
     * “未检”按钮（因为无法检查，比如安装好了盖住了无法打开检验）,
     */
    public static final String STR_QUALITY_INSPECT_NOT_START = "待质检";
    public static final String STR_QUALITY_INSPECT_NOT_NEED = "无此检验条目";
    public static final String STR_QUALITY_INSPECT_FAILURE = "质检不合格";
    public static final String STR_QUALITY_INSPECT_SUCCESS = "质检合格";
    public static final String STR_QUALITY_INSPECT_HAVE_NOT_CHECKED = "未检";

    public static String getInstallStatusString(int status) {
        String result = "";
        switch (status){
            case SinSimApp.TASK_INITIAL:
                result = "初始化";
                break;
            case SinSimApp.TASK_PLANED:
                result = "已计划";
                break;
            case SinSimApp.TASK_INSTALL_WAITING:
                result = "待安装";
                break;
            case SinSimApp.TASK_INSTALLING:
                result = "安装中";
                break;
            case SinSimApp.TASK_INSTALLED:
                result = "待质检";
                break;
            case SinSimApp.TASK_QUALITY_DOING:
                result = "质检中";
                break;
            case SinSimApp.TASK_QUALITY_DONE:
                result = "质检合格";
                break;
            case SinSimApp.TASK_INSTALL_ABNORMAL:
                result = "安装异常";
                break;
            case SinSimApp.TASK_QUALITY_ABNORMAL:
                result = "质检不合格";
                break;
            case SinSimApp.TASK_SKIP:
                result = "跳过";
                break;

                //3期质检
            case SinSimApp.TASK_QUALITY_INSPECT_NOT_STARTED:
                result = SinSimApp.STR_QUALITY_INSPECT_NOT_START;
                break;
            case SinSimApp.TASK_QUALITY_INSPECT_NO_SUCH_ITEM:
                result = SinSimApp.STR_QUALITY_INSPECT_NOT_NEED;
                break;
            case SinSimApp.TASK_QUALITY_INSPECT_NG:
                result = SinSimApp.STR_QUALITY_INSPECT_FAILURE;
                break;
            case SinSimApp.TASK_QUALITY_INSPECT_OK:
                result = SinSimApp.STR_QUALITY_INSPECT_SUCCESS;
                break;
            case SinSimApp.TASK_QUALITY_INSPECT_HAVE_NOT_CHECKED:
                result = SinSimApp.STR_QUALITY_INSPECT_HAVE_NOT_CHECKED;
                break;


            default:
                break;

        }
        return result;
    }

    private boolean isLogined = false; // 是否已登录
    private String account;//用户账号
    private String fullname; //用户姓名
    private String password; //用户密码
    private int role; //用户角色（工长，质检）
    private String ip;
    private int appUserId;
    private int groupId;
    private String groupName;
    private String groupType;

    private String IMEI;
    private static SinSimApp mApp;

    private boolean isAvdTest = false;
    /**
     * 缓存
     */
    private CacheUtils mCache;

    /**
     * Shared preferences editor for writing program settings.
     */
    private SharedPreferences.Editor mPrefEditor = null;
    private SharedPreferences mSharedPrefs;

    /**
     * Http Client
     */
    private OkHttpClient mOKHttpClient;


    /**
     * Persistent value types.
     */
    public enum PersistentValueType {
        ACCOUNT,    //账号
        PASSWORD,   //密码
        FULL_NAME,  //名字
        IS_LOGIN,   //是否登录
        ROLE,        //角色：工长、质检
        SERVICE_IP,  //服务器地址
        USER_ID,     //用户id
        GROUP_ID,
        GROUP_NAME,
        GROUP_TYPE,
        LAST_CONNECT_PRINTER_NAME
    }

    public static SinSimApp getApp() {
        return mApp;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        //start log
        LogUtils.logInit(true);

        Utils.init(this);
        mCache = CacheUtils.getInstance(this.getCacheDir());

        mOKHttpClient = new OkHttpClient();
        /*
		 * Get shared preferences and editor so we can read/write program settings.
		 */
        mSharedPrefs = getSharedPreferences( SHARED_PREFS_FILENAME, 0 );
        mPrefEditor = mSharedPrefs.edit();

        try {
            String name = readValue(PersistentValueType.FULL_NAME,null);
            if( name != null ) {
                fullname = name;
            }
        } catch(Exception e) {
            fullname = null;
        }

        this.isLogined  = Boolean.valueOf(readValue(PersistentValueType.IS_LOGIN, "0"));
        this.account = readValue(PersistentValueType.ACCOUNT, "");
        this.password = readValue(PersistentValueType.PASSWORD, "");
        this.fullname = readValue(PersistentValueType.FULL_NAME, "");
        String roleStr = readValue(PersistentValueType.ROLE, "1");
        if("".equals(roleStr)) {
            this.role = 0;
        }else {
            this.role = Integer.valueOf(readValue(PersistentValueType.ROLE, "0"));
        }
        this.ip = readValue(PersistentValueType.SERVICE_IP, "192.168.1.139:8004");
//        this.ip = "192.168.1.139:8080";
//        this.ip = "10.0.2.2:8080";
//模拟器
        if(isAvdTest) {
            this.ip = "10.0.2.2:8004";
        }
        String appUserIdStr = readValue(PersistentValueType.USER_ID, "0");
        if ("".equals(appUserIdStr)){
            this.appUserId =0;
        }else {
            this.appUserId = Integer.valueOf(readValue(PersistentValueType.USER_ID, "0"));
        }
        String groupIdStr = readValue(PersistentValueType.GROUP_ID, "0");
        if ("".equals(groupIdStr)){
            this.groupId =0;
        }else {
            this.groupId = Integer.valueOf(readValue(PersistentValueType.GROUP_ID, "0"));
        }
        this.groupName = readValue(PersistentValueType.GROUP_NAME, "");
        this.groupType = readValue(PersistentValueType.GROUP_TYPE, "");
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getIMEI() {
        IMEI = null;
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        if (telephonyManager != null) {
           IMEI = telephonyManager.getDeviceId();
//模拟器
            if(isAvdTest) {
                IMEI = "AVDAVD7890AVDAV";
            }
        } else {
            Log.d(TAG, "getIMEI: have some error");
        }
        if(IMEI == null) {
            IMEI = "868906040986480";
        }
        return IMEI;
    }
    /**
     * 由片段调用，设置登录信息
     *
     * @param isLogined
     * @param account
     * @param fullname
     */
    public void setIsLogined(boolean isLogined, String account, String fullname, String password, int role, int appUserId, int groupId, String groupName, String groupType) {
        writePreferenceValue(PersistentValueType.IS_LOGIN, String.valueOf(isLogined));
        writePreferenceValue(PersistentValueType.ACCOUNT, account);
        writePreferenceValue(PersistentValueType.FULL_NAME, fullname);
        writePreferenceValue(PersistentValueType.PASSWORD, password);
        writePreferenceValue(PersistentValueType.ROLE, String.valueOf(role));
        writePreferenceValue(PersistentValueType.USER_ID, String.valueOf(appUserId));
        writePreferenceValue(PersistentValueType.GROUP_ID, String.valueOf(groupId));
        writePreferenceValue(PersistentValueType.GROUP_NAME, groupName);
        writePreferenceValue(PersistentValueType.GROUP_TYPE, groupType);
        try {
            commitValues();
            this.isLogined = isLogined;
            this.account = account;
            this.fullname = fullname;
            this.password = password;
            this.role = role;
            this.appUserId=appUserId;
            this.groupId=groupId;
            this.groupName=groupName;
            this.groupType=groupType;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLogOut() {

        writePreferenceValue(PersistentValueType.IS_LOGIN, "");
        //writePreferenceValue(PersistentValueType.ACCOUNT, "");
        writePreferenceValue(PersistentValueType.FULL_NAME, "");
        writePreferenceValue(PersistentValueType.PASSWORD, "");
        writePreferenceValue(PersistentValueType.ROLE, "");
        writePreferenceValue(PersistentValueType.USER_ID, "");
        writePreferenceValue(PersistentValueType.GROUP_ID, "");
        writePreferenceValue(PersistentValueType.GROUP_NAME, "");
        writePreferenceValue(PersistentValueType.GROUP_TYPE, "");
        try {
            commitValues();
            this.isLogined = false;
            //this.account = "";
            this.fullname = "";
            this.password = "";
            this.role = -1;
            this.appUserId = 0;
            this.groupId = 0;
            this.groupName = "";
            this.groupType = "";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPrinterName(String printerName) {
        writePreferenceValue(PersistentValueType.LAST_CONNECT_PRINTER_NAME, printerName);
        try {
            commitValues();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPrinterName() {
        return readValue(PersistentValueType.LAST_CONNECT_PRINTER_NAME, "");
    }



    /**
     * Reads the specified string persistent value.
     *
     * @param valueType - The persistent value type to read.
     * @param sDefault - The default value to return if the requested value does not exist.
     * @return The requested value.
     */
    public String readValue( PersistentValueType valueType, String sDefault )
    {
        if ( DEBUG_LOG ) {
            Log.i(TAG, String.format("[readValue] ==> value [%s] sDefault [%s]",
                    valueType.toString(), sDefault));
        }

        String sValue = mSharedPrefs.getString(valueType.toString(), sDefault);

        if ( DEBUG_LOG ) {
            Log.i(TAG, "[readValue] <== nValue: " + sValue);
        }
        return sValue;
    }

    /**
     * Writes the specified string persistent value.
     *
     * @param valueType - The persistent value type to read.
     * @param sValue - The value to write.
     */
    public void writePreferenceValue( PersistentValueType valueType, String sValue )
    {
        if ( DEBUG_LOG ) {
            Log.i(TAG, String.format("[writeValue] ==> sValuName [%s] nValue [%s]",
                    valueType.toString(), sValue));
        }

        mPrefEditor.putString(valueType.toString(), sValue );

        if ( DEBUG_LOG ) {
            Log.i(TAG, "[writeValue] <==");
        }
    }

    /**
     * Commits persistent values that were previously written.
     *
     * @throws Exception When the commit operation fails.
     */
    public void commitValues() throws Exception
    {
        if ( DEBUG_LOG ) {
            Log.i(TAG, "[commitValues] ==>");
        }

        boolean bSuccess = mPrefEditor.commit();
        if ( ! bSuccess ){
            throw new Exception( "commit() failed" );
        }
        if ( DEBUG_LOG ) {
            Log.i(TAG, "[commitValues] <==");
        }
    }

    /**
     * Delete the specified string persistent value.
     *
     * @param valueType - The persistent value type to Delete.
     */
    public void deleteValue( PersistentValueType valueType)
    {
        if ( DEBUG_LOG ) {
            Log.i(TAG, String.format("[deleteValue] ==> sValuName [%s] ",
                    valueType.toString()));
        }
        mPrefEditor.remove( valueType.toString() );

        if ( DEBUG_LOG ) {
            Log.i(TAG, "[deleteValue] <==");
        }
    }

    public OkHttpClient getOKHttpClient() {
        return mOKHttpClient;
    }

    public boolean isLogined() {
        return isLogined;
    }

    public String getAccount() {
        return account;
    }

    public String getFullName() {
        return fullname;
    }

    public String getPassword() {
        return password;
    }

    public int getRole() {
        return role;
    }

    public int getAppUserId() {
        return appUserId;
    }

    public int getGroupId() {
        return this.groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupType() {
        return groupType;
    }

    public String getServerIP() {
        return ip;
    }

    public void setServerIP(String ipStr) {
        writePreferenceValue(PersistentValueType.SERVICE_IP, ipStr);
        try {
            commitValues();
            this.ip = ipStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CacheUtils getCache() {
        return mCache;
    }
}
