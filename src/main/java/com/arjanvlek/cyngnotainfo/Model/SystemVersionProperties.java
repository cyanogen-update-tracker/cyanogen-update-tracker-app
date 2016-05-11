package com.arjanvlek.cyngnotainfo.Model;

import android.os.Build;

import com.arjanvlek.cyngnotainfo.ApplicationContext;

import java.util.List;

import static com.arjanvlek.cyngnotainfo.ApplicationContext.NO_CYANOGEN_OS;

public class SystemVersionProperties {

    private String incrementalSystemVersion;
    private String cyanogenDeviceCodeName;
    private String cyanogenOSVersion;
    private String securityPatchDate;
    private String modelNumber;

    public String getIncrementalSystemVersion() {
        return incrementalSystemVersion;
    }

    public void setIncrementalSystemVersion(String incrementalSystemVersion) {
        this.incrementalSystemVersion = incrementalSystemVersion;
    }

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

        if(devices == null || devices.isEmpty()) {
            return Build.TAGS.contains("release-keys") && getCyanogenOSVersion() != null && !getCyanogenOSVersion().equals(NO_CYANOGEN_OS);
            // To bypass false positives on empty server response. This still checks if official ROM is used (no cyanogenMod) and if a cyanogen os version is found on the device.
        }

        for(Device device : devices) {
            if(device.getModelNumber() != null && device.getModelNumber().equals(getCyanogenDeviceCodeName()) && Build.TAGS.contains("release-keys")) {
                supported = true;
                break;
            }
        }
        return supported;
    }
}
