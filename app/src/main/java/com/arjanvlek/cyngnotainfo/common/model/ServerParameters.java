package com.arjanvlek.cyngnotainfo.common.model;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.arjanvlek.cyngnotainfo.common.internal.ApplicationData.DEFAULT_CM_DOWNLOAD_URL;
import static com.arjanvlek.cyngnotainfo.common.internal.ApplicationData.DEFAULT_CM_INSTALL_GUIDE_URL;

public class ServerParameters {

    private Status status;
    private String latestAppVersion;
    private String cmDownloadUrl;
    private String cmInstallGuideUrl;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status != null ? status : Status.UNREACHABLE;
    }

    public String getLatestAppVersion() {
        return latestAppVersion;
    }

    @JsonProperty("latest_app_version")
    public void setLatestAppVersion(String latestAppVersion) {
        this.latestAppVersion = latestAppVersion != null ? latestAppVersion : BuildConfig.VERSION_NAME; // To prevent incorrect app update messages if response is null / invalid
    }

    public String getCmDownloadUrl() {
        return cmDownloadUrl != null && !cmDownloadUrl.isEmpty() ? cmDownloadUrl : DEFAULT_CM_DOWNLOAD_URL;
    }

    @JsonProperty("cm_download_url")
    public void setCmDownloadUrl(String cmDownloadUrl) {
        this.cmDownloadUrl = cmDownloadUrl;
    }

    public String getCmInstallGuideUrl() {
        return cmInstallGuideUrl != null && !cmInstallGuideUrl.isEmpty() ? cmInstallGuideUrl : DEFAULT_CM_INSTALL_GUIDE_URL;
    }

    @JsonProperty("cm_install_guide_url")
    public void setCmInstallGuideUrl(String cmInstallGuideUrl) {
        this.cmInstallGuideUrl = cmInstallGuideUrl;
    }

    public enum Status {
        OK, WARNING, ERROR, TAKEN_DOWN, MAINTENANCE, UNREACHABLE
    }
}
