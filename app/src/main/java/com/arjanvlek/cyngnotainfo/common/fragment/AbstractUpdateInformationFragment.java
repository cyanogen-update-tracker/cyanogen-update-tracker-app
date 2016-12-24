package com.arjanvlek.cyngnotainfo.common.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.cm.model.CyanogenModUpdateData;
import com.arjanvlek.cyngnotainfo.common.internal.ApplicationData;
import com.arjanvlek.cyngnotainfo.common.internal.SystemVersionProperties;
import com.arjanvlek.cyngnotainfo.common.internal.Utils;
import com.arjanvlek.cyngnotainfo.common.model.ServerParameters;
import com.arjanvlek.cyngnotainfo.common.model.UpdateData;
import com.arjanvlek.cyngnotainfo.common.view.ServerMessageBar;
import com.arjanvlek.cyngnotainfo.cos.model.CyanogenOSUpdateData;
import com.arjanvlek.cyngnotainfo.common.model.ServerMessage;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.common.internal.NetworkConnectionManager;
import com.arjanvlek.cyngnotainfo.common.internal.SettingsManager;
import com.arjanvlek.cyngnotainfo.common.view.MessageDialog;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.RelativeLayout.ABOVE;
import static android.widget.RelativeLayout.BELOW;
import static com.arjanvlek.cyngnotainfo.common.internal.ApplicationData.LOCALE_DUTCH;
import static com.arjanvlek.cyngnotainfo.common.internal.SettingsManager.PROPERTY_DEVICE_ID;
import static com.arjanvlek.cyngnotainfo.common.internal.SettingsManager.PROPERTY_UPDATE_METHOD_ID;
import static com.arjanvlek.cyngnotainfo.common.model.ServerParameters.Status.OK;
import static com.arjanvlek.cyngnotainfo.common.model.ServerParameters.Status.UNREACHABLE;

public abstract class AbstractUpdateInformationFragment extends AbstractFragment {

    protected SettingsManager settingsManager;
    protected NetworkConnectionManager networkConnectionManager;
    protected CyanogenOSUpdateData cyanogenOSUpdateData;
    protected CyanogenModUpdateData cyanogenModUpdateData;

    protected SwipeRefreshLayout updateInformationRefreshLayout;
    protected SwipeRefreshLayout systemIsUpToDateRefreshLayout;

    protected Context context;



    protected RelativeLayout rootView;
    protected AdView adView;

    protected Map<String, List<Object>> inAppMessageBarData = new HashMap<>();
    protected List<ServerMessageBar> inAppMessageBars = new ArrayList<>();

