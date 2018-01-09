package com.app_mo.animefaq.model;

import java.io.Serializable;

public class Answer implements Serializable {
    private String result, answerID, answerText, answered, votes, username, img, create_time;
    private AnswerLastReply replay;

    public Answer(String answerID, String answerText, String answered, String votes, String username, String img, String create_time,
                  AnswerLastReply replay) {
        this.answerID = answerID;
        this.answerText = answerText;
        this.answered = answered;
        this.votes = votes;
        this.username = username;
        this.img = img;
        this.create_time = create_time;
        this.replay = replay;
    }

    public String getResult() {
        return result;
    }

    public String getAnswerID() {
        return answerID;
    }

    public String getAnswerText() {
        return answerText;
    }

    public String getAnswered() {
        return answered;
    }

    public String getVotes() {
        return votes;
    }

    public String getUsername() {
        return username;
    }

    public String getImg() {
        return img;
    }

    public String getCreate_time() {
        return create_time;
    }

    public AnswerLastReply getReplay() {
        return replay;
    }
}
