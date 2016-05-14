package com.arjanvlek.cyngnotainfo.views;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arjanvlek.cyngnotainfo.ApplicationContext;
import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.MainActivity;
import com.arjanvlek.cyngnotainfo.Model.ServerMessage;
import com.arjanvlek.cyngnotainfo.Model.ServerStatus;
import com.arjanvlek.cyngnotainfo.Model.SystemVersionProperties;
import com.arjanvlek.cyngnotainfo.Support.DateTimeFormatter;
import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.MD5;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;

import static com.arjanvlek.cyngnotainfo.ApplicationContext.NO_CYANOGEN_OS;
import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.*;

public class UpdateInformationFragment extends AbstractUpdateInformationFragment implements SwipeRefreshLayout.OnRefreshListener, MessageDialog.ErrorDialogListener {

    private String deviceName;

    private SwipeRefreshLayout updateInformationRefreshLayout;
    private SwipeRefreshLayout systemIsUpToDateRefreshLayout;

    private RelativeLayout rootView;
    private AdView adView;
    private Context context;

    private DateTime refreshedDate;
    private boolean isFetched;

    public static final int NOTIFICATION_ID = 1;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(settingsManager != null) {
            deviceName = settingsManager.getPreference(PROPERTY_DEVICE);
        }
        if(getActivity() != null) {
            context = getActivity().getApplicationContext();
        }

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
        if (updateInformationRefreshLayout == null && rootView != null && isAdded()) {
            updateInformationRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.updateInformationRefreshLayout);
            systemIsUpToDateRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.updateInformationSystemIsUpToDateRefreshLayout);
            if(updateInformationRefreshLayout != null) {
                updateInformationRefreshLayout.setOnRefreshListener(this);
                updateInformationRefreshLayout.setColorSchemeResources(R.color.lightBlue, R.color.holo_orange_light, R.color.holo_red_light);
            }
            if(systemIsUpToDateRefreshLayout != null) {
                systemIsUpToDateRefreshLayout.setOnRefreshListener(this);
                systemIsUpToDateRefreshLayout.setColorSchemeResources(R.color.lightBlue, R.color.holo_orange_light, R.color.holo_red_light);
            }
        }
        if (!isFetched && settingsManager.checkIfSettingsAreValid() && isAdded()) {
            if (networkConnectionManager.checkNetworkConnection()) {
                getServerData();
                showAds();
                refreshedDate = DateTime.now();
                isFetched = true;
            } else if (settingsManager.checkIfCacheIsAvailable()) {
                displayUpdateInformation(buildOfflineCyanogenOTAUpdate(), false, false);
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
        if(rootView != null) {
            adView = (AdView) rootView.findViewById(R.id.updateInformationAdView);
        }
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
        if (refreshedDate != null && isFetched && settingsManager.checkIfSettingsAreValid() && isAdded()) {
            if (refreshedDate.plusMinutes(5).isBefore(DateTime.now())) {
                if (networkConnectionManager.checkNetworkConnection()) {
                    getServerData();
                    refreshedDate = DateTime.now();
                } else if (settingsManager.checkIfCacheIsAvailable()) {
                    displayUpdateInformation(buildOfflineCyanogenOTAUpdate(), false, false);
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
        if(getDownloading() && isAdded()) {
            NotificationManager manager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(NOTIFICATION_ID);
        }
        if (adView != null) {
            adView.destroy();
        }
    }

    private void getServerData() {
        new GetUpdateInformation().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        if(settingsManager.showNewsMessages() || settingsManager.showAppUpdateMessages()) {
            new GetServerStatus().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
        if(settingsManager.showNewsMessages()) {
            new GetServerMessages().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    @Override
    protected CyanogenOTAUpdate buildOfflineCyanogenOTAUpdate() {
        CyanogenOTAUpdate cyanogenOTAUpdate = new CyanogenOTAUpdate();
        cyanogenOTAUpdate.setName(settingsManager.getPreference(PROPERTY_OFFLINE_UPDATE_NAME));
        cyanogenOTAUpdate.setSize(settingsManager.getIntPreference(PROPERTY_OFFLINE_UPDATE_DOWNLOAD_SIZE));
        cyanogenOTAUpdate.setDescription(settingsManager.getPreference(PROPERTY_OFFLINE_UPDATE_DESCRIPTION));
        cyanogenOTAUpdate.setUpdateInformationAvailable(true);
        cyanogenOTAUpdate.setFileName(settingsManager.getPreference(PROPERTY_OFFLINE_FILE_NAME));
        return cyanogenOTAUpdate;
    }

    public void displayServerMessages(List<ServerMessage> serverMessages) {
        if(serverMessages != null && settingsManager.showNewsMessages()) {
            rootView.findViewById(R.id.updateInformationFirstServerMessageBar).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationSecondServerMessageBar).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationFirstServerMessageTextView).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationSecondServerMessageTextView).setVisibility(View.GONE);
            int i = 0;
            for (ServerMessage message : serverMessages) {
                // There may never be more than 2 messages displayed in this version of the app.
                if(!message.isDeviceSpecific() || (message.getDeviceId() == settingsManager.getLongPreference(PROPERTY_DEVICE_ID) && (message.getUpdateMethodId() == null || message.getUpdateMethodId() == settingsManager.getLongPreference(PROPERTY_UPDATE_METHOD_ID)))) {
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
                            serverMessageBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_green_light));
                            break;
                        case MEDIUM:
                            serverMessageBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_orange_light));
                            break;
                        case HIGH:
                            serverMessageBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_red_light));
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
                    i++;
                }
            }
        }
    }

    public void displayServerStatus(ServerStatus serverStatus) {

        if(serverStatus != null && serverStatus.getStatus() != ServerStatus.Status.OK) {
            View serverStatusWarningBar = rootView.findViewById(R.id.updateInformationServerErrorBar);
            TextView serverStatusWarningTextView = (TextView) rootView.findViewById(R.id.updateInformationServerErrorTextView);

            if(settingsManager.showNewsMessages()) {
                serverStatusWarningBar.setVisibility(View.VISIBLE);
                serverStatusWarningTextView.setVisibility(View.VISIBLE);
            }
            if(isAdded()) {
                switch (serverStatus.getStatus()) {
                    case WARNING:
                        if (settingsManager.showNewsMessages()) {
                            serverStatusWarningBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_orange_light));
                            serverStatusWarningTextView.setText(getString(R.string.server_status_warning));
                        }
                        break;
                    case ERROR:
                        if (settingsManager.showNewsMessages()) {
                            serverStatusWarningBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_red_light));
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
                        if (settingsManager.showNewsMessages()) {
                            serverStatusWarningBar.setBackgroundColor(ContextCompat.getColor(context, R.color.holo_orange_light));
                            serverStatusWarningTextView.setText(getString(R.string.server_status_unreachable));
                        }
                        break;
                }
            }
        }
        if(serverStatus != null && settingsManager.showAppUpdateMessages()) {
            if (!checkIfAppIsUpToDate(serverStatus.getLatestAppVersion()) && isAdded()) {
                View appUpdateNotificationBar = rootView.findViewById(R.id.updateInformationNewAppNotificationBar);
                TextView appUpdateNotificationTextView = (TextView) rootView.findViewById(R.id.updateInformationNewAppNotificationTextView);
                appUpdateNotificationBar.setVisibility(View.VISIBLE);
                appUpdateNotificationTextView.setVisibility(View.VISIBLE);

                appUpdateNotificationTextView.setText(Html.fromHtml(String.format(getString(R.string.new_app_version), serverStatus.getLatestAppVersion())));
                appUpdateNotificationTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }


    public void displayUpdateInformation(final CyanogenOTAUpdate cyanogenOTAUpdate, final boolean online, boolean displayInfoWhenUpToDate) {
        // Abort if no update data is found or if the fragment is not attached to its activity to prevent crashes.
        if(cyanogenOTAUpdate == null || !isAdded()) {
            return;
        }

        // Display the "No connection" bar depending on the network status of the device.
        View noConnectionBar = rootView.findViewById(R.id.updateInformationNoConnectionBar);
        TextView noConnectionTextField = (TextView) rootView.findViewById(R.id.updateInformationNoConnectionTextView);

        View updateInformationLayout = rootView.findViewById(R.id.updateInformationRefreshLayout);
        View systemIsUpToDateLayout = rootView.findViewById(R.id.updateInformationSystemIsUpToDateRefreshLayout);

        if(online) {
            if(noConnectionBar != null) {
                noConnectionBar.setVisibility(View.GONE);
            }
            if(noConnectionTextField != null) {
                noConnectionTextField.setVisibility(View.GONE);
            }
        } else {
            if(noConnectionBar != null) {
                noConnectionBar.setVisibility(View.VISIBLE);
            }
            if(noConnectionTextField != null) {
                noConnectionTextField.setVisibility(View.VISIBLE);
            }
        }

        // Display the "System is up to date" screen if the system is up to date and if "View update information" is NOT clicked, or if no update information is available.
        if(((cyanogenOTAUpdate.isSystemIsUpToDate(settingsManager)) && !displayInfoWhenUpToDate) || !cyanogenOTAUpdate.isUpdateInformationAvailable()) {
            // Show "System is up to date" view.
            updateInformationLayout.setVisibility(View.GONE);
            systemIsUpToDateLayout.setVisibility(View.VISIBLE);

            // Set current Cyanogen OS version if found.
            String cyanogenOSVersion = ((ApplicationContext)getActivity().getApplication()).getSystemVersionProperties().getCyanogenOSVersion();
            TextView versionNumberView = (TextView) rootView.findViewById(R.id.updateInformationSystemIsUpToDateVersionTextView);
            if(!cyanogenOSVersion.equals(NO_CYANOGEN_OS)) {
                versionNumberView.setVisibility(View.VISIBLE);
                versionNumberView.setText(String.format(getString(R.string.cyanogen_os_version), cyanogenOSVersion));
            } else {
                versionNumberView.setVisibility(View.GONE);
                versionNumberView.setVisibility(View.GONE);
            }

            // Set "No Update Information Is Available" button.
            Button updateInformationButton = (Button) rootView.findViewById(R.id.updateInformationSystemIsUpToDateStatisticsButton);
            if(!cyanogenOTAUpdate.isUpdateInformationAvailable()) {
                updateInformationButton.setText(getString(R.string.update_information_no_update_data_available));
                updateInformationButton.setClickable(false);
            } else {
                updateInformationButton.setText(getString(R.string.update_information_view_update_information));
                updateInformationButton.setClickable(true);
                updateInformationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        displayUpdateInformation(cyanogenOTAUpdate, online, true);
                    }
                });
            }

            // Save last time checked if online.
            if(online) {
                settingsManager.savePreference(PROPERTY_UPDATE_CHECKED_DATE, LocalDateTime.now().toString());
            }

            // Show last time checked.
            TextView dateCheckedView = (TextView) rootView.findViewById(R.id.updateInformationSystemIsUpToDateDateTextView);
            DateTimeFormatter dateTimeFormatter = new DateTimeFormatter(context, this);
            dateCheckedView.setText(String.format(getString(R.string.update_information_last_checked_on), dateTimeFormatter.formatDateTime(settingsManager.getPreference(PROPERTY_UPDATE_CHECKED_DATE))));
        }

        // Display the "Update information" screen if an update is available or if View Update Information is clicked on an up to date device.
        else {
            // Show "System update available" view.
            updateInformationLayout.setVisibility(View.VISIBLE);
            systemIsUpToDateLayout.setVisibility(View.GONE);

            // Display available update version number.
            TextView buildNumberView = (TextView) rootView.findViewById(R.id.updateInformationBuildNumberView);
            if (cyanogenOTAUpdate.getName() != null && !cyanogenOTAUpdate.getName().equals("null")) {
                buildNumberView.setText(cyanogenOTAUpdate.getName());
            } else {
                buildNumberView.setText(String.format(getString(R.string.unknown_update_name), deviceName));
            }

            // Display download size.
            TextView downloadSizeView = (TextView) rootView.findViewById(R.id.updateInformationDownloadSizeView);
            downloadSizeView.setText(String.format(getString(R.string.download_size_megabyte), cyanogenOTAUpdate.getSize()));

            // Display update description.
            String description = cyanogenOTAUpdate.getDescription();
            TextView descriptionView = (TextView) rootView.findViewById(R.id.updateDescriptionView);
            descriptionView.setText(description != null && !description.isEmpty() && !description.equals("null") ? description : getString(R.string.update_description_not_available));

            // Display update file name.
            TextView fileNameView = (TextView) rootView.findViewById(R.id.updateFileNameView);
            fileNameView.setText(String.format(getString(R.string.update_information_file_name), cyanogenOTAUpdate.getFileName()));

            final Button downloadButton = (Button) rootView.findViewById(R.id.updateInformationDownloadButton);

            // Activate download button, or make it gray when the device is offline or if the update is not downloadable.
            if (online && cyanogenOTAUpdate.getDownloadUrl() != null) {
                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        if(mainActivity != null) {
                            if(mainActivity.hasDownloadPermissions()) {
                                new UpdateDownloader().execute(cyanogenOTAUpdate.getDownloadUrl(), cyanogenOTAUpdate.getFileName());
                                downloadButton.setText(getString(R.string.downloading));
                                downloadButton.setClickable(false);
                            } else {
                                mainActivity.requestDownloadPermissions();
                            }
                        }
                    }
                });
                downloadButton.setEnabled(true);
                downloadButton.setTextColor(ContextCompat.getColor(context, R.color.lightBlue));
            } else {
                downloadButton.setEnabled(false);
                downloadButton.setTextColor(ContextCompat.getColor(context, R.color.dark_grey));
            }

            // Format top title based on system version installed.
            TextView headerLabel = (TextView) rootView.findViewById(R.id.headerLabel);
            Button updateInstallationGuideButton = (Button) rootView.findViewById(R.id.updateInstallationInstructionsButton);
            View downloadSizeTable = rootView.findViewById(R.id.buttonTable);
            View downloadSizeImage = rootView.findViewById(R.id.downloadSizeImage);


            if(displayInfoWhenUpToDate) {
                headerLabel.setText(getString(R.string.update_information_installed_update));
                downloadButton.setVisibility(View.GONE);
                updateInstallationGuideButton.setVisibility(View.GONE);
                fileNameView.setVisibility(View.GONE);
                downloadSizeTable.setVisibility(View.GONE);
                downloadSizeImage.setVisibility(View.GONE);
                downloadSizeView.setVisibility(View.GONE);
            } else {
                headerLabel.setText(getString(R.string.update_information_latest_available_update));
                downloadButton.setVisibility(View.VISIBLE);
                updateInstallationGuideButton.setVisibility(View.VISIBLE);
                fileNameView.setVisibility(View.VISIBLE);
                downloadSizeTable.setVisibility(View.VISIBLE);
                downloadSizeImage.setVisibility(View.VISIBLE);
                downloadSizeView.setVisibility(View.VISIBLE);
            }



        }
        if(online) {
            // Save update data for offline viewing
            settingsManager.savePreference(PROPERTY_OFFLINE_UPDATE_NAME, cyanogenOTAUpdate.getName());
            settingsManager.saveIntPreference(PROPERTY_OFFLINE_UPDATE_DOWNLOAD_SIZE, cyanogenOTAUpdate.getSize());
            settingsManager.savePreference(PROPERTY_OFFLINE_UPDATE_DESCRIPTION, cyanogenOTAUpdate.getDescription());
            settingsManager.savePreference(PROPERTY_OFFLINE_FILE_NAME, cyanogenOTAUpdate.getFileName());
            settingsManager.savePreference(PROPERTY_UPDATE_CHECKED_DATE, LocalDateTime.now().toString());
        }

        // Hide the refreshing icon if it is present.
        hideRefreshIcons();
    }

    @Override
    public void onDialogRetryButtonClick(DialogFragment dialogFragment) {
        Button downloadButton = (Button)rootView.findViewById(R.id.updateInformationDownloadButton);
        new UpdateDownloader().execute(cyanogenOTAUpdate.getDownloadUrl(), cyanogenOTAUpdate.getFileName());
        downloadButton.setText(getString(R.string.downloading));
        downloadButton.setClickable(false);
        dialogFragment.dismiss();
    }

    @Override
    public void onDialogCancelButtonClick(DialogFragment dialogFragment) {
        dialogFragment.dismiss();
    }

    @Override
    public void onDialogGooglePlayButtonClick(DialogFragment dialogFragment) {
        try {
            final String appPackageName = BuildConfig.APPLICATION_ID;
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        } catch (Exception ignored) {

        }
        dialogFragment.dismiss();
    }

    private class UpdateDownloader extends AsyncTask<String, Long, String> {
        int i=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDownloadProgressBar();
            setDownloading(true);
            getDownloadProgressBar().setIndeterminate(false);
            getDownloadCancelButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isAdded()) {
                        getDownloadButton().setClickable(true);
                        getDownloadButton().setText(getString(R.string.download));
                        hideDownloadProgressBar();
                        hideDownloadNotification();
                    } else {
                        hideDownloadNotification();
                    }
                    setDownloading(false);
                    cancel(true);
                }
            });
        }

        @Override
        protected String doInBackground(String... urls) {
            String filename = urls[1];

            try {
                int count;
                URL url = new URL(urls[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int lengthOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + filename);

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((total*100)/lengthOfFile);
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                if(e.getMessage().contains("No space left")) {
                    return "NO_SPACE_ERR";
                } else if (e.getMessage().contains("ENOENT")) {
                    return "NO_DOWNLOAD_DIR_ERR";
                } else {
                    return null;
                }
            }
            return filename;
        }

        protected void onProgressUpdate(Long... progress) {
            i++;
            getDownloadProgressBar().setProgress(progress[0].intValue());
            if( i > 100) {
                showDownloadNotification(progress[0].intValue(), false, false, false);
                i = 0;
            }
        }

        @Override
        protected void onPostExecute(String fileName) {
            if(fileName == null) {
                if(isAdded()) {
                    showDownloadError(getString(R.string.download_error), getString(R.string.download_error_network), getString(R.string.download_error_retry), getString(R.string.download_error_close), true);
                    getDownloadButton().setClickable(true);
                    getDownloadButton().setText(getString(R.string.download));
                    hideDownloadProgressBar();
                    setDownloading(false);
                } else {
                    setDownloading(false);
                    showDownloadNotification(0,false, false, true);
                }

            } else if(fileName.equals("NO_SPACE_ERR")) {
                if(isAdded()) {
                    showDownloadError(getString(R.string.download_error), getString(R.string.download_error_storage), getString(R.string.download_error_retry), getString(R.string.download_error_close), true);
                    getDownloadButton().setClickable(true);
                    getDownloadButton().setText(getString(R.string.download));
                    hideDownloadProgressBar();
                    setDownloading(false);
                } else {
                    setDownloading(false);
                    showDownloadNotification(0,false, false, true);
                }
            } else if(fileName.equals("NO_DOWNLOAD_DIR_ERR")) {
                if(isAdded() && !makeDownloadDirectory()) {
                    showDownloadError(getString(R.string.download_error), getString(R.string.download_error_directory), getString(R.string.download_error_close), null, true);
                    getDownloadButton().setClickable(true);
                    getDownloadButton().setText(getString(R.string.download));
                    hideDownloadProgressBar();
                    setDownloading(false);
                } else {
                    setDownloading(false);
                    execute(cyanogenOTAUpdate.getDownloadUrl(), cyanogenOTAUpdate.getFileName());
                    showDownloadNotification(0,false, false, true);
                }
            }
            else {
                if(isAdded()) {
                    getDownloadProgressBar().setIndeterminate(true);
                    getDownloadButton().setText(getString(R.string.verifying));
                }
                showDownloadNotification(0, true, false, false);
                new DownloadVerifier().execute(fileName);
            }
        }
    }

    private Button getDownloadButton() {
        return (Button) rootView.findViewById(R.id.updateInformationDownloadButton);
    }

    private void showDownloadProgressBar() {
        View downloadProgressBar = rootView.findViewById(R.id.downloadProgressTable);
        if(downloadProgressBar != null) {
            rootView.findViewById(R.id.downloadProgressTable).setVisibility(View.VISIBLE);
        }
    }

    private ProgressBar getDownloadProgressBar() {
        return (ProgressBar) rootView.findViewById(R.id.updateInformationDownloadProgressBar);
    }

    private ImageButton getDownloadCancelButton() {
        return (ImageButton) rootView.findViewById(R.id.updateInformationDownloadCancelButton);
    }

    private void hideDownloadProgressBar() {
        rootView.findViewById(R.id.downloadProgressTable).setVisibility(View.GONE);

    }

    private void showDownloadNotification(final Integer progress, final boolean indeterminate, final boolean complete, final boolean failed) {
        NotificationCompat.Builder builder;
        try {
            if (complete) {
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                builder = new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setContentTitle(getString(R.string.download_complete))
                        .setContentText(getString(R.string.download_cyanogen_os) + " " + cyanogenOTAUpdate.getName())
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
                try {
                    Toast.makeText(context, getString(R.string.download_complete), Toast.LENGTH_LONG).show();
                } catch(Exception ignore) {

                }
            } else if (failed) {
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                builder = new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setContentTitle(getString(R.string.download_failed))
                        .setContentText(getString(R.string.download_cyanogen_os) + " " + cyanogenOTAUpdate.getName())
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

            } else if (indeterminate) {
                builder = new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setContentTitle(getString(R.string.verifying))
                        .setContentText(getString(R.string.download_cyanogen_os) + " " + cyanogenOTAUpdate.getName());
            } else {
                builder = new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setContentTitle(getString(R.string.downloading))
                        .setContentText(getString(R.string.download_cyanogen_os) + " " + cyanogenOTAUpdate.getName());
            }

            if (Build.VERSION.SDK_INT >= 21) {
                builder.setCategory(Notification.CATEGORY_PROGRESS);
            }
            NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            if (!complete && !failed) {
                builder.setOngoing(true);
                builder.setProgress(100, progress, indeterminate);
            }
            manager.notify(NOTIFICATION_ID, builder.build());
        } catch(Exception e) {
            try {
                NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(NOTIFICATION_ID);
            } catch(Exception e1) {
                try {
                    // If cancelling the notification fails fails (and yes, it happens!), then I assume either that the user's device has a (corrupt) custom firmware or that something is REALLY going wrong.
                    // We try it once more but now with the Application Context instead of the Activity.
                    NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(NOTIFICATION_ID);
                } catch (Exception ignored) {
                    // If the last attempt has also failed, well then there's no hope.
                    // We leave everything as is, but the user will likely be stuck with a download notification that stays until a reboot.
                }
            }
        }
    }

    private void hideDownloadNotification() {
        NotificationManager manager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }


    private class DownloadVerifier extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String filename = params[0];
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + filename);
            return cyanogenOTAUpdate == null || cyanogenOTAUpdate.getMD5Sum() == null || MD5.checkMD5(cyanogenOTAUpdate.getMD5Sum(), file);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(!result) {
                showDownloadError(getString(R.string.download_error), getString(R.string.download_error_corrupt), getString(R.string.download_error_retry), getString(R.string.download_error_close), true);
            }
            getDownloadButton().setClickable(true);
            getDownloadButton().setText(getString(R.string.download));
            hideDownloadProgressBar();
            setDownloading(false);
            if(result) {
                showDownloadNotification(0, false, true, false);
            } else {
                showDownloadNotification(0, false, false, true);
            }
        }
    }

    private void showDownloadError(String title, String errorMessage, String button1, String button2, boolean closable) {
        DialogFragment errorDialog = new MessageDialog();
        Bundle args = new Bundle(4);
        args.putString("message", errorMessage);
        args.putString("title", title);
        args.putString("button1", button1);
        args.putString("button2", button2);
        args.putBoolean("closable", closable);
        errorDialog.setArguments(args);
        errorDialog.setTargetFragment(this, 0);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(errorDialog, "DownloadError");
        transaction.commitAllowingStateLoss();
        showDownloadNotification(0, false, false, true);
    }

    @Override
    public void onRefresh() {
        if(getDownloading()) {
            hideRefreshIcons();
        } else if (networkConnectionManager.checkNetworkConnection()) {
            getServerData();
        } else if (settingsManager.checkIfCacheIsAvailable()) {
            displayUpdateInformation(buildOfflineCyanogenOTAUpdate(), false, false);
        } else {
            showNetworkError();
        }
    }

    private void hideRefreshIcons() {
        if (updateInformationRefreshLayout != null) {
            if (updateInformationRefreshLayout.isRefreshing()) {
                updateInformationRefreshLayout.setRefreshing(false);
            }
        }
        if (systemIsUpToDateRefreshLayout != null) {
            if (systemIsUpToDateRefreshLayout.isRefreshing()) {
                systemIsUpToDateRefreshLayout.setRefreshing(false);
            }
        }
    }

    private boolean makeDownloadDirectory() {
        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return downloadDirectory.mkdirs();
    }

    private void setDownloading(boolean isDownloading) {
        try {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.setDownloading(isDownloading);
        } catch (Exception ignored) {

        }
    }

    private boolean getDownloading() {
        try {
            MainActivity activity = (MainActivity) getActivity();
            return activity.isDownloading();
        } catch (Exception e) {
            return false;
        }
    }
}