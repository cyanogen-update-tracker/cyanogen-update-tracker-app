package com.arjanvlek.cyngnotainfo.Support;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.arjanvlek.cyngnotainfo.MainActivity;

public class SettingsManager {


    //Offline cache properties
    public static final String PROPERTY_OFFLINE_UPDATE_NAME = "offlineUpdateName";
    public static final String PROPERTY_OFFLINE_UPDATE_DOWNLOAD_SIZE = "offlineUpdateDownloadSize";
    public static final String PROPERTY_OFFLINE_UPDATE_SERVER_UPDATE_TIME = "offlineServerUpdateTime";
    public static final String PROPERTY_OFFLINE_UPDATE_DESCRIPTION = "offlineUpdateDescription";
    public static final String PROPERTY_OFFLINE_UPDATE_ROLLOUT_PERCENTAGE = "offlineUpdateRolloutPercentage";

    //Settings properties
    public static final String FULL_UPDATE = "full_update";
    public static final String INCREMENTAL_UPDATE = "incremental_update";
    public static final String PROPERTY_GCM_REG_ID = "registration_id";
    public static final String PROPERTY_GCM_DEVICE_TYPE = "gcm_device_type";
    public static final String PROPERTY_GCM_UPDATE_TYPE = "gcm_update_type";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String PROPERTY_DEVICE_TYPE = "device_type";
    public static final String PROPERTY_UPDATE_METHOD = "update_type";
    public static final String PROPERTY_REGISTRATION_ERROR = "registration_error";
    public static final String PROPERTY_UPDATE_LINK = "update_link";

    private Context context;

    public SettingsManager(Context context) {
        this.context = context;
    }



    public boolean checkIfSettingsAreValid() {
        return checkPreference(PROPERTY_DEVICE_TYPE) && checkPreference(PROPERTY_UPDATE_METHOD) && checkPreference(PROPERTY_UPDATE_LINK);
    }



    /**
     * Saves a String preference to SharedPreferences.
     * @param key Preference Key
     * @param value Preference Value
     */
    public void savePreference(String key, String value) {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Saves an Integer preference to SharedPreferences.
     * @param key Preference Key
     * @param value Preference Value
     */
    public void saveIntPreference(String key, int value) {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Checks if a certain preference is set.
     * @param key Preference Key
     * @return Returns if the given key is stored in the preferences.
     */
    public boolean checkPreference(String key) {
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.contains(key);
    }

    /**
     * Get a String preference from Shared Preferences
     * @param key Preference Key
     * @return Preference Value
     */
    public String getPreference(String key) {
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }

    /**
     * Get a String preference from Shared Preferences
     * @param key Preference Key
     * @return Preference Value
     */
    public int getIntPreference(String key) {
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, 0);
    }


    /**
     * Fetches the Google Cloud Messaging (GCM) preferences which are stored in a separate file.
     * @return Shared Preferences with GCM preferences.
     */
    public SharedPreferences getGCMPreferences() {
        return context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public int getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
            return 0;
        }
    }

    /**
     * Checks if a device, update method and update link have been set.
     * @return if the application is set up properly.
     */
    public boolean checkIfDeviceIsSet() {
        return checkPreference(PROPERTY_DEVICE_TYPE) && checkPreference(PROPERTY_UPDATE_METHOD) && checkPreference(PROPERTY_UPDATE_LINK);
    }


    /**
     * Checks if the registration token for push notifications is still valid.
     * @return returns if the registration token is valid.
     */
    public boolean checkIfRegistrationIsValid(String deviceType, String updateMethod) {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_GCM_REG_ID, "");
        String registeredDeviceType = prefs.getString(PROPERTY_GCM_DEVICE_TYPE, "");
        String registeredUpdateType = prefs.getString(PROPERTY_GCM_UPDATE_TYPE, "");

        // The registration token is empty, so not valid.
        if (registrationId != null && registrationId.isEmpty()) {
            return false;
        }

        // The registration token does not match the registered device type.
        if (!deviceType.equals(registeredDeviceType)) {
            return false;
        }

        // The registration token does not match the registered update method.
        if (!updateMethod.equals(registeredUpdateType)) {
            return false;
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        return registeredVersion == currentVersion;
    }

    /**
     * Checks if the registration for push notifications has failed before.
     */
    public boolean checkIfRegistrationHasFailed() {
        SharedPreferences preferences = getGCMPreferences();
        return preferences.getBoolean(PROPERTY_REGISTRATION_ERROR, false);
    }


    /**
     * Checks if the update information has been saved before so it can be viewed without a network connection
     * @return true or false.
     */
    public boolean checkIfCacheIsAvailable() {
        try {
            return checkPreference(PROPERTY_OFFLINE_UPDATE_ROLLOUT_PERCENTAGE)
                    && checkPreference(PROPERTY_OFFLINE_UPDATE_DESCRIPTION)
                    && checkPreference(PROPERTY_OFFLINE_UPDATE_SERVER_UPDATE_TIME)
                    && checkPreference(PROPERTY_OFFLINE_UPDATE_DOWNLOAD_SIZE)
                    && checkPreference(PROPERTY_OFFLINE_UPDATE_NAME);
        } catch(Exception ignored) {
            return false;
        }
    }

}
