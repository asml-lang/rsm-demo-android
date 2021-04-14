package com.github.saman.rsm.android;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.saman.rsm.android.interfaces.OnNewDeviceListener;
import com.github.saman.rsm.android.interfaces.OnRequestStateListener;
import com.github.saman.rsm.android.interfaces.OnStateChangeListener;
import com.github.saman.rsm.android.models.Config;
import com.github.saman.rsm.android.models.Device;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class RuntimeStateMigration implements OnOnlineListener, OnMessageListener {
    private static Context appContext;
    private static Config config;
    private static Api api;
    private static Device device;
    private static RuntimeStateMigration INSTANCE;
    private static String TAG = "RuntimeStateMigration";
    ObjectMapper mapper = new ObjectMapper();
    private JsonSchemaValidator validator;
    private List<Model> models = new ArrayList<>();
    private List<Device> devices = new ArrayList<>();
    private OnStateChangeListener onStateChangeListener;
    private OnRequestStateListener onRequestStateListener;
    private OnNewDeviceListener onNewDeviceListener;

    private RuntimeStateMigration() {
        api = new Api(appContext, config.getServer(), this, this);
        validator = new JsonSchemaValidator(appContext);
    }

    public static RuntimeStateMigration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RuntimeStateMigration();
        }
        return INSTANCE;
    }

    public static void init(Context context, Config configuration) {
        config = configuration;
        appContext = context;

        device = new Device(config.getName());
    }

    public boolean addModel(String content) throws JsonProcessingException, IllegalStateException {
        if (!validator.isModelValid(content)) {
            throw new IllegalStateException("This model is not valid");
        }

        StateModel model = mapper.readValue(content, StateModel.class);
        if (getModel(content) != null) {
            throw new IllegalStateException("model is already added");
        }
        Model m = new Model(model.info.title, content, device.getId());
        Log.d(TAG, "addModel: " + m.getName());
        return models.add(m);
    }

    public void introduce() {
        if (models.isEmpty()) {
            throw new IllegalStateException("At least one model needs to be added.");
        }
        api.run(device, new OnClientConnectedListener() {
            @Override public void onConnected() {
                for (Model m : models) {
                    Log.d(TAG, "onConnected: Publishing model: " + m.getName());
                    api.publishDevice(device, m);
                }
            }
        });

    }

    public void getStateDevice(String modelName, String deviceId) {
        api.getStateDevice(modelName, deviceId, device);
    }

    public void setState(String modelName, String state) {
        Model model = getModel(modelName);
        if (model == null) {
            throw new IllegalStateException("Could not find the model " + modelName);
        }
        model.setState(state);
    }

    public void sendState(String modelName, String deviceId) {
        Model model = getModel(modelName);
        if (model == null) {
            throw new IllegalStateException("Could not find the model " + modelName);
        }
        api.publishState(modelName, deviceId, device, model.getState());
    }

    public Device getDevice() {
        return device;
    }

    public List<Model> getModels() {
        return models;
    }

    public List<Device> getDevices(String modelName) {
        Model model = getModel(modelName);
        if (model == null) {
            throw new IllegalStateException("Could not find the model " + modelName);
        }
        List<Device> deviceList = new ArrayList<>();
        for (Device device : devices) {
            List<String> deviceModels = device.getModels();
            for (String m : deviceModels) {
                if (m.equals(modelName)) {
                    deviceList.add(device);
                    break;
                }
            }
        }
        return deviceList;
    }

    private Model getModel(String name) {
        for (Model model : models) {
            if (model.getName().equals(name)) {
                return model;
            }
        }
        return null;
    }

    @Override public void onMessage(String topic, String payload, String deviceId) {
        Log.d(TAG, "onMessage() called with: topic = [" + topic + "], payload = [" + payload + "], deviceId = [" + deviceId + "]");
        Model model = getModel(topic);
        if (model == null) {
            Log.d(TAG, "onMessage: Model is null for: " + topic);
            // I think we shouldn't throw an exception in a callback
            //throw new IllegalStateException("onMessage: Could not find the model " + topic);

            return;
        }
        try {
            JSONObject json = new JSONObject(payload);
            String action = json.getString("action");
            Log.d(TAG, "onMessage: action => " + action);
            if (action.equals("device")) {
                JavaType type = mapper.getTypeFactory().constructParametricType(State.class, DeviceState.class);
                State<DeviceState> state = mapper.readValue(payload, type);
                Device device = state.getData().getDevice();
                device.addModel(topic);
                devices.add(device);
                if (state.getData().isNew()) {
                    if (onNewDeviceListener != null) {
                        onNewDeviceListener.onNewDevice(topic, device);
                    }
                    api.publishDeviceToNewDevice(device, RuntimeStateMigration.device, model);
                }
            } else if (action.equals("request-state")) {
                State<RequestState> state = mapper.readValue(payload, new TypeReference<State<RequestState>>() {});
                if (onRequestStateListener != null) {
                    onRequestStateListener.onRequestState(topic, state.getData().getDevice());
                }


            } else if (action.equals("response-state")) {
                State<ResponseState> state = mapper.readValue(payload, new TypeReference<State<ResponseState>>() {});
                if (onStateChangeListener != null) {
                    onStateChangeListener.onState(
                            topic,
                            state.getData().getDevice(),
                            state.getData().getState(),
                            validator.isStateValid(model.getContent(), state.getData().getState())
                    );
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override public void onOnline(String deviceId, boolean isOnline) {
        if (deviceId.equals(RuntimeStateMigration.device.getId())) {
            return;
        }
        if (!isOnline) {
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getId().equals(deviceId)) {
                    devices.remove(i);
                    break;
                }
            }
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.onStateChangeListener = listener;
    }

    public void setOnRequestStateListener(OnRequestStateListener listener) {
        this.onRequestStateListener = listener;
    }

    public void setOnNewDeviceListener(OnNewDeviceListener listener) {
        this.onNewDeviceListener = listener;
    }
}
