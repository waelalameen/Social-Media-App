package com.app_mo.animefaq.model;

/**
 * Created by hp on 8/7/2017.
 */

public class NavModel {
    private String name;
    private int icon;

    public NavModel(String name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }
}
