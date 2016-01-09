package com.arjanvlek.cyngnotainfo.Model;

public class ServerMessage {
    private long id;
    private String message;
    private String messageNl;
    private Long deviceId;
    private Long updateMethodId;
    private ServerMessagePriority priority;
    private boolean marquee;

    public enum ServerMessagePriority {
        LOW, MEDIUM, HIGH
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageNl() {
        return messageNl;
    }

    public void setMessageNl(String messageNl) {
        this.messageNl = messageNl;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getUpdateMethodId() {
        return updateMethodId;
    }

    public void setUpdateMethodId(Long updateMethodId) {
        this.updateMethodId = updateMethodId;
    }

    public ServerMessagePriority getPriority() {
        return priority;
    }

    public void setPriority(ServerMessagePriority priority) {
        this.priority = priority;
    }

    public boolean isMarquee() {
        return marquee;
    }

    public void setMarquee(boolean marquee) {
        this.marquee = marquee;
    }

    public boolean isDeviceSpecific() {
        return deviceId != null;
    }
}


