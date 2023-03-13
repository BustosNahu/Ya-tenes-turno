package com.yatenesturno.functionality.app_observation;

import com.yatenesturno.Constants;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class AppObservationManager {

    public static void addObservationToAppointment(String jobId, String appId, String observation, OnUpdateListener listener) {
        Map<String, String> body = new HashMap<>();
        body.put("observation", observation);

        DatabaseDjangoWrite.getInstance().POST(
                String.format(Constants.DJANGO_URL_APPOINTMENT_OBSERVATION, jobId, appId),
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        listener.onUpdate();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    public static void deleteObservationFromAppointment(String jobId, String appId, OnUpdateListener listener) {
        DatabaseDjangoWrite.getInstance().DELETE(
                String.format(Constants.DJANGO_URL_APPOINTMENT_OBSERVATION, jobId, appId),
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        listener.onUpdate();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    public interface OnUpdateListener {
        void onUpdate();

        void onFailure();
    }
}
