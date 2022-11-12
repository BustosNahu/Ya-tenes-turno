package com.yatenesturno.activities.job_edit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.yatenesturno.R;
import com.yatenesturno.custom_views.NonSwipeableViewPager;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.Service;
import com.yatenesturno.object_interfaces.ServiceInstance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServiceConfigActivity extends AppCompatActivity {

    private static final String FRAGMENT_SERVICE_CONFIGURE = "fragmentServiceConfigure";
    private static final String FRAGMENT_SERVICE_SELECTION = "fragmentServiceSelection";

    /**
     * Instance varaibles
     */
    private Job job;
    private Place place;

    /**
     * UI Reference
     */
    private NonSwipeableViewPager viewPager;
    private ServiceSelectionFragment serviceSelectionFragment;
    private ServiceConfigureFragment serviceConfigureFragment;
    private MenuItem removeServiceMenuItem;
    private ServiceConfigPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_config);

        if (savedInstanceState == null) {
            job = (Job) getIntent().getExtras().getSerializable("job");
            place = (Place) getIntent().getExtras().getSerializable("place");
        } else {
            recoverState(savedInstanceState);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Seleccioná un servicio");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        new Handler(Looper.myLooper()).postDelayed(this::initUI, 200);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void recoverState(Bundle bundle) {
        job = bundle.getParcelable("job");
        place = bundle.getParcelable("place");

        FragmentManager fragmentManager = getSupportFragmentManager();
        serviceConfigureFragment = (ServiceConfigureFragment) fragmentManager.getFragment(bundle, FRAGMENT_SERVICE_CONFIGURE);
        serviceSelectionFragment = (ServiceSelectionFragment) fragmentManager.getFragment(bundle, FRAGMENT_SERVICE_SELECTION);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.putFragment(outState, FRAGMENT_SERVICE_CONFIGURE, serviceConfigureFragment);
        fragmentManager.putFragment(outState, FRAGMENT_SERVICE_SELECTION, serviceSelectionFragment);
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable("job", job);
        bundle.putParcelable("place", place);
        bundle.putInt("view_pager_page", viewPager.getCurrentItem());

        return bundle;
    }

    private void initUI() {
        initViews();
        setUpViewPager();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPagerServiceConfig);
    }

    private void setUpViewPager() {

        if (serviceConfigureFragment == null) {
            serviceConfigureFragment = new ServiceConfigureFragment(place, job);
        }

        if (serviceSelectionFragment == null) {
            serviceSelectionFragment = new ServiceSelectionFragment(job);
        }

        if (adapter == null) {
            setFragmentsListeners();

            List<Fragment> fragments = new ArrayList<>();
            fragments.add(serviceSelectionFragment);
            fragments.add(serviceConfigureFragment);

            adapter = new ServiceConfigPagerAdapter(getSupportFragmentManager(), fragments);
            viewPager.setAdapter(adapter);
        }
    }

    private void setFragmentsListeners() {
        serviceConfigureFragment.setBtnConfirmListener(new ListenerConfirmConfig());
        serviceSelectionFragment.setServiceClickListener(new ListenerServiceClick());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete, menu);
        removeServiceMenuItem = menu.findItem(R.id.delete);
        removeServiceMenuItem.setVisible(false);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            returnOK();
        } else {
            showSelectServiceFragment();
        }
    }

    public void returnOK() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("job", job);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showSelectServiceFragment() {
        getSupportActionBar().setTitle("Seleccioná un servicio");
        removeServiceMenuItem.setVisible(false);
        viewPager.setCurrentItem(0);
    }

    private void showConfigureService(Service service) {
        getSupportActionBar().setTitle(service.getName());

        List<ServiceInstance> otherProvidedServices = removeServiceFromProvidedOnes(service);

        serviceConfigureFragment.setServiceToConfigure(
                service,
                otherProvidedServices
        );

        removeServiceMenuItem.setVisible(true);
        viewPager.setCurrentItem(1);
    }

    private List<ServiceInstance> removeServiceFromProvidedOnes(Service service) {
        List<ServiceInstance> providedServices = serviceSelectionFragment.getProvidedServiceInstances();

        Iterator<ServiceInstance> i = providedServices.iterator();
        while (i.hasNext()) {
            if (i.next().getService().equals(service)) {
                i.remove();
                break;
            }
        }

        return providedServices;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.delete:
                serviceConfigureFragment.removeService();
                break;
        }
        return true;
    }

    private static class ServiceConfigPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList;

        public ServiceConfigPagerAdapter(FragmentManager fm, List<Fragment> frags) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            fragmentList = frags;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    private class ListenerServiceClick implements ServiceSelectionFragment.ServiceClickListener {
        @Override
        public void onServiceViewClick(Service service) {
            showConfigureService(service);
        }
    }

    private class ListenerConfirmConfig implements ServiceConfigureFragment.ListenerConfirm {

        @Override
        public void onConfirmServiceConfiguration() {
            showSelectServiceFragment();
        }
    }
}