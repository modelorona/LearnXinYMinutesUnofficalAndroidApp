package com.ah.xiny3.Dialogs;

import android.app.AlertDialog;
import android.content.Context;

public class SimpleAlert {
    public static void displayWithOK(Context c, String message, String title){
        AlertDialog alert = new AlertDialog.Builder(c).create();
        // set alert title
        alert.setTitle(title);
        // set alert message
        alert.setMessage(message);
        // set OK button
        // hide the alert when clicking ok button
        alert.setButton(AlertDialog.BUTTON_POSITIVE, "Got it", (dialog, which) -> alert.hide());
        alert.show();
    }
}
