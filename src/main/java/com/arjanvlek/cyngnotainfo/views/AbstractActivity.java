package com.arjanvlek.cyngnotainfo.views;

import android.support.v7.app.AppCompatActivity;

import com.arjanvlek.cyngnotainfo.Model.Device;
import com.arjanvlek.cyngnotainfo.Support.ServerConnector;

import java.util.List;

public class AbstractActivity extends AppCompatActivity {

    private List<Device> devices;
    private ServerConnector serverConnector;

    protected List<Device> getDevices() {
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
