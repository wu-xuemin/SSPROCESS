package com.example.nan.ssprocess.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.ServerToClientMsg;
import com.example.nan.ssprocess.ui.activity.ProcessToAdminActivity;
import com.example.nan.ssprocess.ui.activity.ProcessToCheckoutActivity;
import com.example.nan.ssprocess.ui.activity.ProcessToInstallActivity;
import com.example.nan.ssprocess.ui.activity.SplashActivity;
import com.example.nan.ssprocess.util.NotificationUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

/**
 * @author nan 2017/11/22
 */
public class MyMqttService extends Service {

    private static final String TAG = "nlgMqttService";
    private static final String TOPIC_MACHINE_STATUS_CHANGE = "/s2c/machine_status_change";
    private static final String TOPIC_TO_QA = "/s2c/task_quality/";
    private static final String TOPIC_TO_NEXT_INSTALL = "/s2c/task_install/";
    private static final String TOPIC_INSTALL_ABNORMAL_RESOLVE = "/s2c/install_abnormal_resolve/";
    private static final String TOPIC_QA_ABNORMAL_RESOLVE = "/s2c/quality_abnormal_resolve/";
    private static final String TOPIC_INSTALL_ABNORMAL = "/s2c/install_abnormal/";
    private static final String TOPIC_QUALITY_ABNORMAL = "/s2c/quality_abnormal/";
    private static final String TOPIC_INSTALL_PLAN = "/s2c/install_plan/";
    private static final String TOPIC_TASK_REMIND = "/s2c/task_remind/";
    /**
     * 发生安装异常时，通知对应质检员
     */
    public static final String TOPIC_INSTALL_ABNORMAL_TO_QUALITY = "/s2c/install_abnormal/quality/";

    private static final String publishTopic = "exampleAndroidPublishTopic";

    private MqttAndroidClient mqttAndroidClient;
    private NotificationManager mNotificationManager;

