package com.arjanvlek.cyngnotainfo.views;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.Model.ServerMessage;
import com.arjanvlek.cyngnotainfo.Model.ServerStatus;
import com.arjanvlek.cyngnotainfo.Support.NetworkConnectionManager;
import com.arjanvlek.cyngnotainfo.Support.DateTimeFormatter;
import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.SettingsManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.*;

public class UpdateInformationFragment extends AbstractFragment implements SwipeRefreshLayout.OnRefreshListener {

    private String deviceName;
    private String updateDataLink;

    private SwipeRefreshLayout refreshLayout;

    private RelativeLayout rootView;
    private AdView adView;

    private SettingsManager settingsManager;
    private NetworkConnectionManager networkConnectionManager;
    private Context context;

    private DateTime refreshedDate;
    private boolean isFetched;
    private boolean error;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManager = new SettingsManager(getActivity().getApplicationContext());
        networkConnectionManager = new NetworkConnectionManager(getActivity().getApplicationContext());
        deviceName = settingsManager.getPreference(PROPERTY_DEVICE);
        updateDataLink = settingsManager.getPreference(PROPERTY_UPDATE_DATA_LINK);
        context = getActivity().getApplicationContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = (RelativeLayout) inflater.inflate(R.layout.fragment_updateinformation, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (refreshLayout == null && rootView != null) {
            refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.updateInformationRefreshLayout);
            SwipeRefreshLayout refreshLayoutForUpToDateSystem = (SwipeRefreshLayout) rootView.findViewById(R.id.updateInformationSystemIsUpToDateRefreshLayout);
            refreshLayout.setOnRefreshListener(this);
            refreshLayoutForUpToDateSystem.setOnRefreshListener(this);
            refreshLayout.setColorSchemeResources(R.color.lightBlue, R.color.holo_orange_light, R.color.holo_red_light);
            refreshLayoutForUpToDateSystem.setColorSchemeResources(R.color.lightBlue, R.color.holo_orange_light, R.color.holo_red_light);
        }
        if (!isFetched && settingsManager.checkIfSettingsAreValid()) {
            if (networkConnectionManager.checkNetworkConnection()) {
                getUpdateInformation();
                showAds();
                refreshedDate = DateTime.now();
                isFetched = true;
            } else if (settingsManager.checkIfCacheIsAvailable()) {
                getOfflineUpdateInformation();
                refreshedDate = DateTime.now();
                isFetched = true;
            } else {
                hideAds();
                showNetworkError();
            }
        }
    }

    private void hideAds() {
        if (adView != null) {
            adView.destroy();
        }
    }

    private void showAds() {
        adView = (AdView) rootView.findViewById(R.id.updateInformationAdView);
        if (adView != null) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(ADS_TEST_DEVICE_ID_OWN_DEVICE)
                    .addTestDevice(ADS_TEST_DEVICE_ID_EMULATOR_1)
                    .addTestDevice(ADS_TEST_DEVICE_ID_EMULATOR_2)
                    .addTestDevice(ADS_TEST_DEVICE_ID_EMULATOR_3)
                    .addKeyword("smartphone")
                    .addKeyword("tablet")
                    .addKeyword("games")
                    .addKeyword("android")
                    .addKeyword("cyanogen")
                    .addKeyword("cyanogenmod")
                    .addKeyword("cyanogenos")
                    .addKeyword("cyanogen os")
                    .addKeyword("raspberrypi")
                    .addKeyword("oneplus")
                    .addKeyword("yu")
                    .addKeyword("oppo")

                    .build();

            adView.loadAd(adRequest);
        }
    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        super.onPause();
        if (adView != null) {
            adView.pause();
        }

    }

    /**
     * Called when re-opening the activity
     */
    @Override
    public void onResume() {
        super.onResume();

        if (adView != null) {
            adView.resume();
        }
        if (refreshedDate != null && isFetched && settingsManager.checkIfSettingsAreValid()) {
            if (refreshedDate.plusMinutes(5).isBefore(DateTime.now())) {
                if (networkConnectionManager.checkNetworkConnection()) {
                    getUpdateInformation();
                    refreshedDate = DateTime.now();
                } else if (settingsManager.checkIfCacheIsAvailable()) {
                    getOfflineUpdateInformation();
                    refreshedDate = DateTime.now();
                } else {
                    showNetworkError();
                }
            }
        }
    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adView != null) {
            adView.destroy();
        }
    }

    private void getUpdateInformation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new GetUpdateInformation().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            if(settingsManager.showNewsMessages() || settingsManager.showAppUpdateMessages()) {
                new GetServerStatus().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
            if(settingsManager.showNewsMessages()) {
                new GetServerMessages().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        } else {
            new GetUpdateInformation().execute();
            if(settingsManager.showNewsMessages() || settingsManager.showAppUpdateMessages()) {
                new GetServerStatus().execute();
            }
            if(settingsManager.showNewsMessages()) {
                new GetServerMessages().execute();
            }
        }
    }

    private void getOfflineUpdateInformation() {
        displayUpdateInformation(buildOfflineCyanogenOTAUpdate(), false);
    }

    private CyanogenOTAUpdate buildOfflineCyanogenOTAUpdate() {
        CyanogenOTAUpdate cyanogenOTAUpdate = new CyanogenOTAUpdate();
        cyanogenOTAUpdate.setName(settingsManager.getPreference(PROPERTY_OFFLINE_UPDATE_NAME));
        cyanogenOTAUpdate.setSize(settingsManager.getIntPreference(PROPERTY_OFFLINE_UPDATE_DOWNLOAD_SIZE));
        cyanogenOTAUpdate.setDateUpdated(settingsManager.getPreference(PROPERTY_OFFLINE_UPDATE_SERVER_UPDATE_TIME));
        cyanogenOTAUpdate.setDescription(settingsManager.getPreference(PROPERTY_OFFLINE_UPDATE_DESCRIPTION));
        cyanogenOTAUpdate.setRollOutPercentage(settingsManager.getIntPreference(PROPERTY_OFFLINE_UPDATE_ROLLOUT_PERCENTAGE));
        return cyanogenOTAUpdate;
    }

    public void displayServerMessages(List<ServerMessage> serverMessages) {
        if(settingsManager.showNewsMessages() && !error) {
            rootView.findViewById(R.id.updateInformationFirstServerMessageBar).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationSecondServerMessageBar).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationFirstServerMessageTextView).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationSecondServerMessageTextView).setVisibility(View.GONE);
            int i = 0;
            for (ServerMessage message : serverMessages) {
                // There may never be more than 2 messages displayed in this version of the app.
                if(!message.isDeviceSpecific() || (message.getDeviceId() == settingsManager.getLongPreference(PROPERTY_DEVICE_ID))) {
                    View serverMessageBar;
                    TextView serverMessageTextView;
                    if (i == 0) {
                        serverMessageBar = rootView.findViewById(R.id.updateInformationFirstServerMessageBar);
                        serverMessageTextView = (TextView) rootView.findViewById(R.id.updateInformationFirstServerMessageTextView);

                        View updateInformationScreen = rootView.findViewById(R.id.updateInformationRefreshLayout);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                        params.addRule(RelativeLayout.BELOW, R.id.updateInformationFirstServerMessageBar);
                        params.addRule(RelativeLayout.ABOVE, R.id.updateInformationAdView);

                        updateInformationScreen.setLayoutParams(params);

                    } else {
                        serverMessageBar = rootView.findViewById(R.id.updateInformationSecondServerMessageBar);
                        serverMessageTextView = (TextView) rootView.findViewById(R.id.updateInformationSecondServerMessageTextView);

                        View updateInformationScreen = rootView.findViewById(R.id.updateInformationRefreshLayout);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                        params.addRule(RelativeLayout.BELOW, R.id.updateInformationSecondServerMessageBar);
                        params.addRule(RelativeLayout.ABOVE, R.id.updateInformationAdView);

                        updateInformationScreen.setLayoutParams(params);
                    }
                    serverMessageBar.setVisibility(View.VISIBLE);
                    serverMessageTextView.setVisibility(View.VISIBLE);

                    String appLocale = Locale.getDefault().getDisplayLanguage();
                    if (appLocale.equals("Nederlands")) {
                        serverMessageTextView.setText(message.getMessageNl());
                    } else {
                        serverMessageTextView.setText(message.getMessage());
                    }

                    switch (message.getPriority()) {
                        case LOW:
                            serverMessageBar.setBackgroundColor(getResources().getColor(R.color.holo_green_light));
                            break;
                        case MEDIUM:
                            serverMessageBar.setBackgroundColor(getResources().getColor(R.color.holo_orange_light));
                            break;
                        case HIGH:
                            serverMessageBar.setBackgroundColor(getResources().getColor(R.color.holo_red_light));
                            break;
                    }

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
                }

                i++;
            }
        }
    }

    public void displayServerStatus(ServerStatus serverStatus) {

        if(serverStatus.getStatus() != ServerStatus.Status.OK) {
            View serverStatusWarningBar = rootView.findViewById(R.id.updateInformationServerErrorBar);
            TextView serverStatusWarningTextView = (TextView) rootView.findViewById(R.id.updateInformationServerErrorTextView);

            if(settingsManager.showNewsMessages()) {
                serverStatusWarningBar.setVisibility(View.VISIBLE);
                serverStatusWarningTextView.setVisibility(View.VISIBLE);
            }
            switch(serverStatus.getStatus()) {
                case WARNING:
                    if(settingsManager.showNewsMessages()) {
                        serverStatusWarningBar.setBackgroundColor(getResources().getColor(R.color.holo_orange_light));
                        serverStatusWarningTextView.setText(getString(R.string.server_status_warning));
                    }
                    break;
                case ERROR:
                    if(settingsManager.showNewsMessages()) {
                        serverStatusWarningBar.setBackgroundColor(getResources().getColor(R.color.holo_red_light));
                        serverStatusWarningTextView.setText(getString(R.string.server_status_error));
                    }
                    break;
                case MAINTENANCE:
                    showMaintenanceError();
                    break;
                case TAKEN_DOWN:
                    showAppNotValidError();
                    break;
                case UNREACHABLE:
                    if(settingsManager.showNewsMessages()) {
                        serverStatusWarningBar.setBackgroundColor(getResources().getColor(R.color.holo_orange_light));
                        serverStatusWarningTextView.setText(getString(R.string.server_status_unreachable));
                    }
                    break;
            }
        }
        if(settingsManager.showAppUpdateMessages() && !error) {
            if (!checkIfAppIsUpToDate(serverStatus.getLatestAppVersion())) {
                View appUpdateNotificationBar = rootView.findViewById(R.id.updateInformationNewAppNotificationBar);
                TextView appUpdateNotificationTextView = (TextView) rootView.findViewById(R.id.updateInformationNewAppNotificationTextView);
                appUpdateNotificationBar.setVisibility(View.VISIBLE);
                appUpdateNotificationTextView.setVisibility(View.VISIBLE);

                appUpdateNotificationTextView.setText(getString(R.string.new_app_version_first, serverStatus.getLatestAppVersion()) + " " + getString(R.string.new_app_version_second));
                appUpdateNotificationTextView.setLinksClickable(true);
                appUpdateNotificationTextView.setMovementMethod(LinkMovementMethod.getInstance());

            }
        }
    }

    private boolean checkIfAppIsUpToDate(String appVersionFromResult) {
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


    public void displayUpdateInformation(final CyanogenOTAUpdate cyanogenOTAUpdate, boolean online) {
        if(cyanogenOTAUpdate != null && checkIfSystemIsUpToDate(cyanogenOTAUpdate.getName(), cyanogenOTAUpdate.getDateCreatedUnix()) && isAdded()) {
             // switch views
            rootView.findViewById(R.id.updateInformationRefreshLayout).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationSystemIsUpToDateRefreshLayout).setVisibility(View.VISIBLE);

            String cyanogenOSVersion = System.getProperty("ro.cm.display.version", "unsupported");
            TextView versionNumberView = (TextView) rootView.findViewById(R.id.updateInformationSystemIsUpToDateVersionTextView);
            versionNumberView.setText("Cyanogen OS" + getString(R.string.update_information_version) + " " + cyanogenOSVersion);

            DateTimeFormatter dateTimeFormatter = new DateTimeFormatter(context, this);
            TextView dateCheckedView = (TextView) rootView.findViewById(R.id.updateInformationSystemIsUpToDateDateTextView);
            if(online) {
                settingsManager.savePreference(PROPERTY_UPDATE_CHECKED_DATE, dateTimeFormatter.formatDateTime(LocalDateTime.now()));
            }
            dateCheckedView.setText(settingsManager.getPreference(PROPERTY_UPDATE_CHECKED_DATE));
        }
        else {
            if (cyanogenOTAUpdate != null && isAdded()) {
                generateCircleDiagram(cyanogenOTAUpdate);
                rootView.findViewById(R.id.updateInformationRefreshLayout).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.updateInformationSystemIsUpToDateRefreshLayout).setVisibility(View.GONE);
                TextView buildNumberView = (TextView) rootView.findViewById(R.id.updateInformationBuildNumberView);
                if (cyanogenOTAUpdate.getName() != null && !cyanogenOTAUpdate.getName().equals("null")) {
                    buildNumberView.setText(cyanogenOTAUpdate.getName() + " " + getString(R.string.update_information_for) + " " + deviceName);
                } else {
                    buildNumberView.setText(getString(R.string.update) + " " + getString(R.string.update_information_for) + " " + deviceName);
                }

                TextView downloadSizeView = (TextView) rootView.findViewById(R.id.updateInformationDownloadSizeView);
                downloadSizeView.setText((cyanogenOTAUpdate.getSize() / 1048576) + " " + getString(R.string.download_size_megabyte));

                TextView updatedDataView = (TextView) rootView.findViewById(R.id.updateInformationUpdatedDataView);
                DateTimeFormatter dateTimeFormatter = new DateTimeFormatter(getActivity().getApplicationContext(), this);
                String dateUpdated = dateTimeFormatter.formatDateTime(cyanogenOTAUpdate.getDateUpdated());
                updatedDataView.setText(dateUpdated);

                View noConnectionBar = rootView.findViewById(R.id.updateInformationNoConnectionBar);
                TextView noConnectionTextField = (TextView) rootView.findViewById(R.id.updateInformationNoConnectionTextView);

                if (online) {
                    // Hide the "no connection" bar.
                    noConnectionBar.setVisibility(View.GONE);
                    noConnectionTextField.setVisibility(View.GONE);

                    if (cyanogenOTAUpdate.getDownloadUrl() != null) {
                        Button downloadButton = (Button) rootView.findViewById(R.id.updateInformationDownloadButton);
                        downloadButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                downloadUpdate(cyanogenOTAUpdate.getDownloadUrl(), cyanogenOTAUpdate.getFileName());
                            }
                        });
                        downloadButton.setEnabled(true);
                    }
                } else {
                    //Show the "no connection" bar.
                    noConnectionBar.setVisibility(View.VISIBLE);
                    noConnectionTextField.setVisibility(View.VISIBLE);
                    Button downloadButton = (Button) rootView.findViewById(R.id.updateInformationDownloadButton);
                    downloadButton.setEnabled(false);
                }


                Button descriptionButton = (Button) rootView.findViewById(R.id.updateInformationUpdateDescriptionButton);
                descriptionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getActivity(), UpdateDescriptionActivity.class);
                        i.putExtra("update-description", cyanogenOTAUpdate.getDescription());
                        startActivity(i);
                    }
                });

                // Save preferences for offline viewing
                settingsManager.savePreference(PROPERTY_OFFLINE_UPDATE_NAME, cyanogenOTAUpdate.getName());
                settingsManager.saveIntPreference(PROPERTY_OFFLINE_UPDATE_ROLLOUT_PERCENTAGE, cyanogenOTAUpdate.getRollOutPercentage());
                settingsManager.saveIntPreference(PROPERTY_OFFLINE_UPDATE_DOWNLOAD_SIZE, cyanogenOTAUpdate.getSize());
                settingsManager.savePreference(PROPERTY_OFFLINE_UPDATE_SERVER_UPDATE_TIME, cyanogenOTAUpdate.getDateUpdated());
                settingsManager.savePreference(PROPERTY_OFFLINE_UPDATE_DESCRIPTION, cyanogenOTAUpdate.getDescription());

                // Hide the refreshing icon
                if (refreshLayout != null) {
                    if (refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(false);
                    }
                }
            }
        }
    }

    private boolean checkIfSystemIsUpToDate(String newCyanogenOSVersion, int newDateCreated) {
        if(settingsManager.showIfSystemIsUpToDate()) {
            // This grabs Cyanogen OS version from build.prop. As there is no direct SDK way to do this, it has to be done in this way.
            String cyanogenOSVersion = System.getProperty("ro.cm.display.version", "unsupported");
            String dateCreated = System.getProperty("ro.build.date.utc", "unsupported");
            int dateCreatedInt;
            // if this device is not running Cyanogen OS, mark it as not up-to-date to always display version information.
            try {
                if (!dateCreated.equals("unsupported")) {
                    dateCreatedInt = Integer.parseInt(dateCreated);
                } else {
                    dateCreatedInt = -1;
                }

            } catch (Exception e) {
                dateCreatedInt = -1;
            }
            if (dateCreatedInt != -1 && dateCreatedInt == newDateCreated) {
                return true;
            } else if (cyanogenOSVersion.equals("unsupported")) {
                return false;
            } else {
                if (newCyanogenOSVersion.equals(cyanogenOSVersion)) {
                    return true;
                } else {
                    // remove incremental version naming.
                    newCyanogenOSVersion = newCyanogenOSVersion.replace(" Incremental", "");
                    if (newCyanogenOSVersion.equals(cyanogenOSVersion)) {
                        return true;
                    } else {
                        newCyanogenOSVersion = newCyanogenOSVersion.replace("-", " ");
                        return newCyanogenOSVersion.contains(cyanogenOSVersion);
                    }
                }
            }
        }
        else {
            return false; // Always show update info if user does not want to see if system is up to date.
        }
    }

    private void downloadUpdate(String downloadUrl, String downloadName) {
        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(downloadUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription(downloadUrl).setTitle(getString(R.string.download_description));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadName);
        request.setVisibleInDownloadsUi(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        } else {
            //noinspection deprecation as it is only for older Android versions.
            request.setShowRunningNotification(true);
        }
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        downloadManager.enqueue(request);
        Toast.makeText(getActivity(), getString(R.string.download_in_background), Toast.LENGTH_LONG).show();
    }

    private void generateCircleDiagram(CyanogenOTAUpdate cyanogenOTAUpdate) {
        if (isAdded()) {
            PieChart circleDiagram = (PieChart) rootView.findViewById(R.id.updateInformationRollOutPercentageDiagram);
            List<Entry> chartData = new ArrayList<>();
            int percentage = cyanogenOTAUpdate.getRollOutPercentage();
            chartData.add(0, new Entry(percentage, 0));
            if (percentage < 100) {
                chartData.add(1, new Entry(100 - percentage, 1));
            }
            ArrayList<String> xVals = new ArrayList<>();
            xVals.add(0, getString(R.string.update_information_received_update));
            if (percentage < 100) {
                xVals.add(1, getString(R.string.update_information_not_received_update));
            }
            PieDataSet pieDataSet = new PieDataSet(chartData, "");
            pieDataSet.setColors(new int[]{getResources().getColor(R.color.lightBlue), getResources().getColor(android.R.color.darker_gray)});


            PieData pieData = new PieData(xVals, pieDataSet);
            pieData.setDrawValues(false);
            pieData.setValueTextSize(12);
            circleDiagram.setDrawSliceText(false);
            circleDiagram.setCenterText(percentage + "%");
            circleDiagram.setDescription("");
            Legend legend = circleDiagram.getLegend();
            legend.setForm(Legend.LegendForm.CIRCLE);
            legend.setFormSize(10);
            legend.setTextSize(12);
            circleDiagram.setUsePercentValues(true);
            circleDiagram.setData(pieData);
            circleDiagram.setTouchEnabled(false);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                circleDiagram.getLegend().setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);
            } else {
                circleDiagram.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            }
            circleDiagram.setBackgroundColor(getResources().getColor(R.color.chart_background));
            circleDiagram.invalidate();
        }
    }

    @Override
    public void onRefresh() {
        if (networkConnectionManager.checkNetworkConnection()) {
            getUpdateInformation();
        } else if (settingsManager.checkIfCacheIsAvailable()) {
            getOfflineUpdateInformation();
        } else {
            showNetworkError();
        }
    }

    private class GetServerStatus extends  AsyncTask<Void, Void, ServerStatus> {

        @Override
        protected ServerStatus doInBackground(Void... arg0) {
            return getServerConnector().getServerStatus();
        }

        @Override
        protected void onPostExecute(ServerStatus serverStatus) {
            displayServerStatus(serverStatus);
        }
    }

    private class GetServerMessages extends  AsyncTask<Void, Void, List<ServerMessage>> {

        @Override
        protected List<ServerMessage> doInBackground(Void... arg0) {
            return getServerConnector().getServerMessages();
        }

        @Override
        protected void onPostExecute(List<ServerMessage> serverMessages) {
            displayServerMessages(serverMessages);
        }
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetUpdateInformation extends AsyncTask<Void, Void, CyanogenOTAUpdate> {

        @Override
        protected CyanogenOTAUpdate doInBackground(Void... arg0) {
            CyanogenOTAUpdate cyanogenOTAUpdate = getServerConnector().getCyanogenOTAUpdate(updateDataLink);
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
            if (networkConnectionManager.checkNetworkConnection()) {
                displayUpdateInformation(result, true);
            } else {
                displayUpdateInformation(result, false);
            }
        }
    }

    private void showNetworkError() {
        DialogFragment networkErrorFragment = new NetworkErrorFragment();
        networkErrorFragment.show(getFragmentManager(), "NetworkError");
    }

    private void showMaintenanceError() {
        hideAllInterfaceElements();

        DialogFragment serverMaintenanceErrorFragment = new ServerMaintenanceErrorFragment();
        serverMaintenanceErrorFragment.show(getFragmentManager(), "MaintenanceError");
    }

    private void showAppNotValidError() {
        hideAllInterfaceElements();

        DialogFragment appNotValidErrorFragment = new AppNotValidErrorFragment();
        appNotValidErrorFragment.show(getFragmentManager(), "AppNotValidError");
    }

    private void hideAllInterfaceElements() {
        try {
            error = true;
            rootView.findViewById(R.id.updateInformationRefreshLayout).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationSystemIsUpToDateRefreshLayout).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationAdView).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationFirstServerMessageBar).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationFirstServerMessageTextView).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationSecondServerMessageBar).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationSecondServerMessageTextView).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationNewAppNotificationBar).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationNewAppNotificationTextView).setVisibility(View.GONE);
        } catch (Throwable ignored) {
        }
    }

}