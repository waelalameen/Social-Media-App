package com.app_mo.animefaq.model;

public class QuestionInfo {
    private String result, title, text, views, user, img, create_time, answers;

    public QuestionInfo(String title, String text, String views, String user, String img, String create_time, String answers) {
        this.title = title;
        this.text = text;
        this.views = views;
        this.user = user;
        this.img = img;
        this.create_time = create_time;
        this.answers = answers;
    }

    public String getResult() {
        return result;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getViews() {
        return views;
    }

    public String getUser() {
        return user;
    }

    public String getImg() {
        return img;
    }

    public String getCreate_time() {
        return create_time;
    }

    public String getAnswers() {
        return answers;
    }
}
