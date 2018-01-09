package com.app_mo.animefaq.model;

public class ReplyInfo {
    private String replyID, token, result, msg;

    public ReplyInfo(String replyID, String token) {
        this.replyID = replyID;
        this.token = token;
    }

    public String getReplyID() {
        return replyID;
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
