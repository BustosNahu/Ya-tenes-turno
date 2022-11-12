package com.yatenesturno.activities.job_edit.emergency_location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.yatenesturno.R;
import com.yatenesturno.functionality.PermissionUtils;
import com.yatenesturno.functionality.emergency.EmergencyLocation;
import com.yatenesturno.functionality.emergency.EmergencyLocationHandler;

public class NewEmergencyLocationFragment extends BottomSheetDialogFragment implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private final OnCreatedListener onCreatedListener;
    private boolean permissionDenied = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean isRunning;
    private GoogleMap map;
    private Location lastKnownLocation;
    private LatLng selectedLatLng;
    private AppCompatButton btnConfirm;
    private AppCompatEditText editTextName;
    private TextInputLayout textInputLayout;
    private final String jobId;

    public NewEmergencyLocationFragment(String jobId, OnCreatedListener onCreatedListener) {
        this.onCreatedListener = onCreatedListener;
        this.jobId = jobId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        return inflater.inflate(R.layout.fragment_new_emergency_location, container);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();

        if (selectedLatLng != null) {
            bundle.putParcelable("selectedLatLng", selectedLatLng);
        }

        if (lastKnownLocation != null) {
            bundle.putParcelable("lastKnownLocation", lastKnownLocation);
        }

        return bundle;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        }

        initUI();
    }

    private void initUI() {
        editTextName = getView().findViewById(R.id.editTextName);
        textInputLayout = getView().findViewById(R.id.textInputName);
        btnConfirm = getView().findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(view -> onConfirm());
    }

    private void onConfirm() {
        if (validate()) {
            EmergencyLocation emergencyLocation = new EmergencyLocation();
            emergencyLocation.setLat((float) selectedLatLng.latitude);
            emergencyLocation.setLon((float) selectedLatLng.longitude);
            emergencyLocation.setName(editTextName.getText().toString());
            EmergencyLocationHandler.getInstance().addEmergencyLocation(
                    jobId,
                    emergencyLocation,
                    new EmergencyLocationHandler.NewEmergencyLocationListener() {
                        @Override
                        public void onSuccess(EmergencyLocation location) {
                            onCreatedListener.onCreated();
                        }

                        @Override
                        public void onError() {

                        }
                    }
            );
            this.dismiss();
        }
    }

    private boolean validate() {
        if (TextUtils.isEmpty(editTextName.getText())) {
            textInputLayout.setError("Seleccione un nombre para la ubicación");
            return false;
        } else {
            textInputLayout.setError(null);
        }

        if (selectedLatLng == null) {
            Toast.makeText(getContext(), "Seleccione la ubicación", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void recoverState(Bundle bundle) {

        if (bundle.containsKey("selectedLatLng")) {
            selectedLatLng = bundle.getParcelable("selectedLatLng");

            if (map != null) {
                onSelectedLatLngUpdated();
            }
        }

        if (bundle.containsKey("lastKnownLocation")) {
            lastKnownLocation = bundle.getParcelable("lastKnownLocation");

            if (map != null) {
                moveMap();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setMapClickListener();
        if (isRunning) {
            if (selectedLatLng != null) {
                onSelectedLatLngUpdated();
            }
            enableMyLocation();
        }
    }

    public void setMapClickListener() {
        map.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            onSelectedLatLngUpdated();
        });
    }

    private void onSelectedLatLngUpdated() {
        map.clear();
        map.addMarker(new MarkerOptions().position(selectedLatLng));
        moveMapToSelectedPoint();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (map != null) {
                map.setMyLocationEnabled(true);
                getUserLocation();
            }
        } else {
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, dialog -> moveMapToCenter());
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(getActivity(), task -> {
            lastKnownLocation = task.getResult();
            moveMap();
        });
    }

    private void moveMap() {
        if (hasSelectedAPoint()) {
            moveMapToSelectedPoint();
        } else if (hasLocation()) {
            moveMapToUserLocation();
        } else {
            moveMapToCenter();
        }
    }

    private void moveMapToSelectedPoint() {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(selectedLatLng.latitude,
                        selectedLatLng.longitude), 16));
    }

    private boolean hasLocation() {
        return lastKnownLocation != null;
    }

    private boolean hasSelectedAPoint() {
        return selectedLatLng != null;
    }

    public void moveMapToUserLocation() {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude()), 16));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else {
            permissionDenied = true;
            moveMapToCenter();
        }
    }

    private void moveMapToCenter() {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(-38.223757, -66.196825), 5));
    }

    @Override
    public void onResume() {
        super.onResume();
        isRunning = true;
        if (permissionDenied) {
            permissionDenied = false;
        } else {

            if (map != null) {
                moveMap();
            }

            enableMyLocation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
    }

    public interface OnCreatedListener {
        void onCreated();
    }
}