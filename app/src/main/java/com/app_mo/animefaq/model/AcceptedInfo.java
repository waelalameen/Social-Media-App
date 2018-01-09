package com.app_mo.animefaq.model;

public class AcceptedInfo {
    private String answerID, token, result, msg;

    public AcceptedInfo(String answerID, String token) {
        this.answerID = answerID;
        this.token = token;
    }

    public String getAnswerID() {
        return answerID;
    }

    public String getToken() {
        return token;
    }

    public String getResult() {
        return result;
    }

    public String getMsg() {
        return msg;
    }
}
