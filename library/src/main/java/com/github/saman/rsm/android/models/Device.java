package com.github.saman.rsm.android.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Device {
    @JsonProperty("_id")
    private String _id;
    private String name;
    private List<String> models = new ArrayList<>();

    public Device() {
    }

    public Device(String name) {
        this.name = name;
        this._id = UUID.randomUUID().toString();
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public List<String> getModels() {
        return models;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }

    public void addModel(String model) {
        models.add(model);
    }
}
