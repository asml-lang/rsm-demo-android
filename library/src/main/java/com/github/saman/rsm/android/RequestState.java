package com.github.saman.rsm.android;

import com.github.saman.rsm.android.models.Device;

class RequestState implements Publishable {
    private Device device;

    public RequestState() {
    }

    public RequestState(Device device) {
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    @Override public String getAction() {
        return "request-state";
    }
}
