package com.arjanvlek.cyngnotainfo.Model;

public class CyanogenOTAUpdate {

    private String dateUpdated;
    private int dateCreatedUnix;
    private int size;
    private String downloadUrl;
    private String fileName;
    private String description;
    private int rollOutPercentage;
    private String name;
    private String model;


    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRollOutPercentage() {
        return rollOutPercentage;
    }

    public void setRollOutPercentage(int rollOutPercentage) {
        this.rollOutPercentage = rollOutPercentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getDateCreatedUnix() {
        return dateCreatedUnix;
    }

    public void setDateCreatedUnix(int dateCreatedUnix) {
        this.dateCreatedUnix = dateCreatedUnix;
    }
}
