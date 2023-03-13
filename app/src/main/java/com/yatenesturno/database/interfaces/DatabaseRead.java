package com.yatenesturno.database.interfaces;

import com.yatenesturno.listeners.DatabaseCallback;

import java.util.Map;

/**
 *
 */
public interface DatabaseRead {

    void GET(String subDirURL, Map<String, String> body, DatabaseCallback callback);

    void invalidate();
}