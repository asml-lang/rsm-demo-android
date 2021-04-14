package com.github.saman.rsm.android.models;

public class Config {
    private Server server;
    private String name;

    public Config(Server server, String name) {
        this.server = server;
        this.name = name;
    }

    public Server getServer() {
        return server;
    }

    public String getName() {
        return name;
    }
}
