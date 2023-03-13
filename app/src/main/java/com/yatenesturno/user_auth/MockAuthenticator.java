package com.yatenesturno.user_auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.yatenesturno.Constants;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.objects.CustomUserImpl;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Mock Test authenticator
 */
public class MockAuthenticator implements Authenticator {

    /**
     * Instance  variables
     */
    private static final String NAME_ID = "AUTHENTICATOR_MOCK";


    @Override
    public void init(Context context) {

    }

    @Override
    public String getNameId() {
        return NAME_ID;
    }

    @Override
    public void onActivityResult(Intent data) {

    }

    @Override
    public void signIn(Activity activity, SignInListener listener) {
        listener.onSuccess();
    }

    @Override
    public void signOut(Activity activity, SignOutListener listener) {
        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_SIGN_OUT, null, new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        listener.onSuccess();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    @Override
    public void fetchUserData(Context context, FetchUserDataListener listener) {
        Bundle out = new Bundle();

        /*
          Static Test User
         */
        out.putSerializable("access_token", "4aef534ab10309eaf7d594e505eaa44fa8f90f39");
        CustomUser user = new CustomUserImpl(
                "17",
                "ivan nicolas",
                "cruz",
                "ivannicolascruz01@gmail.com",
                "https://lh3.googleusercontent.com/a-/AFdZucrqkeo2TzMEzGftp6fD_ePkTsbY4QIUk6YoLQ-1=s96-c"
        );
        out.putSerializable("user", user);

        listener.onUserDataFetch(context, out);
    }
}
