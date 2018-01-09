package com.app_mo.animefaq;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by wael on 7/31/2017.
 */

public class ChangeTypeface {
    public static void setTypeface(Context context, View view) {
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "jannal.ttf");

        if (view instanceof TextView || view instanceof EditText || view instanceof Button) {
            ((TextView) view).setTypeface(typeface);
        }
    }
}
