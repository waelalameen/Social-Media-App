package com.app_mo.animefaq.model;

public class EditQuestionInfo {
    private String qID, title, text, token, result, msg;

    public EditQuestionInfo(String qID, String title, String text, String token) {
        this.qID = qID;
        this.title = title;
        this.text = text;
        this.token = token;
    }

    public String getqID() {
        return qID;
    }

    public String getTitle() {
        return title;
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

    public String getMsg() {
        return msg;
    }
}
