package com.github.saman.rsm.android;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.saman.rsm.android.models.Device;
import com.github.saman.rsm.android.models.Server;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


class Api {
    private String TAG = "RuntimeStateMigration:API";
    private MqttAndroidClient client;
    private Server serverConfig;
    private OnMessageListener onMessageListener;
    private OnOnlineListener onOnlineListener;
    private Context appContext;


    public Api(Context appContext, Server serverConfig, OnMessageListener onMessageListener, OnOnlineListener onOnlineListener) {
        this.appContext = appContext;
        this.serverConfig = serverConfig;
        this.onMessageListener = onMessageListener;
        this.onOnlineListener = onOnlineListener;

    }

    public void run(Device device, OnClientConnectedListener listener) {
        Log.d(TAG, "run: " + device.getId());
        client = new MqttAndroidClient(appContext, serverConfig.getUri(), device.getId());
        client.setTraceEnabled(true);

        client.setCallback(new MqttCallbackExtended() {
            @Override public void connectComplete(boolean reconnect, String serverURI) {
                Log.d(TAG, "connectComplete() called with: reconnect = [" + reconnect + "], serverURI = [" + serverURI + "]");
                listener.onConnected();
            }

            @Override public void connectionLost(Throwable cause) {
                Log.d(TAG, "connectionLost() called with: cause = [" + cause + "]");
            }

            @Override public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, "messageArrived() called with: topic = [" + topic + "], message = [" + message + "]");
                String[] topicParts = topic.split("/");
                if (topicParts.length < 2) {
                    // TODO: Handle this situation
                }
                String mainTopic = topicParts[0];
                String deviceId = topicParts[1];
                Log.d(TAG, "messageArrived: Topic: " + mainTopic);
                if (mainTopic.equals("online")) {
                    onOnlineListener.onOnline(deviceId, message.toString().equals("true"));
                } else {
                    onMessageListener.onMessage(mainTopic, message.toString(), deviceId);
                }
            }

            @Override public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "deliveryComplete() called with: token = [" + token + "]");
            }
        });
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setWill("online/" + device.getId(), new byte[]{}, 2, true);
        try {
            Log.d(TAG, "run: Trying to connect to " + serverConfig.getUri());
            client.connect(options, null, new IMqttActionListener() {
                @Override public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess: Connection");
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    client.setBufferOpts(disconnectedBufferOptions);
                    setOnline(device.getId());
                }

                @Override public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure() called with: asyncActionToken = [" + asyncActionToken + "], exception = [" + exception + "]");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void publishDevice(Device device, Model model) {
        State<DeviceState> data = new State<>(new DeviceState(device, true));
        this.subscribe(model.getName() + "/" + device.getId());
        publish(model.getName(), data);
        subscribe(model.getName());
    }

    public void publishState(String modelName, String deviceId, Device device, String state) {
        State<ResponseState> data = new State<>(new ResponseState(device, state));
        publish(modelName + "/" + deviceId, data);
    }

    public void getStateDevice(String modelName, String deviceId, Device device) {
        State<RequestState> data = new State<>(new RequestState(device));
        publish(modelName + "/" + deviceId, data);
    }

    private void setOnline(String deviceId) {
        try {
            // tell everybody I'm online
            client.publish("online/" + deviceId, "true".getBytes(), 2, true);

            // subscribe to online's topic to get everybody's online status
            subscribe("online/+");

            // unsubscribe itself from online topic
            client.unsubscribe("online/" + deviceId);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishDeviceToNewDevice(Device otherDevice, Device device, Model model) {
        Log.d(TAG, "publishDeviceToNewDevice() called with: otherDevice = [" + otherDevice.getId() + "], device = [" + device + "], model = [" + model + "]");
        State<DeviceState> data = new State<>(new DeviceState(device, false));

        this.publish(model.getName() + "/" + otherDevice.getId(), data);
    }

    private void subscribe(String topic) {
        try {
            client.subscribe(topic, 2);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publish(String topic, State message) {
        ObjectMapper mapper = new ObjectMapper();

        byte[] payload = new byte[0];
        try {
            payload = mapper.writeValueAsBytes(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        MqttMessage msg = new MqttMessage(payload);
        try {
            client.publish(topic, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
