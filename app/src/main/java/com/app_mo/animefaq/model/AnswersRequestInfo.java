package com.app_mo.animefaq.model;

public class AnswersRequestInfo {
    private String id, from, to, token;

    public AnswersRequestInfo(String id, String from, String to, String token) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getToken() {
        return token;
    }
}

