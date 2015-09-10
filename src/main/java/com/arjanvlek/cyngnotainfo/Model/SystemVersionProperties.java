package com.arjanvlek.cyngnotainfo.Model;

public class SystemVersionProperties {

    private String cyanogenOSVersion;
    private int dateCreatedUtc;

    public String getCyanogenOSVersion() {
        return cyanogenOSVersion;
    }

    public void setCyanogenOSVersion(String cyanogenOSVersion) {
        this.cyanogenOSVersion = cyanogenOSVersion;
    }

    public int getDateCreatedUtc() {
        return dateCreatedUtc;
    }

    public void setDateCreatedUtc(int dateCreatedUtc) {
        this.dateCreatedUtc = dateCreatedUtc;
    }
}
