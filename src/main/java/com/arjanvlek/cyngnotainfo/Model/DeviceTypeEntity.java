package com.arjanvlek.cyngnotainfo.Model;

/**
 * Created by Arjan on 31-5-2015. Part of Cyanogen Update Tracker.
 */
@SuppressWarnings("DefaultFileTemplate")
public class DeviceTypeEntity {
    private long id;
    private String deviceType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