    public static final String UNABLE_TO_FIND_A_MORE_RECENT_BUILD = "unable to find a more recent build";
    // In app message bar collections and identifiers.
    protected static final String KEY_APP_UPDATE_BARS = "app_update_bars";
    protected static final String KEY_SERVER_ERROR_BARS = "server_error_bars";
    protected static final String KEY_NO_NETWORK_CONNECTION_BARS = "no_network_connection_bars";
    protected static final String KEY_SERVER_MESSAGE_BARS = "server_message_bars";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManager = new SettingsManager(getActivity().getApplicationContext());
        networkConnectionManager = new NetworkConnectionManager(getActivity().getApplicationContext());
    }

    protected abstract void displayUpdateInformation(UpdateData updateData, boolean online, boolean force);

    protected abstract void initDownloadManager();

    protected abstract void checkIfUpdateIsAlreadyDownloaded(UpdateData updateData);

    protected abstract UpdateData buildOfflineUpdateData();

    public class GetServerMessages extends  AsyncTask<Void, Void, List<ServerMessage>> {

        @Override
        protected List<ServerMessage> doInBackground(Void... arg0) {
            return getApplicationData().getServerConnector().getServerMessages(settingsManager.getLongPreference(PROPERTY_DEVICE_ID), settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID));
        }

        @Override
        protected void onPostExecute(List<ServerMessage> serverMessages) {
            displayServerMessages(serverMessages);
        }
    }


    public class GetCOSUpdateInformation extends AsyncTask<Void, Void, CyanogenOSUpdateData> {

        @Override
        protected CyanogenOSUpdateData doInBackground(Void... arg0) {
            CyanogenOSUpdateData cyanogenOSUpdateData = getApplicationData().getServerConnector().getCyanogenOSUpdateData(settingsManager.getLongPreference(PROPERTY_DEVICE_ID), settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID), Build.VERSION.INCREMENTAL);
            if (cyanogenOSUpdateData != null) {
                if(cyanogenOSUpdateData.getInformation() != null && cyanogenOSUpdateData.getInformation().equals(UNABLE_TO_FIND_A_MORE_RECENT_BUILD) && cyanogenOSUpdateData.isUpdateInformationAvailable() && cyanogenOSUpdateData.isSystemIsUpToDateCheck()) {
                    cyanogenOSUpdateData = getApplicationData().getServerConnector().getMostRecentCyanogenOSUpdateData(settingsManager.getLongPreference(PROPERTY_DEVICE_ID), settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID));
                }
                return cyanogenOSUpdateData;

            } else {
                if (settingsManager.checkIfCacheIsAvailable()) {
                    return (CyanogenOSUpdateData) buildOfflineUpdateData();
                } else {
                    showNetworkError();
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(CyanogenOSUpdateData result) {
            super.onPostExecute(result);
            cyanogenOSUpdateData = result;
            displayUpdateInformation(result, networkConnectionManager.checkNetworkConnection(), false);
            initDownloadManager();
            checkIfUpdateIsAlreadyDownloaded(cyanogenOSUpdateData);
        }
    }

    public class GetCMUpdateInformation extends AsyncTask<Void, Void, CyanogenModUpdateData> {

        @Override
        protected CyanogenModUpdateData doInBackground(Void... arg0) {
            ApplicationData applicationData = getApplicationData();
            CyanogenModUpdateData cyanogenModUpdateData = getApplicationData().getServerConnector().getCyanogenModUpdateData(applicationData.SYSTEM_PROPERTIES);
            if (cyanogenModUpdateData != null) {
                return cyanogenModUpdateData;

            } else {
                if (settingsManager.checkIfCacheIsAvailable()) {
                    return (CyanogenModUpdateData) buildOfflineUpdateData();
                } else {
                    showNetworkError();
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(CyanogenModUpdateData result) {
            super.onPostExecute(result);
            cyanogenModUpdateData = result;
            displayUpdateInformation(result, networkConnectionManager.checkNetworkConnection(), false);
            initDownloadManager();
            checkIfUpdateIsAlreadyDownloaded(cyanogenModUpdateData);
        }
    }

    protected void showNetworkError() {
        MessageDialog errorDialog = new MessageDialog()
                .setTitle(getString(R.string.error_app_requires_network_connection))
                .setMessage(getString(R.string.error_app_requires_network_connection_message))
                .setNegativeButtonText(getString(R.string.download_error_close))
                .setClosable(false);
        errorDialog.setTargetFragment(this, 0);
        errorDialog.show(getFragmentManager(), "NetworkError");
    }

    protected void showMaintenanceError() {
        MessageDialog serverMaintenanceErrorFragment = new MessageDialog()
                .setTitle(getString(R.string.error_maintenance))
                .setMessage(getString(R.string.error_maintenance_message))
                .setNegativeButtonText(getString(R.string.download_error_close))
                .setClosable(false);
        serverMaintenanceErrorFragment.setTargetFragment(this, 0);
        serverMaintenanceErrorFragment.show(getFragmentManager(), "MaintenanceError");
    }

    protected void showAppNotValidError() {
        MessageDialog appNotValidErrorFragment = new MessageDialog()
                .setTitle(getString(R.string.error_app_outdated))
                .setMessage(getString(R.string.error_app_outdated_message))
                .setPositiveButtonText(getString(R.string.error_google_play_button_text))
                .setNegativeButtonText(getString(R.string.download_error_close))
                .setClosable(false)
                .setDialogListener(new MessageDialog.DialogListener() {
                    @Override
                    public void onDialogPositiveButtonClick(DialogFragment dialogFragment) {
                        try {
                            final String appPackageName = BuildConfig.APPLICATION_ID;
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                        } catch (Exception ignored) {

                        }
                    }

                    @Override
                    public void onDialogNegativeButtonClick(DialogFragment dialogFragment) {

                    }
        });
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

    /*
      -------------- METHODS FOR DISPLAYING DATA ON THE FRAGMENT -------------------
     */


    protected void checkNoConnectionBar() {
        // Display the "No connection" bar depending on the network status of the device.
        List<Object> noConnectionBars = new ArrayList<>(1);

        if(!networkConnectionManager.checkNetworkConnection()) {
            noConnectionBars.add(new Object());
        }

        inAppMessageBarData.put(KEY_NO_NETWORK_CONNECTION_BARS, noConnectionBars);

        if(areAllServerMessageBarsLoaded()) {
            displayInAppMessageBars();
        }
    }
    public void displayServerMessages(List<ServerMessage> serverMessages) {
        List<Object> serverMessageBars = new ArrayList<>();

        if(serverMessages != null && settingsManager.showNewsMessages()) {
            for(ServerMessage serverMessage : serverMessages) {
                serverMessageBars.add(serverMessage);
            }
        }
        inAppMessageBarData.put(KEY_SERVER_MESSAGE_BARS, serverMessageBars);

        if(areAllServerMessageBarsLoaded()) {
            displayInAppMessageBars();
        }
    }

    /**
     * Displays the status of the server (warning, error, maintenance or invalid app version)
     * @param serverParameters Server status data from the backend
     */
    public void displayServerStatus(ServerParameters serverParameters) {

        List<Object> serverErrorBars = new ArrayList<>(1);
        List<Object> appUpdateBars = new ArrayList<>(1);

        if(serverParameters != null && isAdded() && serverParameters.getStatus() != OK) {
            serverErrorBars.add(serverParameters);
        }

        if(serverParameters != null && settingsManager.showAppUpdateMessages() && !checkIfAppIsUpToDate(serverParameters.getLatestAppVersion())) {
            appUpdateBars.add(serverParameters);
        }

        if(serverParameters == null) {
            ServerParameters status = new ServerParameters();
            status.setLatestAppVersion(BuildConfig.VERSION_NAME);
            status.setStatus(UNREACHABLE);
            serverErrorBars.add(status);
        }

        inAppMessageBarData.put(KEY_SERVER_ERROR_BARS, serverErrorBars);
        inAppMessageBarData.put(KEY_APP_UPDATE_BARS, appUpdateBars);

        if(areAllServerMessageBarsLoaded()) {
            displayInAppMessageBars();
        }

    }

    private int addMessageBar(ServerMessageBar view, int numberOfBars) {
        // Add the message to the update information screen if it is not null.
        if(this.rootView != null) {
            // Set the layout params based on the view count.
            // First view should go below the app update message bar (if visible)
            // Consecutive views should go below their parent / previous view.
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            params.topMargin = numberOfBars * Utils.diPToPixels(getActivity(), 20);
            view.setId((numberOfBars * 20000000 + 1));
            this.rootView.addView(view, params);
            numberOfBars = numberOfBars + 1;
            this.inAppMessageBars.add(view);
        }
        return numberOfBars;
    }

    private void deleteAllInAppMessageBars() {
        for(ServerMessageBar view : this.inAppMessageBars) {
            if(view != null && isAdded()) {
                this.rootView.removeView(view);
            }
        }
        this.inAppMessageBars = new ArrayList<>();
    }

    private void displayInAppMessageBars() {
        if(!isAdded()) {
            return;
        }
        deleteAllInAppMessageBars();
        int numberOfBars = 0;

        // Display the "No connection" bar if no connection is available.
        for(Object ignored : inAppMessageBarData.get(KEY_NO_NETWORK_CONNECTION_BARS)) {
            ServerMessageBar noConnectionError = new ServerMessageBar(getApplicationData(), null);
            View noConnectionErrorBar = noConnectionError.getBackgroundBar();
            TextView noConnectionErrorTextView = noConnectionError.getTextView();

            noConnectionErrorBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_red_light));
            noConnectionErrorTextView.setText(getString(R.string.error_no_internet_connection));
            numberOfBars = addMessageBar(noConnectionError, numberOfBars);
        }

        // Display server error bars / messages
        for(Object serverStatusObject : inAppMessageBarData.get(KEY_SERVER_ERROR_BARS)) {
            ServerParameters serverParameters = (ServerParameters)serverStatusObject;

            // Create a new server message view and get its contents
            ServerMessageBar serverStatusView = new ServerMessageBar(getApplicationData(), null);
            View serverStatusWarningBar = serverStatusView.getBackgroundBar();
            TextView serverStatusWarningTextView = serverStatusView.getTextView();

            if (settingsManager.showNewsMessages()) {
                serverStatusWarningBar.setVisibility(VISIBLE);
                serverStatusWarningTextView.setVisibility(VISIBLE);
            }

            switch (serverParameters.getStatus()) {
                case WARNING:
                    if (settingsManager.showNewsMessages()) {
                        serverStatusWarningBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_orange_light));
                        serverStatusWarningTextView.setText(getString(R.string.server_status_warning));
                        numberOfBars = addMessageBar(serverStatusView, numberOfBars);
                    }
                    break;
                case ERROR:
                    if (settingsManager.showNewsMessages()) {
                        serverStatusWarningBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_red_light));
                        serverStatusWarningTextView.setText(getString(R.string.server_status_error));
                        numberOfBars = addMessageBar(serverStatusView, numberOfBars);
                    }
                    break;
                case MAINTENANCE:
                    showMaintenanceError();
                    break;
                case TAKEN_DOWN:
                    showAppNotValidError();
                    break;
                case UNREACHABLE:
                    serverStatusWarningBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_red_light));
                    serverStatusWarningTextView.setText(getString(R.string.server_status_unreachable));
                    numberOfBars = addMessageBar(serverStatusView, numberOfBars);
                    break;
            }

        }

        // Display app update message if available
        for(Object serverStatusObject : inAppMessageBarData.get(KEY_APP_UPDATE_BARS)) {
            ServerParameters serverParameters = (ServerParameters)serverStatusObject;
            if (isAdded()) {
                // getActivity() is required here. Otherwise, clicking on the update message link will crash the application.
                ServerMessageBar appUpdateMessageView = new ServerMessageBar(getActivity(), null);
                View appUpdateMessageBar = appUpdateMessageView.getBackgroundBar();
                TextView appUpdateMessageTextView = appUpdateMessageView.getTextView();

                appUpdateMessageBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_green_light));

                appUpdateMessageTextView.setText(Html.fromHtml(String.format(getString(R.string.new_app_version), serverParameters.getLatestAppVersion())));
                appUpdateMessageTextView.setMovementMethod(LinkMovementMethod.getInstance());
                numberOfBars = addMessageBar(appUpdateMessageView, numberOfBars);
            }
        }

        // Display server message bars / messages
        List<Object> serverMessageObjects = this.inAppMessageBarData.get(KEY_SERVER_MESSAGE_BARS);
        for (Object messageObject : serverMessageObjects) {
            ServerMessage message = (ServerMessage)messageObject;
            // Create a new server message view and get its contents
            ServerMessageBar serverMessageBar = new ServerMessageBar(getApplicationData(), null);
            View serverMessageBackgroundBar = serverMessageBar.getBackgroundBar();
            TextView serverMessageTextView = serverMessageBar.getTextView();


            // Set the right locale text of the message in the view.
            String appLocale = Locale.getDefault().getDisplayLanguage();

            if (appLocale.equals(LOCALE_DUTCH)) {
                serverMessageTextView.setText(message.getMessageNl());
            } else {
                serverMessageTextView.setText(message.getMessage());
            }

            // Set the background color of the view according to the priority data from the backend.
            switch (message.getPriority()) {
                case LOW:
                    serverMessageBackgroundBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_green_light));
                    break;
                case MEDIUM:
                    serverMessageBackgroundBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_orange_light));
                    break;
                case HIGH:
                    serverMessageBackgroundBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_red_light));
                    break;
            }

            // Set the message as moving text if it is marked as being marquee from the backend.
            if (message.isMarquee()) {
                serverMessageTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                serverMessageTextView.setHorizontallyScrolling(true);
                serverMessageTextView.setSingleLine(true);
                serverMessageTextView.setMarqueeRepeatLimit(-1); // -1 is forever
                serverMessageTextView.setFocusable(true);
                serverMessageTextView.setFocusableInTouchMode(true);
                serverMessageTextView.requestFocus();
                serverMessageTextView.setSelected(true);
            }

            numberOfBars = addMessageBar(serverMessageBar, numberOfBars);
        }

        // Set the margins of the app ui to be below the last added server message bar.
        if(inAppMessageBars.size() > 0 && isAdded()) {
            View lastServerMessageView = inAppMessageBars.get(inAppMessageBars.size() - 1);
            if (lastServerMessageView != null) {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                params.addRule(BELOW, lastServerMessageView.getId());

                if(adView != null) {
                    params.addRule(ABOVE, adView.getId());
                }

                if(systemIsUpToDateRefreshLayout != null) {
                    systemIsUpToDateRefreshLayout.setLayoutParams(params);
                }
                if(updateInformationRefreshLayout != null) {
                    updateInformationRefreshLayout.setLayoutParams(params);
                }
            }
        }


    }

    private boolean areAllServerMessageBarsLoaded() {
        return inAppMessageBarData.containsKey(KEY_APP_UPDATE_BARS) && inAppMessageBarData.containsKey(KEY_NO_NETWORK_CONNECTION_BARS) && inAppMessageBarData.containsKey(KEY_SERVER_ERROR_BARS) && inAppMessageBarData.containsKey(KEY_SERVER_MESSAGE_BARS);
    }
}
