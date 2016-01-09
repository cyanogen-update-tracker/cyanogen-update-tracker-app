package com.arjanvlek.cyngnotainfo.views;

import android.app.DownloadManager;
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
    private boolean isDownloading;
    private boolean error;

    public static final int NOTIFICATION_ID = 1;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceName = settingsManager.getPreference(PROPERTY_DEVICE);
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
        if (updateInformationRefreshLayout == null && rootView != null) {
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
        if (!isFetched && settingsManager.checkIfSettingsAreValid()) {
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
        if(isDownloading) {
            NotificationManager manager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(NOTIFICATION_ID);
        }
        if (adView != null) {
            adView.destroy();
        }
    }

    private void getServerData() {
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

    @Override
    protected CyanogenOTAUpdate buildOfflineCyanogenOTAUpdate() {
        CyanogenOTAUpdate cyanogenOTAUpdate = new CyanogenOTAUpdate();
        cyanogenOTAUpdate.setName(settingsManager.getPreference(PROPERTY_OFFLINE_UPDATE_NAME));
        cyanogenOTAUpdate.setSize(settingsManager.getIntPreference(PROPERTY_OFFLINE_UPDATE_DOWNLOAD_SIZE));
        cyanogenOTAUpdate.setDescription(settingsManager.getPreference(PROPERTY_OFFLINE_UPDATE_DESCRIPTION));
        cyanogenOTAUpdate.setUpdateInformationAvailable(true);
        return cyanogenOTAUpdate;
    }

    public void displayServerMessages(List<ServerMessage> serverMessages) {
        if(serverMessages != null && settingsManager.showNewsMessages() && !error) {
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
                }
                i++;
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
        if(serverStatus != null && settingsManager.showAppUpdateMessages() && !error) {
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


    public void displayUpdateInformation(final CyanogenOTAUpdate cyanogenOTAUpdate, boolean online, boolean force) {
        View noConnectionBar = rootView.findViewById(R.id.updateInformationNoConnectionBar);
        TextView noConnectionTextField = (TextView) rootView.findViewById(R.id.updateInformationNoConnectionTextView);
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
        if(cyanogenOTAUpdate != null && !cyanogenOTAUpdate.isUpdateInformationAvailable()) {
            // switch views
            rootView.findViewById(R.id.updateInformationRefreshLayout).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationSystemIsUpToDateRefreshLayout).setVisibility(View.VISIBLE);

            String cyanogenOSVersion = getSystemVersionProperties().getCyanogenOSVersion();
            TextView versionNumberView = (TextView) rootView.findViewById(R.id.updateInformationSystemIsUpToDateVersionTextView);
            if(!cyanogenOSVersion.equals(NO_CYANOGEN_OS)) {
                versionNumberView.setVisibility(View.VISIBLE);
                versionNumberView.setText(String.format(getString(R.string.cyanogen_os_version), cyanogenOSVersion));
            } else {
                versionNumberView.setVisibility(View.GONE);
            }

            Button updateInformationButton = (Button) rootView.findViewById(R.id.updateInformationSystemIsUpToDateStatisticsButton);
            updateInformationButton.setText(getString(R.string.update_information_no_update_data_available));
            updateInformationButton.setClickable(false);

            DateTimeFormatter dateTimeFormatter = new DateTimeFormatter(context, this);
            TextView dateCheckedView = (TextView) rootView.findViewById(R.id.updateInformationSystemIsUpToDateDateTextView);
            if(online) {
                settingsManager.savePreference(PROPERTY_UPDATE_CHECKED_DATE, dateTimeFormatter.formatDateTime(LocalDateTime.now()));
            }
            dateCheckedView.setText(String.format(getString(R.string.update_information_last_checked_on), settingsManager.getPreference(PROPERTY_UPDATE_CHECKED_DATE)));
            // Hide the refreshing icon
            hideRefreshIcons();
        }
        else if(cyanogenOTAUpdate != null && checkIfSystemIsUpToDate(cyanogenOTAUpdate.getName(), 10000) && isAdded() && !force) {
             // switch views
            rootView.findViewById(R.id.updateInformationRefreshLayout).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationSystemIsUpToDateRefreshLayout).setVisibility(View.VISIBLE);

            String cyanogenOSVersion = getSystemVersionProperties().getCyanogenOSVersion();
            TextView versionNumberView = (TextView) rootView.findViewById(R.id.updateInformationSystemIsUpToDateVersionTextView);
            versionNumberView.setText(String.format(getString(R.string.cyanogen_os_version), cyanogenOSVersion));

            Button updateInformationButton = (Button) rootView.findViewById(R.id.updateInformationSystemIsUpToDateStatisticsButton);
            updateInformationButton.setText(getString(R.string.update_information_view_update_information));
            updateInformationButton.setClickable(true);
            updateInformationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showUpdateInformationIfUpToDate();
                }
            });

            DateTimeFormatter dateTimeFormatter = new DateTimeFormatter(context, this);
            TextView dateCheckedView = (TextView) rootView.findViewById(R.id.updateInformationSystemIsUpToDateDateTextView);
            if(online) {
                settingsManager.savePreference(PROPERTY_UPDATE_CHECKED_DATE, dateTimeFormatter.formatDateTime(LocalDateTime.now()));
            }
            dateCheckedView.setText(String.format(getString(R.string.update_information_last_checked_on), settingsManager.getPreference(PROPERTY_UPDATE_CHECKED_DATE) ));
            // Hide the refreshing icon
            hideRefreshIcons();
        }
        else {
            if (cyanogenOTAUpdate != null && isAdded()) {
                rootView.findViewById(R.id.updateInformationRefreshLayout).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.updateInformationSystemIsUpToDateRefreshLayout).setVisibility(View.GONE);
                TextView buildNumberView = (TextView) rootView.findViewById(R.id.updateInformationBuildNumberView);
                if (cyanogenOTAUpdate.getName() != null && !cyanogenOTAUpdate.getName().equals("null")) {
                    buildNumberView.setText(cyanogenOTAUpdate.getName());
                } else {
                    buildNumberView.setText(String.format(getString(R.string.unknown_update_name), deviceName));
                }

                TextView downloadSizeView = (TextView) rootView.findViewById(R.id.updateInformationDownloadSizeView);
                downloadSizeView.setText(String.format(getString(R.string.download_size_megabyte), (cyanogenOTAUpdate.getSize()) / 1048576));

                String description = cyanogenOTAUpdate.getDescription();
                TextView descriptionView = (TextView) rootView.findViewById(R.id.updateDescriptionView);
                descriptionView.setText(description != null && !description.isEmpty() && !description.equals("null") ? description : getString(R.string.update_description_not_available));

                TextView fileNameView = (TextView) rootView.findViewById(R.id.updateFileNameView);
                fileNameView.setText(String.format(getString(R.string.update_information_file_name), cyanogenOTAUpdate.getFileName()));

                final Button downloadButton = (Button) rootView.findViewById(R.id.updateInformationDownloadButton);
                if (online) {
                    if (cyanogenOTAUpdate.getDownloadUrl() != null) {
                        downloadButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
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
                                } else {
                                    //noinspection deprecation as it is only used on older Android versions.
                                    downloadUpdate(cyanogenOTAUpdate.getDownloadUrl(), cyanogenOTAUpdate.getFileName());
                                }
                            }
                        });
                        downloadButton.setEnabled(true);
                    }
                } else {
                    downloadButton.setEnabled(false);
                }

                // Save preferences for offline viewing
                settingsManager.savePreference(PROPERTY_OFFLINE_UPDATE_NAME, cyanogenOTAUpdate.getName());
                settingsManager.saveIntPreference(PROPERTY_OFFLINE_UPDATE_DOWNLOAD_SIZE, cyanogenOTAUpdate.getSize());
                settingsManager.savePreference(PROPERTY_OFFLINE_UPDATE_DESCRIPTION, cyanogenOTAUpdate.getDescription());

                // Hide the refreshing icon
                hideRefreshIcons();
            }
        }
    }

    private boolean checkIfSystemIsUpToDate(String newCyanogenOSVersion, int newDateCreated) {
        if(settingsManager.showIfSystemIsUpToDate()) {
            // This grabs Cyanogen OS version from build.prop. As there is no direct SDK way to do this, it has to be done in this way.
            SystemVersionProperties systemVersionProperties = getSystemVersionProperties();

            String cyanogenOSVersion = systemVersionProperties.getCyanogenOSVersion();

            int dateCreatedUtc = systemVersionProperties.getDateCreatedUtc();

            if(newCyanogenOSVersion == null) {
                return false;
            }

            if (dateCreatedUtc != -1 && dateCreatedUtc == newDateCreated) {
                return true;
            } else if (cyanogenOSVersion.equals(NO_CYANOGEN_OS)) {
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
            isDownloading = true;
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
                    isDownloading = false;
                } else {
                    isDownloading = false;
                    showDownloadNotification(0,false, false, true);
                }

            } else if(fileName.equals("NO_SPACE_ERR")) {
                if(isAdded()) {
                    showDownloadError(getString(R.string.download_error), getString(R.string.download_error_storage), getString(R.string.download_error_retry), getString(R.string.download_error_close), true);
                    getDownloadButton().setClickable(true);
                    getDownloadButton().setText(getString(R.string.download));
                    hideDownloadProgressBar();
                    isDownloading = false;
                } else {
                    isDownloading = false;
                    showDownloadNotification(0,false, false, true);
                }
            } else if(fileName.equals("NO_DOWNLOAD_DIR_ERR")) {
                if(isAdded()) {
                    showDownloadError(getString(R.string.download_error), getString(R.string.download_error_directory), getString(R.string.download_error_retry), getString(R.string.download_error_close), true);
                    getDownloadButton().setClickable(true);
                    getDownloadButton().setText(getString(R.string.download));
                    hideDownloadProgressBar();
                    isDownloading = false;
                } else {
                    isDownloading = false;
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
                builder = new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setContentTitle(getString(R.string.download_complete))
                        .setContentText(getString(R.string.download_cyanogen_os) + " " + cyanogenOTAUpdate.getName());
                try {
                    Toast.makeText(context, getString(R.string.download_complete), Toast.LENGTH_LONG).show();
                } catch(Exception ignore) {

                }
            } else if (failed) {
                builder = new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setContentTitle(getString(R.string.download_failed))
                        .setContentText(getString(R.string.download_cyanogen_os) + " " + cyanogenOTAUpdate.getName());

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
            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
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
            isDownloading = false;
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

    @Deprecated
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

    @Override
    public void onRefresh() {
        if (networkConnectionManager.checkNetworkConnection()) {
            getServerData();
        } else if (settingsManager.checkIfCacheIsAvailable()) {
            displayUpdateInformation(buildOfflineCyanogenOTAUpdate(), false, false);
        } else {
            showNetworkError();
        }
    }

    @Override
    protected void showNetworkError() {
        DialogFragment errorDialog = new MessageDialog();
        Bundle args = new Bundle(4);
        args.putString("message", getString(R.string.error_app_requires_network_connection_message));
        args.putString("title", getString(R.string.error_app_requires_network_connection));
        args.putString("button1", getString(R.string.download_error_close));
        args.putBoolean("closable", true);
        errorDialog.setArguments(args);
        errorDialog.setTargetFragment(this, 0);
        errorDialog.show(getFragmentManager(), "NetworkError");
    }

    private void showMaintenanceError() {
        hideAllInterfaceElements();
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

    private void showAppNotValidError() {
        hideAllInterfaceElements();
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

    private void showUpdateInformationIfUpToDate() {
        if(cyanogenOTAUpdate != null) {
            rootView.findViewById(R.id.updateInformationSystemIsUpToDateRefreshLayout).setVisibility(View.GONE);
            rootView.findViewById(R.id.updateInformationRefreshLayout).setVisibility(View.VISIBLE);
            displayUpdateInformation(cyanogenOTAUpdate, networkConnectionManager.checkNetworkConnection(), true);
        }
        else {
            getServerData();
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
}