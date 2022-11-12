package com.yatenesturno.activities.place_register;

import static android.icu.lang.UCharacter.IndicSyllabicCategory.NUMBER;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.yatenesturno.R;
import com.yatenesturno.activities.ObjectConfigurator;
import com.yatenesturno.activities.place_register.step_1.NewPlaceStep1;
import com.yatenesturno.functionality.PermissionUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NewPlaceLocationFragment extends ObjectConfigurator implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private int numberOfRequestsToMake = NUMBER;
    private boolean hasRequestFailed = false;

    private boolean permissionDenied = false;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private GoogleMap map;
    private Location lastKnownLocation;
    private LatLng selectedLatLng;

    private ImageButton finishBtn;
    private boolean isRunning;
    String address;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        return inflater.inflate(R.layout.new_place_b_fragment, container, false);
    }
    private void backToStep1Fragment() {
        getParentFragmentManager().setFragmentResult("dataFromMap",saveState());
        Fragment fragment = new NewPlaceStep1();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.new_place_fragment_main, fragment)
                .commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }




    private Bundle saveState() {
        Bundle bundle = new Bundle();

        if (selectedLatLng != null){
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
        finishBtn = requireView().findViewById(R.id.finish_location_btn);
        finishBtn.setOnClickListener(view -> {
//            if (address != null){
//
//            }
            saveState();
            backToStep1Fragment();
        });
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

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (map != null) {
                map.setMyLocationEnabled(true);
                getUserLocation();
            }
        } else {
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            moveMapToCenter();
                        }


                    });
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                lastKnownLocation = task.getResult();
                moveMap();
            }
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
        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION) && PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            enableMyLocation();
        } else {
            permissionDenied = true;
            moveMapToCenter();
        }
    }

    private void moveMapToCenter() {
        if (map != null){
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(-38.223757, -66.196825), 5));
        }
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

    @Override
    public Bundle getData() {
        Bundle bundle = new Bundle();

        bundle.putString("address", getSelectedPointString());

        return bundle;
    }

    public String getSelectedPointString() {
        return selectedLatLng.latitude + ":" + selectedLatLng.longitude;
    }

    @Override
    public boolean validateData() {
        if (selectedLatLng == null) {
            showSnackBar("Seleccione una ubicación en el mapa clickeando en este.");
            return false;
        }

        return true;
    }

    private void showSnackBar(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }
}