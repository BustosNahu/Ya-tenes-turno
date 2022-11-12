package com.yatenesturno.functionality.push_notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.yatenesturno.Constants;
import com.yatenesturno.database.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class PushNotificationHandler {

    private PushNotificationHandler() {

    }


    public static void fetchFCMTokenFromAPI() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String currentUserToken = task.getResult();
                        saveTokenToRemote(currentUserToken);
                    }
                });
    }

    public static void saveTokenToRemote(String token) {
        SyncHttpClient client = new SyncHttpClient();

        Map<String, String> body = new HashMap<>();
        body.put("token", token);

        RequestParams params = new RequestParams(body);


        client.post(
                Constants.DJANGO_URL_UPDATE_FCM_TOKEN,
                params,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );


    }
}
