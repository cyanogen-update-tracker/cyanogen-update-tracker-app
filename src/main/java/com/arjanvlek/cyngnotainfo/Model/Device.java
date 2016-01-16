package com.arjanvlek.cyngnotainfo.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Device {

    public Device() {}

    public Device(long id, String deviceName) {
        this.id = id;
        this.deviceName = deviceName;
    }
    private long id;
    private String deviceName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @JsonProperty("device_name")
    public String getDeviceName() {
        return deviceName;
    }

    @JsonProperty("device_name")
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
