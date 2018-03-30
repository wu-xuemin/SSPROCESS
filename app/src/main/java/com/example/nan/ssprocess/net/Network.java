package com.example.nan.ssprocess.net;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.bean.basic.MachineData;
import com.example.nan.ssprocess.bean.response.AbnormalRecordReponseDataWrap;
import com.example.nan.ssprocess.bean.response.AbnormalTypeResponseDataWrap;
import com.example.nan.ssprocess.bean.response.ListDataWrap;
import com.example.nan.ssprocess.bean.response.LoginResponseDataWrap;
import com.example.nan.ssprocess.bean.response.MachineResponseDataWrap;
import com.example.nan.ssprocess.bean.response.QualityRecordReponseDataWrap;
import com.example.nan.ssprocess.bean.response.ResponseData;
import com.example.nan.ssprocess.bean.response.TaskRecordFromIdResponseDataWrap;
import com.example.nan.ssprocess.bean.response.TaskRecordResponseDataWrap;
import com.example.nan.ssprocess.bean.response.UserResponseDataWrap;
import com.example.nan.ssprocess.ui.activity.DetailToCheckoutActivity;
import com.example.nan.ssprocess.util.ShowMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * @author nan
 */
public class Network {
    private static String TAG = "nlgNetwork";
    @SuppressLint("StaticFieldLeak")
    private static Network mNetWork;
    @SuppressLint("StaticFieldLeak")
    private static Application mCtx;
    private static ThreadPoolExecutor executor;
    private static final int CORE_THREAD_NUM = 3;
    public static final int OK = 1;
    private static final int NG = 0;
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");


    private Network() {
    }