    public MyMqttService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MqttService onCreate executed");
        String serverIp = SinSimApp.getApp().getServerIP();
        final String serverUri = URL.TCP_HEAD + serverIp.substring(0, serverIp.indexOf(":")) + URL.MQTT_PORT;
        //以用户ID作为client ID
        mqttAndroidClient = new MqttAndroidClient(MyMqttService.this, serverUri, SinSimApp.getApp().getIMEI());
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.d(TAG, "connectComplete: " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeAllTopics();
                } else {
                    Log.d(TAG, "connectComplete: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {

                Log.d(TAG, "connectionLost: connection was lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());
                Log.d(TAG, "Topic: " + topic + " ==> Payload: " + payload);
                if(mNotificationManager == null) {
                    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                }
                int roleId = SinSimApp.getApp().getRole();
                Gson gson = new Gson();
                ServerToClientMsg msg = gson.fromJson(payload, new TypeToken<ServerToClientMsg>(){}.getType());


                if(msg != null) {
                    if( roleId == SinSimApp.LOGIN_FOR_QA) {
                        //质检员接受消息
                        if(topic != null) {
                            if(topic.equals(TOPIC_TO_QA + SinSimApp.getApp().getAppUserId())) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToCheckoutActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("待质检", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_TO_QA,9,pi);

//                                Intent intent = new Intent(MyMqttService.this, ProcessToCheckoutActivity.class);
//                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_TO_QA);
//                                Notification notify = builder.setSmallIcon(R.mipmap.to_quality)
//                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.to_quality))
//                                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                        .setContentTitle("待质检")
//                                        .setAutoCancel(true)
//                                        .setContentIntent(pi)
//                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                        .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                        //不设置此项不会悬挂,false 不会出现悬挂
//                                        .build();
//                                mNotificationManager.notify(9,notify);
                            } else if(topic.equals(TOPIC_QA_ABNORMAL_RESOLVE + SinSimApp.getApp().getAppUserId())) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToCheckoutActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("质检异常解决", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_QA_ABNORMAL_RESOLVE,2, pi);

//                                Intent intent = new Intent(MyMqttService.this, ProcessToCheckoutActivity.class);
//                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_QA_ABNORMAL_RESOLVE);
//                                Notification notify = builder.setSmallIcon(R.mipmap.quality_abnormal_resolve)
//                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.quality_abnormal_resolve))
//                                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                        .setContentTitle("质检异常解决")
//                                        .setAutoCancel(true)
//                                        .setContentIntent(pi)
//                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                        .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                        //不设置此项不会悬挂,false 不会出现悬挂
//                                        .build();
//                                mNotificationManager.notify(2,notify);
                            }else if(topic.equals(TOPIC_INSTALL_ABNORMAL_TO_QUALITY + SinSimApp.getApp().getAppUserId())) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToCheckoutActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("安装异常", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_INSTALL_ABNORMAL_TO_QUALITY,13,pi);

//                                Intent intent = new Intent(MyMqttService.this, ProcessToCheckoutActivity.class);
//                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_INSTALL_ABNORMAL_TO_QUALITY);
//                                Notification notify = builder.setSmallIcon(R.mipmap.install_abnormal)
//                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.install_abnormal))
//                                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                        .setContentTitle("安装异常")
//                                        .setAutoCancel(true)
//                                        .setContentIntent(pi)
//                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                        .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                        //不设置此项不会悬挂,false 不会出现悬挂
//                                        .build();
//                                mNotificationManager.notify(13,notify);
                            }
                        }
                    } else if(roleId == SinSimApp.LOGIN_FOR_ADMIN) {
                        //生产部管理员接受消息
                        if(topic != null) {
                            if(topic.contains(TOPIC_TO_NEXT_INSTALL)) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToAdminActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("待安装", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_TO_NEXT_INSTALL,3, pi);

//                                Intent intent = new Intent(MyMqttService.this, ProcessToAdminActivity.class);
//                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_TO_NEXT_INSTALL);
//                                Notification notify = builder.setSmallIcon(R.mipmap.to_install)
//                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.to_install))
//                                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                        .setContentTitle("待安装")
//                                        .setAutoCancel(true)
//                                        .setContentIntent(pi)
//                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                        .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                        //不设置此项不会悬挂,false 不会出现悬挂
//                                        .build();
//                                mNotificationManager.notify(3,notify);
                            }else if(topic.contains(TOPIC_INSTALL_ABNORMAL_RESOLVE)) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToAdminActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("安装异常解决", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_INSTALL_ABNORMAL_RESOLVE,4,pi);

//                                Intent intent = new Intent(MyMqttService.this, ProcessToAdminActivity.class);
//                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_INSTALL_ABNORMAL_RESOLVE);
//                                Notification notify = builder.setSmallIcon(R.mipmap.install_abnormall_resolve)
//                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.install_abnormall_resolve))
//                                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                        .setContentTitle("安装异常解决")
//                                        .setAutoCancel(true)
//                                        .setContentIntent(pi)
//                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                        .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                        //不设置此项不会悬挂,false 不会出现悬挂
//                                        .build();
//                                mNotificationManager.notify(4,notify);
                            }else if(topic.contains(TOPIC_QA_ABNORMAL_RESOLVE)) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToAdminActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("质检异常解决", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_QA_ABNORMAL_RESOLVE,14,pi);

//                                Intent intent = new Intent(MyMqttService.this, ProcessToAdminActivity.class);
//                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_QA_ABNORMAL_RESOLVE);
//                                Notification notify = builder.setSmallIcon(R.mipmap.quality_abnormal_resolve)
//                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.quality_abnormal_resolve))
//                                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                        .setContentTitle("质检异常解决")
//                                        .setAutoCancel(true)
//                                        .setContentIntent(pi)
//                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                        .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                        //不设置此项不会悬挂,false 不会出现悬挂
//                                        .build();
//                                mNotificationManager.notify(14,notify);
                            } else if(topic.equals(TOPIC_MACHINE_STATUS_CHANGE)) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToAdminActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                String title = null;
                                int iconId = -1;
                                if(msg.getType().equals(ServerToClientMsg.MsgType.ORDER_CHANGE)) {
                                    title = "机器改单";
                                    iconId = R.mipmap.order_change;
                                } else if(msg.getType().equals(ServerToClientMsg.MsgType.ORDER_SPLIT)) {
                                    title = "机器拆单";
                                    iconId = R.mipmap.order_split;
                                } else if(msg.getType().equals(ServerToClientMsg.MsgType.ORDER_CANCEL)) {
                                    title = "机器取消";
                                    iconId = R.mipmap.order_cancel;
                                }
                                if(title != null) {
                                    NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                    notificationUtils.sendNotification(title, "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_MACHINE_STATUS_CHANGE,5,pi);

//                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_MACHINE_STATUS_CHANGE);
//                                    Notification notify = builder.setSmallIcon(iconId)
//                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), iconId))
//                                            .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                            .setContentTitle(title)
//                                            .setAutoCancel(true)
//                                            .setContentIntent(pi)
//                                            .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                            .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                            //不设置此项不会悬挂,false 不会出现悬挂
//                                            .build();
//                                    mNotificationManager.notify(5,notify);
                                }
                            } else if(topic.contains(TOPIC_INSTALL_ABNORMAL)) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToAdminActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("安装异常", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_INSTALL_ABNORMAL,10,pi);

//                                Intent intent = new Intent(MyMqttService.this, ProcessToAdminActivity.class);
//                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_INSTALL_ABNORMAL);
//                                Notification notify = builder.setSmallIcon(R.mipmap.install_abnormal)
//                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.install_abnormal))
//                                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                        .setContentTitle("安装异常")
//                                        .setAutoCancel(true)
//                                        .setContentIntent(pi)
//                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                        .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                        //不设置此项不会悬挂,false 不会出现悬挂
//                                        .build();
//                                mNotificationManager.notify(10,notify);
                            }else if(topic.contains(TOPIC_QUALITY_ABNORMAL)) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToAdminActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("质检异常", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_QUALITY_ABNORMAL,11,pi);

