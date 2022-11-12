package com.yatenesturno.database;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.yatenesturno.Constants;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.user_auth.UserManagement;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 *
 */
public class DatabaseDjangoWrite implements DatabaseWrite {

    private static DatabaseDjangoWrite instance;
    private static AsyncHttpClient client;
    private static Database database;

    /**
     * Default constructor
     */
    private DatabaseDjangoWrite() {
        database = new DatabaseDjango();

        // HttpClient setup
        client = new AsyncHttpClient();

        client.setTimeout(Constants.CONNECTION_TIMEOUT);
        client.addHeader("Authorization", "Token " + UserManagement.getInstance().getAccessToken());
    }

    public static DatabaseDjangoWrite getInstance() {
        if (instance == null) {
            instance = new DatabaseDjangoWrite();
        }
        return instance;
    }

    @Override
    public void POST(String subDirURL, Map<String, String> body, DatabaseCallback callback) {

        RequestParams params = new RequestParams(body);

        String url = database.getUrl() + subDirURL;

        client.post(
                url,
                params,
                callback
        );
    }

    @Override
    public void PUT(String subDirURL, Map<String, String> body, DatabaseCallback callback) {
        RequestParams params = new RequestParams(body);

        String url = database.getUrl() + subDirURL;

        client.put(
                url,
                params,
                callback
        );
    }

    @Override
    public void PUT(String subDirURL, JSONObject body, DatabaseCallback callback) {

        StringEntity entity = null;

        try {
            entity = new StringEntity(body.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = database.getUrl() + subDirURL;

        client.put(
                null,
                url,
                entity,
                "application/json",
                callback
        );
    }

    @Override
    public void DELETE(String subDirURL, DatabaseCallback callback) {

        String url = database.getUrl() + subDirURL;

        client.delete(
                url,
                callback
        );
    }

    @Override
    public void POSTJSON(String subDirURL, JSONObject json, DatabaseCallback callback) {
        StringEntity entity = null;

        try {
            entity = new StringEntity(json.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = database.getUrl() + subDirURL;

        client.post(
                null,
                url,
                entity,
                "application/json",
                callback
        );
    }

    @Override
    public void invalidate() {
        instance = null;
    }

}