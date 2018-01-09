package com.app_mo.animefaq.model;

public class SendReplyInfo {
    private String aID, text, token, result;

    public SendReplyInfo(String aID, String text, String token) {
        this.aID = aID;
        this.text = text;
        this.token = token;
    }

    public String getaID() {
        return aID;
    }

    public String getText() {
        return text;
    }

    public String getToken() {
        return token;
    }

    public String getResult() {
        return result;
    }
}
