package com.arjanvlek.cyngnotainfo;

import android.app.Application;

import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.List;

public class ApplicationContext extends Application {
    private List<Device> devices;
    private ServerConnector serverConnector;

    public List<Device> getDevices() {
        if(devices != null) {
            return devices;
        }

        else {
            if(serverConnector == null) {
                serverConnector = new ServerConnector();
            }
            devices = serverConnector.getDevices();
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
