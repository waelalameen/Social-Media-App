package com.app_mo.animefaq.behavior;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.app_mo.animefaq.R;

/**
 * Created by hp on 8/14/2017.
 */

public class Animations {
    private static final int DURATION = 200;

    public static void expandTextView(TextView textView) {
        int collapseMaxLines = 3;
        ObjectAnimator animator = ObjectAnimator.ofInt(textView, "maxLines",
                textView.getMaxLines() == collapseMaxLines ? textView.getLineCount() : collapseMaxLines);
        animator.setDuration(DURATION).start();
    }

    private static void rotateFloatingActionButton(String type, View view, Context context) {
        Animation fbOpen = AnimationUtils.loadAnimation(context, R.anim.fb_open);
        Animation fbClose = AnimationUtils.loadAnimation(context, R.anim.fb_close);
        Animation cw = AnimationUtils.loadAnimation(context, R.anim.rotate_clockwise);
        Animation ccw = AnimationUtils.loadAnimation(context, R.anim.rotate_anti_clockwise);

        if (view != null) {

            FloatingActionButton fb = (FloatingActionButton) view.findViewById(R.id.fab);

            switch (type) {
                case "fbopen":
                    fb.startAnimation(fbOpen);
                    break;
                case "fbclose":
                    fb.startAnimation(fbClose);
                    break;
                case "cw":
                    fb.startAnimation(cw);
                    break;
                case "ccw":
                    fb.startAnimation(ccw);
                    break;
                default:
                    break;
            }
        }
    }
}
