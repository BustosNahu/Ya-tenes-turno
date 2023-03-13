package com.yatenesturno.user_auth;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.yatenesturno.Constants;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.functionality.PlacePremiumManager;
import com.yatenesturno.functionality.push_notification.PushNotificationHandler;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.serializers.BuilderObjectCustomUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class UserManagement {

    private static UserManagement instance;
    private final List<Authenticator> authenticatorList;
    private String accessToken;
    private CustomUser user;
    private Authenticator authenticatorInUse;

    private UserManagement() {
        authenticatorList = getAuthenticators();
    }

    public static UserManagement getInstance() {
        if (instance == null) {
            instance = new UserManagement();
        }
        return instance;
    }

    public CustomUser getUser() {
        return user;
    }

    public void authenticate(Context context, final UserManagementAuthenticateListener listener) {

        if (authenticatorInUse == null) {
            authenticatorInUse = getAuthenticatorInUseFromLocal(context);
        }

        if (authenticatorInUse != null) {
            authenticatorInUse.init(context);

            boolean gotFromLocal = getUserDataFromLocal(context);

            if (accessToken == null && gotFromLocal) {
                authenticatorInUse.fetchUserData(context, new Authenticator.FetchUserDataListener() {
                    @Override
                    public void onUserDataFetch(Context context, Bundle userData) {
                        accessToken = userData.getString("access_token");
                        user = (CustomUser) userData.getSerializable("user");

                        DatabaseDjangoWrite.getInstance().invalidate();
                        DatabaseDjangoRead.getInstance().invalidate();

                        setupFCM();

                        saveUserDataToLocal(context, userData);
                        refreshPremium(listener);
                        Log.d("UserManagment", "onUserDataFetch fetch user data  good");

                    }

                    @Override
                    public void onFailure(Context context) {
                        listener.userNotAuthenticated();
                        invalidateUserData(context);
                        Log.d("UserManagment", "onfailure fetch user data ");
                    }
                });
            } else if (accessToken != null && user != null) {
                setupFCM();
                refreshPremium(listener);
                Log.d("UserManagment", "onfailure fetch user data ");

            } else {
                listener.userNotAuthenticated();
                Log.d("UserManagment", "No authenticated");

            }

        } else {
            Log.d("UserManagment", "No authenticated");

            listener.userNotAuthenticated();
            invalidateUserData(context);
        }
    }

    private void refreshPremium(final UserManagementAuthenticateListener listener) {
        PlacePremiumManager.getInstance().refresh(() -> listener.userAuthenticated());
    }

    private List<Authenticator> getAuthenticators() {
        List<Authenticator> authenticatorList = new ArrayList<>();

        authenticatorList.add(new AuthenticatorGoogle());
        authenticatorList.add(new MockAuthenticator());

        return authenticatorList;
    }

    public void setAuthenticatorInUse(Authenticator authenticatorInUse) {
        this.authenticatorInUse = authenticatorInUse;
    }

    public void signIn(final Activity activity, final UserManagementListener listener) {
        authenticatorInUse.signIn(activity, new Authenticator.SignInListener() {
            @Override
            public void onSuccess() {
                fetchAccessToken(activity, listener);
                saveAuthenticatorInUseToLocal(activity);
            }

            @Override
            public void onFailure() {
                Log.d("signIn", "No authenticated FAILURE");

                listener.onFailure();
            }
        });
    }

    public void signOut(final Activity activity, final UserManagementListener listener) {
        authenticatorInUse.signOut(activity, new Authenticator.SignOutListener() {
            @Override
            public void onSuccess() {
                if (listener != null) {
                    listener.onSuccess();
                }
                invalidateUserData(activity);
                invalidateAuthenticatorInUse(activity);
            }

            @Override
            public void onFailure() {
                if (listener != null) {
                    listener.onFailure();
                }
            }
        });
    }

    private void fetchAccessToken(Activity activity, final UserManagementListener listener) {
        authenticatorInUse.fetchUserData(activity, new Authenticator.FetchUserDataListener() {
            @Override
            public void onUserDataFetch(Context context, Bundle userData) {
                accessToken = userData.getString("access_token");
                user = (CustomUser) userData.getSerializable("user");

                saveUserDataToLocal(context, userData);
                listener.onSuccess();
            }

            @Override
            public void onFailure(Context context) {
                listener.onFailure();
                invalidateUserData(context);
            }
        });
    }

    private Authenticator getAuthenticatorInUseFromLocal(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        String NAME_ID_AUTHENTICATOR = sharedPreferences.getString(Constants.SHARED_PREF_AUTHENTICATOR, null);
        if (NAME_ID_AUTHENTICATOR != null) {
            for (Authenticator authenticator : authenticatorList) {
                if (authenticator.getNameId().equals(NAME_ID_AUTHENTICATOR)) {
                    return authenticator;
                }
            }
        }
        return null;
    }

    private void saveAuthenticatorInUseToLocal(Context context) {
        String NAME_ID_AUTHENTICATOR = null;
        if (authenticatorInUse != null) {
            NAME_ID_AUTHENTICATOR = authenticatorInUse.getNameId();
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        sharedPreferences.edit().putString(Constants.SHARED_PREF_AUTHENTICATOR, NAME_ID_AUTHENTICATOR).apply();
    }

    private void invalidateAuthenticatorInUse(Context context) {
        authenticatorInUse = null;
        saveAuthenticatorInUseToLocal(context);
    }

    public void onIntentCallback(Intent data) {
        authenticatorInUse.onActivityResult(data);
    }

    private boolean getUserDataFromLocal(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        accessToken = sharedPreferences.getString(Constants.SHARED_PREF_ACCESS_TOKEN, null);
        try {
            String json = sharedPreferences.getString(Constants.SHARED_PREF_USER, null);
            user = new BuilderObjectCustomUser().build(new JSONObject(json));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setupFCM() {
        PushNotificationHandler.fetchFCMTokenFromAPI();
    }

    private void saveUserDataToLocal(Context context, Bundle userData) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);

        sharedPreferences.edit()
                .putString(Constants.SHARED_PREF_ACCESS_TOKEN, userData.getString("access_token"))
                .putString(Constants.SHARED_PREF_USER, new BuilderObjectCustomUser().serialize((CustomUser) userData.getSerializable("user")))
                .apply();
    }

    private void invalidateUserData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(Constants.SHARED_PREF_ACCESS_TOKEN, null)
                .putString(Constants.SHARED_PREF_USER, null)
                .apply();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public interface UserManagementAuthenticateListener {

        void userNotAuthenticated();

        void userAuthenticated();

    }

    public interface UserManagementListener {
        void onSuccess();

        void onFailure();
    }
}