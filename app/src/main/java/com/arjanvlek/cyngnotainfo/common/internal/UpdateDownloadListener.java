package com.arjanvlek.cyngnotainfo.common.internal;

import com.arjanvlek.cyngnotainfo.common.model.DownloadProgressData;

public interface UpdateDownloadListener {

    void onDownloadManagerInit();

    void onDownloadStarted(long downloadID);
    void onDownloadPending();
    void onDownloadProgressUpdate(DownloadProgressData downloadProgressData);
    void onDownloadPaused(int statusCode);
    void onDownloadComplete();
    void onDownloadCancelled();
    void onDownloadError(int statusCode);

    void onVerifyStarted();
    void onVerifyError();
    void onVerifyComplete();

}
