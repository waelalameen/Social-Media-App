package com.app_mo.animefaq.model;

public class MainQuestions {
    private String token, page;

    public MainQuestions(String token, String page) {
        this.token = token;
        this.page = page;
    }

    public String getToken() {
        return token;
    }

    public String getPage() {
        return page;
    }
}

