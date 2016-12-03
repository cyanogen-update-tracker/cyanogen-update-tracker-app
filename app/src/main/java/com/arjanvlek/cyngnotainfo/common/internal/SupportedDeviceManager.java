package com.arjanvlek.cyngnotainfo.common.internal;

import android.os.AsyncTask;

import com.arjanvlek.cyngnotainfo.cos.model.Device;

import java.util.List;

import static com.arjanvlek.cyngnotainfo.common.internal.SettingsManager.PROPERTY_IGNORE_UNSUPPORTED_DEVICE_WARNINGS;
import static com.arjanvlek.cyngnotainfo.common.internal.SystemVersionProperties.SystemType.UNKNOWN;

public class SupportedDeviceManager extends AsyncTask<Void, Void, List<Device>> {

    private SupportedDeviceCallback callback;
    private ApplicationData applicationData;

    public SupportedDeviceManager(Object callback, ApplicationData applicationData) {
        try {
            this.callback = (SupportedDeviceCallback) callback;
        } catch (ClassCastException e) {
            this.callback = null;
        }
        this.applicationData = applicationData;
    }

    @Override
    protected List<Device> doInBackground(Void... params) {
        return applicationData.getDevices();
    }

    @Override
    protected void onPostExecute(List<Device> devices) {


        boolean deviceIsSupported = applicationData.SYSTEM_TYPE != UNKNOWN;

        if(deviceIsSupported) { // To prevent unnecessary device checks.
            SettingsManager settingsManager = new SettingsManager(applicationData.getApplicationContext());
            settingsManager.saveBooleanPreference(PROPERTY_IGNORE_UNSUPPORTED_DEVICE_WARNINGS, true);
        }

        if(callback != null) {
            callback.displayUnsupportedMessage(deviceIsSupported);
        }
    }
}
