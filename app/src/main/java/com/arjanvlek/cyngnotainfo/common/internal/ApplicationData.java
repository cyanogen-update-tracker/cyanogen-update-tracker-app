package com.arjanvlek.cyngnotainfo.common.internal;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.cos.model.Device;

import org.joda.time.LocalDateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ApplicationData extends Application {

    private List<Device> devices;
    private LocalDateTime deviceFetchDate;
    private ServerConnector serverConnector;

    public static final String APP_USER_AGENT = "Cyanogen_update_tracker_" + BuildConfig.VERSION_NAME;
    public static final String PACKAGE_REPLACED_KEY = "package_upgrade";
    public static final String IS_COS_KEY = "is_cos";
    public static final String LOCALE_DUTCH = "Nederlands";
    public static final int NUMBER_OF_INSTALL_GUIDE_PAGES = 5;


    public final SystemVersionProperties.SystemType SYSTEM_TYPE;
    public final String CYANOGEN_VERSION;
    public final String CYANOGEN_DEVICE;
    public final String SECURITY_PATCH_DATE;

    public List<Device> getDevices() {
        return getDevices(false);
    }

    public ApplicationData() {
        SystemVersionProperties properties = new SystemVersionProperties();
        SYSTEM_TYPE = properties.getSystemType();
        CYANOGEN_VERSION = properties.getCyanogenVersion();
        CYANOGEN_DEVICE = properties.getCyanogenDeviceName();
        SECURITY_PATCH_DATE = properties.getSecurityPatchDate();
    }

    /**
     * Prevents the /devices request to be performed more than once by storing it in the Application class.
     * If the stored data is more than 5 minutes old, one new request is allowed and so on for each 5 minutes.
     * @return List of Devices that are enabled on the server.
     */
    public List<Device> getDevices(boolean alwaysFetch) {
        LocalDateTime now = LocalDateTime.now();
        if(devices != null && deviceFetchDate != null && deviceFetchDate.plusMinutes(5).isAfter(now) && !alwaysFetch) {
            return devices;
        }

        else {
            if(serverConnector == null) {
                serverConnector = new ServerConnector();
            }
            devices = serverConnector.getDevices();
            deviceFetchDate = LocalDateTime.now();
            return devices;
        }
    }

    public ServerConnector getServerConnector() {
        if(serverConnector == null) {
            serverConnector = new ServerConnector();
            return serverConnector;
        }
        else {
            return serverConnector;
        }
    }
}
