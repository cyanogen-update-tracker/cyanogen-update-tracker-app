package com.arjanvlek.cyngnotainfo.common.model;


import com.arjanvlek.cyngnotainfo.common.internal.SettingsManager;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class UpdateData {

    private String versionNumber;
    private String description;
    private String downloadUrl;
    private String filename;
    private String md5sum;

    public String getVersionNumber() {
        return versionNumber;
    }

    @JsonProperty("name")
    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    @JsonProperty("download_url")
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMd5sum() {
        return md5sum;
    }

    @JsonProperty("md5sum")
    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public abstract boolean isSystemUpToDate(SettingsManager settingsManager);
}
