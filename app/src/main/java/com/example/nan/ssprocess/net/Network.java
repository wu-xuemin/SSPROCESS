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
import com.example.nan.ssprocess.bean.response.ProcessModelsResponseDataWrap;
import com.example.nan.ssprocess.bean.response.ProcessRecordResponseData;
import com.example.nan.ssprocess.bean.response.ProcessRecordResponseDataWrap;
import com.example.nan.ssprocess.bean.response.ResponseData;
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
                            boolean success = false;
                            if (response.isSuccessful()) {
                                Gson gson = new Gson();
                                LoginResponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<LoginResponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getStatus() == 1) {
                                        success = true;
                                        msg.obj = responseData.getData();
                                    } else if (responseData.getStatus() == 0) {
                                        Log.e(TAG, responseData.getInfo());
                                        msg.obj = responseData.getInfo();
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
                                    if (responseData.getStatus() == 1) {
                                        success = true;
                                    } else if (responseData.getStatus() == 0) {
                                        Log.e(TAG, responseData.getInfo());
                                        errorMsg = responseData.getInfo();
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
                                    if (responseData.getStatus() == 1) {
                                        success = true;
                                        msg.obj = responseData.getData();
                                    } else if (responseData.getStatus() == 0) {
                                        Log.e(TAG, responseData.getInfo());
                                        msg.obj = responseData.getInfo();
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
                                    if (responseData.getStatus() == 1) {
                                        success = true;
                                        msg.obj = responseData.getData();//Guidance的内容
                                    } else if (responseData.getStatus() == 0) {
                                        Log.e(TAG, responseData.getInfo());
                                        msg.obj = responseData.getInfo();
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
                                ProcessModelsResponseDataWrap responseData = gson.fromJson(response.body().string(), new TypeToken<ProcessModelsResponseDataWrap>(){}.getType());
                                if (responseData != null) {
                                    if (responseData.getStatus() == 1) {
                                        success = true;
                                        msg.obj = responseData.getData();
                                    } else if (responseData.getStatus() == 0) {
                                        Log.e(TAG, responseData.getInfo());
                                        msg.obj = responseData.getInfo();
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
                                    if (responseData.getStatus() == 1) {
                                        success = true;
                                    } else if (responseData.getStatus() == 0) {
                                        msg.obj = responseData.getInfo();
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
                                    msg.obj = responseData.getInfo();
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
                                    if (responseData.getStatus() == 1) {
                                        success = true;
                                        msg.obj = responseData.getInfo();
                                    } else if (responseData.getStatus() == 0) {
                                        msg.obj = responseData.getInfo();
                                    }
                                }
                                if (success) {
                                    msg.what = OK;
                                }
                            } else {
                                msg.what = NG;
                                if(responseData != null) {
                                    msg.obj = responseData.getInfo();
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
                                    if (responseData.getStatus() == 1) {
                                        success = true;
                                        msg.obj = responseData.getData();
                                    } else if (responseData.getStatus() == 0) {
                                        msg.obj = responseData.getInfo();
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
                                    if (responseData.getStatus() == 1) {
                                        success = true;
                                    } else if (responseData.getStatus() == 0) {
                                        msg.obj = responseData.getInfo();
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
                                    if (responseData.getStatus() == 1) {
                                        success = true;
                                    } else if (responseData.getStatus() == 0) {
                                        msg.obj = responseData.getInfo();
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
