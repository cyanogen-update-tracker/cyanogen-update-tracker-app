package com.arjanvlek.cyngnotainfo.common.internal.asynctask;

import android.os.AsyncTask;

import com.arjanvlek.cyngnotainfo.common.internal.ApplicationData;
import com.arjanvlek.cyngnotainfo.common.internal.Callback;
import com.arjanvlek.cyngnotainfo.common.model.ServerParameters;

public class GetServerStatus extends AsyncTask<Void, Void, ServerParameters> {

    private final ApplicationData applicationData;
    private final Callback callback;

    public GetServerStatus(ApplicationData applicationData, Callback callback) {
        this.applicationData = applicationData;
        this.callback = callback;
    }

    @Override
    protected ServerParameters doInBackground(Void... arg0) {
        return this.applicationData.getServerConnector().getServerParameters();
    }

    @Override
    protected void onPostExecute(ServerParameters serverParameters) {
        callback.onActionPerformed(serverParameters);
    }
}