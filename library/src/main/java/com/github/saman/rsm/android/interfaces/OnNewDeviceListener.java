package com.github.saman.rsm.android.interfaces;

import com.github.saman.rsm.android.models.Device;

public interface OnNewDeviceListener {
    void onNewDevice(String modelName, Device device);
}
