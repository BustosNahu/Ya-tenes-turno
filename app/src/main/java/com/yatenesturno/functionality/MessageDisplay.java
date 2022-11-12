package com.yatenesturno.functionality;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.main_screen.MainActivity;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MessageDisplay {
    public static final int PERSONAL = 0;
    public static final int FATAL = 1;
    public static final int WARNING = 2;
    public static final int NEW_UPDATE = 3;
    public static final int WELCOME_MESSAGE = 4;

    /**
     * Initialize class functionality, that is, fetch messages from database
     *
     * @param activity host activity
     */
    public static void init(Activity activity) {
        fetchForMessages(activity);
    }

    private static void fetchForMessages(final Activity activity) {
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_FETCH_MESSAGE,
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        handleResponse(activity, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );
    }

    private static void handleResponse(Activity activity, JSONObject response) {
        try {
            JSONArray messages = response.getJSONArray("messages");

            int currMaxRelevance = 10;
            int currMaxIndex = -1;
            int relevance;
            JSONObject messageData;

            for (int i = 0; i < messages.length(); i++) {
                messageData = messages.getJSONObject(i);
                relevance = messageData.getInt("relevance");

                if (shouldShowAgain(activity, messageData.getString("id")) && currMaxRelevance >= relevance) {
                    currMaxIndex = i;
                    currMaxRelevance = relevance;
                }

            }

            if (currMaxIndex != -1) {
                messageData = messages.optJSONObject(currMaxIndex);
                displayMessage(activity, messageData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Router method to the corresponding message type
     * @param activity
     * @param messageData
     * @throws JSONException
     */
    private static void displayMessage(Activity activity, JSONObject messageData) throws JSONException {
        boolean shouldShowAgain = shouldShowAgain(activity, messageData.getString("id"));
        boolean shouldCheckVersion = messageData.getBoolean("shouldCheckVersion");

        if (shouldShowAgain && (!shouldCheckVersion || isAppVersionLower(activity, messageData.getInt("version")))) {
            switch (messageData.getInt("relevance")) {
                case PERSONAL:
                case FATAL:
                    showFatalMessage(activity, messageData);
                    break;
                case WARNING:
                    showWarningMessage(activity, messageData);
                    break;
                case NEW_UPDATE:
                    showNewUpdateMessage(activity, messageData);
                    break;
                case WELCOME_MESSAGE:
                    showWelcomeMessage(activity, messageData);
                    break;

                default:
            }
        }
    }

    /**
     * Displays a message that will sign out the user upon accepting or dismissing the dialog
     *
     * @param activity    host activity
     * @param messageData json containing message's data: id, message, title, version, shouldCheckVersion
     */
    private static void showFatalMessage(final Activity activity, JSONObject messageData) throws JSONException {
        DialogInterface.OnClickListener positiveListener;
        DialogInterface.OnDismissListener onDismissListener;

        positiveListener = (dialog, which) -> ((MainActivity) activity).signOut();
        onDismissListener = dialog -> ((MainActivity) activity).signOut();

        new CustomAlertDialogBuilder(activity)
                .setTitle(messageData.getString("title"))
                .setMessage(messageData.getString("message"))
                .setPositiveButton(R.string.accept, positiveListener)
                .setOnDismissListener(onDismissListener)
                .setNeutralButton(R.string.open_google_play, (dialog, which) -> {
                    final String appPackageName = activity.getPackageName();
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                })
                .show();
    }

    /**
     * Displays a message warning the user about future bans/restrictions to their account/place if no action is made
     * after a certain time
     *
     * @param activity    host activity
     * @param messageData json containing message's data: id, message, title, version, shouldCheckVersion
     */
    private static void showWarningMessage(Activity activity, JSONObject messageData) throws JSONException {
        CustomAlertDialogBuilder customAlertDialogBuilder = new CustomAlertDialogBuilder(activity)
                .setTitle(messageData.getString("title"))
                .setMessage(messageData.getString("message"));

        addDontShowAgainCheckBox(activity, messageData, customAlertDialogBuilder);

        customAlertDialogBuilder.show();
    }

    /**
     * Displays a message welcoming the user to the app or to the new version
     *
     * @param activity    host activity
     * @param messageData json containing message's data: id, message, title, version, shouldCheckVersion
     */
    private static void showWelcomeMessage(final Activity activity, final JSONObject messageData) throws JSONException {
        new CustomAlertDialogBuilder(activity)
                .setTitle(messageData.getString("title"))
                .setMessage(messageData.getString("message"))
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    try {
                        saveToNotShowAgain(activity, messageData.getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                })
                .setOnDismissListener(dialog -> {
                    try {
                        saveToNotShowAgain(activity, messageData.getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                })
                .show();
    }

    /**
     * Displays a message warning the user that a new version is available
     *
     * @param activity    host activity
     * @param messageData json containing message's data: id, message, title, version, shouldCheckVersion
     */
    private static void showNewUpdateMessage(final Activity activity, JSONObject messageData) throws JSONException {
        CustomAlertDialogBuilder customAlertDialogBuilder = new CustomAlertDialogBuilder(activity)
                .setTitle(messageData.getString("title"))
                .setMessage(messageData.getString("message"))
                .setNeutralButton(R.string.open_google_play, (dialog, which) -> {
                    final String appPackageName = activity.getPackageName();
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                });

        addDontShowAgainCheckBox(activity, messageData, customAlertDialogBuilder);

        customAlertDialogBuilder.show();
    }

    private static boolean isAppVersionLower(Activity activity, int version) {
        int appVersion = getVersionNumber(activity);
        if (version != -1) {
            return appVersion <= version;
        }
        return false;
    }

    private static int getVersionNumber(Activity activity) {
        PackageManager manager = activity.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(activity.getPackageName(), PackageManager.GET_ACTIVITIES);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Checks whether a message should be displayed again to the user
     *
     * @param activity host activity
     * @param id       the message's id
     * @return true if the message should be displayed, false if the user has already decided not to see this message again
     */
    private static boolean shouldShowAgain(Activity activity, String id) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        String jsonMessageIds = sharedPreferences.getString(Constants.SHARED_PREF_SHOW_MESSAGE_AGAIN, null);

        if (jsonMessageIds != null) {
            List<String> messageIds = new Gson().fromJson(jsonMessageIds, new TypeToken<List<String>>() {
            }.getType());

            return !messageIds.contains(id);
        }

        return true;
    }

    /**
     * Mark a message so user doesnt see it again
     *
     * @param activity host activity
     * @param id       the id of the message to be marked
     */
    private static void saveToNotShowAgain(Activity activity, String id) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        String jsonMessageIds = sharedPreferences.getString(Constants.SHARED_PREF_SHOW_MESSAGE_AGAIN, null);
        List<String> messageIds = new Gson().fromJson(jsonMessageIds, new TypeToken<List<String>>() {
        }.getType());

        if (messageIds == null) {
            messageIds = new ArrayList<>();
        }

        messageIds.add(id);

        sharedPreferences.edit().putString(Constants.SHARED_PREF_SHOW_MESSAGE_AGAIN, new Gson().toJson(messageIds)).apply();
    }

    /**
     * Adds the not show again checkbox to a dialog builder
     */
    private static void addDontShowAgainCheckBox(final Activity activity, final JSONObject messageData, CustomAlertDialogBuilder customAlertDialogBuilder) {
        ViewGroup view = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.schedule_save_warning, null, false);
        view.getChildAt(0).setVisibility(View.GONE);
        final CheckBox checkBox = view.findViewById(R.id.checkBoxWarning);

        customAlertDialogBuilder.setView(view);

        DialogInterface.OnClickListener positiveListenerCheckDontShowAgain = (dialog, which) -> {
            if (checkBox.isChecked()) {
                try {
                    saveToNotShowAgain(activity, messageData.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        customAlertDialogBuilder.setPositiveButton(R.string.accept, positiveListenerCheckDontShowAgain);
    }

    /**
     * Invalidates the not show again preference
     */
    private static void invalidate(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        sharedPreferences.edit().putString(Constants.SHARED_PREF_SHOW_MESSAGE_AGAIN, null).apply();
    }
}
