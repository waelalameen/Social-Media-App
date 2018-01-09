package com.app_mo.animefaq.model;

/**
 * Created by hp on 8/21/2017.
 */

public class DescriptionInfo {
    private String id, description, result;

    public DescriptionInfo(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getResult() {
        return result;
    }
}
