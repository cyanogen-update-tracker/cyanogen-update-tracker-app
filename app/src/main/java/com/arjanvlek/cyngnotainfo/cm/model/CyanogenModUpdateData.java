package com.arjanvlek.cyngnotainfo.cm.model;


import android.os.Build;

import com.arjanvlek.cyngnotainfo.common.internal.SettingsManager;
import com.arjanvlek.cyngnotainfo.common.model.UpdateData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CyanogenModUpdateData extends UpdateData {

    private String id;
    private List<CyanogenModUpdateDataResult> result;
    private String error;

    @Override
    public String getVersionNumber() {
        String filename = result.get(0).getFilename();
        if (filename == null || filename.isEmpty() || !filename.contains(".zip")) return null;
        return filename.substring(0, filename.lastIndexOf(".zip"));
    }

    @Override
    public String getDownloadUrl() {
        return result.get(0).getDownloadUrl();
    }

    @Override
    public String getFilename() {
        return result.get(0).getFilename();
    }

    @Override
    public String getMd5sum() {
        return result.get(0).getMd5sum();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setResult(List<CyanogenModUpdateDataResult> result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isUpdateInformationAvailable() {
        return result != null && !result.isEmpty();
    }

    public String getChangelogUrl() {
        return result.get(0).getChangelogUrl();
    }

    public boolean isSystemUpToDate(SettingsManager _) {
        return this.result == null || this.result.isEmpty() || Build.VERSION.INCREMENTAL.equals(result.get(0).getIncrementalVersion());
    }

    public boolean isMajorUpdate() {
        return this.result != null && !this.result.isEmpty() && this.result.get(0).apiLevel != null && this.result.get(0).apiLevel != Build.VERSION.SDK_INT;
    }

    @JsonIgnoreProperties(ignoreUnknown =  true)
    private static class CyanogenModUpdateDataResult extends UpdateData {
        private String incrementalVersion;
        private String channel;
        private String changelogUrl;
        private Integer apiLevel;

        public CyanogenModUpdateDataResult() {

        }

        public String getIncrementalVersion() {
            return incrementalVersion;
        }

        @Override
        @JsonProperty("url")
        public void setDownloadUrl(String downloadUrl) {
            super.setDownloadUrl(downloadUrl);
        }

        @JsonProperty("incremental")
        public void setIncrementalVersion(String incrementalVersion) {
            this.incrementalVersion = incrementalVersion;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getChangelogUrl() {
            return changelogUrl;
        }

        @JsonProperty("changes")
        public void setChangelogUrl(String changelogUrl) {
            this.changelogUrl = changelogUrl;
        }

        public Integer getApiLevel() {
            return apiLevel;
        }

        @JsonProperty("api_level")
        public void setApiLevel(Integer apiLevel) {
            this.apiLevel = apiLevel;
        }

        @Override
        public boolean isSystemUpToDate(SettingsManager settingsManager) {
            throw new UnsupportedOperationException("Function is not implemented in this class!");
        }
    }

}