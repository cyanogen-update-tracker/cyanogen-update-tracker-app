package com.arjanvlek.cyngnotainfo.Model;

public class UpdateLink {
    private long id;
    private long tracking_update_type_id;
    private long tracking_device_type_id;
    private String information_url;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTracking_update_type_id() {
        return tracking_update_type_id;
    }

    public void setTracking_update_type_id(long tracking_update_type_id) {
        this.tracking_update_type_id = tracking_update_type_id;
    }

    public long getTracking_device_type_id() {
        return tracking_device_type_id;
    }

    public void setTracking_device_type_id(long tracking_device_type_id) {
        this.tracking_device_type_id = tracking_device_type_id;
    }

    public String getInformation_url() {
        return information_url;
    }

    public void setInformation_url(String information_url) {
        this.information_url = information_url;
    }
}
