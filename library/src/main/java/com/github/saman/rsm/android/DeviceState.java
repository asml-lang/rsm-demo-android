package com.github.saman.rsm.android;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.saman.rsm.android.models.Device;

class DeviceState implements Publishable {
    private Device device;
    @JsonProperty("new")
    private boolean isNew;

    public DeviceState() {
    }

    public DeviceState(Device device, boolean isNew) {
        this.device = device;
        this.isNew = isNew;
    }

    public Device getDevice() {
        return device;
    }

    public boolean isNew() {
        return isNew;
    }

    @Override public String getAction() {
        return "device";
    }
}
