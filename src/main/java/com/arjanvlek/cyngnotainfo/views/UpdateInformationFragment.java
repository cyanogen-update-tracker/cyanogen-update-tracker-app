package com.arjanvlek.cyngnotainfo.views;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arjanvlek.cyngnotainfo.MainActivity;
import com.arjanvlek.cyngnotainfo.Support.DateTimeFormatter;
import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.ServiceHandler;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UpdateInformationFragment extends Fragment {

    private String deviceName;
    private String updateType;
    private String updateLink;

    private CyanogenOTAUpdate cyanogenOTAUpdate;

    private RelativeLayout rootView;
    private AdView adView;

    private ProgressDialog progressDialog;
    private DateTime refreshedDate;
    private boolean isFetched;

    public static final String ADS_TEST_DEVICE_ID_OWN_DEVICE = "7CFCF353FBC40363065F03DFAC7D7EE4";
    public static final String ADS_TEST_DEVICE_ID_EMULATOR_1 = "D9323E61DFC727F573528DB3820F7215";
    public static final String ADS_TEST_DEVICE_ID_EMULATOR_2 = "D732F1B481C5274B05D707AC197B33B2";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_APPEND);
        deviceName = preferences.getString(MainActivity.PROPERTY_DEVICE_TYPE, "");
        updateType = preferences.getString(MainActivity.PROPERTY_UPDATE_TYPE, "");
        updateLink = preferences.getString(MainActivity.PROPERTY_UPDATE_LINK, "");
        assert updateType != null;
        if(updateType.equals(MainActivity.FULL_UPDATE)) {
            updateType = getString(R.string.full_update);

        }
        else {
            updateType = getString(R.string.incremental_update);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = (RelativeLayout)inflater.inflate(R.layout.fragment_updateinformation, container, false);
        return rootView;
    }

    private boolean checkIfSettingsAreValid() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_APPEND);
        return preferences.contains(MainActivity.PROPERTY_DEVICE_TYPE) && preferences.contains(MainActivity.PROPERTY_UPDATE_TYPE) && preferences.contains(MainActivity.PROPERTY_UPDATE_LINK);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!isFetched && checkIfSettingsAreValid()) {
            if(checkNetworkConnection()) {
                getUpdateInformation();
                showAds();
                refreshedDate = DateTime.now();
                isFetched = true;
            }
            else {
                hideAds();
                showNetworkError();
            }
        }

    }

    private void hideAds() {
        if(adView != null) {
            adView.destroy();
        }
    }

    private void showAds() {
        adView = (AdView) rootView.findViewById(R.id.update_information_banner_field);
        if(adView != null) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(ADS_TEST_DEVICE_ID_OWN_DEVICE)
                    .addTestDevice(ADS_TEST_DEVICE_ID_EMULATOR_1)
                    .addTestDevice(ADS_TEST_DEVICE_ID_EMULATOR_2)
                    .addKeyword("smartphone")
                    .addKeyword("tablet")
                    .addKeyword("news apps")
                    .addKeyword("games")
                    .build();

            adView.loadAd(adRequest);
        }
    }
    private boolean checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(refreshedDate != null && isFetched && checkIfSettingsAreValid()) {
            if (refreshedDate.plusMinutes(5).isBefore(DateTime.now())) {
                if(checkNetworkConnection()) {
                    getUpdateInformation();
                    refreshedDate = DateTime.now();
                }
                else {
                    showNetworkError();
                }
            }
        }
    }

    private void getUpdateInformation() {
        new GetUpdateInformation().execute();
    }

    private void displayUpdateInformation() {
        if(cyanogenOTAUpdate != null) {
            generateCircleDiagram();
            TextView buildNumberView = (TextView) rootView.findViewById(R.id.buildNumberLabel);
            buildNumberView.setText(cyanogenOTAUpdate.getName() + " " + getString(R.string.string_for) + " " + deviceName + ", " + updateType + getString(R.string.dot));

            TextView downloadSizeView = (TextView) rootView.findViewById(R.id.downloadSizeLabel);
            downloadSizeView.setText((cyanogenOTAUpdate.getSize() / 1048576) + " " + getString(R.string.megabyte));

            TextView updatedDataView = (TextView) rootView.findViewById(R.id.lastUpdatedLabel);
            DateTimeFormatter dateTimeFormatter = new DateTimeFormatter(getActivity().getApplicationContext(),this);
            String dateUpdated = dateTimeFormatter.formatDateTime(cyanogenOTAUpdate.getDateUpdated());
            updatedDataView.setText(dateUpdated);


            if(cyanogenOTAUpdate.getDownloadUrl() != null) {
                Button downloadButton = (Button)rootView.findViewById(R.id.downloadButton);
                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadUpdate(cyanogenOTAUpdate.getDownloadUrl(), cyanogenOTAUpdate.getFileName());
                    }
                });
                downloadButton.setEnabled(true);
            }


            Button descriptionButton = (Button)rootView.findViewById(R.id.updateDescriptionButton);
            descriptionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getActivity(), UpdateDetailsActivity.class);
                    i.putExtra("update-description", cyanogenOTAUpdate.getDescription());
                    startActivity(i);
                }
            });
        }

    }

    private void downloadUpdate(String downloadUrl, String downloadName) {
        DownloadManager downloadManager =  (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(downloadUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription(getActivity().getString(R.string.downloader_description)).setTitle(getString(R.string.downloader_description));
        request.setDestinationInExternalFilesDir(getActivity(), Environment.DIRECTORY_DOWNLOADS, downloadName);
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        downloadManager.enqueue(request);
        Toast.makeText(getActivity(),getString(R.string.downloading_in_background),Toast.LENGTH_LONG).show();
    }

    private void generateCircleDiagram() {
        if(isAdded()) {
            PieChart pieChartView = (PieChart) rootView.findViewById(R.id.rolloutPercentageDiagram);
            List<Entry> chartData = new ArrayList<>();
            int percentage = cyanogenOTAUpdate.getRolloutPercentage();
            chartData.add(0, new Entry(percentage, 0));
            if (percentage < 100) {
                chartData.add(1, new Entry(100 - percentage, 1));
            }
            ArrayList<String> xVals = new ArrayList<>();
            xVals.add(0, getString(R.string.updated));
            if (percentage < 100) {
                xVals.add(1, getString(R.string.not_updated));
            }
            PieDataSet pieDataSet = new PieDataSet(chartData, "");
            pieDataSet.setColors(new int[]{getResources().getColor(R.color.lightblue), getResources().getColor(android.R.color.darker_gray)});


            PieData pieData = new PieData(xVals, pieDataSet);
            pieData.setDrawValues(false);
            pieData.setValueTextSize(12);
            pieChartView.setDrawSliceText(false);
            pieChartView.setCenterText(percentage + "%");
            pieChartView.setDescription("");
            Legend legend = pieChartView.getLegend();
            legend.setForm(Legend.LegendForm.CIRCLE);
            legend.setFormSize(10);
            legend.setTextSize(12);
            pieChartView.setUsePercentValues(true);
            pieChartView.setData(pieData);
            pieChartView.setMinimumWidth(pieChartView.getWidth());
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                pieChartView.getLegend().setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);
            } else {
                pieChartView.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            }
            pieChartView.setBackgroundColor(getResources().getColor(R.color.chart_background));
            pieChartView.invalidate();


        }

    }


    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetUpdateInformation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.fetching_update));
            progressDialog.setTitle(getString(R.string.loading));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            progressDialog.show();


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler serviceHandler = new ServiceHandler();
            String jsonStr = serviceHandler.makeServiceCall(updateLink, ServiceHandler.GET);
            if (jsonStr != null) {
                try {
                    JSONObject object = new JSONObject(jsonStr);
                        cyanogenOTAUpdate = new CyanogenOTAUpdate();
                        cyanogenOTAUpdate.setDateUpdated(object.getString("date_updated"));
                        cyanogenOTAUpdate.setIncremental(object.getString("incremental"));
                        cyanogenOTAUpdate.setRequiredIncremental(object.getBoolean("required_incremental"));
                        cyanogenOTAUpdate.setSize(object.getInt("size"));
                        cyanogenOTAUpdate.setBuildNumber(object.getString("build_number"));
                        cyanogenOTAUpdate.setIncrementalParent(object.getString("incremental_parent"));
                        cyanogenOTAUpdate.setDownloadUrl(object.getString("download_url"));
                        cyanogenOTAUpdate.setFileName(object.getString("filename"));
                        cyanogenOTAUpdate.setSha1Sum(object.getString("sha1sum"));
                        cyanogenOTAUpdate.setType(object.getString("type"));
                        cyanogenOTAUpdate.setDescription(object.getString("description"));
                        cyanogenOTAUpdate.setDateCreatedUnix(object.getString("date_created_unix"));
                        cyanogenOTAUpdate.setRolloutPercentage(object.getInt("rollout_percentage"));
                        cyanogenOTAUpdate.setKey(object.getString("key"));
                        cyanogenOTAUpdate.setPath(object.getString("path"));
                        cyanogenOTAUpdate.setName(object.getString("name"));
                        cyanogenOTAUpdate.setMd5Sum(object.getString("md5sum"));
                        cyanogenOTAUpdate.setPublished(object.getBoolean("published"));
                        cyanogenOTAUpdate.setDateCreated(object.getString("date_created"));
                        cyanogenOTAUpdate.setModel(object.getString("model"));
                        cyanogenOTAUpdate.setApiLevel(object.getInt("api_level"));

                    } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                if(progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                showNetworkError();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            displayUpdateInformation();
            if(progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void showNetworkError() {
        DialogFragment networkErrorFragment = new NetworkErrorFragment();
        networkErrorFragment.show(getFragmentManager(), "NetworkError");
    }


}