package com.yatenesturno.activities.job_edit;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.emergency_location.EmergencyLocationActivity;
import com.yatenesturno.custom_views.ServiceSelectionView;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.database.DatabaseDjangoWrite;
import com.yatenesturno.functionality.emergency.EmergencyLocation;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.utils.CalendarUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


public class EmergencySetupFragment extends Fragment {

    private ActivityResultLauncher<Intent> selectLocationLauncher;
    private Job job;
    private Place place;
    private CardView btnStartEmergency;
    private CardView btnSelectStart, btnSelectEnd, btnSelectLocation;
    private Calendar start, end;
    private EmergencyLocation selectedLocation;
    private String locationActive, startActive, endActive;
    private AppCompatImageView btnSelectTimeImg, btnSelectLocationImg;
    private ServiceSelectionView serviceSelectionView;

    public EmergencySetupFragment(Place place, Job job) {
        this.place = place;
        this.job = job;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("job", job);
        outState.putParcelable("place", place);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emergency_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        }

        init();
        initUI();
    }

    private void recoverState(Bundle savedInstanceState) {
        this.job = savedInstanceState.getParcelable("job");
        this.place = savedInstanceState.getParcelable("place");
    }

    private void init() {
        selectLocationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bundle data = result.getData().getExtras();
                        onLocationSelected(data.getParcelable("location"));
                    }
                });

        initTimes();
    }

    private void initTimes() {
        start = Calendar.getInstance();
        start.add(Calendar.MINUTE, 10);
        if (start.get(Calendar.HOUR_OF_DAY) == 0) {
            start.set(Calendar.HOUR_OF_DAY, 23);
            start.set(Calendar.MINUTE, 55);
        }

        end = Calendar.getInstance();
        end.add(Calendar.MINUTE, 70);
        if (end.get(Calendar.HOUR_OF_DAY) == 0) {
            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
        }
    }

    private void initUI() {
        btnStartEmergency = getView().findViewById(R.id.btnStartEmergency);
        btnSelectStart = getView().findViewById(R.id.btnSelectStart);
        btnSelectEnd = getView().findViewById(R.id.btnSelectEnd);
        btnSelectLocation = getView().findViewById(R.id.btnSelectLocation);
        btnSelectLocationImg = getView().findViewById(R.id.btnSelectLocationImg);
        btnSelectTimeImg = getView().findViewById(R.id.btnSelectTimeImg);
        serviceSelectionView = getView().findViewById(R.id.serviceSelectionView);

        btnSelectStart.setOnClickListener(view -> selectStart());
        btnSelectEnd.setOnClickListener(view -> selectEnd());
        btnSelectLocation.setOnClickListener(view -> selectLocation());

        updateTimeLabels();
        initServiceSelection();
        fetchEmergencyStatus();
    }

    private void fetchEmergencyStatus() {
        showLoading();
        DatabaseDjangoRead.getInstance().GET(
                String.format(Constants.DJANGO_URL_EMERGENCY, place.getId(), job.getId()),
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        onFetchStatus(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );
    }

    private void onFetchStatus(JSONObject response) {
        try {
            if (response.getBoolean("active")) {
                if (response.has("location")) {
                    this.locationActive = response.getString("location");
                }
                this.startActive = response.getString("start");
                this.endActive = response.getString("end");
                showCurrentlyActive();
            } else {
                showCurrentlyInactive();
            }
        } catch (JSONException ignored) {

        }
    }

    private void showLoading() {
        getView().findViewById(R.id.progressBarEmergency).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.containerEmergency).setVisibility(View.GONE);
    }

    private void hideLoading() {
        getView().findViewById(R.id.progressBarEmergency).setVisibility(View.GONE);
        getView().findViewById(R.id.containerEmergency).setVisibility(View.VISIBLE);
    }

    private void showCurrentlyActive() {
        hideLoading();

        if (locationActive == null) {
            ((TextView) btnSelectLocation.getChildAt(0)).setText("Sin lugar seleccionado");
        } else {
            ((TextView) btnSelectLocation.getChildAt(0)).setText(locationActive);
        }
        ((TextView) btnSelectLocation.getChildAt(0)).setTextColor(getResources().getColor(R.color.darker_grey, null));

        btnSelectLocation.setEnabled(false);
        btnSelectStart.setEnabled(false);
        btnSelectEnd.setEnabled(false);
        ((TextView) btnSelectStart.getChildAt(0)).setText(startActive);
        ((TextView) btnSelectStart.getChildAt(0)).setTextColor(getResources().getColor(R.color.darker_grey, null));
        ((TextView) btnSelectEnd.getChildAt(0)).setText(endActive);
        ((TextView) btnSelectEnd.getChildAt(0)).setTextColor(getResources().getColor(R.color.darker_grey, null));

        btnSelectLocationImg.setImageDrawable(getContext().getDrawable(R.drawable.ic_baseline_location_on_24_grey));
        btnSelectTimeImg.setImageDrawable(getContext().getDrawable(R.drawable.ic_baseline_access_time_24_grey));

        serviceSelectionView.setVisibility(View.GONE);
        getView().findViewById(R.id.labelServices).setVisibility(View.GONE);

        ((TextView) btnStartEmergency.findViewById(R.id.turnOnText)).setText("Parar");
        ((TextView) btnStartEmergency.findViewById(R.id.turnOnText)).setTextColor(getResources().getColor(R.color.green, null));
        ((ImageView) getView().findViewById(R.id.turnOnImage)).setImageDrawable(getResources().getDrawable(R.drawable.ic_power_off, null));
        btnStartEmergency.setOnClickListener(view -> haltEmergency());
    }


    private void showCurrentlyInactive() {
        hideLoading();

        if (selectedLocation == null) {
            ((TextView) btnSelectLocation.getChildAt(0)).setText("Sin lugar seleccionado");
        }

        ((TextView) btnSelectLocation.getChildAt(0)).setTextColor(getResources().getColor(R.color.black, null));
        btnSelectLocation.setEnabled(true);
        btnSelectStart.setEnabled(true);
        btnSelectEnd.setEnabled(true);

        ((TextView) btnSelectStart.getChildAt(0)).setTextColor(getResources().getColor(R.color.black, null));
        ((TextView) btnSelectEnd.getChildAt(0)).setTextColor(getResources().getColor(R.color.black, null));
        btnSelectLocationImg.setImageDrawable(getContext().getDrawable(R.drawable.ic_baseline_location_on_24));
        btnSelectTimeImg.setImageDrawable(getContext().getDrawable(R.drawable.ic_baseline_access_time_24));

        serviceSelectionView.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.labelServices).setVisibility(View.VISIBLE);

        ((TextView) btnStartEmergency.findViewById(R.id.turnOnText)).setText("Comenzar");
        ((TextView) btnStartEmergency.findViewById(R.id.turnOnText)).setTextColor(getResources().getColor(R.color.red, null));
        ((ImageView) getView().findViewById(R.id.turnOnImage)).setImageDrawable(getResources().getDrawable(R.drawable.ic_power_on, null));
        btnStartEmergency.setOnClickListener(view -> startEmergency());
    }


    private void selectLocation() {
        Intent intent = new Intent(getContext(), EmergencyLocationActivity.class);

        Bundle extras = new Bundle();
        extras.putString("jobId", job.getId());
        intent.putExtras(extras);

        selectLocationLauncher.launch(intent);
    }

    private void onLocationSelected(EmergencyLocation selectedLocation) {
        this.selectedLocation = selectedLocation;
        ((TextView) btnSelectLocation.getChildAt(0)).setText(selectedLocation.getName());
    }

    private void initServiceSelection() {
        List<ServiceInstance> emergencyServiceInstances = getEmergencyServiceInstances();
        serviceSelectionView.setServices(emergencyServiceInstances);
    }


    private List<ServiceInstance> getEmergencyServiceInstances() {
        List<ServiceInstance> out = new ArrayList<>();
        Map<String, Boolean> addedServiceInstances = new HashMap<>();
        for (DaySchedule ds : job.getDaySchedules()) {
            for (ServiceInstance si : ds.getServiceInstances()) {
                if (si.isEmergency()) {
                    if (!addedServiceInstances.containsKey(si.getId())) {
                        out.add(si);
                        addedServiceInstances.put(si.getId(), true);
                    }
                }
            }
        }
        return out;
    }

    private void updateTimeLabels() {
        ((TextView) btnSelectStart.getChildAt(0)).setText(CalendarUtils.formatTime(start));
        ((TextView) btnSelectEnd.getChildAt(0)).setText(CalendarUtils.formatTime(end));
    }

    private void selectStart() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    if (isAValidStartTime(hourOfDay, minute)) {
                        start.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        start.set(Calendar.MINUTE, minute);

                        updateTimeLabels();
                    } else {
                        Toast.makeText(getContext(), "Horario inválido", Toast.LENGTH_SHORT).show();
                    }
                },
                start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE), true
        );
        timePickerDialog.setCustomTitle(inflateTitleView("Horario de comienzo"));
        timePickerDialog.show();
    }

    private void selectEnd() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    if (isAValidEndTime(hourOfDay, minute)) {
                        end.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        end.set(Calendar.MINUTE, minute);

                        updateTimeLabels();
                    } else {
                        Toast.makeText(getContext(), "Horario inválido", Toast.LENGTH_SHORT).show();
                    }
                },
                end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE), true
        );
        timePickerDialog.setCustomTitle(inflateTitleView("Horario de finalización"));
        timePickerDialog.show();
    }

    private TextView inflateTitleView(String title) {
        TextView textViewTitle = new TextView(getContext());
        textViewTitle.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        textViewTitle.setTextColor(getResources().getColor(R.color.white));
        textViewTitle.setGravity(Gravity.CENTER);
        textViewTitle.setText(title);
        textViewTitle.setPadding(10, 20, 10, 10);
        textViewTitle.setTextSize(18);
        return textViewTitle;
    }

    private boolean isAValidStartTime(int hour, int minute) {
        Calendar aux = Calendar.getInstance();
        aux.set(Calendar.HOUR_OF_DAY, hour);
        aux.set(Calendar.MINUTE, minute);

        return aux.compareTo(end) < 0 && isOlderThanNow(aux);
    }

    private boolean isAValidEndTime(int hour, int minute) {
        Calendar aux = Calendar.getInstance();
        aux.set(Calendar.HOUR_OF_DAY, hour);
        aux.set(Calendar.MINUTE, minute);

        return aux.compareTo(start) > 0 && isOlderThanNow(aux);
    }

    private boolean isOlderThanNow(Calendar aux) {
        return aux.compareTo(Calendar.getInstance()) > 0;
    }

    private void onEmergencyHalted() {
        selectedLocation = null;
        locationActive = null;
        showCurrentlyInactive();
    }

    private void haltEmergency() {
        DatabaseDjangoWrite.getInstance().DELETE(
                String.format(Constants.DJANGO_URL_EMERGENCY, place.getId(), job.getId()),
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        onEmergencyHalted();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );
    }

    private void startEmergency() {
        if (validate()) {
            DatabaseDjangoWrite.getInstance().POSTJSON(
                    String.format(Constants.DJANGO_URL_EMERGENCY, place.getId(), job.getId()),
                    getBody(),
                    new DatabaseCallback() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            onEmergencyStarted();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                        }
                    }
            );
        }
    }

    private void onEmergencyStarted() {
        Toast.makeText(getContext(), "Emergencias activadas", Toast.LENGTH_SHORT).show();
        startActive = String.format("%s", CalendarUtils.formatTime(start));
        endActive = String.format("%s", CalendarUtils.formatTime(end));
        if (selectedLocation != null) {
            locationActive = selectedLocation.getName();
        }
        showCurrentlyActive();
    }

    private JSONObject getBody() {
        JSONObject out = new JSONObject();

        List<ServiceInstance> selectedServices = serviceSelectionView.getSelectedServices();
        JSONArray selectedServicesIds = new JSONArray();

        for (ServiceInstance si : selectedServices) {
            selectedServicesIds.put(si.getId());
        }

        try {
            out.put("services_instance", selectedServicesIds);
            out.put("start", CalendarUtils.formatTime(start));
            out.put("end", CalendarUtils.formatTime(end));

            if (selectedLocation != null) {
                out.put("location_id", selectedLocation.getId());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return out;
    }

    private boolean validate() {
        if (!isOlderThanNow(start)) {
            Toast.makeText(getContext(), "Seleccione un horario de comienzo válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isOlderThanNow(end)) {
            Toast.makeText(getContext(), "Seleccione un horario de finalización válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (serviceSelectionView.getSelectedServices().size() == 0) {
            Toast.makeText(getContext(), "Seleccione al menos un servicio", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}

