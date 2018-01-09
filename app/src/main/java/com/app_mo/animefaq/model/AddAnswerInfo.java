package com.app_mo.animefaq.model;

public class AddAnswerInfo {
    private String id, token, text, result;

    public AddAnswerInfo(String id, String token, String text) {
        this.id = id;
        this.token = token;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getText() {
        return text;
    }

    public String getResult() {
        return result;
    }
}
