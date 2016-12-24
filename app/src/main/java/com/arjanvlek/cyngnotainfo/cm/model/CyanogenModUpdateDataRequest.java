package com.arjanvlek.cyngnotainfo.cm.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CyanogenModUpdateDataRequest {

    private final CMRequestParams params;

    public CyanogenModUpdateDataRequest(CMRequestParams params) {
        this.params = params;
    }

    public String getMethod() {
        return "get_all_builds";
    }

    public CMRequestParams getParams() {
        return params;
    }

    public static class CMRequestParams {
        private final String deviceCodeName;
        private final List<String> channels;

        public CMRequestParams(String deviceCodeName, String updateChannel) {
            this.deviceCodeName = deviceCodeName;
            this.channels = new ArrayList<>();
            this.channels.add(updateChannel);
        }

        @JsonProperty("device")
        public String getDeviceCodeName() {
            return deviceCodeName;
        }

        public List<String> getChannels() {
            return channels;
        }
    }

}
