package com.app_mo.animefaq.model;

public class UserSignOutInfo {
    private String userID, token, result, msg, method;

    public UserSignOutInfo(String userID, String token) {
        this.userID = userID;
        this.token = token;
    }

    public String getUserID() {
        return userID;
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

    public String getMethod() {
        return method;
    }
}
