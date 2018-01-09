package com.app_mo.animefaq.model;

public class Question {
    private String user, qID, question, answered, views, answers, img, create_time, result, msg;

    public Question(String user, String qID, String question, String answered, String views, String answers, String img, String create_time) {
        this.user = user;
        this.qID = qID;
        this.question = question;
        this.answered = answered;
        this.views = views;
        this.answers = answers;
        this.img = img;
        this.create_time = create_time;
    }

    public String getUser() {
        return user;
    }

    public String getqID() {
        return qID;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswered() {
        return answered;
    }

    public String getViews() {
        return views;
    }

    public String getAnswers() {
        return answers;
    }

    public String getImg() {
        return img;
    }

    public String getCreate_time() {
        return create_time;
    }

    public String getResult() {
        return result;
    }

    public String getMsg() {
        return msg;
    }
}