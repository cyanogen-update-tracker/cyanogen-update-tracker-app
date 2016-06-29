package com.arjanvlek.cyngnotainfo.Support;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;

import com.arjanvlek.cyngnotainfo.Model.CyanogenOTAUpdate;
import com.arjanvlek.cyngnotainfo.Model.DownloadProgressData;
import com.arjanvlek.cyngnotainfo.R;

import java.io.File;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static android.app.DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR;
import static android.app.DownloadManager.COLUMN_REASON;
import static android.app.DownloadManager.COLUMN_STATUS;
import static android.app.DownloadManager.COLUMN_TOTAL_SIZE_BYTES;
import static android.app.DownloadManager.Request.NETWORK_MOBILE;
import static android.app.DownloadManager.Request.NETWORK_WIFI;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
import static android.app.DownloadManager.STATUS_PAUSED;
import static android.app.DownloadManager.STATUS_PENDING;
import static android.app.DownloadManager.STATUS_RUNNING;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.arjanvlek.cyngnotainfo.Support.SettingsManager.PROPERTY_DOWNLOAD_ID;
import static com.arjanvlek.cyngnotainfo.Support.UpdateDownloader.DownloadSpeedUnits.BYTES;
import static com.arjanvlek.cyngnotainfo.Support.UpdateDownloader.DownloadSpeedUnits.KILO_BYTES;
import static com.arjanvlek.cyngnotainfo.Support.UpdateDownloader.DownloadSpeedUnits.MEGA_BYTES;
import static java.math.BigDecimal.ROUND_CEILING;

public class UpdateDownloader {

    private final Activity baseActivity;
    private final DownloadManager downloadManager;
    private boolean initialized;
    private final SettingsManager settingsManager;

    private UpdateDownloadListener listener;

    public final static int NOT_SET = -1;

    private int previousBytesDownloadedSoFar = NOT_SET;
    private long previousTimeStamp;

    private DownloadSpeedUnits previousSpeedUnits = BYTES;
    private double previousDownloadSpeed = NOT_SET;
    private long previousNumberOfSecondsRemaining = NOT_SET;

    public UpdateDownloader(Activity baseActivity) {
        this.baseActivity = baseActivity;
        this.downloadManager = (DownloadManager) baseActivity.getSystemService(Context.DOWNLOAD_SERVICE);
        this.settingsManager = new SettingsManager(baseActivity.getApplicationContext());
    }

    public UpdateDownloader setUpdateDownloadListener(UpdateDownloadListener listener) {
        this.listener = listener;

        if(!initialized) {
            listener.onDownloadManagerInit();
            initialized = true;
        }
        return this;
    }


    public void downloadUpdate(CyanogenOTAUpdate cyanogenOTAUpdate) {
        Uri downloadUri = Uri.parse(cyanogenOTAUpdate.getDownloadUrl());

        DownloadManager.Request request = new DownloadManager.Request(downloadUri)
                .setDescription(cyanogenOTAUpdate.getDownloadUrl())
                .setTitle(baseActivity.getString(R.string.download_title, cyanogenOTAUpdate.getName()))
                .setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, cyanogenOTAUpdate.getFileName())
                .setVisibleInDownloadsUi(false)
                .setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedNetworkTypes(NETWORK_WIFI | NETWORK_MOBILE);

        long downloadID = downloadManager.enqueue(request);

        previousBytesDownloadedSoFar = NOT_SET;
        settingsManager.saveLongPreference(PROPERTY_DOWNLOAD_ID, downloadID);

        checkDownloadProgress(cyanogenOTAUpdate);

