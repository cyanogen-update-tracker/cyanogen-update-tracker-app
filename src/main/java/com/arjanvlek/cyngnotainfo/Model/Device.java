package com.arjanvlek.cyngnotainfo.Model;

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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
