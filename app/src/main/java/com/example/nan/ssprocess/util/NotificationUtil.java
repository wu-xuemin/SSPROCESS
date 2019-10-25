package com.example.nan.ssprocess.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.nan.ssprocess.R;

import static android.app.Notification.VISIBILITY_SECRET;

public class NotificationUtil extends ContextWrapper {

    private NotificationManager mManager;
    private static final String TAG = "nlgNotificationUtil";

    public NotificationUtil(Context context) {
        super(context);
    }

    public void sendNotification(String title, String content, String channelId, int notifyId) {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel(channelId);
            Notification notification = getNotification_26(title, content, channelId).build();
            getmManager().notify(notifyId, notification);
        } else {
            Notification notification = getNotification_25(title, content, channelId).build();
            getmManager().notify(notifyId, notification);
        }
    }

    private NotificationManager getmManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannel(String channelId) {
        NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH);
        //是否绕过请勿打扰模式
        channel.canBypassDnd();
        //闪光灯
        channel.enableLights(true);
        //锁屏显示通知
        channel.setLockscreenVisibility(VISIBILITY_SECRET);
        //闪关灯的灯光颜色
        channel.setLightColor(Color.RED);
        //桌面launcher的消息角标
        channel.canShowBadge();
        //是否允许震动
        channel.enableVibration(true);
        //获取系统通知响铃声音的配置
        channel.getAudioAttributes();
        //获取通知取到组
        channel.getGroup();
        //设置可绕过  请勿打扰模式
        channel.setBypassDnd(true);
        //设置震动模式
        channel.setVibrationPattern(new long[]{100,200,300,400});
        //是否会有灯光
        channel.shouldShowLights();
        getmManager().createNotificationChannel(channel);
    }

    public NotificationCompat.Builder getNotification_25(String title, String content, String channelId) {

        // 以下是展示大图的通知
        android.support.v4.app.NotificationCompat.BigPictureStyle style = new android.support.v4.app.NotificationCompat.BigPictureStyle();
        style.setBigContentTitle(title);
        style.setSummaryText(content);
        style.bigPicture(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));

        // 以下是展示多文本通知
        android.support.v4.app.NotificationCompat.BigTextStyle style1 = new android.support.v4.app.NotificationCompat.BigTextStyle();
        style1.setBigContentTitle(title);
        style1.bigText(content);

        return new NotificationCompat.Builder(getApplicationContext(),channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setStyle(style)
                .setAutoCancel(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification_26(String title, String content, String channelId) {
        return new Notification.Builder(getApplicationContext(), channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setStyle(new Notification.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher)))
                .setNumber(1)
                .setVibrate(new long[]{100,200,300})
                .setAutoCancel(true);
    }

}
