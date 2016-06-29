package com.arjanvlek.cyngnotainfo.views;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
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
import com.arjanvlek.cyngnotainfo.MainActivity;
import com.arjanvlek.cyngnotainfo.Model.DownloadProgressData;
import com.arjanvlek.cyngnotainfo.Model.ServerMessage;
import com.arjanvlek.cyngnotainfo.Model.ServerStatus;
import com.arjanvlek.cyngnotainfo.Model.SystemVersionProperties;
import com.arjanvlek.cyngnotainfo.Support.DateTimeFormatter;
import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.UpdateDownloadListener;
import com.arjanvlek.cyngnotainfo.Support.UpdateDownloader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static android.app.DownloadManager.ERROR_CANNOT_RESUME;
import static android.app.DownloadManager.ERROR_DEVICE_NOT_FOUND;
import static android.app.DownloadManager.ERROR_FILE_ALREADY_EXISTS;
import static android.app.DownloadManager.ERROR_FILE_ERROR;
import static android.app.DownloadManager.ERROR_HTTP_DATA_ERROR;
import static android.app.DownloadManager.ERROR_INSUFFICIENT_SPACE;
import static android.app.DownloadManager.ERROR_TOO_MANY_REDIRECTS;
import static android.app.DownloadManager.ERROR_UNHANDLED_HTTP_CODE;
import static android.app.DownloadManager.PAUSED_QUEUED_FOR_WIFI;
import static android.app.DownloadManager.PAUSED_UNKNOWN;
import static android.app.DownloadManager.PAUSED_WAITING_FOR_NETWORK;
import static android.app.DownloadManager.PAUSED_WAITING_TO_RETRY;
import static com.arjanvlek.cyngnotainfo.ApplicationContext.LOCALE_DUTCH;
import static com.arjanvlek.cyngnotainfo.ApplicationContext.NO_CYANOGEN_OS;
import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.*;
import static com.arjanvlek.cyngnotainfo.Support.UpdateDownloader.NOT_SET;

public class UpdateInformationFragment extends AbstractUpdateInformationFragment {


    private SwipeRefreshLayout updateInformationRefreshLayout;
    private SwipeRefreshLayout systemIsUpToDateRefreshLayout;
    private RelativeLayout rootView;
    private AdView adView;

    private Context context;
    private UpdateDownloader updateDownloader;