//                                Intent intent = new Intent(MyMqttService.this, ProcessToAdminActivity.class);
//                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_QUALITY_ABNORMAL);
//                                Notification notify = builder.setSmallIcon(R.mipmap.quality_abnormal)
//                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.quality_abnormal))
//                                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                        .setContentTitle("质检异常")
//                                        .setAutoCancel(true)
//                                        .setContentIntent(pi)
//                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                        .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                        //不设置此项不会悬挂,false 不会出现悬挂
//                                        .build();
//                                mNotificationManager.notify(11,notify);
                            }
                        }
                    } else if(roleId == SinSimApp.LOGIN_FOR_INSTALL) {
                        //安装组长接受消息
                        if(topic != null) {
                            if(topic.equals(TOPIC_TO_NEXT_INSTALL + SinSimApp.getApp().getGroupId())) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToInstallActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("待安装", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_TO_NEXT_INSTALL,6,pi);

//                                Intent intent = new Intent(MyMqttService.this, ProcessToInstallActivity.class);
//                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_TO_NEXT_INSTALL);
//                                Notification notify = builder.setSmallIcon(R.mipmap.to_install)
//                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.to_install))
//                                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                        .setContentTitle("待安装")
//                                        .setAutoCancel(true)
//                                        .setContentIntent(pi)
//                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                        .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                        //不设置此项不会悬挂,false 不会出现悬挂
//                                        .build();
//                                mNotificationManager.notify(6,notify);
                            }else if(topic.equals(TOPIC_INSTALL_ABNORMAL_RESOLVE + SinSimApp.getApp().getGroupId())) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToInstallActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("安装异常解决", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_INSTALL_ABNORMAL_RESOLVE,7,pi);

//                                Intent intent = new Intent(MyMqttService.this, ProcessToInstallActivity.class);
//                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_INSTALL_ABNORMAL_RESOLVE);
//                                Notification notify = builder.setSmallIcon(R.mipmap.install_abnormall_resolve)
//                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.install_abnormall_resolve))
//                                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                        .setContentTitle("安装异常解决")
//                                        .setAutoCancel(true)
//                                        .setContentIntent(pi)
//                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                        .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                        //不设置此项不会悬挂,false 不会出现悬挂
//                                        .build();
//                                mNotificationManager.notify(7,notify);
                            }else if(topic.equals(TOPIC_MACHINE_STATUS_CHANGE)) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToInstallActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                String title = null;
                                int iconId = -1;
                                if(msg.getType().equals(ServerToClientMsg.MsgType.ORDER_CHANGE)) {
                                    title = "机器改单";
                                    iconId = R.mipmap.order_change;
                                } else if(msg.getType().equals(ServerToClientMsg.MsgType.ORDER_SPLIT)) {
                                    title = "机器拆单";
                                    iconId = R.mipmap.order_split;
                                } else if(msg.getType().equals(ServerToClientMsg.MsgType.ORDER_CANCEL)) {
                                    title = "机器取消";
                                    iconId = R.mipmap.order_cancel;
                                }
                                Log.d(TAG, "messageArrived: "+title);
                                if(title != null) {
                                    NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                    notificationUtils.sendNotification(title, "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_MACHINE_STATUS_CHANGE,8,pi);

//                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_MACHINE_STATUS_CHANGE);
//                                    Notification notify = builder.setSmallIcon(iconId)
//                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), iconId))
//                                            .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                            .setContentTitle(title)
//                                            .setAutoCancel(true)
//                                            .setContentIntent(pi)
//                                            .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                            .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                            //不设置此项不会悬挂,false 不会出现悬挂
//                                            .build();
//                                    mNotificationManager.notify(8,notify);
                                }
                            }else if(topic.equals(TOPIC_QUALITY_ABNORMAL + SinSimApp.getApp().getGroupId())) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToInstallActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("质检异常", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_QUALITY_ABNORMAL,12,pi);

