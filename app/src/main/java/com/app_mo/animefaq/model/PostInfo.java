package com.app_mo.animefaq.model;

public class PostInfo {
    private String title, question, token, result, msg;

    public PostInfo(String title, String question, String token) {
        this.title = title;
        this.question = question;
        this.token = token;
    }

    public String getResult() {
        return result;
    }

    public String getMsg() {
        return msg;
    }
}
