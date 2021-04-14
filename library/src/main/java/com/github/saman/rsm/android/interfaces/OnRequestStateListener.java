package com.github.saman.rsm.android.interfaces;

import com.github.saman.rsm.android.models.Device;

public interface OnRequestStateListener {
    void onRequestState(String modelName, Device device);
}
