package com.arjanvlek.cyngnotainfo.cm.model;


import android.os.Build;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CyanogenModUpdateData {

    private String id;
    private List<CyanogenModUpdateDataResult> result;
    private String error;

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

    public CyanogenModUpdateDataResult getCyanogenModUpdateData() {
        return result == null || result.isEmpty() ? null : result.get(0);
    }

    public boolean isSystemUpToDate() {
        return this.result == null || this.result.isEmpty() || Build.VERSION.INCREMENTAL.equals(result.get(0).getIncrementalVersion());
    }

    public boolean isMajorUpdate() {
        return this.result != null && !this.result.isEmpty() && this.result.get(0).apiLevel != null && this.result.get(0).apiLevel != Build.VERSION.SDK_INT;
    }

    @JsonIgnoreProperties(ignoreUnknown =  true)
    public class CyanogenModUpdateDataResult {
        private String downloadUrl;
        private String md5sum;
        private String filename;
        private String incrementalVersion;
        private String channel;
        private String changelogUrl;
        private Integer apiLevel;

        public String getDownloadUrl() {
            return downloadUrl;
        }

        @JsonProperty("download_url")
        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public String getMd5sum() {
            return md5sum;
        }

        @JsonProperty("md5sum")
        public void setMd5sum(String md5sum) {
            this.md5sum = md5sum;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getIncrementalVersion() {
            return incrementalVersion;
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
    }

}