    public static Network Instance(Application ctx){
        if( mNetWork == null ) {
            mCtx= ctx;
            mNetWork = new Network();
            executor = new ThreadPoolExecutor(CORE_THREAD_NUM, 20, 500, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(5), new ThreadPoolExecutor.DiscardPolicy());
        }
        return mNetWork;
    }
    /**
     * 判断是否有网络连接
     */
    public boolean isNetworkConnected() {
        ConnectivityManager connectivity = (ConnectivityManager) (mCtx.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (connectivity != null) {
            NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取login信息
     */
    public void fetchLoginData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            Log.d(TAG, "fetchLoginData: 没网络");
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            Log.d(TAG, "fetchLoginData: 有网络");
            if (url != null && values != null) {
                Log.d(TAG, "fetchLoginData: not null");
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        for (Object o : values.entrySet()) {
                            HashMap.Entry entry = (HashMap.Entry) o;
                            builder.add((String) entry.getKey(), (String) entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Log.d(TAG, "fetchLoginData run: response success");
                                Gson gson = new Gson();
                                LoginResponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<LoginResponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    Log.d(TAG, "fetchLoginData run: responseData："+responseData.getCode());
                                    if (responseData.getCode() == 200) {
                                        if(responseData.getData().getValid()!=1){
                                            Log.e(TAG, "用户已离职");
                                            msg.obj="用户已离职";
                                        }else {
                                            success = true;
                                            msg.obj = responseData.getData();
                                        }
                                    } else if (responseData.getCode() == 400) {
                                        Log.e(TAG, responseData.getMessage());
                                        Log.d(TAG, "fetchLoginData run: error 400 :"+responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 500) {
                                        Log.e(TAG, responseData.getMessage());
                                        Log.d(TAG, "fetchLoginData run: error 500 :"+responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    }else {
                                        Log.e(TAG, "fetchLoginData Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "Network error!";
                            Log.d(TAG, "fetchLoginData run: catch "+e);
                        } finally {
                            handler.sendMessage(msg);
                            Log.d(TAG, "fetchLoginData run: finally");
                            if(response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 获取machineTaskListDetail信息
     */
    public void fetchProcessTaskRecordData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            Log.d(TAG, "fetchProcessTaskRecordData: network_not_connect");
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            if (url != null && values != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        for (Object o : values.entrySet()) {
                            HashMap.Entry entry = (HashMap.Entry) o;
                            builder.add((String) entry.getKey(), (String) entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url( url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                TaskRecordResponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<TaskRecordResponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    Log.d(TAG, "fetchProcessTaskRecordData run: "+responseData.getCode());
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                        msg.obj = responseData.getData().getList();
                                    } else if (responseData.getCode() == 400) {
                                        Log.e(TAG, responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 500) {
                                        Log.e(TAG, responseData.getMessage());
                                        Log.d(TAG, "fetchProcessTaskRecordData run: error 500 :"+responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else {
                                        Log.e(TAG, "fetchProcessTaskRecordData Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                                msg.obj = "网络请求错误！";
                            }
                            response.close();
                        } catch (Exception e) {
                            Log.d(TAG, "fetchProcessTaskRecordData run: "+e);
                            msg.what = NG;
                            msg.obj = "网络请求错误!";
                        } finally {
                            handler.sendMessage(msg);
                            if (response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 获取单个machineByNameplate
     */
    public void fetchMachineByNameplate(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            if (url != null && values != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        for (Object o : values.entrySet()) {
                            HashMap.Entry entry = (HashMap.Entry) o;
                            builder.add((String) entry.getKey(), (String) entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                MachineResponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<MachineResponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    Log.d(TAG, "fetchMachineByNameplate run: "+responseData.getData());
                                    if (responseData.getCode() == 200) {
                                        if (responseData.getData()!=null) {
                                            success = true;
                                            msg.obj = responseData.getData();
                                        } else {
                                            msg.what = NG;
                                            msg.obj = "没有这个钢印号的机器信息！";
                                        }
                                    } else if (responseData.getCode() == 400) {
                                        Log.e(TAG, responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 500) {
                                        Log.e(TAG, responseData.getMessage());
                                        Log.d(TAG, "fetchMachineByNameplate run: error 500 :"+responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else {
                                        Log.e(TAG, "fetchMachineByNameplate Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                                msg.obj = "网络请求错误！";
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "网络请求错误！";
                        } finally {
                            handler.sendMessage(msg);
                            if(response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 获取质检结果
     */
    public void fetchProcessQARecordData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            if (url != null && values != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        for (Object o : values.entrySet()) {
                            HashMap.Entry entry = (HashMap.Entry) o;
                            builder.add((String) entry.getKey(), (String) entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                QualityRecordReponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<QualityRecordReponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    Log.d(TAG, "fetchProcessQARecordData run: "+responseData.getCode());
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                        msg.obj = responseData.getData().getList();
                                    } else if (responseData.getCode() == 400) {
                                        Log.e(TAG, responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 500) {
                                        Log.e(TAG, responseData.getMessage());
                                        Log.d(TAG, "fetchProcessQARecordData run: error 500 :"+responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else {
                                        Log.e(TAG, "Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                                msg.obj = "网络请求错误！";
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "网络请求错误！";
                        } finally {
                            handler.sendMessage(msg);
                            if(response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 获取安装结果
     */
    public void fetchProcessInstallRecordData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            if (url != null && values != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        for (Object o : values.entrySet()) {
                            HashMap.Entry entry = (HashMap.Entry) o;
                            builder.add((String) entry.getKey(), (String) entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                AbnormalRecordReponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<AbnormalRecordReponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    Log.d(TAG, "fetchProcessInstallRecordData run: getCode: "+responseData.getCode());
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                        msg.obj = responseData.getData().getList();
                                    } else if (responseData.getCode() == 400) {
                                        Log.e(TAG, responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 500) {
                                        Log.e(TAG, responseData.getMessage());
                                        Log.d(TAG, "fetchProcessInstallRecordData run: error 500 :"+responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else {
                                        Log.e(TAG, "Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                                msg.obj = "网络请求错误！";
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "网络请求错误！";
                            Log.d(TAG, "run: "+e);
                        } finally {
                            handler.sendMessage(msg);
                            if(response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 获取安装组员名单
     */
    public void fetchInstallerList(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            if (url != null && values != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        for (Object o : values.entrySet()) {
                            HashMap.Entry entry = (HashMap.Entry) o;
                            builder.add((String) entry.getKey(), (String) entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                UserResponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<UserResponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    Log.d(TAG, "fetchProcessInstallRecordData run: getCode: "+responseData.getCode());
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                        msg.obj = responseData.getData().getList();
                                    } else if (responseData.getCode() == 400) {
                                        Log.e(TAG, responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 500) {
                                        Log.e(TAG, responseData.getMessage());
                                        Log.d(TAG, "fetchProcessInstallRecordData run: error 500 :"+responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else {
                                        Log.e(TAG, "Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                                msg.obj = "网络请求失败！";
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "网络请求错误！";
                        } finally {
                            handler.sendMessage(msg);
                            if(response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 获取安装异常类型
     */
    public void fetchAbnormalTypeList(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            if (url != null && values != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        for (Object o : values.entrySet()) {
                            HashMap.Entry entry = (HashMap.Entry) o;
                            builder.add((String) entry.getKey(), (String) entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                AbnormalTypeResponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<AbnormalTypeResponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    Log.d(TAG, "fetchProcessInstallRecordData run: getCode: "+responseData.getCode());
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                        msg.obj = responseData.getData().getList();
                                    } else if (responseData.getCode() == 400) {
                                        Log.e(TAG, responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 500) {
                                        Log.e(TAG, responseData.getMessage());
                                        Log.d(TAG, "fetchProcessInstallRecordData run: error 500 :"+responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else {
                                        Log.e(TAG, "Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                                msg.obj = "网络请求失败！";
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "网络请求错误！";
                        } finally {
                            handler.sendMessage(msg);
                            if(response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 获取装车单文件名
     */
    public void fetchInstallFileList(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            if (url != null && values != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        for (Object o : values.entrySet()) {
                            HashMap.Entry entry = (HashMap.Entry) o;
                            builder.add((String) entry.getKey(), (String) entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                ListDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<ListDataWrap>(){}.getType());
                                if (responseData != null) {
                                    Log.d(TAG, "fetchProcessInstallRecordData run: getCode: "+responseData.getCode());
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                        msg.obj = responseData.getData();
                                    } else if (responseData.getCode() == 400) {
                                        Log.e(TAG, responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 500) {
                                        Log.e(TAG, responseData.getMessage());
                                        Log.d(TAG, "fetchProcessInstallRecordData run: error 500 :"+responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else {
                                        Log.e(TAG, "Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                                msg.obj = "网络请求失败！";
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "网络请求错误！";
                        } finally {
                            handler.sendMessage(msg);
                            if(response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 下载装车单
     */
    public void downloadFile(String url, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
            Log.d(TAG, "downloadFile: 没有网络");
        } else {
            InputStream is = null;
            RandomAccessFile savedFile = null;
            final File file;
            Log.d(TAG, "downloadFile: 有网络");
//            try {
//                long downloadedLength = 0; // 记录已下载的文件长度
                String fileName = url.substring(url.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                file = new File(directory + fileName);
                if (file.exists()) {
                    msg.what=OK;
                    msg.obj=""+file;
                    handler.sendMessage(msg);
                    return;
//                    downloadedLength = file.length();
                }

                Log.d(TAG, "downloadFile: 开始okhttp");
                OkHttpClient mOkHttpClient = new OkHttpClient();
                Log.d(TAG, "downloadFile: 开始同步call");
                final Request request = new Request.Builder().url(url).build();
                final Call call = mOkHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        InputStream is = null;
                        byte[] buf = new byte[2048];
                        int len = 0;
                        FileOutputStream fos = null;
                        try {
                            long total = response.body().contentLength();
                            Log.e(TAG, "total------>" + total);
                            long current = 0;
                            is = response.body().byteStream();
                            fos = new FileOutputStream(file);
                            while ((len = is.read(buf)) != -1) {
                                current += len;
                                fos.write(buf, 0, len);
                                Log.e(TAG, "current------>" + current);
                            }
                            fos.flush();
                            Log.d(TAG, "onResponse: 下载成功！");
                            msg.what = OK;
                            Log.d(TAG, "onResponse: file: "+file);
                            msg.obj = ""+file;
                            Log.d(TAG, "onResponse: obj : "+msg.obj);
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                            Log.d(TAG, "onResponse: catch 下载失败");
                        } finally {
                            handler.sendMessage(msg);
                            try {
                                if (is != null) {
                                    is.close();
                                }
                                if (fos != null) {
                                    fos.close();
                                }
                            } catch (IOException e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    }
                });
//                if (response != null) {
//                    long contentLength = response.body().contentLength();
//                    if (contentLength == 0) {
//                        msg.what = NG;
//                        msg.obj = "装车单为空！";
//                    } else if (contentLength == downloadedLength) {
//                        // 已下载字节和文件总字节相等，说明已经下载完成了
//                        msg.what = OK;
//                        msg.obj = "装车单已存在！";
//                    }
//                    is = response.body().byteStream();
//                    Log.d(TAG, "LocalFile: "+file);
//                    savedFile = new RandomAccessFile(file, "rw");
//                    // 跳过已下载的字节
//                    savedFile.seek(downloadedLength);
//                    byte[] b = new byte[1024];
//                    int total = 0;
//                    int len;
//                    while ((len = is.read(b)) != -1) {
//
//                        total += len;
//                        savedFile.write(b, 0, len);
//                        // 计算已下载的百分比
//                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
//
//                    }
//                    response.body().close();
//                    msg.what = OK;
//                    msg.obj = directory + fileName;
//                    Log.d(TAG, "downloadFile: 成功下载："+fileName);
//                }
//            } catch (Exception e) {
//                msg.what = NG;
//                msg.obj = "下载失败！";
//                e.printStackTrace();
//                Log.d(TAG, "downloadFile: catch报错："+e);
//            } finally {
//                handler.sendMessage(msg);
//                try {
//                    if (is != null) {
//                        is.close();
//                    }
//                    if (savedFile != null) {
//                        savedFile.close();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
       }
    }
    /**
     * 更新位置数据
     */
    public void updateProcessRecordData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            if (url != null && values != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        for (Object o : values.entrySet()) {
                            HashMap.Entry entry = (HashMap.Entry) o;
                            builder.add((String) entry.getKey(), (String) entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url( url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                ResponseData responseData = gson.fromJson(response.body().string(), new TypeToken<ResponseData>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                    } else if (responseData.getCode() == 400) {
                                        msg.obj = responseData.getMessage();
                                    } else {
                                        msg.obj = responseData.getMessage();
                                        Log.e(TAG, "updateProcessRecordData Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "Network error!";
                            Log.d(TAG, "updateProcessRecordData run: network error!");
                        } finally {
                            handler.sendMessage(msg);
                            if(response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 上传图片
     */
    public void uploadTaskRecordImage(final String url, final ArrayList<String> imageUrlList, final LinkedHashMap<String, String> values, final Handler handler) {
        final Message msg = handler.obtainMessage();
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            msg.what = NG;
            msg.obj = mCtx.getString(R.string.network_not_connect);
            handler.sendMessage(msg);
        } else {
            if (url != null && imageUrlList != null && values != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                        Log.d(TAG, "uploadTaskRecordImage: "+imageUrlList.size());
                        for (int i = 0; i <imageUrlList.size() ; i++) {
                            Log.d(TAG, "uploadImgUrl: "+imageUrlList.get(i));
                            File file=new File(imageUrlList.get(i));
                            builder.addFormDataPart("files", file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));
                        }
                        //添加其它信息
                        for (Object o : values.entrySet()) {
                            HashMap.Entry entry = (HashMap.Entry) o;
                            builder.addFormDataPart((String) entry.getKey(), (String) entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp)mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            //同步网络请求
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                ResponseData responseData = gson.fromJson(response.body().string(), new TypeToken<ResponseData>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                    } else if (responseData.getCode() == 400) {
                                        msg.obj = responseData.getMessage();
                                    } else {
                                        Log.e(TAG, "uploadTaskRecordImage Format JSON string to object error!");
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "Network error!";
                            Log.d(TAG, "uploadTaskRecordImage run: network error!");
                        } finally {
                            handler.sendMessage(msg);
                            if(response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }
}
