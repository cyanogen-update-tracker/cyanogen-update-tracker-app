package com.arjanvlek.cyngnotainfo.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CyanogenOTAUpdate {

    private int size;
    private String downloadUrl;
    private String fileName;
    private String description;
    private String name;
    private String MD5Sum;
    private boolean updateInformationAvailable;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    @JsonProperty("download_url")
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    @JsonProperty("filename")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUpdateInformationAvailable() {
        return updateInformationAvailable || (downloadUrl != null);
    }

    public void setUpdateInformationAvailable(boolean updateInformationAvailable) {
        this.updateInformationAvailable = updateInformationAvailable;
    }

    public String getMD5Sum() {
        return MD5Sum;
    }

    @JsonProperty("md5sum")
    public void setMD5Sum(String MD5Sum) {
        this.MD5Sum = MD5Sum;
    }
}
