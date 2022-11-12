package com.yatenesturno.user_auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * User Registration abstraction
 */
public interface Authenticator {

    void init(Context context);


    String getNameId();

    void onActivityResult(Intent data);

    void signIn(Activity activity, SignInListener listener);

    void signOut(Activity activity, SignOutListener listener);

    void fetchUserData(Context context, FetchUserDataListener listener);

    interface FetchUserDataListener {
        void onUserDataFetch(Context context, Bundle accessToken);

        void onFailure(Context context);
    }

    interface SignInListener {
        void onSuccess();

        void onFailure();
    }

    interface SignOutListener {
        void onSuccess();

        void onFailure();
    }
}