//                                Intent intent = new Intent(MyMqttService.this, ProcessToInstallActivity.class);
//                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
//                                NotificationCompat.Builder builder = new NotificationCompat.Builder(MyMqttService.this, TOPIC_QUALITY_ABNORMAL);
//                                Notification notify = builder.setSmallIcon(R.mipmap.quality_abnormal)
//                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.quality_abnormal))
//                                        .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
//                                        .setContentTitle("质检异常")
//                                        .setAutoCancel(true)
//                                        .setContentIntent(pi)
//                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
//                                        .setContentText("需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate())
//                                        //不设置此项不会悬挂,false 不会出现悬挂
//                                        .build();
//                                mNotificationManager.notify(12,notify);
                            }else if(topic.equals(TOPIC_INSTALL_PLAN + SinSimApp.getApp().getGroupId())) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToInstallActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("排班计划", "后续排班计划已送达！", TOPIC_INSTALL_PLAN,16,pi);
                            }else if(topic.equals(TOPIC_TASK_REMIND + SinSimApp.getApp().getGroupId())) {
                                Intent intent = new Intent(MyMqttService.this, ProcessToInstallActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(MyMqttService.this, 0, intent, 0);
                                NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
                                notificationUtils.sendNotification("漏扫提醒", "需求单号：" + msg.getOrderNum() + " | 机器编号：" + msg.getNameplate(), TOPIC_TASK_REMIND,17,pi);
                            }
                        }
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //即服务器成功delivery消息
            }
        });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        //断开后，是否自动连接
        mqttConnectOptions.setAutomaticReconnect(true);
        //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
        mqttConnectOptions.setCleanSession(false);
        //设置超时时间，单位为秒
        //mqttConnectOptions.setConnectionTimeout(2);
        //心跳时间，单位为秒。即多长时间确认一次Client端是否在线
        //mqttConnectOptions.setKeepAliveInterval(2);
        //允许同时发送几条消息（未收到broker确认信息）
        //mqttConnectOptions.setMaxInflight(10);
        //选择MQTT版本
        mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        try {
            Log.d(TAG, "onCreate: Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess: Success to connect to " + serverUri);
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeAllTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure: Failed to connect to " + serverUri);
                    exception.printStackTrace();
                    Log.d(TAG, "onFailure: "+exception);
                }
            });
        } catch (MqttException ex) {
            ex.printStackTrace();
        }


        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra(SinSimApp.FROM_NOTIFICATION, true);
        PendingIntent pi = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationUtil notificationUtils =new NotificationUtil(MyMqttService.this);
        notificationUtils.sendNotification("浙江信胜", "作业流程管理系统", "浙江信胜",1,pi);

        //这边设置“FLAG_UPDATE_CURRENT”是为了让后面的Activity接收pendingIntent中Extra的数据
