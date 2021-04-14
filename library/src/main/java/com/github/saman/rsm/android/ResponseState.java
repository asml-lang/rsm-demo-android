package com.github.saman.rsm.android;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.saman.rsm.android.models.Device;

class ResponseState implements Publishable {
    private Device device;

    private Object state;

    public ResponseState() {
    }

    public ResponseState(Device device, String state) {
        this.device = device;
        this.state = state;
    }

    public Device getDevice() {
        return device;
    }

    @JsonRawValue
    public String getState() {
        return state == null ? null : state.toString();
    }

    public void setState(JsonNode node) {
        this.state = node;
    }

    @Override public String getAction() {
        return "response-state";
    }
}
