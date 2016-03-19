package com.arjanvlek.cyngnotainfo.Model;

import android.os.Build;

import java.util.List;

public class SystemVersionProperties {

    private String cyanogenDeviceCodeName;
    private String cyanogenOSVersion;
    private String securityPatchDate;
    private String modelNumber;

    public String getCyanogenDeviceCodeName() {
        return cyanogenDeviceCodeName;
    }

    public void setCyanogenDeviceCodeName(String cyanogenDeviceCodeName) {
        this.cyanogenDeviceCodeName = cyanogenDeviceCodeName;
    }

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

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public boolean isSupportedDevice(List<Device> devices) {
        boolean supported = false;

        for(Device device : devices) {
            if(device.getModelNumber() != null && device.getModelNumber().equals(getCyanogenDeviceCodeName()) && Build.TAGS.contains("release-keys")) {
                supported = true;
                break;
            }
        }
        return supported;
    }
}
