package com.arjanvlek.cyngnotainfo;

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

import java.util.List;
import java.util.Locale;

public class GcmNotificationListenerService extends com.google.android.gms.gcm.GcmListenerService {

    public static int NEW_UPDATE_NOTIFICATION_ID = 1;
    public static int NEW_DEVICE_NOTIFICATION_ID = 2;
    public static int MAINTENANCE_NOTIFICATION_ID = 3;

    /**
     * Send a notification when a message is received.
     * @param from From who the notification came (not used in this Application).
     * @param data Data bundle containing the message data.
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        sendNotification(data);

    }

    /**
     * Displays a GCM notification
     * @param msg Bundle with GCM Notification Data.
     */
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
                message = getString(R.string.notification_version) + " " + msg.getString("version_number") + " " + getString(R.string.notification_is_now_available) + " " + deviceName + "!";
                messageType = "update";
            } else if (msg.getString("new_device") != null) {
                String deviceName = getDeviceName(msg.getString("new_device"));
                message = getString(R.string.notification_new_device) + " " + deviceName + " " + getString(R.string.nofitication_new_device_2);
                messageType = "newDevice";
            } else if (msg.getString("new_device") == null && msg.getString("version_number") == null && msg.getString("maintenance") == null && msg.getString("tracking_device_type_id") != null) {
                String deviceName = getDeviceName(msg.getString("tracking_device_type_id"));
                message = getString(R.string.notification_unknown_version_number) + " " + deviceName + "!";
                messageType = "update";

            } else if (msg.getString("maintenance") != null && msg.getString("maintenance_nl") != null) {
                String language = Locale.getDefault().getDisplayLanguage();
                switch (language) {
                    case "Nederlands":
                        String message1 = msg.getString("maintenance_nl");
                        message = message1.replace("_", " ");
                        break;
                    default:
                        String message2 = msg.getString("maintenance");
                        message = message2.replace("_", " ");
                        break;
                }
                messageType = "maintenance";
            }
        }
        if (message != null) {
            switch (messageType) {
                case "update": {
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
                    break;
                }
                case "newDevice": {
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
                    break;
                }
                case "maintenance": {
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

                    break;
                }
            }
        }
    }

    /**
     * Gets a device name from the Application Server by supplying an internal Device ID.
     * @param deviceId Device ID from the database.
     * @return Device name
     */
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