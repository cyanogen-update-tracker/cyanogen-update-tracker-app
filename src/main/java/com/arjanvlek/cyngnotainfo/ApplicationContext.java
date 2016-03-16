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

    public List<Device> getDevices() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(5);
        if(devices != null && deviceFetchDate != null && deviceFetchDate.isBefore(now)) {
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
