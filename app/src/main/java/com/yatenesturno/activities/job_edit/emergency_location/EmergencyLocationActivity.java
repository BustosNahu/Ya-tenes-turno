package com.yatenesturno.activities.job_edit.emergency_location;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.R;
import com.yatenesturno.functionality.emergency.EmergencyLocation;
import com.yatenesturno.functionality.emergency.EmergencyLocationHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.davidea.flexibleadapter.FlexibleAdapter;

public class EmergencyLocationActivity extends AppCompatActivity {

    private EmergencyLocationHandler emergencyLocationHandler;
    private String jobId;
    private RecyclerView recyclerViewEmergencyLocations;
    private FlexibleAdapter<EmergencyLocationView> adapter;
    private List<EmergencyLocationView> emergencyLocationViewsList;
    private NewEmergencyLocationFragment newEmergencyLocationFragment;
    private EmergencyLocation selectedLocation;
    private AppCompatButton btnConfirmSelection, btnNewEmergencyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_location);

        setupToolbar();
        jobId = getIntent().getExtras().getString("jobId");

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        }

        emergencyLocationHandler = EmergencyLocationHandler.getInstance();
        newEmergencyLocationFragment = new NewEmergencyLocationFragment(jobId, this::onNewLocationCreated);

        initUI();
    }

    private void onNewLocationCreated() {
        initRecyclerView();
        hideCreatingNewLocation();
    }

    private void initUI() {
        initRecyclerView();
        initNewLocation();


        btnConfirmSelection = findViewById(R.id.btnConfirmSelection);
        btnConfirmSelection.setOnClickListener(view -> {
            Intent intent = getResultIntent();
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private Intent getResultIntent() {
        Intent intent = new Intent();

        Bundle bundle = new Bundle();
        bundle.putParcelable("location", selectedLocation);
        intent.putExtras(bundle);

        return intent;
    }

    private void initNewLocation() {
        btnNewEmergencyLocation = findViewById(R.id.btnNewEmergencyLocation);
        btnNewEmergencyLocation.setOnClickListener(view -> {
            showCreatingNewLocation();
            newEmergencyLocationFragment.show(getSupportFragmentManager(), "newLocationFrag");
        });
    }

    private void initRecyclerView() {
        recyclerViewEmergencyLocations = findViewById(R.id.recyclerViewEmergencyLocations);
        recyclerViewEmergencyLocations.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        emergencyLocationHandler.getEmergencyLocations(jobId, new EmergencyLocationHandler.GetEmergencyLocationListener() {
            @Override
            public void onSuccess(List<EmergencyLocation> locations) {
                onGetLocations(locations);
            }

            @Override
            public void onError() {

            }
        });
    }

    private void onGetLocations(List<EmergencyLocation> locations) {
        emergencyLocationViewsList = new ArrayList<>();
        EmergencyLocationView.LocationViewListener listener = new LocationViewListenerImpl();
        for (EmergencyLocation location : locations) {
            emergencyLocationViewsList.add(new EmergencyLocationView(location, listener));
        }

        adapter = new FlexibleAdapter<>(emergencyLocationViewsList);
        recyclerViewEmergencyLocations.setAdapter(adapter);
    }

    private void recoverState(Bundle bundle) {
        this.jobId = bundle.getString("jobId");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("jobId", jobId);
    }

    private void setupToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Seleccioná una ubicación");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private void updateSelected() {
        for (EmergencyLocationView v : emergencyLocationViewsList) {
            v.setSelected(v.getEmergencyLocation().equals(selectedLocation));
        }

        adapter.notifyDataSetChanged();
    }

    private void showCreatingNewLocation() {
        btnNewEmergencyLocation.setEnabled(false);
    }

    private void hideCreatingNewLocation() {
        btnNewEmergencyLocation.setEnabled(true);
    }

    private class LocationViewListenerImpl implements EmergencyLocationView.LocationViewListener {

        @Override
        public void onViewLocationClick(EmergencyLocation location) {
            String uri = String.format(
                    Locale.ENGLISH,
                    "http://maps.google.com/maps?q=loc:%f,%f(%s)",
                    location.getLat(),
                    location.getLon(),
                    location.getName()
            );
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        }

        @Override
        public void onDeleteLocationClick(EmergencyLocation location) {
            emergencyLocationHandler.removeEmergencyLocation(jobId, location, new EmergencyLocationHandler.RemoveEmergencyLocationListener() {
                @Override
                public void onSuccess() {
                    if (location.equals(selectedLocation)) {
                        selectedLocation = null;
                        btnConfirmSelection.setVisibility(View.GONE);
                    }
                    initRecyclerView();
                }

                @Override
                public void onError() {

                }
            });
        }

        @Override
        public void onSelected(EmergencyLocation location) {
            selectedLocation = location;
            updateSelected();
            btnConfirmSelection.setVisibility(View.VISIBLE);
        }
    }
}