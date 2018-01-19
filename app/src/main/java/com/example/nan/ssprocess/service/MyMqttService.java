package com.example.nan.ssprocess.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.ui.activity.LoginActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * @author nan 2017/11/22
 */
public class MyMqttService extends Service {

    private static final String TAG="nlgMqttService";
    private final String clientId = "ExampleAndroidClient";
    private final String serverUri = "tcp://192.168.1.4:1883";
    private final String subscriptionTopic = "exampleAndroidPublishTopic";
    private static final String publishTopic = "exampleAndroidPublishTopic";

    private MqttAndroidClient mqttAndroidClient;


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

        mqttAndroidClient = new MqttAndroidClient(MyMqttService.this, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                                          @Override
                                          public void connectComplete(boolean reconnect, String serverURI) {
                                              if (reconnect) {
                                                  Log.d(TAG, "connectComplete: " + serverURI);
                                                  // Because Clean Session is true, we need to re-subscribe
                                                  subscribeToTopic();
                                              } else {
                                                  Log.d(TAG, "connectComplete: " + serverURI);
                                              }
                                          }

                                          @Override
                                          public void connectionLost(Throwable cause) {
                                              Log.d(TAG, "connectionLost: connection was lost");
                                          }

                                          @Override
                                          public void messageArrived(String topic, MqttMessage message) throws Exception {
                                              String content = new String(message.getPayload());
                                              Log.d(TAG, "messageArrived: " + content);
                                          }

                                          @Override
                                          public void deliveryComplete(IMqttDeliveryToken token) {

                                          }
                                      });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        try {
            Log.d(TAG, "onCreate: Connecting to " + serverUri);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d(TAG, "onSuccess: Success to connect to "+serverUri);
                            DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                            disconnectedBufferOptions.setBufferEnabled(true);
                            disconnectedBufferOptions.setBufferSize(100);
                            disconnectedBufferOptions.setPersistBuffer(false);
                            disconnectedBufferOptions.setDeleteOldestMessages(false);
                            mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                            subscribeToTopic();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.d(TAG, "onFailure: Failed to connect to " + serverUri);
                            exception.printStackTrace();
                        }
                    });
        } catch (MqttException ex){
            ex.printStackTrace();
        }



        Intent intent = new Intent(this, LoginActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("This is content title")
                .setContentText("This is content text")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .build();
        startForeground(1, notification);
    }

    //订阅消息
    public void subscribeToTopic() {
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
        }catch (MqttException ex){
            Log.d(TAG, "subscribeToTopic: Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    //发布消息
    public void publishMessage(String msg){
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
        Log.d(TAG, "MqttService onDestroy executed");
    }
}
