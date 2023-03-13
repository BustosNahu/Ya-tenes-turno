package com.yatenesturno.activities.place_credits;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.yatenesturno.Constants;
import com.yatenesturno.Parser.ParserGeneric;
import com.yatenesturno.R;
import com.yatenesturno.activities.employee.client.PlaceClientCreditsFragment;
import com.yatenesturno.custom_views.NonSwipeableViewPager;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.serializers.BuilderObjectServiceInstance;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class PlaceClientCreditsActivity extends AppCompatActivity {

    /**
     * Argument variables keys
     */
    public static final String ARG_JOB = "job";
    public static final String ARG_PLACE = "place";

    /**
     * Instance variables
     */
    private Place place;
    private FragmentPlaceClients placeClientListFragment;
    private PlaceClientCreditsFragment placeClientCreditsFragment;
    private PlaceClientCreditsAdapter adapter;

    /**
     * UI references
     */
    private NonSwipeableViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_client_credits);

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        } else {
            recoverState(getIntent().getExtras());
        }

        initViews();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPagerPlaceClients);

        setUpNavBar();
        if (place != null) {
            setUpViewPager();
        } else {
            setupNoPlace();
        }
    }

    /**
     * When no place is owned display a message indicating so
     */
    private void setupNoPlace() {
        findViewById(R.id.noOwnedPlaces).setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.GONE);
    }

    /**
     * Style navbar
     */
    private void setUpNavBar() {
        if (place != null) {
            setTitle(place.getBusinessName());
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    /**
     * Verify at least one service instance using credits exists
     * and update UI accordingly
     *
     * @param result service instance list using credits
     */
    private void onServiceInstancesFetch(List<ServiceInstance> result) {
        placeClientCreditsFragment.setServiceInstancesWithCredits(result);
    }

    private void setUpViewPager() {
        if (placeClientCreditsFragment == null) {
            placeClientCreditsFragment = PlaceClientCreditsFragment.newInstance(place.getId());
            placeClientListFragment = FragmentPlaceClients.newInstance(place);
        }
        placeClientListFragment.setListener(client -> {
            placeClientCreditsFragment.setClient(client);
            showClientCreditsFragment(client);
        });

        viewPager.setOffscreenPageLimit(4);

        adapter = new PlaceClientCreditsAdapter(getSupportFragmentManager());

        adapter.addFragment(placeClientListFragment);
        adapter.addFragment(placeClientCreditsFragment);

        viewPager.setAdapter(adapter);

        findServicesWithCredits();
    }

    private void showClientCreditsFragment(CustomUser client) {
        setTitle(client.getName());
        viewPager.setCurrentItem(1);
    }

    @Override
    public void onBackPressed() {

        if (viewPager.getCurrentItem() == 1) {
            viewPager.setCurrentItem(0);
            setTitle(place.getBusinessName());
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Iterates through day schedules finding service Instances being provided using credits
     */
    private void findServicesWithCredits() {
        DatabaseDjangoRead.getInstance().GET(
                String.format(Constants.DJANGO_URL_FETCH_SI_WITH_CREDITS, place.getId()),
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        List<ServiceInstance> serviceInstanceList = new ParserGeneric<>(ServiceInstance.class, "data").parseByList(
                                response,
                                new BuilderObjectServiceInstance()
                        );
                        onServiceInstancesFetch(serviceInstanceList);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Toast.makeText(PlaceClientCreditsActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void recoverState(Bundle extras) {
        place = extras.getParcelable(ARG_PLACE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_PLACE, place);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public interface OnClientClickListener {
        void onClientClick(CustomUser client);
    }

    private static class PlaceClientCreditsAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList;

        public PlaceClientCreditsAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            fragmentList = new ArrayList<>();
        }

        public void addFragment(Fragment fragment) {
            fragmentList.add(fragment);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

}