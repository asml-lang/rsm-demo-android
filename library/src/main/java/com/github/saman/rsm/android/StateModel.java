package com.github.saman.rsm.android;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"properties", "required"})
class StateModel {
    public String asml = "1.0.0";
    public Info info;

    static class Info {
        public String title;
        public String description;
        public String version;
        public Contact contact;

    }

    static class Contact {
        public String name;
        public String email;
        public String url;
    }
}
