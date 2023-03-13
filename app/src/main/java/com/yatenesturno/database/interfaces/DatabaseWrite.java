package com.yatenesturno.database.interfaces;

import com.yatenesturno.listeners.DatabaseCallback;

import org.json.JSONObject;

import java.util.Map;

/**
 *
 */
public interface DatabaseWrite {

    void POST(String subDirURL, Map<String, String> body, DatabaseCallback callback);

    void PUT(String subDirURL, Map<String, String> body, DatabaseCallback callback);

    void PUT(String subDirURL, JSONObject body, DatabaseCallback callback);

    void DELETE(String subDirURL, DatabaseCallback callback);

    void POSTJSON(String subDirURL, JSONObject json, DatabaseCallback callback);

    void invalidate();
}