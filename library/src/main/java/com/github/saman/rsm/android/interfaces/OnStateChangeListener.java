package com.github.saman.rsm.android.interfaces;

import com.github.saman.rsm.android.models.Device;

public interface OnStateChangeListener {
    void onState(String modelName, Device device, String state, boolean isValid);
}
