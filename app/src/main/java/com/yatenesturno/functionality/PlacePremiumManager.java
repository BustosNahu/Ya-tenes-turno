package com.yatenesturno.functionality;

import com.yatenesturno.Constants;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.listeners.DatabaseCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class PlacePremiumManager {

    private static PlacePremiumManager instance;
    private Map<String, List<String>> userPlaceMap;

    private PlacePremiumManager() {

    }

    public static PlacePremiumManager getInstance() {
        if (instance == null) {
            instance = new PlacePremiumManager();
        }
        return instance;
    }

    public boolean getIsPremium(final String placeId, final String userId) {
        if (userPlaceMap != null) {
            return userPlaceMap.containsKey(placeId) && userPlaceMap.get(placeId).contains(userId);
        }
        return false;
    }

    public void refresh(final OnRefreshListener listener) {
        fetchPremiumFromRemote(new DatabaseCallback() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                handleResponse(response);
                if (listener != null) {
                    listener.onRefresh();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }
        });
    }

    private void fetchPremiumFromRemote(DatabaseCallback callback) {
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_GET_PREMIUM_PLACES,
                null,
                callback
        );
    }

    private void handleResponse(JSONObject response) {
        try {
            JSONObject premiumPlacesJson = response.getJSONObject("premium_places");
            JSONArray jobsIds;
            List<String> jobIdsList;
            userPlaceMap = new HashMap<>();

            for (Iterator<String> it = premiumPlacesJson.keys(); it.hasNext(); ) {
                String key = it.next();
                jobsIds = premiumPlacesJson.getJSONArray(key);

                jobIdsList = new ArrayList<>();
                for (int i = 0; i < jobsIds.length(); i++) {
                    jobIdsList.add(jobsIds.getString(i));
                }
                userPlaceMap.put(key, jobIdsList);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

}
