package com.arjanvlek.cyngnotainfo.common.activity;

import android.support.v7.app.AppCompatActivity;

import com.arjanvlek.cyngnotainfo.common.internal.ApplicationData;
import com.arjanvlek.cyngnotainfo.common.internal.ServerConnector;

public class AbstractActivity extends AppCompatActivity {

    private ServerConnector serverConnector;
    private ApplicationData applicationData;

    public ServerConnector getServerConnector() {
        if(applicationData == null) {
            applicationData = getAppApplicationContext();
        }
        if(serverConnector == null) {
            serverConnector = applicationData.getServerConnector();
        }
        return serverConnector;
    }

    protected ApplicationData getAppApplicationContext() {
        if(applicationData == null) {
            applicationData = (ApplicationData)getApplication();
        }
        return applicationData;
    }
}
