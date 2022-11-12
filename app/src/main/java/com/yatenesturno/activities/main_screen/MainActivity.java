package com.yatenesturno.activities.main_screen;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.activities.get_premium.SubscriptionsTemplatesActivity;
import com.yatenesturno.activities.help.HelpActivity;
import com.yatenesturno.activities.job_request.JobRequestActivity;
import com.yatenesturno.activities.place_credits.PaymentsActivity;
import com.yatenesturno.activities.place_register.NewPlaceActivity;
import com.yatenesturno.activities.place_view.AdminPlacesActivity;
import com.yatenesturno.activities.purchases.PurchasesActivity;
import com.yatenesturno.activities.settings.AboutActivity;
import com.yatenesturno.activities.settings.SettingsActivity;
import com.yatenesturno.activities.sign_in.ActivitySignIn;
import com.yatenesturno.activities.tutorial_screen.Screen;
import com.yatenesturno.activities.tutorial_screen.ScreenImpl;
import com.yatenesturno.activities.tutorial_screen.TutorialScreenImpl;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.database.DatabaseDjangoWrite;
import com.yatenesturno.database.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.functionality.MessageDisplay;
import com.yatenesturno.functionality.PlacePremiumManager;
import com.yatenesturno.functionality.StatsRetriever;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.user_auth.UserManagement;

