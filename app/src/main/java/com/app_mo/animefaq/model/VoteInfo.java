package com.app_mo.animefaq.model;

public class VoteInfo {
    private String answerID, token, votes, result;

    public VoteInfo(String answerID, String token) {
        this.answerID = answerID;
        this.token = token;
    }

    public String getAnswerID() {
        return answerID;
    }

    public String getVotes() {
        return votes;
    }

    public String getResult() {
        return result;
    }
}
