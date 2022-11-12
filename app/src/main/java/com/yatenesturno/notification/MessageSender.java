package com.yatenesturno.notification;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MessageSender {

    private final CustomUser client;

    public MessageSender(CustomUser client) {
        this.client = client;
    }

    public void sendNotification(final String message, final Context context) {
        Map<String, String> body = new HashMap<>();
        body.put("client_id", client.getId());
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_NOTIFICATION_METHOD,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        onMethodFetch(context, message, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Toast.makeText(context, context.getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void onMethodFetch(Context context, String message, JSONObject response) {
        try {
            JSONObject notificationJson = response.getJSONObject("notification");
            String notificationType = notificationJson.getString("notification_type");
            String contactInfo = notificationJson.getString("contact_info");

            NotificationMethod method = getNotificationMethod(notificationType);
            Bundle bundle = new Bundle();
            bundle.putString("contactInfo", contactInfo);
            bundle.putString("message", message);

            method.sendMessage(context, bundle);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private NotificationMethod getNotificationMethod(String notificationType) {
        List<NotificationMethod> methods = getNotificationMethodList();

        for (NotificationMethod m : methods) {
            if (m.getType().equals(notificationType)) {
                return m;
            }
        }
        return null;
    }

    private List<NotificationMethod> getNotificationMethodList() {
        List<NotificationMethod> methods = new ArrayList<>();

        methods.add(new WhatsappNotification());
        methods.add(new EmailNotification());

        return methods;
    }
}
