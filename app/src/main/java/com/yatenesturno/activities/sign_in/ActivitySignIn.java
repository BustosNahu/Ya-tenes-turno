package com.yatenesturno.activities.sign_in;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.first_shop.FirstShop;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.activities.main_screen.MainActivity;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.database.DatabaseDjangoWrite;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.functionality.PlacePremiumManager;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.user_auth.Authenticator;
import com.yatenesturno.user_auth.AuthenticatorGoogle;
import com.yatenesturno.user_auth.MockAuthenticator;
import com.yatenesturno.user_auth.UserManagement;

import java.util.List;
import java.util.Objects;

public class ActivitySignIn extends AppCompatActivity implements View.OnClickListener {


    /**
     * UI Ref
     */
    private LoadingOverlay loadingOverlay;
    private boolean passwordVisible;
     ConstraintLayout constraintLayout;
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_activity);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(R.color.black));
        constraintLayout = findViewById(R.id.login_constraint_layout);
        constraintLayout.setVisibility(View.GONE);
        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));
        LottieAnimationView lottieSplashAnimation = findViewById(R.id.login_animation);
        progressBar = findViewById(R.id.pb_signIn);
        lottieSplashAnimation.animate();

        new Handler().postDelayed(() -> {
            initUI();
            lottieSplashAnimation.pauseAnimation();
            lottieSplashAnimation.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);
        }, 2000);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initUI() {
        findViewById(R.id.google_sign_in_button).setOnClickListener(this);

        AppCompatButton btnSignIn = findViewById(R.id.login_btn);
        TextInputEditText inputEmail = findViewById(R.id.tv_username);
        TextInputEditText inputPassword = findViewById(R.id.tv_userpassword);

        btnSignIn.setOnClickListener(view1 -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(inputEmail.getText()).toString())) {
                inputEmail.setError("");
                hideLoading();
                return;
            } else {
                inputEmail.setError(null);
            }

            if (TextUtils.isEmpty(Objects.requireNonNull(inputPassword.getText()).toString())) {
                inputPassword.setError("");
                hideLoading();
                return;
            } else {
                inputPassword.setError(null);
            }

            signInWithEmailAndPassword(btnSignIn, inputEmail, inputPassword);
        });

        inputPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int right = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= inputPassword.getRight()-inputPassword.getCompoundDrawables()[right].getBounds().width() - 50) {
                        int selection = inputPassword.getSelectionEnd();
                        if (passwordVisible){
                            inputPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.password_icon, 0, R.drawable.dont_show_password, 0);
                            inputPassword.setTransformationMethod(null);
                            passwordVisible = false;
                        }else{
                            inputPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.password_icon, 0, R.drawable.show_password, 0);
                            inputPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            passwordVisible = true;
                        }
                        inputPassword.setSelection(selection);
                        return true;
                    }
                }
                return false;
            }
        });

    }

    /**
     * mock sign in
     *
     * @param btn      loading btn
     * @param email    email
     * @param password pwd
     */
    private void signInWithEmailAndPassword(AppCompatButton btn, TextInputEditText email, TextInputEditText password) {
        showLoading();
//        if (!verifyCredentials(email.getText().toString(), password.getText().toString())) {
//            hideLoading();
//            email.setError("Error");
//            password.setError("Error");
//            return;
//        }
        email.setError(null);
        password.setError(null);

        Authenticator authenticator = new MockAuthenticator();
        authenticator.init(this);
        UserManagement.getInstance().setAuthenticatorInUse(authenticator);
        UserManagement.getInstance().signIn(this, new UserManagement.UserManagementListener() {
            @Override
            public void onSuccess() {
                checkCanCreateNewPlace();
                DatabaseDjangoWrite.getInstance().invalidate();
                DatabaseDjangoRead.getInstance().invalidate();
                hideLoading();
            }

            @Override
            public void onFailure() {
                hideLoading();
            }
        });
    }

    /**
//     * Mock auth
//     *
//     * @param email    email
//     * @param password pwd
//     * @return valid mock creds
//     */
////    private boolean verifyCredentials(String email, String password) {
////        return email.equals("a") && password.equals("a");
////    }

    private Boolean isFirstShop(){
        sharedPreferences = getSharedPreferences("firstShop", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("firstShop", true);
    }
    private void checkCanCreateNewPlace() {
        ManagerPlace.getInstance().getPlaces(new ManagerPlace.OnPlaceListFetchListener() {
            @Override
            public void onFetch(List<Place> placeList) {
                boolean hasAtLeastOnePremium = false;
                for (Place place : placeList) {
                    if (PlacePremiumManager.getInstance().getIsPremium(place.getId(), UserManagement.getInstance().getUser().getId())) {
                        hasAtLeastOnePremium = true;
                        break;
                    }
                }
                if (hasAtLeastOnePremium) {
                    Intent intent = new Intent(ActivitySignIn.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    if (placeList.size() > 0) {
                        GetPremiumActivity.showPremiumInfoFromActivity(ActivitySignIn.this, null);
                        Intent intent = new Intent(ActivitySignIn.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(ActivitySignIn.this, FirstShop.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure() {

            }
        });
    }



    @Override
    public void onClick(View v) {
        loadingOverlay.show();
        if (v.getId() == R.id.google_sign_in_button) {
            onGoogleSignInClickListener();
        }
    }

    public void onGoogleSignInClickListener() {
        Authenticator authenticatorGoogle = new AuthenticatorGoogle();
        authenticatorGoogle.init(this);

        UserManagement.getInstance().setAuthenticatorInUse(authenticatorGoogle);
        UserManagement.getInstance().signIn(this, new ListenerAuthenticatorGoogle());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RC_SIGN_IN_ACTIVITY) {
            UserManagement.getInstance().onIntentCallback(data);
        }
    }

    private class ListenerAuthenticatorGoogle implements UserManagement.UserManagementListener {

        @Override
        public void onSuccess() {
            checkCanCreateNewPlace();
            DatabaseDjangoWrite.getInstance().invalidate();
            DatabaseDjangoRead.getInstance().invalidate();
        }

        @Override
        public void onFailure() {

            Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
            loadingOverlay.hide();
        }
    }



    private void hideLoading(){
        progressBar.setVisibility(View.GONE);
        constraintLayout.setVisibility(View.VISIBLE);
    }
    private void showLoading(){
        constraintLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

    }
}