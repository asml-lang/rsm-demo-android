package com.github.saman.rsm.android;

class State<T extends Publishable> {
    private String action;
    private T data;

    public State() {
    }

    public State(T data) {
        this.data = data;
        this.action = data.getAction();
    }

    public String getAction() {
        return action;
    }

    public T getData() {
        return data;
    }
}
