package com.yatenesturno.listeners;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public abstract class DatabaseCallback extends JsonHttpResponseHandler {

    public abstract void onSuccess(int statusCode, Header[] headers, JSONObject response);

    public abstract void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable);

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
        onFailure(statusCode, headers, "", throwable);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
        onFailure(statusCode, headers, "", throwable);
    }
}
