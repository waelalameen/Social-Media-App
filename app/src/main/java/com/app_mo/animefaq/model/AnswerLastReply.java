package com.app_mo.animefaq.model;

import java.io.Serializable;

public class AnswerLastReply implements Serializable {
    private String replayUserID, replayUsername, replayText, replayCreate_Time;

    public AnswerLastReply(String replayUserID, String replayUsername, String replayText, String replayCreate_Time) {
        this.replayUserID = replayUserID;
        this.replayUsername = replayUsername;
        this.replayText = replayText;
        this.replayCreate_Time = replayCreate_Time;
    }

    public String getReplayUserID() {
        return replayUserID;
    }

    public String getReplayUsername() {
        return replayUsername;
    }

    public String getReplayText() {
        return replayText;
    }

    public String getReplayCreate_Time() {
        return replayCreate_Time;
    }
}
