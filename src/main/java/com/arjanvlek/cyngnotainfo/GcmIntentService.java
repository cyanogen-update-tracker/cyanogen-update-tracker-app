package com.arjanvlek.cyngnotainfo;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.arjanvlek.cyngnotainfo.Model.DeviceTypeEntity;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.List;
import java.util.Locale;

/**
 * Part of Cyanogen Update Tracker.
 */
public class GcmIntentService extends IntentService {
    public static int NEW_UPDATE_NOTIFICATION_ID = 1;
    public static int NEW_DEVICE_NOTIFICATION_ID = 2;
    public static int MAINTENANCE_NOTIFICATION_ID = 3;

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
        String messageType = "none";
        if (msg != null) {

            if (msg.getString("version_number") != null) {
                String deviceName = getDeviceName(msg.getString("tracking_device_type_id"));
                if (deviceName != null) {
                    message = getString(R.string.notification_version) + " " + msg.getString("version_number") + " " + getString(R.string.notification_is_now_available) + " " + deviceName + "!";
                    messageType = "update";
                }
            } else if (msg.getString("new_device") != null) {
                String deviceName = getDeviceName(msg.getString("new_device"));
                if (deviceName != null) {
                    message = getString(R.string.notification_new_device) + " " + deviceName + " " + getString(R.string.nofitication_new_device_2);
                    messageType = "newDevice";
                }
            }
            else if (msg.getString("new_device") == null && msg.getString("version_number") == null) {
                String deviceName = getDeviceName(msg.getString("tracking_device_type_id"));
                if (deviceName != null) {
                    message = getString(R.string.notification_unknown_version_number) + " " + deviceName + "!";
                    messageType = "update";
                }

            }
            else if(msg.getString("maintenance") != null && msg.getString("maintenance_nl") != null) {
                String language = Locale.getDefault().getDisplayLanguage();
                switch(language) {
                    case "Nederlands":
                        message = msg.getString("maintenance_nl");
                        break;
                    default:
                        message = msg.getString("maintenance");
                        break;
                }
                messageType = "maintenance";
            }
        }
        if (message != null) {
            if (messageType.equals("update")) {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_stat_notification_update)
                                .setContentTitle(getString(R.string.app_name))
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(message)
                                        .setSummaryText(getString(R.string.notification_update_short)))
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setAutoCancel(true)
                                .setContentText(message);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                    mBuilder.setPriority(Notification.PRIORITY_HIGH);
                }
                mBuilder.setContentIntent(contentIntent);
                mNotificationManager.notify(NEW_UPDATE_NOTIFICATION_ID, mBuilder.build());
            } else if (messageType.equals("newDevice")) {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_stat_notification_new_phone)
                                .setContentTitle(getString(R.string.app_name))
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(message)
                                        .setSummaryText(getString(R.string.notification_new_device_short)))
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setAutoCancel(true)
                                .setContentText(message);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                    mBuilder.setPriority(Notification.PRIORITY_HIGH);
                }
                mBuilder.setContentIntent(contentIntent);
                mNotificationManager.notify(NEW_DEVICE_NOTIFICATION_ID, mBuilder.build());
            } else if (messageType.equals("maintenance")) {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_stat_notification_maintenance)
                                .setContentTitle(getString(R.string.app_name))
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(message))
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setAutoCancel(true)
                                .setContentText(message);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                    mBuilder.setPriority(Notification.PRIORITY_HIGH);
                }
                mBuilder.setContentIntent(contentIntent);
                mNotificationManager.notify(MAINTENANCE_NOTIFICATION_ID, mBuilder.build());

            }
        }
    }

    private String getDeviceName(String deviceId) {
        Long deviceIdLong;
        try {
            deviceIdLong = Long.parseLong(deviceId);
        } catch (NumberFormatException ignored) {
            return null;
        }
        String deviceName = null;
        ServerConnector serverConnector = new ServerConnector();
        List<DeviceTypeEntity> deviceTypeEntityList = serverConnector.getDeviceTypeEntities();
        for (DeviceTypeEntity deviceTypeEntity : deviceTypeEntityList) {
            if (deviceTypeEntity.getId() == deviceIdLong) {
                deviceName = deviceTypeEntity.getDeviceType();
            }
        }
        return deviceName;
    }
}