package com.yatenesturno.notification;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class EmailNotification implements NotificationMethod {

    private static final String TYPE = "email";

    @Override
    public void sendMessage(Context context, Bundle data) {
        String[] toEmails = data.getString("contactInfo").split(",");

        Intent email = new Intent(Intent.ACTION_SENDTO);
        email.setData(Uri.parse("mailto:"));
        email.putExtra(Intent.EXTRA_EMAIL, toEmails);
        email.putExtra(Intent.EXTRA_SUBJECT, data.getString("subject"));
        email.putExtra(Intent.EXTRA_TEXT, data.getString("message"));

        context.startActivity(Intent.createChooser(email, "Enviar mediante:"));
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
