package com.app_mo.animefaq.behavior;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import com.app_mo.animefaq.R;

/**
 * Created by hp on 8/10/2017.
 */

public class Dialogs {
    private static ProgressDialog progressDialog;

    public static void makeDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    public static void onMakeProgress(Activity activity, final Context context, final String title, final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(context);
                progressDialog.setTitle(title);
                progressDialog.setMessage(message);
                progressDialog.setMax(100);
                progressDialog.setProgress(100);
            }
        });
    }

    public static void onHideProgress(Activity activity, final Context context) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(context);
                progressDialog.dismiss();
            }
        });
    }
}