    private DateTime refreshedDate;
    private boolean isFetched;
    private String deviceName;

    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SettingsManager is created in the parent class.
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
        if(isAdded()) {
            initLayout();
            initData();
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
        if (adView != null) {
            adView.destroy();
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

                    if (appLocale.equals(LOCALE_DUTCH)) {
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

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    appUpdateNotificationTextView.setText(Html.fromHtml(String.format(getString(R.string.new_app_version), serverStatus.getLatestAppVersion()), Html.FROM_HTML_MODE_LEGACY));
                } else {
                    //noinspection deprecation as it is only for older Android versions
                    appUpdateNotificationTextView.setText(Html.fromHtml(String.format(getString(R.string.new_app_version), serverStatus.getLatestAppVersion())));
                }
                appUpdateNotificationTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }


    public void displayUpdateInformation(final CyanogenOTAUpdate cyanogenOTAUpdate, final boolean online, boolean displayInfoWhenUpToDate) {
        // Abort if no update data is found or if the fragment is not attached to its activity to prevent crashes.
        if(cyanogenOTAUpdate == null || !isAdded()) {
            return;
        }

        if(!cyanogenOTAUpdate.isSystemIsUpToDateCheck()) {
            cyanogenOTAUpdate.setSystemIsUpToDate(isSystemUpToDateStringCheck(cyanogenOTAUpdate));
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
                       onDownloadButtonClick(downloadButton);
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

    /**
     * Additional check if system is up to date by comparing version Strings.
     * This is needed to show the "System is up to date" message for full updates as incremental (parent) versions are not checked there.
     * @param cyanogenOTAUpdate CyanogenOTAUpdate that needs to be checked against the current version.
     * @return True if the system is up to date, false if not.
     */
    private boolean isSystemUpToDateStringCheck(CyanogenOTAUpdate cyanogenOTAUpdate) {
        if(settingsManager.showIfSystemIsUpToDate()) {
            // This grabs Cyanogen OS version from build.prop. As there is no direct SDK way to do this, it has to be done in this way.
            SystemVersionProperties systemVersionProperties = ((ApplicationContext)getActivity().getApplication()).getSystemVersionProperties();

            String cyanogenOSVersion = systemVersionProperties.getCyanogenOSVersion();
            String newCyanogenOSVersion = cyanogenOTAUpdate.getName();

            if(newCyanogenOSVersion == null || newCyanogenOSVersion.isEmpty()) {
                return false;
            }

            if (cyanogenOSVersion == null || cyanogenOSVersion.isEmpty() || cyanogenOSVersion.equals(NO_CYANOGEN_OS)) {
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
                        cyanogenOSVersion = cyanogenOSVersion.replace("-", " ");
                        return newCyanogenOSVersion.contains(cyanogenOSVersion);
                    }
                }
            }
        }
        else {
            return false; // Always show update info if user does not want to see if system is up to date.
        }
    }

    private Button getDownloadButton() {
        return (Button) rootView.findViewById(R.id.updateInformationDownloadButton);
    }

    private TextView getDownloadStatusText() {
        return (TextView) rootView.findViewById(R.id.updateInformationDownloadDetailsView);
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

    private void showVerifyingNotification() {
        NotificationCompat.Builder builder;
        try {
                builder = new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setContentTitle(getString(R.string.verifying))
                        .setOngoing(true)
                        .setProgress(100, 50, true);

            if (Build.VERSION.SDK_INT >= 21) {
                builder.setCategory(Notification.CATEGORY_PROGRESS);
            }
            NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
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

    private void hideVerifyingNotification() {
        NotificationManager manager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
    }


    private void showDownloadError(String title, String message, String positiveButtonText, String negativeButtonText, boolean closable) {
        MessageDialog errorDialog = new MessageDialog()
                .setTitle(title)
                .setMessage(message)
                .setPositiveButtonText(positiveButtonText)
                .setNegativeButtonText(negativeButtonText)
                .setClosable(closable)
                .setDialogListener(new MessageDialog.DialogListener() {
                    @Override
                    public void onDialogPositiveButtonClick(DialogFragment dialogFragment) {
                        updateDownloader.cancelDownload();
                        updateDownloader.downloadUpdate(cyanogenOTAUpdate);
                    }

                    @Override
                    public void onDialogNegativeButtonClick(DialogFragment dialogFragment) {

                    }
                });
        errorDialog.setTargetFragment(this, 0);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(errorDialog, "DownloadError");
        transaction.commitAllowingStateLoss();
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

    private void initLayout() {
        if (updateInformationRefreshLayout == null && rootView != null && isAdded()) {
            updateInformationRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.updateInformationRefreshLayout);
            systemIsUpToDateRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.updateInformationSystemIsUpToDateRefreshLayout);
            if(updateInformationRefreshLayout != null) {
                updateInformationRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
                });
                updateInformationRefreshLayout.setColorSchemeResources(R.color.lightBlue, R.color.holo_orange_light, R.color.holo_red_light);
            }
            if(systemIsUpToDateRefreshLayout != null) {
                systemIsUpToDateRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
                });
                systemIsUpToDateRefreshLayout.setColorSchemeResources(R.color.lightBlue, R.color.holo_orange_light, R.color.holo_red_light);
            }
        }
    }

    private void initData() {
        if (!isFetched && settingsManager.checkIfSettingsAreValid()) {
            if (networkConnectionManager.checkNetworkConnection()) {
                getServerData();
                showAds();
                refreshedDate = DateTime.now();
                isFetched = true;
            } else if (settingsManager.checkIfCacheIsAvailable()) {
                cyanogenOTAUpdate = buildOfflineCyanogenOTAUpdate();
                displayUpdateInformation(cyanogenOTAUpdate, false, false);
                initDownloadManager();
                refreshedDate = DateTime.now();
                isFetched = true;
            } else {
                hideAds();
                showNetworkError();
            }
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

    @Override
    protected void initDownloadManager() {
        if(isAdded() && updateDownloader == null) {
            updateDownloader = new UpdateDownloader(getActivity())
                    .setUpdateDownloadListener(new UpdateDownloadListener() {
                        @Override
                        public void onDownloadManagerInit() {
                            getDownloadCancelButton().setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updateDownloader.cancelDownload();
                                }
                            });
                        }

                        @Override
                        public void onDownloadStarted(long downloadID) {
                            if(isAdded()) {
                                getDownloadButton().setText(getString(R.string.downloading));
                                getDownloadButton().setClickable(false);

                                showDownloadProgressBar();
                                getDownloadProgressBar().setIndeterminate(false);
                            }
                        }

                        @Override
                        public void onDownloadPending() {
                            if(isAdded()) {
                                showDownloadProgressBar();
                                getDownloadButton().setText(getString(R.string.downloading));
                                getDownloadButton().setClickable(false);
                                TextView downloadStatusText = getDownloadStatusText();
                                downloadStatusText.setText(getString(R.string.download_pending));
                            }
                        }

                        @Override
                        public void onDownloadProgressUpdate(DownloadProgressData downloadProgressData) {
                            if(isAdded()) {
                                showDownloadProgressBar();
                                getDownloadButton().setText(getString(R.string.downloading));
                                getDownloadButton().setClickable(false);
                                getDownloadProgressBar().setIndeterminate(false);
                                getDownloadProgressBar().setProgress(downloadProgressData.getProgress());

                                if(downloadProgressData.getDownloadSpeed() == NOT_SET || downloadProgressData.getTimeRemaining() == null) {
                                    getDownloadStatusText().setText(getString(R.string.download_progress_text_unknown_time_remaining, downloadProgressData.getProgress()));
                                } else {
                                    getDownloadStatusText().setText(getString(R.string.download_progress_text_with_time_remaining, downloadProgressData.getProgress(), downloadProgressData.getTimeRemaining().getMinutes(), downloadProgressData.getTimeRemaining().getSeconds(), downloadProgressData.getDownloadSpeed(), downloadProgressData.getSpeedUnits().getStringValue()));
                                }
                            }
                        }

                        @Override
                        public void onDownloadPaused(int statusCode) {
                            if(isAdded()) {
                                showDownloadProgressBar();
                                getDownloadButton().setText(getString(R.string.downloading));
                                getDownloadButton().setClickable(false);

                                TextView downloadStatusText = getDownloadStatusText();
                                switch (statusCode) {
                                    case PAUSED_QUEUED_FOR_WIFI:
                                        downloadStatusText.setText(getString(R.string.download_waiting_for_wifi));
                                        break;
                                    case PAUSED_WAITING_FOR_NETWORK:
                                        downloadStatusText.setText(getString(R.string.download_waiting_for_network));
                                        break;
                                    case PAUSED_WAITING_TO_RETRY:
                                        downloadStatusText.setText(getString(R.string.download_will_retry_soon));
                                        break;
                                    case PAUSED_UNKNOWN:
                                        downloadStatusText.setText(getString(R.string.download_paused_unknown));
                                        break;
                                }
                            }
                        }

                        @Override
                        public void onDownloadComplete() {
                            if(isAdded()) {
                                Toast.makeText(getApplicationContext(), getString(R.string.download_verifying), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onDownloadCancelled() {
                            if(isAdded()) {
                                getDownloadButton().setClickable(true);
                                getDownloadButton().setText(getString(R.string.download));
                                hideDownloadProgressBar();
                            }
                        }

                        @Override
                        public void onDownloadError(int statusCode) {
                            // Treat any HTTP status code exception (lower than 1000) as a network error.
                            // Handle any other errors according to the error message.
                            if(isAdded()) {
                                if (statusCode < 1000) {
                                    showDownloadError(getString(R.string.download_error), getString(R.string.download_error_network), getString(R.string.download_error_retry), getString(R.string.download_error_close), true);
                                } else {
                                    switch (statusCode) {
                                        case ERROR_UNHANDLED_HTTP_CODE:
                                        case ERROR_HTTP_DATA_ERROR:
                                        case ERROR_TOO_MANY_REDIRECTS:
                                            showDownloadError(getString(R.string.download_error), getString(R.string.download_error_network), getString(R.string.download_error_retry), getString(R.string.download_error_close), true);
                                            break;
                                        case ERROR_FILE_ERROR:
                                            updateDownloader.makeDownloadDirectory();
                                            showDownloadError(getString(R.string.download_error), getString(R.string.download_error_directory), getString(R.string.download_error_close), null, true);
                                            break;
                                        case ERROR_INSUFFICIENT_SPACE:
                                            showDownloadError(getString(R.string.download_error), getString(R.string.download_error_storage), getString(R.string.download_error_retry), getString(R.string.download_error_close), true);
                                            break;
                                        case ERROR_DEVICE_NOT_FOUND:
                                            showDownloadError(getString(R.string.download_error), getString(R.string.download_error_sd_card), getString(R.string.download_error_retry), getString(R.string.download_error_close), true);
                                            break;
                                        case ERROR_CANNOT_RESUME:
                                            updateDownloader.cancelDownload();
                                            if (networkConnectionManager.checkNetworkConnection() && cyanogenOTAUpdate != null && cyanogenOTAUpdate.getDownloadUrl() != null) {
                                                updateDownloader.downloadUpdate(cyanogenOTAUpdate);
                                            }
                                        case ERROR_FILE_ALREADY_EXISTS:
                                            Toast.makeText(getApplicationContext(), getString(R.string.update_already_downloaded), Toast.LENGTH_LONG).show();
                                            onUpdateDownloaded(true);
                                    }
                                }

                                // Make sure the failed download file gets deleted before the user tries to download it again.
                                updateDownloader.cancelDownload();
                                hideDownloadProgressBar();
                                onUpdateDownloaded(false);
                            }
                        }

                        @Override
                        public void onVerifyStarted() {
                            if(isAdded()) {
                                getDownloadProgressBar().setIndeterminate(true);
                                showVerifyingNotification();
                                getDownloadButton().setText(getString(R.string.verifying));
                                getDownloadStatusText().setText(getString(R.string.download_progress_text_verifying));
                            }
                        }

                        @Override
                        public void onVerifyError() {
                            if(isAdded()) {
                                showDownloadError(getString(R.string.download_error), getString(R.string.download_error_corrupt), getString(R.string.download_error_retry), getString(R.string.download_error_close), true);
                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + cyanogenOTAUpdate.getFileName());
                                try {
                                    //noinspection ResultOfMethodCallIgnored
                                    file.delete();
                                } catch (Exception ignored) {

                                }
                                hideVerifyingNotification();
                            }
                        }

                        @Override
                        public void onVerifyComplete() {
                            if(isAdded()) {
                                hideDownloadProgressBar();
                                hideVerifyingNotification();
                                onUpdateDownloaded(true);
                            }
                        }
                    });
            updateDownloader.checkDownloadProgress(cyanogenOTAUpdate);
        }
    }

    private void onUpdateDownloaded(boolean updateIsDownloaded) {
        final Button downloadButton = getDownloadButton();

        if(updateIsDownloaded) {
            downloadButton.setEnabled(true);
            downloadButton.setTextColor(ContextCompat.getColor(context, R.color.lightBlue));
            downloadButton.setClickable(true);
            downloadButton.setText(getString(R.string.downloaded));
            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onDownloadedButtonClick();
                }
            });
        } else {
            if (networkConnectionManager != null && networkConnectionManager.checkNetworkConnection() && cyanogenOTAUpdate != null && cyanogenOTAUpdate.getDownloadUrl() != null) {
                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDownloadButtonClick(downloadButton);
                    }
                });
                downloadButton.setEnabled(true);
                downloadButton.setTextColor(ContextCompat.getColor(context, R.color.lightBlue));
            } else {
                downloadButton.setEnabled(false);
                downloadButton.setTextColor(ContextCompat.getColor(context, R.color.dark_grey));
            }
            downloadButton.setText(getString(R.string.download));
        }
    }

    private void onDownloadButtonClick(Button downloadButton) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if(mainActivity != null) {
            if(mainActivity.hasDownloadPermissions()) {
                updateDownloader.downloadUpdate(cyanogenOTAUpdate);
                downloadButton.setText(getString(R.string.downloading));
                downloadButton.setClickable(false);
            } else {
                mainActivity.requestDownloadPermissions();
            }
        }
    }

    private void onDownloadedButtonClick() {
        MessageDialog dialog = new MessageDialog()
                .setTitle(getString(R.string.delete_message_title))
                .setMessage(getString(R.string.delete_message_contents))
                .setClosable(true)
                .setPositiveButtonText(getString(R.string.download_error_close))
                .setNegativeButtonText(getString(R.string.delete_message_delete_button))
                .setDialogListener(new MessageDialog.DialogListener() {
                    @Override
                    public void onDialogPositiveButtonClick(DialogFragment dialogFragment) {

                    }

                    @Override
                    public void onDialogNegativeButtonClick(DialogFragment dialogFragment) {
                        if(cyanogenOTAUpdate != null) {
                            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + cyanogenOTAUpdate.getFileName());
                            if(file.exists()) {
                                if(file.delete()) {
                                    checkIfUpdateIsAlreadyDownloaded(cyanogenOTAUpdate);
                                }
                            }
                        }
                    }
                });
        dialog.setTargetFragment(this, 0);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(dialog, "DeleteDownload");
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void checkIfUpdateIsAlreadyDownloaded(CyanogenOTAUpdate cyanogenOTAUpdate) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + cyanogenOTAUpdate.getFileName());
        onUpdateDownloaded(file.exists());
    }
}