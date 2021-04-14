package com.github.saman.rsm.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SendingEmailState {
    private String from;
    private String to;
    private String body;

    public SendingEmailState() {
    }

    public SendingEmailState(String from, String to, String body) {
        this.from = from;
        this.to = to;
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getBody() {
        return body;
    }

    @JsonIgnore
    @Override public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "{" +
                "\"from\":\"" + from + '"' +
                ", \"to\"" + to + '\"' +
                ", \"body\"" + body + '\"' +
                '}';
    }

    @JsonIgnore
    public static SendingEmailState fromJsonString(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, SendingEmailState.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new SendingEmailState();
    }
}
