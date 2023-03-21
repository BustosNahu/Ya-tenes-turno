package com.yatenesturno.activities.get_premium;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.functionality.PlacePremiumManager;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.user_auth.UserManagement;

public class GetPremiumActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_SUB_OPTIONS = 1;
    private LoadingOverlay loadingOverlay;
    private Place place;

    public static void showPremiumInfoFromActivity(Activity activity, String placeId) {
        Place place = ManagerPlace.getInstance().getPlaceById(placeId);

        Intent intent = new Intent(activity, GetPremiumActivity.class);

        Bundle bundle = new Bundle();
        if (place != null) {
            bundle.putParcelable("place", place);
        }
        intent.putExtras(bundle);

        activity.startActivityForResult(intent, Constants.RC_GET_PREMIUM);
    }


    /**
     * Method to validate if the user is premium or not, if not it show you get premium screen
     * @param activity
     * @param placeId
     * @param userId
     * @return
     */
    public static boolean hasPremiumInPlaceOrShowScreen(Activity activity, String placeId, String userId) {
        if (!PlacePremiumManager.getInstance().getIsPremium(placeId, userId)) {
            showPremiumInfoFromActivity(activity, placeId);
            return false;
        }
        return true;
    }

    public static boolean hasPremiumInPlace(String placeId, String userId) {
        return PlacePremiumManager.getInstance().getIsPremium(placeId, userId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_premium);

        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.black));
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");

        place = getIntent().getExtras().getParcelable("place");

        new Handler(Looper.myLooper()).postDelayed(() -> initUI(), 100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_close, menu);

        MenuItem removeServiceMenuItem = menu.findItem(R.id.close);
        removeServiceMenuItem.setOnMenuItemClickListener(menuItem -> {
            finish();
            return false;
        });

        return true;
    }

    private void initUI() {
        findViewById(R.id.btnGetPremium).setOnClickListener(view -> startSubsOptionActivity());

        if (place != null) {
            ((TextView) findViewById(R.id.textView2)).setText(
                    getString(R.string.premium_invite_with_place,
                            UserManagement.getInstance().getUser().getGivenName(),
                            place.getBusinessName()
                    ));
        } else {
            ((TextView) findViewById(R.id.textView2)).setText(
                    getString(R.string.premium_invite_without_place,
                            UserManagement.getInstance().getUser().getGivenName()
                    ));
        }

        loadingOverlay = new LoadingOverlay(findViewById(R.id.coordinator));
    }

    private void startSubsOptionActivity() {
        loadingOverlay.show();
        Intent intent = new Intent(this, SubscriptionsTemplatesActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SUB_OPTIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SUB_OPTIONS && resultCode == RESULT_OK) {
            checkPremium();
        } else {
            hideLoadingOverlay();
        }
    }

    private void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    private void checkPremium() {
        PlacePremiumManager.getInstance().refresh(() -> {
            setResult(RESULT_OK);
            finish();
        });
    }
}