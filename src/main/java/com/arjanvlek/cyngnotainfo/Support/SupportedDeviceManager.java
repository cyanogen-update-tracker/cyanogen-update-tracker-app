package com.arjanvlek.cyngnotainfo.Support;

import android.os.AsyncTask;

import com.arjanvlek.cyngnotainfo.ApplicationContext;
import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.Model.SystemVersionProperties;

import java.util.List;

import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_IGNORE_UNSUPPORTED_DEVICE_WARNINGS;

public class SupportedDeviceManager extends AsyncTask<Void, Void, List<Device>> {

    private SupportedDeviceCallback callback;
    private ApplicationContext applicationContext;

    public SupportedDeviceManager(Object callback, ApplicationContext applicationContext) {
        try {
            this.callback = (SupportedDeviceCallback) callback;
        } catch (ClassCastException e) {
            this.callback = null;
        }
        this.applicationContext = applicationContext;
    }

    @Override
    protected List<Device> doInBackground(Void... params) {
        return applicationContext.getDevices();
    }

    @Override
    protected void onPostExecute(List<Device> devices) {

        SystemVersionProperties systemVersionProperties = applicationContext.getSystemVersionProperties();

        boolean deviceIsSupported = systemVersionProperties.isSupportedDevice(devices);

        if(deviceIsSupported) { // To prevent unnecessary device checks.
            SettingsManager settingsManager = new SettingsManager(applicationContext.getApplicationContext());
            settingsManager.saveBooleanPreference(PROPERTY_IGNORE_UNSUPPORTED_DEVICE_WARNINGS, true);
        }

        if(callback != null) {
            callback.displayUnsupportedMessage(deviceIsSupported);
        }
    }
}
