package com.arjanvlek.cyngnotainfo;

import android.app.Application;

import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import org.joda.time.LocalDateTime;

import java.util.List;

public class ApplicationContext extends Application {
    private List<Device> devices;
    private LocalDateTime deviceFetchDate;
    private ServerConnector serverConnector;

    /**
     * Prevents the /devices request to be performed more than once by storing it in the Application class.
     * If the stored data is more than 5 minutes old, one new request is allowed and so on for each 5 minutes.
     * @return List of Devices that are enabled on the server.
     */
    public List<Device> getDevices() {
        LocalDateTime now = LocalDateTime.now();
        if(devices != null && deviceFetchDate != null && deviceFetchDate.plusMinutes(5).isAfter(now)) {
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
