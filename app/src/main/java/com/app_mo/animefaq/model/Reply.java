package com.app_mo.animefaq.model;

public class Reply {
    private String result, replayID, replayText, likes, dislikes, username, img, create_time, like_trigger
            , dislike_trigger;

    public Reply(String replayID, String replayText, String likes, String dislikes, String username, String img, String create_time, String like_trigger, String dislike_trigger) {
        this.replayID = replayID;
        this.replayText = replayText;
        this.likes = likes;
        this.dislikes = dislikes;
        this.username = username;
        this.img = img;
        this.create_time = create_time;
        this.like_trigger = like_trigger;
        this.dislike_trigger = dislike_trigger;
    }

    public String getReplayID() {
        return replayID;
    }

    public String getReplayText() {
        return replayText;
    }

    public String getLikes() {
        return likes;
    }

    public String getDislikes() {
        return dislikes;
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

    public String getLike_trigger() {
        return like_trigger;
    }

    public String getDislike_trigger() {
        return dislike_trigger;
    }

    public String getResult() {
        return result;
    }
}
