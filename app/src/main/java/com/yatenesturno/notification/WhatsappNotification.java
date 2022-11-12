package com.yatenesturno.notification;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class WhatsappNotification implements NotificationMethod {

    private static final String TYPE = "whatsapp";

    @Override
    public void sendMessage(Context context, Bundle data) {
        try {
            String url = "https://api.whatsapp.com/send?phone=" +
                    data.get("contactInfo") +
                    "&text=" +
                    URLEncoder.encode(data.getString("message"), "UTF-8");

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
