package com.app_mo.animefaq.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hp on 8/12/2017.
 */

public class SavePreferences {
    private SharedPreferences preferences;
    private Context context;

    public SavePreferences(Context context) {
        this.context = context;
    }

    public void save(String preferenceName, String key, String value) {
        preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getValues(String preferenceName, String key) {
        preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }
}
