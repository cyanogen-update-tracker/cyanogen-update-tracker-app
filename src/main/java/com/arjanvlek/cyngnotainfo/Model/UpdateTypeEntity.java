package com.arjanvlek.cyngnotainfo.Model;

/**
 * Created by Arjan on 31-5-2015. Part of Cyanogen Update Tracker.
 */
@SuppressWarnings("DefaultFileTemplate")
public class UpdateTypeEntity {
    private long id;
    private String updateType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }
}
