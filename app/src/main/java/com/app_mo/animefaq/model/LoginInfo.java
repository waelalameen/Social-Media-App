package com.app_mo.animefaq.model;

/**
 * Created by hp on 8/12/2017.
 */

public class LoginInfo {
    private String userName, password, method, MAC, result, msg, token;
    private int id;

    public LoginInfo(int id, String MAC) {
        this.id = id;
        this.MAC = MAC;
    }

    public LoginInfo(String userName, String password, String method, String MAC) {
        this.userName = userName;
        this.password = password;
        this.method = method;
        this.MAC = MAC;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getMethod() {
        return method;
    }

    public String getMAC() {
        return MAC;
    }

    public String getResult() {
        return result;
    }

    public String getMsg() {
        return msg;
    }

    public String getToken() {
        return token;
    }

    public int getId() {
        return id;
    }
}
