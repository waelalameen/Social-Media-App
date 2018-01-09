package com.app_mo.animefaq.model;

public class ReplyRequestInfo {
    private String id, page, token, result;

    public ReplyRequestInfo(String id, String page, String token) {
        this.id = id;
        this.page = page;
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public String getPage() {
        return page;
    }

    public String getToken() {
        return token;
    }

    public String getResult() {
        return result;
    }
}
