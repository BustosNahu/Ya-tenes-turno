package com.yatenesturno.user_auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.yatenesturno.Constants;
import com.yatenesturno.Flags;
import com.yatenesturno.database.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.serializers.BuilderObjectCustomUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class AuthenticatorGoogle implements Authenticator {

    private static final String NAME_ID = "AUTHENTICATOR_GOOGLE";
    private static final String CLIENT_ID_WEB_APP_DEBUG = "237762704075-gm5tdur3e20cn87urghkhjo5huv8kh82.apps.googleusercontent.com";
    private static final String CLIENT_ID_WEB_APP_RELEASE = "237762704075-er7i01or1vgq94hj59jn5kttf0v2v1du.apps.googleusercontent.com";

    private GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount account;

    private SignInListener listener;

    @Override
    public void init(Context context) {

        String web_app_id;
        if (Flags.DEBUG) {
            web_app_id = CLIENT_ID_WEB_APP_DEBUG;
        } else {
            web_app_id = CLIENT_ID_WEB_APP_RELEASE;
        }

        //TODO VER LA KEY EN FIREBASE
        //F3:AC:7B:6B:74:3D:EC:D8:E0:22:E2:ED:83:C8:F9:A9:0A:B9:4E:95 Añadir a firebase
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("237762704075-er7i01or1vgq94hj59jn5kttf0v2v1du.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions);
        account = GoogleSignIn.getLastSignedInAccount(context);
    }

    @Override
    public void signIn(Activity activity, SignInListener listener) {
        this.listener = listener;
        activity.startActivityForResult(googleSignInClient.getSignInIntent(), Constants.RC_SIGN_IN_ACTIVITY);
    }

    @Override
    public String getNameId() {
        return NAME_ID;
    }

    @Override
    public void onActivityResult(Intent data) {
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            account = task.getResult(ApiException.class);

            listener.onSuccess();
        } catch (ApiException e) {
            listener.onFailure();
            e.printStackTrace();
        }

    }

    @Override
    public void signOut(final Activity activity, final SignOutListener listener) {
        postSignOutToServer(new SignOutListener() {
            @Override
            public void onSuccess() {
                googleSignInClient.signOut().addOnCompleteListener(
                        activity,
                        task -> listener.onSuccess());
            }

            @Override
            public void onFailure() {

            }
        });
    }

    public void postSignOutToServer(final SignOutListener listener) {
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
    public void fetchUserData(final Context context, final FetchUserDataListener listener) {
        if (account == null) {
            account = GoogleSignIn.getLastSignedInAccount(context);
        }

        String idToken = account.getIdToken();
        Map<String, String> body = new HashMap<>();
        body.put("id_token", idToken);

        DatabaseDjangoWrite.getInstance().POST(Constants.DJANGO_URL_CONVERT_TOKEN, body, new DatabaseCallback() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Bundle userData = new Bundle();
                    userData.putString("access_token", response.getString("access_token"));
                    CustomUser user = new BuilderObjectCustomUser().build(response.getJSONObject("user"));
                    userData.putSerializable("user", user);

                    listener.onUserDataFetch(context, userData);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("exceptionFromPost", e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFailure(context);
                Log.d("exceptionFromFailureDjango", statusCode + "" + headers + "" + responseString);
            }
        });
    }

}