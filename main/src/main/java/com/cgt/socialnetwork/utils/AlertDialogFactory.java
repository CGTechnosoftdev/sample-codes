package com.cgt.socialnetwork.utils;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.cgt.socialnetwork.R;

/**
 * Created by CGTechnosoft
 */
public class AlertDialogFactory {

    public static void alertBox(Context context, String title, String msg, DialogInterface.OnClickListener lisPos, DialogInterface.OnClickListener lisNag, boolean isCancelable) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        AlertDialog dialog = alertDialog.create();
        dialog.setCancelable(isCancelable);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.txtYes), lisPos);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.txtNo), lisNag);
        dialog.show();
    }

    public static void alertBox(Context context, String title, String msg, String buttonText, DialogInterface.OnClickListener lisPos, boolean isCancelable) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        AlertDialog dialog = alertDialog.create();
        dialog.setCancelable(isCancelable);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, buttonText, lisPos);
        dialog.show();
    }

}
