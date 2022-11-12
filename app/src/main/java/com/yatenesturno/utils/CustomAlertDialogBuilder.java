package com.yatenesturno.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yatenesturno.R;

public class CustomAlertDialogBuilder {

    private final MaterialAlertDialogBuilder dialogBuilder;
    private AlertDialog dialog;

    public CustomAlertDialogBuilder(Context context) {
        dialogBuilder = new MaterialAlertDialogBuilder(context);
    }

    public CustomAlertDialogBuilder setView(View view) {
        dialogBuilder.setView(view);
        return this;
    }

    public CustomAlertDialogBuilder setPositiveButton(int text, DialogInterface.OnClickListener listener) {
        dialogBuilder.setPositiveButton(text, listener);
        return this;
    }

    public CustomAlertDialogBuilder setNegativeButton(int text, DialogInterface.OnClickListener listener) {
        dialogBuilder.setNeutralButton(text, listener);
        return this;
    }

    public CustomAlertDialogBuilder setTitle(String title) {
        TextView textViewTitle = (TextView) LayoutInflater.from(dialogBuilder.getContext()).inflate(R.layout.alert_dialog_title, null);
        textViewTitle.setText(title);
        dialogBuilder.setCustomTitle(textViewTitle);
        return this;
    }

    public AlertDialog show() {
        dialog = dialogBuilder.create();
        dialog.setOnShowListener(dialogInterface -> {
            if (dialog.getButton(AlertDialog.BUTTON_NEUTRAL) != null) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(dialog.getContext().getResources().getColor(R.color.darker_grey, null));
            }
            if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dialog.getContext().getResources().getColor(R.color.darker_grey, null));
            }
        });
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_border_white);
        dialog.show();
        return dialog;
    }

    public CustomAlertDialogBuilder setMessage(int message) {
        dialogBuilder.setMessage(message);
        return this;
    }

    public CustomAlertDialogBuilder setMessage(String message) {
        dialogBuilder.setMessage(message);
        return this;
    }

    public CustomAlertDialogBuilder setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        dialogBuilder.setOnDismissListener(onDismissListener);
        return this;
    }

    public CustomAlertDialogBuilder setNeutralButton(int text, DialogInterface.OnClickListener onClickListener) {
        dialogBuilder.setNeutralButton(text, onClickListener);
        return this;
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public Dialog create() {
        return dialogBuilder.create();
    }
}
