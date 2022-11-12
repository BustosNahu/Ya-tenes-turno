package com.yatenesturno.functionality.emergency;

import com.yatenesturno.Constants;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.database.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.serializers.BuilderListEmergencyLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class EmergencyLocationHandler {
    private static EmergencyLocationHandler instance;
    private final Map<String, List<EmergencyLocation>> jobEmergencyLocationsMap;

    private EmergencyLocationHandler() {
        jobEmergencyLocationsMap = new HashMap<>();
    }

    public static EmergencyLocationHandler getInstance() {
        if (instance == null) {
            instance = new EmergencyLocationHandler();
        }
        return instance;
    }

    public void getEmergencyLocations(String jobId, GetEmergencyLocationListener listener) {
        if (jobEmergencyLocationsMap.containsKey(jobId)) {
            listener.onSuccess(jobEmergencyLocationsMap.get(jobId));
        }
        fetchFromRemote(jobId, listener);
    }

    private void fetchFromRemote(String jobId, GetEmergencyLocationListener listener) {
        DatabaseDjangoRead.getInstance().GET(
                String.format(Constants.DJANGO_URL_EMERGENCY_LOCATIONS, jobId),
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        BuilderListEmergencyLocation builder = new BuilderListEmergencyLocation();
                        List<EmergencyLocation> locations = builder.build(response);
                        saveToLocal(jobId, locations);
                        listener.onSuccess(locations);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );
    }

    public void addEmergencyLocation(String jobId, EmergencyLocation location, NewEmergencyLocationListener listener) {
        addToRemote(jobId, location, listener);
    }

    public void removeEmergencyLocation(String jobId, EmergencyLocation location, RemoveEmergencyLocationListener listener) {
        if (jobEmergencyLocationsMap.containsKey(jobId)) {
            jobEmergencyLocationsMap.get(jobId).remove(location);
            removeInRemote(jobId, location, listener);
        }
    }

    private void removeInRemote(String jobId, EmergencyLocation location, RemoveEmergencyLocationListener listener) {
        DatabaseDjangoWrite.getInstance().DELETE(
                String.format(Constants.DJANGO_URL_EMERGENCY_LOCATIONS_UPDATE, jobId, location.getId()),
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        listener.onSuccess();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );
    }

    private void addToRemote(String jobId, EmergencyLocation location, NewEmergencyLocationListener listener) {
        DatabaseDjangoWrite.getInstance().POSTJSON(
                String.format(Constants.DJANGO_URL_EMERGENCY_LOCATIONS, jobId),
                location.toJson(),
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        EmergencyLocation out = new EmergencyLocation();
                        try {
                            JSONObject locationJson = response.getJSONObject("location");
                            out = BuilderListEmergencyLocation.parseEmergencyLocation(locationJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (!jobEmergencyLocationsMap.containsKey(jobId)) {
                            jobEmergencyLocationsMap.put(jobId, new ArrayList<>());
                        }
                        jobEmergencyLocationsMap.get(jobId).add(out);
                        listener.onSuccess(out);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onError();
                    }
                }
        );
    }

    private void saveToLocal(String jobId, List<EmergencyLocation> locations) {
        jobEmergencyLocationsMap.put(jobId, locations);
    }

    public interface GetEmergencyLocationListener {
        void onSuccess(List<EmergencyLocation> locations);

        void onError();
    }

    public interface NewEmergencyLocationListener {
        void onSuccess(EmergencyLocation location);

        void onError();
    }

    public interface RemoveEmergencyLocationListener {
        void onSuccess();

        void onError();
    }

}

