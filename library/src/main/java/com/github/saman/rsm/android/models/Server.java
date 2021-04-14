package com.github.saman.rsm.android.models;

public class Server {
    private String url;
    private int port;

    public Server(String url, int port) {
        this.url = url;
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public int getPort() {
        return port;
    }

    public String getUri() {
        return url + ":" + port;
    }
}
