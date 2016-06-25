package com.arjanvlek.cyngnotainfo.Support;

public interface UpdateDownloadListener {

    void onDownloadManagerInit();

    void onDownloadStarted(long downloadID);
    void onDownloadPending();
    void onDownloadProgressUpdate(int progress, long averageNetworkSpeed, UpdateDownloader.DownloadSpeedUnits networkSpeedUnits, long secondsRemaining);
    void onDownloadPaused(int statusCode);
    void onDownloadComplete();
    void onDownloadCancelled();
    void onDownloadError(int statusCode);

    void onVerifyStarted();
    void onVerifyError();
    void onVerifyComplete();

}
