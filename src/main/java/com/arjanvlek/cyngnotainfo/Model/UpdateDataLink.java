package com.arjanvlek.cyngnotainfo.Model;

public class UpdateDataLink {
    private long id;
    private long updateMethodId;
    private long deviceId;
    private String updateDataUrl;

    public UpdateDataLink() {}

    public UpdateDataLink(long id, long deviceId, long updateMethodId, String updateDataUrl) {
        this.id = id;
        this.deviceId = deviceId;
        this.updateMethodId = updateMethodId;
        this.updateDataUrl = updateDataUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUpdateMethodId() {
        return updateMethodId;
    }

    public void setUpdateMethodId(long updateMethodId) {
        this.updateMethodId = updateMethodId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public String getUpdateDataUrl() {
        return updateDataUrl;
    }

    public void setUpdateDataUrl(String updateDataUrl) {
        this.updateDataUrl = updateDataUrl;
    }
}
