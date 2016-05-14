package com.arjanvlek.cyngnotainfo.views;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.Model.ServerMessage;
import com.arjanvlek.cyngnotainfo.Model.ServerStatus;

import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.NetworkConnectionManager;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;

import java.util.List;

import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_DEVICE_ID;
import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_UPDATE_METHOD_ID;

public abstract class AbstractUpdateInformationFragment extends AbstractFragment {

    protected SettingsManager settingsManager;
    protected NetworkConnectionManager networkConnectionManager;
    protected CyanogenOTAUpdate cyanogenOTAUpdate;

    public static final String UNABLE_TO_FIND_A_MORE_RECENT_BUILD = "unable to find a more recent build";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManager = new SettingsManager(getActivity().getApplicationContext());
        networkConnectionManager = new NetworkConnectionManager(getActivity().getApplicationContext());
    }

    protected abstract void displayServerStatus(ServerStatus serverStatus);

    protected abstract void displayServerMessages(List<ServerMessage> serverMessages);

    protected abstract void displayUpdateInformation(CyanogenOTAUpdate cyanogenOTAUpdate, boolean online, boolean force);

    protected abstract CyanogenOTAUpdate buildOfflineCyanogenOTAUpdate();

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
            CyanogenOTAUpdate cyanogenOTAUpdate = getApplicationContext().getServerConnector().getCyanogenOTAUpdate(settingsManager.getLongPreference(PROPERTY_DEVICE_ID), settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID), Build.VERSION.INCREMENTAL);
            if (cyanogenOTAUpdate != null) {
                if(cyanogenOTAUpdate.getInformation() != null && cyanogenOTAUpdate.getInformation().equals(UNABLE_TO_FIND_A_MORE_RECENT_BUILD) && cyanogenOTAUpdate.isUpdateInformationAvailable() && cyanogenOTAUpdate.isSystemIsUpToDateCheck()) {
                    cyanogenOTAUpdate = getApplicationContext().getServerConnector().getMostRecentCyanogenOTAUpdate(settingsManager.getLongPreference(PROPERTY_DEVICE_ID), settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID));
                }
                return cyanogenOTAUpdate;

            } else {
                if (settingsManager.checkIfCacheIsAvailable()) {
                    return buildOfflineCyanogenOTAUpdate();
                } else {
                    showNetworkError();
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(CyanogenOTAUpdate result) {
            super.onPostExecute(result);
            cyanogenOTAUpdate = result;
            displayUpdateInformation(result, networkConnectionManager.checkNetworkConnection(), false);
        }
    }

    protected void showNetworkError() {
        DialogFragment errorDialog = new MessageDialog();
        Bundle args = new Bundle(4);
        args.putString("message", getString(R.string.error_app_requires_network_connection_message));
        args.putString("title", getString(R.string.error_app_requires_network_connection));
        args.putString("button1", getString(R.string.download_error_close));
        args.putBoolean("closable", false);
        errorDialog.setArguments(args);
        errorDialog.setTargetFragment(this, 0);
        errorDialog.show(getFragmentManager(), "NetworkError");
    }

    protected void showMaintenanceError() {
        DialogFragment serverMaintenanceErrorFragment = new MessageDialog();
        Bundle args = new Bundle(4);
        args.putString("message", getString(R.string.error_maintenance_message));
        args.putString("title", getString(R.string.error_maintenance));
        args.putString("button1", getString(R.string.download_error_close));
        args.putBoolean("closable", false);
        serverMaintenanceErrorFragment.setArguments(args);
        serverMaintenanceErrorFragment.setTargetFragment(this, 0);
        serverMaintenanceErrorFragment.show(getFragmentManager(), "MaintenanceError");
    }

    protected void showAppNotValidError() {
        DialogFragment appNotValidErrorFragment = new MessageDialog();
        Bundle args = new Bundle(4);
        args.putString("message", getString(R.string.error_app_not_valid_message));
        args.putString("title", getString(R.string.error_app_not_valid));
        args.putString("button1", getString(R.string.error_google_play_button_text));
        args.putString("button2", getString(R.string.download_error_close));
        args.putBoolean("closable", false);
        appNotValidErrorFragment.setArguments(args);
        appNotValidErrorFragment.setTargetFragment(this, 0);
        appNotValidErrorFragment.show(getFragmentManager(), "AppNotValidError");
    }

    protected boolean checkIfAppIsUpToDate(String appVersionFromResult) {
        String appVersion = BuildConfig.VERSION_NAME;
        appVersion = appVersion.replace(".", "");
        appVersionFromResult = appVersionFromResult.replace(".", "");
        try {
            int appVersionNumeric = Integer.parseInt(appVersion);
            int appVersionFromResultNumeric = Integer.parseInt(appVersionFromResult);
            return appVersionFromResultNumeric <= appVersionNumeric;
        } catch(Exception e) {
            return true;
        }
    }
}
