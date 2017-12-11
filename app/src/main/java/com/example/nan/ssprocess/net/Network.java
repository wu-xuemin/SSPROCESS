package com.example.nan.ssprocess.net;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.activity.LoginActivity;
import com.example.nan.ssprocess.activity.UpdateOperationStatusListener;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.bean.response.GuidanceResponseDataWrap;
import com.example.nan.ssprocess.bean.response.LoginResponseDataWrap;
import com.example.nan.ssprocess.bean.response.ProcessRecordResponseDataWrap;
import com.example.nan.ssprocess.bean.response.ResponseData;
import com.example.nan.ssprocess.bean.response.TaskRecordDataWrap;
import com.example.nan.ssprocess.bean.response.ToolsPackageResponseDataWrap;
import com.example.nan.ssprocess.util.LogUtils;
import com.example.nan.ssprocess.util.ShowMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Network {
    private static String TAG = "nlgNetwork";
    private static Network mNetWork;
    private static Application mCtx;
    private static ThreadPoolExecutor executor;
    private static final int CORE_THREAD_NUM = 3;
    public static final int OK = 1;
    public static final int NG = 0;

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
    /*判断是否有网络连接
    * */
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

    public void fetchLoginData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        if (!isNetworkConnected()) {
            Log.d(TAG, "fetchLoginData: 没网络");
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
        } else {
            Log.d(TAG, "fetchLoginData: 有网络");
            if (url != null && values != null && handler != null) {
                Log.d(TAG, "fetchLoginData: not null");
                final Message msg = handler.obtainMessage();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        Iterator iterator = values.entrySet().iterator();//map的迭代器
                        while (iterator.hasNext()) {
                            HashMap.Entry entry = (HashMap.Entry)iterator.next();
                            builder.add((String)entry.getKey(), (String)entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();//同步网络请求
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Log.d(TAG, "run: response success");
                                Gson gson = new Gson();
                                LoginResponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<LoginResponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    Log.d(TAG, "run: responseData："+responseData.getCode());
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
                                        Log.d(TAG, "run: error 400 :"+responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 500) {
                                        Log.e(TAG, responseData.getMessage());
                                        Log.d(TAG, "run: error 500 :"+responseData.getMessage());
                                        msg.obj = responseData.getMessage();
                                    }else {
                                        Log.e(TAG, "Format JSON string to object error!");
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
                            Log.d(TAG, "run: catch "+e);
                        } finally {
                            handler.sendMessage(msg);
                            Log.d(TAG, "run: finally");
                            if(response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    public void updateOperationStatus(final String url, final LinkedHashMap<String, String> values, final UpdateOperationStatusListener listener){
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
        } else {
            if (url != null && values != null && listener != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        Iterator iterator = values.entrySet().iterator();
                        while (iterator.hasNext()) {
                            HashMap.Entry entry = (HashMap.Entry)iterator.next();
                            builder.add((String) entry.getKey(), (String)entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url( url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        String errorMsg = "";
                        boolean success = false;
                        try {
                            response = client.newCall(request).execute();
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                ResponseData responseData = gson.fromJson(response.body().string(), new TypeToken<ResponseData>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getCode() == 1) {
                                        success = true;
                                    } else if (responseData.getCode() == 0) {
                                        Log.e(TAG, responseData.getMessage());
                                        errorMsg = responseData.getMessage();
                                    } else {
                                        Log.e(TAG, "Format JSON string to object error!");
                                    }
                                }
                            }
                            response.close();
                        } catch (Exception e) {
                            errorMsg = "Network error!";
                        } finally {
                            listener.onUpdateOperationStatus(success, errorMsg);
                            if(response != null) {
                                response.close();
                            }
                        }
                    }
                });
            }
        }
    }

    public void fetchToolsPackageData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
        } else {
            if (url != null && values != null && handler != null) {
                final Message msg = handler.obtainMessage();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        Iterator iterator = values.entrySet().iterator();
                        while (iterator.hasNext()) {
                            HashMap.Entry entry = (HashMap.Entry)iterator.next();
                            builder.add((String) entry.getKey(), (String)entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url( url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                ToolsPackageResponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<ToolsPackageResponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getCode() == 1) {
                                        success = true;
                                        msg.obj = responseData.getData();
                                    } else if (responseData.getCode() == 0) {
                                        Log.e(TAG, responseData.getMessage());
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
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "Network error!";
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

    public void fetchGuidanceData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
        } else {
            if (url != null && values != null && handler != null) {
                final Message msg = handler.obtainMessage();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        Iterator iterator = values.entrySet().iterator();
                        while (iterator.hasNext()) {
                            HashMap.Entry entry = (HashMap.Entry)iterator.next();
                            builder.add((String) entry.getKey(), (String)entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url( url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                GuidanceResponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<GuidanceResponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getCode() == 1) {
                                        success = true;
                                        msg.obj = responseData.getData();//Guidance的内容
                                    } else if (responseData.getCode() == 0) {
                                        Log.e(TAG, responseData.getMessage());
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
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "Network error!";
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

    public void fetchProcessModuleData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
            Log.d(TAG, "fetchProcessModuleData: network_not_connect");
        } else {
            if (url != null && values != null && handler != null) {
                final Message msg = handler.obtainMessage();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        Iterator iterator = values.entrySet().iterator();
                        while (iterator.hasNext()) {
                            HashMap.Entry entry = (HashMap.Entry)iterator.next();
                            builder.add((String) entry.getKey(), (String)entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url( url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                TaskRecordDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<TaskRecordDataWrap>(){}.getType());
                                Log.d(TAG, "run: "+responseData.getCode());
                                if (responseData != null) {
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                        msg.obj = responseData.getData().getList();
                                    } else if (responseData.getCode() == 400) {
                                        Log.e(TAG, responseData.getMessage());
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
                            }
                            response.close();
                        } catch (Exception e) {
                            Log.d(TAG, "run: "+e);
                            msg.what = NG;
                            msg.obj = "Network error!";
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
     *添加流程模板记录
     * @param url
     * @param values
     * @param handler
     */
    public void addProcessRecordData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
        } else {
            if (url != null && values != null && handler != null) {
                final Message msg = handler.obtainMessage();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        Iterator iterator = values.entrySet().iterator();
                        while (iterator.hasNext()) {
                            HashMap.Entry entry = (HashMap.Entry)iterator.next();
                            builder.add((String) entry.getKey(), (String)entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url( url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            ResponseData responseData = null;
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                responseData = gson.fromJson(response.body().string(), new TypeToken<ResponseData>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getCode() == 1) {
                                        success = true;
                                    } else if (responseData.getCode() == 0) {
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
                                if(responseData != null) {
                                    msg.obj = responseData.getMessage();
                                }
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "网络出错！";
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
     *添加流程模板记录
     * @param url
     * @param values
     * @param handler
     */
    public void fetchProcessRecordStatusData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
        } else {
            if (url != null && values != null && handler != null) {
                final Message msg = handler.obtainMessage();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        Iterator iterator = values.entrySet().iterator();
                        while (iterator.hasNext()) {
                            HashMap.Entry entry = (HashMap.Entry)iterator.next();
                            builder.add((String) entry.getKey(), (String)entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            ResponseData responseData = null;
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                responseData = gson.fromJson(response.body().string(), new TypeToken<ResponseData>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                        msg.obj = responseData.getMessage();
                                    } else if (responseData.getCode() == 400) {
                                        msg.obj = responseData.getMessage();
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                                if(responseData != null) {
                                    msg.obj = responseData.getMessage();
                                }
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

    public void fetchProcessRecordData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
        } else {
            if (url != null && values != null && handler != null) {
                final Message msg = handler.obtainMessage();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        Iterator iterator = values.entrySet().iterator();
                        while (iterator.hasNext()) {
                            HashMap.Entry entry = (HashMap.Entry)iterator.next();
                            builder.add((String) entry.getKey(), (String)entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url( url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                ProcessRecordResponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<ProcessRecordResponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getCode() == 200) {
                                        success = true;
                                        msg.obj = responseData.getData();
                                    } else if (responseData.getCode() == 400) {
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
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "Network error!";
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

    public void updateProcessRecordData(final String url, final LinkedHashMap<String, String> values, final Handler handler) {
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
        } else {
            if (url != null && values != null && handler != null) {
                final Message msg = handler.obtainMessage();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestBody requestBody;
                        FormBody.Builder builder = new FormBody.Builder();
                        Iterator iterator = values.entrySet().iterator();
                        while (iterator.hasNext()) {
                            HashMap.Entry entry = (HashMap.Entry)iterator.next();
                            builder.add((String) entry.getKey(), (String)entry.getValue());
                        }
                        requestBody = builder.build();
                        //Post method
                        Request request = new Request.Builder().url( url).post(requestBody).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                ResponseData responseData = gson.fromJson(response.body().string(), new TypeToken<ResponseData>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getCode() == 1) {
                                        success = true;
                                    } else if (responseData.getCode() == 0) {
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
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "Network error!";
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

    public void printTable(final String url, final Handler handler){
        if (!isNetworkConnected()) {
            ShowMessage.showToast(mCtx, mCtx.getString(R.string.network_not_connect), ShowMessage.MessageDuring.SHORT);
        } else {
            if (url != null && handler != null) {
                final Message msg = handler.obtainMessage();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        //Get method
                        Request request = new Request.Builder().url(url).build();
                        OkHttpClient client = ((SinSimApp) mCtx).getOKHttpClient();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                ResponseData responseData = gson.fromJson(response.body().string(), new TypeToken<ResponseData>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getCode() == 1) {
                                        success = true;
                                    } else if (responseData.getCode() == 0) {
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
                            }
                            response.close();
                        } catch (Exception e) {
                            msg.what = NG;
                            msg.obj = "Network error!";
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
