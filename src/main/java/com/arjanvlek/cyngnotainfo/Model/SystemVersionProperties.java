package com.arjanvlek.cyngnotainfo.Model;

public class SystemVersionProperties {

    private String cyanogenOSVersion;
    private String securityPatchDate;
    private int dateCreatedUtc;

    public String getCyanogenOSVersion() {
        return cyanogenOSVersion;
    }

    public void setCyanogenOSVersion(String cyanogenOSVersion) {
        this.cyanogenOSVersion = cyanogenOSVersion;
    }

    public String getSecurityPatchDate() {
        return securityPatchDate;
    }

    public void setSecurityPatchDate(String securityPatchDate) {
        this.securityPatchDate = securityPatchDate;
    }

    public int getDateCreatedUtc() {
        return dateCreatedUtc;
    }

    public void setDateCreatedUtc(int dateCreatedUtc) {
        this.dateCreatedUtc = dateCreatedUtc;
    }
}
