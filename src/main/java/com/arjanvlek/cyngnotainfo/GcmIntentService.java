package com.arjanvlek.cyngnotainfo;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.arjanvlek.cyngnotainfo.Model.DeviceTypeEntity;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.List;

/**
 * Part of Cyanogen Update Tracker.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                extras.putBoolean("Send error", true);
                sendNotification(extras);
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                extras.putBoolean("deleted", true);
                sendNotification(extras);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                // Post notification of received message.
                sendNotification(extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Bundle msg) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        String message = null;
        if(msg!= null) {
            System.out.println(msg.getString("version_number"));


            if(msg.getString("version_number") != null) {
                String deviceName = getDeviceName(msg.getString("tracking_device_type_id"));
                if(deviceName != null) {
                    message = "Version " + msg.getString("version_number") + " is now available for your " + deviceName + "!";
                }
            }
            else if (msg.getLong("new_device_id", 0) != 0 ) {
                String deviceName = getDeviceName(msg.getString("new_device_id"));
                if(deviceName != null) {
                    message = "A new Cyanogen device can now be tracked: the " + deviceName + "!";
                }
            }
        }
        long[] vibrationPattern = new long[2];
        vibrationPattern[0] = 100L;
        vibrationPattern[1] = 100L;
        if(message != null) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(getString(R.string.app_name))
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(message))
                            .setVibrate(vibrationPattern)
                            .setAutoCancel(true)
                            .setContentText(message);


            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    private String getDeviceName(String deviceId) {
        Long deviceIdLong;
        try {
            deviceIdLong = Long.parseLong(deviceId);
        }
        catch (NumberFormatException ignored) {
            return null;
        }
        String deviceName = null;
        ServerConnector serverConnector = new ServerConnector();
        List<DeviceTypeEntity> deviceTypeEntityList = serverConnector.getDeviceTypeEntities();
            for (DeviceTypeEntity deviceTypeEntity : deviceTypeEntityList) {
                if(deviceTypeEntity.getId() == deviceIdLong) {
                    deviceName = deviceTypeEntity.getDeviceType();
                }
            }
        return deviceName;
    }
}