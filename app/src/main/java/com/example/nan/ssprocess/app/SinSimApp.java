package com.example.nan.ssprocess.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
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
    public static final int LOGIN_FOR_ADMIN = 2;
    public static final int LOGIN_FOR_QA = 11;
    public static final int LOGIN_FOR_INSTALL = 3;

    private boolean isLogined = false; // 是否已登录
    private String account;//用户账号
    private String fullname; //用户姓名
    private String password; //用户密码
    private int role; //用户角色（工长，质检）
    private String ip;
    private int userId;
    private static SinSimApp mApp;

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
        USER_ID     //用户id
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
        this.ip = readValue(PersistentValueType.SERVICE_IP, "");
        this.userId = Integer.valueOf(readValue(PersistentValueType.USER_ID, "0"));
    }

    /**
     * 由片段调用，设置登录信息
     *
     * @param isLogined
     * @param account
     * @param fullname
     */
    public void setIsLogined(boolean isLogined, String account, String fullname, String password, int role, int userId) {
        writePreferenceValue(PersistentValueType.IS_LOGIN, String.valueOf(isLogined));
        writePreferenceValue(PersistentValueType.ACCOUNT, account);
        writePreferenceValue(PersistentValueType.FULL_NAME, fullname);
        writePreferenceValue(PersistentValueType.PASSWORD, password);
        writePreferenceValue(PersistentValueType.ROLE, String.valueOf(role));
        writePreferenceValue(PersistentValueType.USER_ID, String.valueOf(userId));
        try {
            commitValues();
            this.isLogined = isLogined;
            this.account = account;
            this.fullname = fullname;
            this.password = password;
            this.role = role;
            this.userId=userId;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLogOut() {

        writePreferenceValue(PersistentValueType.IS_LOGIN, "");
        writePreferenceValue(PersistentValueType.ACCOUNT, "");
        writePreferenceValue(PersistentValueType.FULL_NAME, "");
        writePreferenceValue(PersistentValueType.PASSWORD, "");
        writePreferenceValue(PersistentValueType.ROLE, "");
        writePreferenceValue(PersistentValueType.USER_ID, "");
        try {
            commitValues();
            this.isLogined = false;
            this.account = "";
            this.fullname = "";
            this.password = "";
            this.role = -1;
            this.userId = -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        String sValue = mSharedPrefs.getString( valueType.toString(), sDefault );

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

    public int getUserId() {
        return userId;
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
