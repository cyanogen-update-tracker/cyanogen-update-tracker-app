package com.arjanvlek.cyngnotainfo.Model;

public class UpdateMethod {

    public UpdateMethod() {}

    public UpdateMethod(long id, String updateMethod, String updateMethodNl) {
        this.id = id;
        this.updateMethod = updateMethod;
        this.updateMethodNl = updateMethodNl;
    }

    private long id;
    private String updateMethod;
    private String updateMethodNl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUpdateMethod() {
        return updateMethod;
    }

    public void setUpdateMethod(String updateMethod) {
        this.updateMethod = updateMethod;
    }

    public String getUpdateMethodNl() {
        return updateMethodNl;
    }

    public void setUpdateMethodNl(String updateMethodNl) {
        this.updateMethodNl = updateMethodNl;
    }
}
