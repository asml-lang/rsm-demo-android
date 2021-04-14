package com.github.saman.rsm.android;

import com.fasterxml.jackson.annotation.JsonProperty;

class Model {
    @JsonProperty("_id")
    private String id;
    private String name;
    private String content;
    private String deviceId;
    private String state;

    public Model() {
    }

    public Model(String name, String content, String deviceId) {
        this.name = name;
        this.content = content;
        this.deviceId = deviceId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
