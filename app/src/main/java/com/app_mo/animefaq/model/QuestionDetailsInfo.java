package com.app_mo.animefaq.model;

public class QuestionDetailsInfo {
    private String qID, token;

    public QuestionDetailsInfo(String qID, String token) {
        this.qID = qID;
        this.token = token;
    }

    public String getqID() {
        return qID;
    }

    public String getToken() {
        return token;
    }
}
