package com.example.nan.ssprocess.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.nan.ssprocess.R;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.VISIBILITY_SECRET;

public class NotificationUtil extends ContextWrapper {

    private NotificationManager mManager;
    private static final String TAG = "nlgNotificationUtil";

    public NotificationUtil(Context context) {
        super(context);
    }

    public void sendNotification(String title, String content, String channelId, int notifyId, PendingIntent pi) {
        if (Build.VERSION.SDK_INT >= 26) {
            createNotificationChannel(channelId);
            Notification notification = getNotification_26(title, content, channelId, pi).build();
            getmManager().notify(notifyId, notification);
        } else {
            Notification notification = getNotification_25(title, content, channelId, pi).build();
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

        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_NOTIFICATION);
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
        //获取通知取到组
        channel.getGroup();
        //设置可绕过  请勿打扰模式
        channel.setBypassDnd(true);
        //设置震动模式
        channel.setVibrationPattern(new long[]{0,1300,500,1700});
        //是否会有灯光
        channel.shouldShowLights();
        getmManager().createNotificationChannel(channel);
    }

    public NotificationCompat.Builder getNotification_25(String title, String content, String channelId, PendingIntent pi) {
        return new NotificationCompat.Builder(getApplicationContext(),channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationCompat.Builder getNotification_26(String title, String content, String channelId, PendingIntent pi) {
        return new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher)))
                .setContentIntent(pi)
                //设置默认的三色灯与振动器
                .setDefaults(Notification.DEFAULT_LIGHTS)
                //设置LED闪烁
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setTicker(content)
                .setNumber(1)
                .setAutoCancel(true);
    }

}
