package com.arjanvlek.cyngnotainfo.common.model;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerParameters {

    private Status status;
    private String latestAppVersion;
    private String cyanogenModDownloadUrl;
    private String cyanogenModInstallGuideUrl;

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

    public String getCyanogenModDownloadUrl() {
        return cyanogenModDownloadUrl != null ? cyanogenModDownloadUrl : "https://get.cm"; // todo implement
    }

    @JsonProperty("cyanogen_mod_download_url")
    public void setCyanogenModDownloadUrl(String cyanogenModDownloadUrl) {
        this.cyanogenModDownloadUrl = cyanogenModDownloadUrl;
    }

    public String getCyanogenModInstallGuideUrl() {
        return cyanogenModInstallGuideUrl != null ? cyanogenModInstallGuideUrl : "https://cyanogenmod.org";
    }

    @JsonProperty("cyanogen_mod_install_guide_url")
    public void setCyanogenModInstallGuideUrl(String cyanogenModInstallGuideUrl) {
        this.cyanogenModInstallGuideUrl = cyanogenModInstallGuideUrl;
    }

    public enum Status {
        OK, WARNING, ERROR, TAKEN_DOWN, MAINTENANCE, UNREACHABLE
    }
}