import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final String FRAGMENT_MAIN_NAME = "fragment_main";

    private DrawerLayout drawerLayout;
    private FragmentMainViewPager fragmentMain;
    private Toolbar toolbar;

    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppThemeNoActionBar);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(getResources().getColor(R.color.black));

        if (savedInstanceState == null) {
            showLoadingOverlay();
            authenticate();
        } else {
            recoverState(savedInstanceState);
        }
    }

    private void recoverState(Bundle savedInstanceState) {
        fragmentMain = (FragmentMainViewPager) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_MAIN_NAME);

        if (fragmentMain == null) {
            initUI();
        }

        setUpToolbar();
        setUpDrawer();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (fragmentMain != null) {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_MAIN_NAME, fragmentMain);
        }
    }

    private void showLoadingOverlay() {
        new LoadingOverlay(findViewById(R.id.coordinatorOverlayMain)).show();
    }

    private void authenticate() {
        UserManagement.getInstance().authenticate(
                this,
                new ListenerAuthenticate());
    }

    private void  fetchMessages() {
        new Handler(Looper.myLooper()).postDelayed(() -> MessageDisplay.init(MainActivity.this), 200);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void startSignInActivity() {
        Intent intent = new Intent(this, ActivitySignIn.class);
        startActivity(intent);
        finish();
    }

    private void userIsAuthenticated() {
        removeLoadingOverlay();
        setUpToolbar();
        setUpDrawer();
        checkForUpdates();

        new Handler(Looper.myLooper()).postDelayed(() -> {
            refreshContent();
            fetchMessages();
            postStats();
        }, 200);
    }



    private void checkForUpdates() {
        //TODO("IMPLEMENT");
//        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
//
//        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
//
//        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
//            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
//                int priority = appUpdateInfo.updatePriority();
//
//                try {
//                    if (priority >= 4) {
//                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
//
//                            appUpdateManager.startUpdateFlowForResult(
//                                    appUpdateInfo,
//                                    AppUpdateType.IMMEDIATE,
//                                    this,
//                                    Constants.RC_UPDATE);
//
//                        }
//                    } else {
//                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
//                            appUpdateManager.registerListener(state -> {
//                                if (state.installStatus() == InstallStatus.DOWNLOADED) {
//                                    popupSnackbarForCompleteUpdate(appUpdateManager);
//                                }
//                            });
//                            appUpdateManager.startUpdateFlowForResult(
//                                    appUpdateInfo,
//                                    AppUpdateType.FLEXIBLE,
//                                    this,
//                                    Constants.RC_UPDATE);
//                        }
//                    }
//                } catch (IntentSender.SendIntentException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
    }

    private void popupSnackbarForCompleteUpdate(AppUpdateManager appUpdateManager) {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.root),
                        "La actualización ha terminado su descarga.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }



    private void postStats() {
        new Handler(Looper.myLooper()).postDelayed(() -> new StatsRetriever(MainActivity.this), 500);
    }



    private void removeLoadingOverlay() {
        findViewById(R.id.coordinatorOverlayMain).setVisibility(View.GONE);
    }

    private void setUpToolbar() {
        toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
    }

    private void setUpDrawer() {
        drawerLayout = findViewById(R.id.drawerMain);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(new NavigationItemSelectedListener());

        TextView textViewName = navigationView.getHeaderView(0)
                .findViewById(R.id.textViewName);

        setTitle("");


        if (UserManagement.getInstance().getUser().getName() != null){
            textViewName.setText(UserManagement.getInstance().getUser().getName());
        }
        getProfilePic(navigationView.getHeaderView(0).findViewById(R.id.ivProfilePic));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void getProfilePic(final CircleImageView circleImageView) {
        ImageLoaderRead loaderRead = new ImageLoaderReadImpl();
        loaderRead.getImage(this, UserManagement.getInstance().getUser(), new ImageLoaderRead.OnGetImageListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                circleImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure() {

            }
        });
    }

    public void refreshContent() {
        if (fragmentMain == null) {
            initUI();
        } else {
            fragmentMain.refresh();
        }
    }

    private void initUI() {
        if (isRunning) {
            fragmentMain = new FragmentMainViewPager();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_main_screen, fragmentMain, FRAGMENT_MAIN_NAME);
            transaction.commit();
        }
    }

    public void signOut() {
        startSignInActivity();
        UserManagement.getInstance().signOut(this, new SignOutListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.RC_EDIT_PLACE:
            case Constants.RC_JOB_REQUESTS:
            case Constants.RC_EDIT_JOB:

            case Constants.RC_NEW_ANONYMOUS_APP:
                if (resultCode == Activity.RESULT_OK) {
                    refreshContent();
                }
                break;
            case Constants.RC_NEW_PLACE:
                if (resultCode == Activity.RESULT_OK) {
                    refreshContent();
                    Log.d("returnOKFrom", "RC_NEW_PLACE");
                }
                break;
            case Constants.RC_UPDATE:
                if (resultCode != RESULT_OK) {
                    checkForUpdates();
                }
                break;
        }
    }

    private void checkCanCreateNewPlace() {
        ManagerPlace.getInstance().getOwnedPlaces(new ManagerPlace.OnPlaceListFetchListener() {
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
                    startNewPlaceActivity();
                } else {
                    if (placeList.size() > 0) {
                        GetPremiumActivity.showPremiumInfoFromActivity(MainActivity.this, null);
                    } else {
                        startNewPlaceActivity();
                    }
                }

            }

            @Override
            public void onFailure() {

            }
        });
    }

    protected void startNewPlaceActivity() {
        Intent intent = new Intent(this, NewPlaceActivity.class);
        startActivityForResult(intent, Constants.RC_NEW_PLACE);
    }

    protected void startJobRequestsActivity() {
        Intent intent = new Intent(this, JobRequestActivity.class);
        intent.putExtra("fromFirstShop", false);
        startActivityForResult(intent, Constants.RC_JOB_REQUESTS);
    }

    private void startAdminPlacesActivity() {
        Intent intent = new Intent(this, AdminPlacesActivity.class);
        startActivityForResult(intent, Constants.RC_EDIT_PLACE);
    }

    public void resetState() {
        DatabaseDjangoWrite.getInstance().invalidate();
        DatabaseDjangoRead.getInstance().invalidate();
        ManagerPlace.getInstance().invalidate();
    }

    private void startGetPremiumActivity() {
        Intent intent = new Intent(this, SubscriptionsTemplatesActivity.class);
        startActivityForResult(intent, Constants.RC_GET_PREMIUM);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, Constants.RC_SETTINGS);
    }

    private void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void startPurchasesActivity() {
        Intent intent = new Intent(this, PurchasesActivity.class);
        startActivity(intent);
    }

    private void startHelpActivity() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    private void startPaymentsActivity() {
        Intent intent = new Intent(this, PaymentsActivity.class);

        Bundle bundle = new Bundle();

        bundle.putParcelable("place", fragmentMain.getSelectedPlace());

        intent.putExtras(bundle);

        startActivity(intent);
    }

    private class SignOutListener implements UserManagement.UserManagementListener {

        @Override
        public void onSuccess() {
            resetState();
        }

        @Override
        public void onFailure() {

        }
    }

    private class NavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.nav_new_place:
                    checkCanCreateNewPlace();
                    break;
                case R.id.nav_job_request:
                    startJobRequestsActivity();
                    break;
                case R.id.nav_admin_places:
                    startAdminPlacesActivity();
                    break;
                case R.id.nav_sign_out:
                    signOut();
                    break;
                case R.id.nav_get_premium:
                    startGetPremiumActivity();
                    break;
                case R.id.nav_view_purchases:
                    startPurchasesActivity();
                    break;
                case R.id.nav_view_payments:
                    startPaymentsActivity();
                    break;
                case R.id.about:
                    startAboutActivity();
                    break;
                case R.id.nav_settings:
                    startSettingsActivity();
                    break;
                case R.id.help:
                    startHelpActivity();
                    break;
            }

            drawerLayout.closeDrawer(GravityCompat.START);

            return true;
        }
    }

    private class ListenerAuthenticate implements UserManagement.UserManagementAuthenticateListener {
        @Override
        public void userNotAuthenticated() {
            startSignInActivity();
        }

        @Override
        public void userAuthenticated() {
            userIsAuthenticated();
        }
    }
}
