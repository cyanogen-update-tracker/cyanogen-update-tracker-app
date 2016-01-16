package com.arjanvlek.cyngnotainfo.views;

import android.os.AsyncTask;
import android.os.Bundle;

import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.Model.ServerMessage;
import com.arjanvlek.cyngnotainfo.Model.ServerStatus;

import com.arjanvlek.cyngnotainfo.Support.NetworkConnectionManager;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;

import java.util.List;

import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_DEVICE_ID;
import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_UPDATE_METHOD_ID;

public abstract class AbstractUpdateInformationFragment extends AbstractFragment {

    protected SettingsManager settingsManager;
    protected NetworkConnectionManager networkConnectionManager;
    protected CyanogenOTAUpdate cyanogenOTAUpdate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManager = new SettingsManager(getActivity().getApplicationContext());
        networkConnectionManager = new NetworkConnectionManager(getActivity().getApplicationContext());
    }

    protected abstract void displayServerStatus(ServerStatus serverStatus);

    protected abstract void displayServerMessages(List<ServerMessage> serverMessages);

    protected abstract boolean displayUpdateInformation(CyanogenOTAUpdate cyanogenOTAUpdate, boolean online, boolean force);

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


    protected class GetUpdateInformation extends AsyncTask<Void, Void, CyanogenOTAUpdate> {

        @Override
        protected CyanogenOTAUpdate doInBackground(Void... arg0) {
            CyanogenOTAUpdate cyanogenOTAUpdate = getApplicationContext().getServerConnector().getCyanogenOTAUpdate(settingsManager.getLongPreference(PROPERTY_DEVICE_ID), settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID));
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
