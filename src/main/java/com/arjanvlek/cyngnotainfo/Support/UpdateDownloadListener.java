package com.arjanvlek.cyngnotainfo.Support;

import com.arjanvlek.cyngnotainfo.Model.DownloadETA;

public interface UpdateDownloadListener {

    void onDownloadManagerInit();

    void onDownloadStarted(long downloadID);
    void onDownloadPending();
    void onDownloadProgressUpdate(DownloadETA downloadETA);
    void onDownloadPaused(int statusCode);
    void onDownloadComplete();
    void onDownloadCancelled();
    void onDownloadError(int statusCode);

    void onVerifyStarted();
    void onVerifyError();
    void onVerifyComplete();

}
