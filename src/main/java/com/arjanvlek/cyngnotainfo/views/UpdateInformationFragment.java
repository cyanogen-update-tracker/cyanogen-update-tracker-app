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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;

import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.*;

public class UpdateInformationFragment extends AbstractFragment implements SwipeRefreshLayout.OnRefreshListener {

    private String deviceName;
    private String updateDataLink;

    private SwipeRefreshLayout refreshLayout;

    private RelativeLayout rootView;
    private AdView adView;

    private SettingsManager settingsManager;
    private NetworkConnectionManager networkConnectionManager;

    private DateTime refreshedDate;
    private boolean isFetched;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsManager = new SettingsManager(getActivity().getApplicationContext());
        networkConnectionManager = new NetworkConnectionManager(getActivity().getApplicationContext());
        deviceName = settingsManager.getPreference(PROPERTY_DEVICE);
        updateDataLink = settingsManager.getPreference(PROPERTY_UPDATE_DATA_LINK);

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
            refreshLayout.setOnRefreshListener(this);
            refreshLayout.setColorSchemeResources(R.color.lightBlue, R.color.holo_orange_light, R.color.holo_red_light);
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
        new GetUpdateInformation().execute();
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

    public void displayUpdateInformation(final CyanogenOTAUpdate cyanogenOTAUpdate, boolean online) {
        if (cyanogenOTAUpdate != null && isAdded()) {
            generateCircleDiagram(cyanogenOTAUpdate);
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
            //noinspection deprecation
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

}