//        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification notification = new NotificationCompat.Builder(this)
//                .setContentTitle("浙江信胜")
//                .setContentText("作业流程管理系统")
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//                .setContentIntent(pi)
//                .build();
//        startForeground(1, notification);
    }







    private void subscribeAllTopics() {
        if(SinSimApp.getApp().getRole() == SinSimApp.LOGIN_FOR_QA) {
            //User ID不为零则有效
            if(SinSimApp.getApp().getAppUserId() != 0) {
                //质检员订阅质检消息
                subscribeToTopic(TOPIC_TO_QA + SinSimApp.getApp().getAppUserId());
                //质检员订阅质检异常恢复消息
                subscribeToTopic(TOPIC_QA_ABNORMAL_RESOLVE + SinSimApp.getApp().getAppUserId());
                //质检员订阅安装异常
                subscribeToTopic(TOPIC_INSTALL_ABNORMAL_TO_QUALITY + SinSimApp.getApp().getAppUserId());
            }
        } else {
            //安装组长和安装部管理员订阅改单、拆单消息
            subscribeToTopic(TOPIC_MACHINE_STATUS_CHANGE);
            if(SinSimApp.getApp().getRole() == SinSimApp.LOGIN_FOR_ADMIN) {
                //生产部管理员订阅全部安装消息
                subscribeToTopic(TOPIC_TO_NEXT_INSTALL + "#");
                //生产部管理员订阅全部安装异常恢复消息
                subscribeToTopic(TOPIC_INSTALL_ABNORMAL_RESOLVE + "#");
                //生产部管理员订阅质检异常恢复消息
                subscribeToTopic(TOPIC_QA_ABNORMAL_RESOLVE + "#");
                //发生安装异常时，通知生产部管理员
                subscribeToTopic(TOPIC_INSTALL_ABNORMAL_TO_QUALITY + "#");
                //发生质检异常时，通知生产部管理员
                subscribeToTopic(TOPIC_QUALITY_ABNORMAL + "#");
            }
            if(SinSimApp.getApp().getRole() == SinSimApp.LOGIN_FOR_INSTALL) {
                if(SinSimApp.getApp().getGroupId() > 0) {
                    //安装组长订阅自己组安装消息
                    subscribeToTopic(TOPIC_TO_NEXT_INSTALL + SinSimApp.getApp().getGroupId());
                    //安装组长订阅自己租的安装异常恢复消息
                    subscribeToTopic(TOPIC_INSTALL_ABNORMAL_RESOLVE + SinSimApp.getApp().getGroupId());
                    //发生质检异常时，通知对应安装组长
                    subscribeToTopic(TOPIC_QUALITY_ABNORMAL + SinSimApp.getApp().getGroupId());
                    //安装组长订阅自己组的排班计划
                    subscribeToTopic(TOPIC_INSTALL_PLAN + SinSimApp.getApp().getGroupId());
                    //安装组长订阅別人發給自己的安裝提醒
                    subscribeToTopic(TOPIC_TASK_REMIND + SinSimApp.getApp().getGroupId());
                }
            }
        }
    }

    /**
     * 订阅消息
     */
    public void subscribeToTopic(String subscriptionTopic) {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 2, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess: Success to Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure: Failed to subscribe");
                }
            });
        } catch (MqttException ex) {
            Log.d(TAG, "subscribeToTopic: Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    /**
     * 发布消息
     */
    public void publishMessage(String msg) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            Log.d(TAG, "publishMessage: Message Published: " + msg);
        } catch (MqttException e) {
            Log.d(TAG, "publishMessage: Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MqttService onStartCommand executed");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if(mqttAndroidClient!=null){
                mqttAndroidClient.disconnect();
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.close();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "MqttService onDestroy executed");
    }
}
