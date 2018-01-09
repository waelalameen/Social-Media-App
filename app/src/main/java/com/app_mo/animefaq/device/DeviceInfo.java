package com.app_mo.animefaq.device;

import android.app.Activity;
import android.os.Build;

/**
 * Created by hp on 8/9/2017.
 */

public class DeviceInfo {

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (model.startsWith(manufacturer))
            return capitalize(model);
        else
            return manufacturer + " " + model;
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0)
            return "";

        char first = s.charAt(0);

        if (Character.isUpperCase(first))
            return s;
        else
            return Character.toUpperCase(first) + s.substring(1);
    }

    public static void exitFromApplication(Activity activity) {
        activity.moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
