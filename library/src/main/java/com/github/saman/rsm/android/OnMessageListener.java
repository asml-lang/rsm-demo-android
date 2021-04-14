package com.github.saman.rsm.android;

interface OnMessageListener {
    void onMessage(String topic, String payload, String deviceId);
}
