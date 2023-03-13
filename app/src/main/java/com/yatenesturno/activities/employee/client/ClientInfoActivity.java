package com.yatenesturno.activities.employee.client;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingButton;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.custom_views.NonSwipeableViewPager;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.functionality.DayScheduleManager;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.utils.CustomAlertDialogBuilder;
import com.yatenesturno.utils.TaskRunner;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class ClientInfoActivity extends AppCompatActivity {

    /**
     * Parameter arguments
     */
    public static final String PLACE_ID = "placeId";
    public static final String JOB_ID = "jobId";
    public static final String CLIENT = "client";
    public static final String HAS_EDIT_RIGHTS = "hasEditRights";

    /**
     * Instance variables
     */
    private CustomUser client;
    private boolean hasEditRights;
    private String jobId;
    private String placeId;

    /**
     * UI references
     */
    private LoadingOverlay loadingOverlay;
    private NonSwipeableViewPager viewPager;

    /**
     * References
     */
    private ClientInfoFragment clientInfoFragment;
    private PlaceClientCreditsFragment placeClientCreditsFragment;
    private ClientAppointmentsFragment clientAppointmentsFragment;
    private ClientInfoPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_info);

        setupActionBar();

        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));
        viewPager = findViewById(R.id.viewPager);

        if (savedInstanceState != null) {
            client = savedInstanceState.getParcelable(CLIENT);
            jobId = savedInstanceState.getString(JOB_ID);
            placeId = savedInstanceState.getString(PLACE_ID);
            hasEditRights = savedInstanceState.getBoolean(HAS_EDIT_RIGHTS);
        } else {
            client = (CustomUser) getIntent().getExtras().getSerializable(CLIENT);
            jobId = getIntent().getExtras().getString(JOB_ID);
            placeId = getIntent().getExtras().getString(PLACE_ID);
            hasEditRights = getIntent().getExtras().getBoolean(HAS_EDIT_RIGHTS);
        }

        initFragments();
    }

    private void initFragments() {

        if (clientInfoFragment == null) {
            clientInfoFragment = ClientInfoFragment.newInstance(jobId, client);
//            clientAppointmentsFragment = ClientAppointmentsFragment.newInstance(jobId, client);
        }
        initViewPager();
    }

    private void initViewPager() {
        viewPager.setOffscreenPageLimit(4);

        adapter = new ClientInfoPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(clientInfoFragment);
//        adapter.addFragment(clientAppointmentsFragment);

        viewPager.setAdapter(adapter);
        getDaySchedules();
    }

    /**
     * get day schedules to find service instances using credits
     */
    private void getDaySchedules() {
        DayScheduleManager.getInstance().getDaySchedules(
                jobId,
                this::findServicesWithCredits
        );
    }

    /**
     * Iterates through day schedules finding service Instances being provided using credits
     *
     * @param dayScheduleList day schedules for Job
     */
    private void findServicesWithCredits(List<DaySchedule> dayScheduleList) {
        new TaskRunner().executeAsync(
                () -> {
                    List<ServiceInstance> serviceInstancesWithCredits = new ArrayList<>();

                    Map<String, Boolean> visitedServices = new HashMap<>();

                    for (DaySchedule daySchedule : dayScheduleList) {
                        List<ServiceInstance> serviceInstanceList = daySchedule.getServiceInstances();

                        for (ServiceInstance serviceInstance : serviceInstanceList) {
                            if (!visitedServices.containsKey(serviceInstance.getId())) {
                                if (serviceInstance.isCredits()) {
                                    serviceInstancesWithCredits.add(serviceInstance);
                                }
                                visitedServices.put(serviceInstance.getId(), true);
                            }
                        }
                    }

                    return serviceInstancesWithCredits;
                },
                this::onServiceInstancesFetch
        );
    }

    /**
     * Verify at least one service instance using credits exists
     * and update UI accordingly
     *
     * @param result service instance list using credits
     */
    private void onServiceInstancesFetch(List<ServiceInstance> result) {
        if (result.size() > 0) {
            clientInfoFragment.showBtnCredits();
            if (placeClientCreditsFragment == null) {
                placeClientCreditsFragment = PlaceClientCreditsFragment.newInstance(placeId, jobId, client, hasEditRights, result);
            }

            adapter.addFragment(placeClientCreditsFragment);
            adapter.notifyDataSetChanged();
        }
    }

    public void showClientInfoFragment() {
        viewPager.setCurrentItem(0);
        setTitle("");
    }

    public void showJobCreditsFragment() {
        viewPager.setCurrentItem(1);
        setTitle(client.getName());
    }

    public void showAppointmentsFragment() {
        viewPager.setCurrentItem(2);
        setTitle(client.getName());
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() > 0) {
            showClientInfoFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!hasEditRights) {
            return true;
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_client_info, menu);

        MenuItem removeServiceMenuItem = menu.findItem(R.id.administrate);
        removeServiceMenuItem.setOnMenuItemClickListener(menuItem -> {
            showAdministrateDialog();
            return false;
        });

        return true;
    }

    private void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setTitle("");
    }

    private void showAdministrateDialog() {
        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this);

        View view = getLayoutInflater().inflate(R.layout.dialog_administrate_client, null, false);
        LoadingButton btnDeleteClient = view.findViewById(R.id.btnDeleteClient);

        btnDeleteClient.setOnClickListener(view1 -> showConfirmation(btnDeleteClient, builder));

        builder.setTitle(getString(R.string.administrate));
        builder.setView(view);
        builder.show();
    }

    private void showConfirmation(LoadingButton btnDeleteClient, CustomAlertDialogBuilder builder) {
        CustomAlertDialogBuilder builderConfirmation = new CustomAlertDialogBuilder(this);

        builderConfirmation.setTitle("¿Confirma la eliminación del cliente?");
        builderConfirmation.setMessage("Esta acción es irreversible.\nSi continúa, toda información del cliente vinculada con este empleado será eliminada");
        builderConfirmation.setPositiveButton(R.string.remove, (dialogInterface, i) -> {
            deleteClient(btnDeleteClient, builder);
            dialogInterface.dismiss();
        });

        builderConfirmation.show();
    }

    private void deleteClient(LoadingButton btnDeleteClient, CustomAlertDialogBuilder builder) {
        DatabaseDjangoWrite.getInstance().POST(
                String.format(Constants.DJANGO_URL_REMOVE_CLIENT, jobId, client.getId()),
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        btnDeleteClient.hideLoading();
                        builder.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        btnDeleteClient.hideLoading();
                    }
                }
        );

    }

    public void showLoadingOverlay() {
        loadingOverlay.show();
    }

    public void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(JOB_ID, jobId);
        outState.putString(PLACE_ID, placeId);
        outState.putParcelable(CLIENT, client);
        outState.putBoolean(HAS_EDIT_RIGHTS, hasEditRights);
    }

    private static class ClientInfoPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList;

        public ClientInfoPagerAdapter(FragmentManager fm) {
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