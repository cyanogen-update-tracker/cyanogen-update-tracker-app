package com.arjanvlek.cyngnotainfo.views;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.Model.ServerMessage;
import com.arjanvlek.cyngnotainfo.Model.ServerStatus;
import com.arjanvlek.cyngnotainfo.Model.UpdateDataLink;
import com.arjanvlek.cyngnotainfo.Support.NetworkConnectionManager;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;

import java.util.List;

import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_UPDATE_DATA_LINK;
import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_UPDATE_METHOD_ID;

public abstract class AbstractUpdateInformationFragment extends AbstractFragment {

    protected SettingsManager settingsManager;
    protected NetworkConnectionManager networkConnectionManager;
    protected CyanogenOTAUpdate cyanogenOTAUpdate;
    private String updateDataLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManager = new SettingsManager(getActivity().getApplicationContext());
        networkConnectionManager = new NetworkConnectionManager(getActivity().getApplicationContext());
        updateDataLink = settingsManager.getPreference(PROPERTY_UPDATE_DATA_LINK);
    }

    protected abstract void displayServerStatus(ServerStatus serverStatus);

    protected abstract void displayServerMessages(List<ServerMessage> serverMessages);

    protected abstract void displayUpdateInformation(CyanogenOTAUpdate cyanogenOTAUpdate, boolean online, boolean force);

    protected abstract CyanogenOTAUpdate buildOfflineCyanogenOTAUpdate();

    protected abstract void showNetworkError();

    protected class GetServerStatus extends AsyncTask<Void, Void, ServerStatus> {

        @Override
        protected ServerStatus doInBackground(Void... arg0) {
            return getApplicationContext().getServerConnector().getServerStatus();
        }

        @Override
        protected void onPostExecute(ServerStatus serverStatus) {
            displayServerStatus(serverStatus);
        }
    }

    protected class GetServerMessages extends  AsyncTask<Void, Void, List<ServerMessage>> {

        @Override
        protected List<ServerMessage> doInBackground(Void... arg0) {
            return getApplicationContext().getServerConnector().getServerMessages();
        }

        @Override
        protected void onPostExecute(List<ServerMessage> serverMessages) {
            displayServerMessages(serverMessages);
        }
    }

    /**
     * Async task class to get json by making HTTP call
     */

    protected class VerifyUpdateDataLinkAndGetUpdateInformation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            UpdateDataLink updateDataLink1 = getApplicationContext().getServerConnector().getUpdateDataLink(settingsManager.getLongPreference(SettingsManager.PROPERTY_DEVICE_ID),settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID));
            if(!updateDataLink.equals(updateDataLink1.getUpdateDataUrl())) {
                settingsManager.savePreference(PROPERTY_UPDATE_DATA_LINK, updateDataLink1.getUpdateDataUrl());
                updateDataLink = updateDataLink1.getUpdateDataUrl();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new GetUpdateInformation().executeOnExecutor(SERIAL_EXECUTOR);
            } else {
                new GetUpdateInformation().execute();
            }
        }
    }
    protected class GetUpdateInformation extends AsyncTask<Void, Void, CyanogenOTAUpdate> {

        @Override
        protected CyanogenOTAUpdate doInBackground(Void... arg0) {
            CyanogenOTAUpdate cyanogenOTAUpdate = getApplicationContext().getServerConnector().getCyanogenOTAUpdate(updateDataLink);
            if (cyanogenOTAUpdate != null) {
                return cyanogenOTAUpdate;

            } else {
                if (settingsManager.checkIfCacheIsAvailable()) {
                    return buildOfflineCyanogenOTAUpdate();
                } else {
                    showNetworkError();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(CyanogenOTAUpdate result) {
            super.onPostExecute(result);
            cyanogenOTAUpdate = result;
            if (networkConnectionManager.checkNetworkConnection()) {
                displayUpdateInformation(result, true, false);
            } else {
                displayUpdateInformation(result, false, false);
            }
        }
    }
}
