package com.app_mo.animefaq.model;

/**
 * Created by hp on 8/9/2017.
 */

public class Users {
    private String userName, fullName, email, password, MACAddress, deviceName, firebaseToken, result, msg;
    private int id;

    public Users(String userName, String email, String fullName, String password, String MACAddress, String deviceName, String firebaseToken) {
        this.userName = userName;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.MACAddress = MACAddress;
        this.deviceName = deviceName;
        this.firebaseToken = firebaseToken;
    }

    public String getUserName() {
        return userName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public String getResult() {
        return result;
    }

    public String getMsg() {
        return msg;
    }

    public int getId() {
        return id;
    }
}
