package com.arjanvlek.cyngnotainfo.Model;

public class ServerStatus {

    private Status status;
    private String latestAppVersion;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLatestAppVersion() {
        return latestAppVersion;
    }

    public void setLatestAppVersion(String latestAppVersion) {
        this.latestAppVersion = latestAppVersion;
    }

    public enum Status {
        OK, WARNING, ERROR, TAKEN_DOWN, MAINTENANCE, UNREACHABLE
    }
}
