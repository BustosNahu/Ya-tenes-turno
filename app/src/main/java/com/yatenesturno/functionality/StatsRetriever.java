package com.yatenesturno.functionality;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.yatenesturno.Constants;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.user_auth.UserManagement;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Records and updates backend about stats such as version number
 */
public class StatsRetriever {

    public static final String VERSION_CODE = "version_code";

    private final Bundle bundle;

    public StatsRetriever(Context context) {
        bundle = new Bundle();

        bundle.putString("user_id", UserManagement.getInstance().getUser().getId());

        fetchStats(context);
        saveStats();
    }

    private void fetchStats(Context context) {
        getVersionCode(context);
        // upcoming stats
    }

    /**
     * Retrieves current app version code
     *
     * @param context app context
     */
    private void getVersionCode(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info;

        int versionCode = -1;
        try {
            info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        bundle.putInt(VERSION_CODE, versionCode);
    }


    /**
     * Posts stats to backend
     */
    private void saveStats() {
        Map<String, String> body = getBody();

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_POST_STATS,
                body,
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

    private Map<String, String> getBody() {
        Map<String, String> out = new HashMap<>();

        for (String key : bundle.keySet()) {
            out.put(key, String.valueOf(bundle.get(key)));
        }

        return out;
    }
}