        listener.onDownloadStarted(downloadID);
    }

    public void cancelDownload() {
        if(settingsManager.containsPreference(PROPERTY_DOWNLOAD_ID)) {
            downloadManager.remove(settingsManager.getLongPreference(PROPERTY_DOWNLOAD_ID));
            clearUp();

            listener.onDownloadCancelled();
        }
    }

    public void checkDownloadProgress(CyanogenOTAUpdate cyanogenOTAUpdate) {

        final long downloadId = settingsManager.getLongPreference(PROPERTY_DOWNLOAD_ID);

        if(settingsManager.containsPreference(PROPERTY_DOWNLOAD_ID)) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);

            Cursor cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
                switch (status) {
                    case STATUS_PENDING:
                        listener.onDownloadPending();

                        recheckDownloadProgress(cyanogenOTAUpdate, 1);
                        break;
                    case STATUS_PAUSED:
                        listener.onDownloadPaused(cursor.getInt(cursor.getColumnIndex(COLUMN_REASON)));

                        recheckDownloadProgress(cyanogenOTAUpdate, 5);
                        break;
                    case STATUS_RUNNING:

                        int bytesDownloadedSoFar = cursor.getInt(cursor.getColumnIndex(COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int totalSizeBytes = cursor.getInt(cursor.getColumnIndex(COLUMN_TOTAL_SIZE_BYTES));

                        DownloadProgressData eta = calculateDownloadETA(bytesDownloadedSoFar, totalSizeBytes);

                        listener.onDownloadProgressUpdate(eta);

                        previousBytesDownloadedSoFar = cursor.getInt(cursor.getColumnIndex(COLUMN_BYTES_DOWNLOADED_SO_FAR));

                        recheckDownloadProgress(cyanogenOTAUpdate, 1);

                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        clearUp();

                        listener.onDownloadComplete();

                        verifyDownload(cyanogenOTAUpdate);
                        break;
                    case DownloadManager.STATUS_FAILED:
                        clearUp();

                        listener.onDownloadError(cursor.getInt(cursor.getColumnIndex(COLUMN_REASON)));
                        break;
                }
            }
        }
    }

    public void verifyDownload(CyanogenOTAUpdate cyanogenOTAUpdate) {
        new DownloadVerifier().execute(cyanogenOTAUpdate);
    }

    public boolean makeDownloadDirectory() {
        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return downloadDirectory.mkdirs();
    }

    private void recheckDownloadProgress(final CyanogenOTAUpdate cyanogenOTAUpdate, int secondsDelay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkDownloadProgress(cyanogenOTAUpdate);
            }
        }, (secondsDelay * 1000));
    }

    private void clearUp() {
        previousTimeStamp = NOT_SET;
        previousBytesDownloadedSoFar = NOT_SET;
        previousSpeedUnits = BYTES;
        previousDownloadSpeed = NOT_SET;
        previousNumberOfSecondsRemaining = NOT_SET;
        settingsManager.deletePreference(PROPERTY_DOWNLOAD_ID);
    }

    private DownloadProgressData calculateDownloadETA(long bytesDownloadedSoFar, long totalSizeBytes) {
        double bytesDownloadedInSecond = NOT_SET;
        double downloadSpeed = NOT_SET;
        long numberOfSecondsRemaining = NOT_SET;

        DownloadSpeedUnits speedUnits = BYTES;
        if(previousBytesDownloadedSoFar != NOT_SET) {
            long currentTimeStamp = System.currentTimeMillis();

            double numberOfElapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(currentTimeStamp - previousTimeStamp);

            if((bytesDownloadedSoFar - previousBytesDownloadedSoFar) > 0) {
                bytesDownloadedInSecond =  (bytesDownloadedSoFar - previousBytesDownloadedSoFar) / (numberOfElapsedSeconds) ;
                previousTimeStamp = currentTimeStamp;
            } else {
                bytesDownloadedInSecond = 0;
            }

            long bytesRemainingToDownload = totalSizeBytes - bytesDownloadedSoFar;

            // DownloadManager.query doesn't always have new data. If no new data is available, return the previously stored data to keep the UI showing that.
            if(bytesDownloadedInSecond >= 1) {
                numberOfSecondsRemaining = bytesRemainingToDownload / (long)bytesDownloadedInSecond;
                previousNumberOfSecondsRemaining = numberOfSecondsRemaining;
            } else {
                numberOfSecondsRemaining = previousNumberOfSecondsRemaining;
            }
        }

        if(bytesDownloadedInSecond != NOT_SET) {
            if(bytesDownloadedInSecond == 0) {
                downloadSpeed = previousDownloadSpeed;
                speedUnits = previousSpeedUnits;
            }else {
                if (bytesDownloadedInSecond >= 0 && bytesDownloadedInSecond < 1000) {
                    downloadSpeed = bytesDownloadedInSecond;
                    speedUnits = BYTES;

                } else if (bytesDownloadedInSecond >= 1000 && bytesDownloadedInSecond < 1000000) {
                    downloadSpeed = new BigDecimal(bytesDownloadedInSecond).setScale(0, ROUND_CEILING).divide(new BigDecimal(1000), ROUND_CEILING).doubleValue();
                    speedUnits = KILO_BYTES;
                } else if (bytesDownloadedInSecond >= 1000000) {
                    downloadSpeed = new BigDecimal(bytesDownloadedInSecond).setScale(2, ROUND_CEILING).divide(new BigDecimal(1000000), ROUND_CEILING).doubleValue();
                    speedUnits = MEGA_BYTES;
                }

                previousDownloadSpeed = downloadSpeed;
                previousSpeedUnits = speedUnits;
            }
        }

        int progress = (int)((bytesDownloadedSoFar * 100)  / totalSizeBytes);

        return new DownloadProgressData(downloadSpeed, speedUnits, numberOfSecondsRemaining, progress);
    }

    private class DownloadVerifier extends AsyncTask<CyanogenOTAUpdate, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            listener.onVerifyStarted();
        }

        @Override
        protected Boolean doInBackground(CyanogenOTAUpdate... params) {
            String filename = params[0].getFileName();
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + filename);
            return params[0] == null || params[0].getMD5Sum() == null || MD5.checkMD5(params[0].getMD5Sum(), file);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                listener.onVerifyComplete();
            } else {
                listener.onVerifyError();
            }
        }
    }

    public enum DownloadSpeedUnits {
        BYTES("B/s"), KILO_BYTES("KB/s"), MEGA_BYTES("MB/s");

        String stringValue;

        DownloadSpeedUnits(String stringValue) {
            this.stringValue = stringValue;
        }

        public String getStringValue() {
            return stringValue;
        }
    }
}
