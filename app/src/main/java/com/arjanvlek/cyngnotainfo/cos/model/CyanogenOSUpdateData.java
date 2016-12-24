package com.arjanvlek.cyngnotainfo.cos.model;

import com.arjanvlek.cyngnotainfo.common.internal.SettingsManager;
import com.arjanvlek.cyngnotainfo.common.model.UpdateData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CyanogenOSUpdateData extends UpdateData {

    private int size;
    private String information;
    private boolean updateInformationAvailable;
    private boolean systemIsUpToDate;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public boolean isUpdateInformationAvailable() {
        return updateInformationAvailable || (getVersionNumber() != null);
    }

    @JsonProperty("update_information_available")
    public void setUpdateInformationAvailable(boolean updateInformationAvailable) {
        this.updateInformationAvailable = updateInformationAvailable;
    }

    public boolean isSystemUpToDate(SettingsManager settingsManager) {
        //noinspection SimplifiableIfStatement
        if(settingsManager != null && settingsManager.showIfSystemIsUpToDate()) {
            return systemIsUpToDate;
        } else {
            return false;
        }
    }

    public boolean isSystemIsUpToDateCheck() {
        return systemIsUpToDate;
    }

    @JsonProperty("system_is_up_to_date")
    public void setSystemIsUpToDate(boolean systemIsUpToDate) {
        this.systemIsUpToDate = systemIsUpToDate;
    }
}
