package com.yatenesturno.activities.job_appointment_book;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.JobDateSelectionButton;
import com.yatenesturno.custom_views.LoadingButton;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.custom_views.ServiceSelectionView;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.functionality.LabelSelectorView;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Label;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.serializers.BuilderListServiceInstance;
import com.yatenesturno.serializers.BuilderListTimestamps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import eu.davidea.flexibleadapter.FlexibleAdapter;


public class AppointmentDateSelectionFragment extends Fragment {

    /**
     * Arguments keys
     */
    public static final String CALENDAR = "calendar";
    public static final String JOB = "job";

    /**
     * Instance variables
     */
    private String placeId;
    private Job job;
    private AnonymousAppActivity.ListenerSelectedDateAndTime listenerSelectedDateAndTime;
    private AnonymousAppActivity.OnLabelSelectedListener onLabelSelectedListener;
    private Calendar selectedDate;
    private List<Calendar> appointmentList;
    private ArrayList<TimestampViewKt> appointmentViewList;
    private String dayScheduleId;
    private Label selectedLabel;
    private LabelSelectorView labelSelectorView;
    private List<ServiceInstance> selectedServices;

    /**
     * UI references
     */
    private LoadingButton btnLookForApps;
    private LoadingOverlay loadingOverlay;
    private RecyclerView recyclerViewAvailableApps;
    private ServiceSelectionView serviceSelectionView;
    private CardView cardViewLabel;
    private JobDateSelectionButton jobDateSelectionButton;
    private View labelsNoAppsFound;

    public AppointmentDateSelectionFragment(Job job, String placeId) {
        this.placeId = placeId;
        this.job = job;
    }

    public AppointmentDateSelectionFragment() {

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();

        bundle.putSerializable(CALENDAR, selectedDate);
        bundle.putParcelable(JOB, job);

        return bundle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_appointment_date_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        }

        new Handler(Looper.myLooper()).postDelayed(() -> initViews(), 100);
    }

    private void recoverState(Bundle bundle) {
        selectedDate = (Calendar) bundle.getSerializable(CALENDAR);
        job = bundle.getParcelable(JOB);
    }

    private void initViews() {
        labelsNoAppsFound = getView().findViewById(R.id.labelsNoAppsFound);
        loadingOverlay = new LoadingOverlay(getView().findViewById(R.id.root));

        btnLookForApps = getView().findViewById(R.id.btnLookForApps);
        recyclerViewAvailableApps = getView().findViewById(R.id.recyclerViewAvailableApps);
        serviceSelectionView = getView().findViewById(R.id.serviceSelectionView);

        btnLookForApps.setMatchParent();
        recyclerViewAvailableApps.setLayoutManager(new GridLayoutManager(getContext(), 4));

        fetchServiceInstances();
        initDateSelection();
        initBtnLookForApps();
        initSetLabel();
    }

    private void initSetLabel() {
        cardViewLabel = getView().findViewById(R.id.cardViewLabel);
        selectedLabel = null;
        labelSelectorView = new LabelSelectorView(placeId, job.getId(), null);
        updateLabelView();
        labelSelectorView.setOnLabelSelectedListener(new LabelSelectorView.OnLabelClickListener() {
            @Override
            public void onLabelSelected(Label label) {
                selectedLabel = label;
                onLabelSelectedListener.onSelected(label);
                updateLabelView();
            }

            @Override
            public void onDelete(Label label) {
                if (label.equals(selectedLabel)) {
                    selectedLabel = null;
                    onLabelSelectedListener.onSelected(null);
                    updateLabelView();
                }
            }
        });

        cardViewLabel.setOnClickListener(view -> labelSelectorView.showChangeLabelDialog(getContext()));
    }

    private void updateLabelView() {
        LabelSelectorView.fillView(cardViewLabel, selectedLabel, false);
    }

    private void fetchServiceInstances() {
        Map<String, String> body = new HashMap<>();
        body.put("job_id", job.getId());
        loadingOverlay.show();
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_SERVICE_INSTANCES,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        List<ServiceInstance> providedServices;
                        try {
                            providedServices = new BuilderListServiceInstance().build(response.getJSONArray("service_instance"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            providedServices = new ArrayList<>();
                        }
                        loadingOverlay.hide();
                        Log.d("fetchServiceInstances SUCESS", body.toString());
                        initServiceSelection(providedServices);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.d("fetchServiceInstances FAILURE", responseString.toString());
                    }
                }
        );
    }

    private void initServiceSelection(List<ServiceInstance> providedServices) {
        serviceSelectionView.setServices(providedServices);
        serviceSelectionView.setListener(() -> {
            if (hasSelectedServices()) {
                btnLookForApps.setVisibility(View.VISIBLE);
            } else {
                hideBtnLookForApps();
            }
            reset();
        });
    }

    private void initDateSelection() {
        if (selectedDate == null) {
            selectedDate = Calendar.getInstance();
        }
        jobDateSelectionButton = getView().findViewById(R.id.jobDateSelection);
        jobDateSelectionButton.setJob(job, false);
        jobDateSelectionButton.setDate(selectedDate);
        jobDateSelectionButton.setListener(date -> {
            if (hasSelectedServices()) {
                btnLookForApps.setVisibility(View.VISIBLE);
            } else {
                hideBtnLookForApps();
            }
            reset();
        });
    }

    public void reset() {
        hideNoAppointmentsFound();
        hideRecyclerViewAppointments();
    }

    private void initBtnLookForApps() {
        btnLookForApps.setOnClickListener(v -> {
            if (hasSelectedServices()) {
                fetchAvailableApps();
            } else {
                btnLookForApps.hideLoading();
                displayNoSelectedServicesWarning();
            }
        });
    }

    private void displayNoSelectedServicesWarning() {
        Snackbar.make(btnLookForApps.getRootView(), "Seleccione al menos un servicio", Snackbar.LENGTH_SHORT).show();
    }

    ////////////////////////////////////// TECA ////////////////////////////////////////////////////
    private void fetchAvailableApps() {
        JSONObject body = getBody();

        String dateString = getDateString();
        DatabaseDjangoWrite.getInstance().POSTJSON(
                String.format(Constants.DJANGO_URL_GET_AVAILABLE_APPOINTMENTS, placeId, job.getId(), dateString),
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        handleAvailableAppsResponse(response);
                        hideBtnLookForApps();


                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        showNoAvailableAppsFound();
                        hideRecyclerViewAppointments();
                        hideBtnLookForApps();


                    }
                }
        );
    }
    ////////////////////////////////////// TECA ////////////////////////////////////////////////////


    private String getDateString() {

        Calendar selectedDate = jobDateSelectionButton.getDate();

        StringBuilder builder = new StringBuilder();

        if (String.valueOf(selectedDate.get(Calendar.DAY_OF_MONTH) ).length() == 1) {
            builder.append("0");
        }
        builder.append(selectedDate.get(Calendar.DAY_OF_MONTH));
        builder.append("-");
        if (String.valueOf(selectedDate.get(Calendar.MONTH) + 1).length() == 1) {//+1
            builder.append("0");
        }
        builder.append(selectedDate.get(Calendar.MONTH) + 1);
        builder.append("-");

        builder.append(selectedDate.get(Calendar.YEAR));


        return builder.toString();
    }

    private void hideBtnLookForApps() {
        btnLookForApps.hideLoading();
        btnLookForApps.setVisibility(View.GONE);
    }

    private void showNoAvailableAppsFound() {
        labelsNoAppsFound.setVisibility(View.VISIBLE);
    }

    private void hideNoAppointmentsFound() {
        labelsNoAppsFound.setVisibility(View.GONE);
    }

    private void hideRecyclerViewAppointments() {
        recyclerViewAvailableApps.setVisibility(View.GONE);
    }

    private void showRecyclerViewAppointments() {
        recyclerViewAvailableApps.setVisibility(View.VISIBLE);
    }

    private void handleAvailableAppsResponse(JSONObject response) {

        try {
            appointmentList = BuilderListTimestamps.build(response.getJSONObject("content"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        selectedServices = serviceSelectionView.getSelectedServices();
        dayScheduleId = getDayScheduleId(response);

        if (appointmentList.size() > 0) {
            hideNoAppointmentsFound();
            showRecyclerViewAppointments();
            populateAppointmentsRecyclerView();
        } else {
            hideRecyclerViewAppointments();
            showNoAvailableAppsFound();
        }
    }

    private String getDayScheduleId(JSONObject response) {

        try {
            JSONArray divisions = response.getJSONObject("content").getJSONArray("divisions");
            JSONObject timestamp = divisions.getJSONArray(0).getJSONObject(0);
            JSONObject timestamp1 = timestamp.getJSONObject(timestamp.keys().next());
            return timestamp1.getString("day_schedule");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void populateAppointmentsRecyclerView() {
        appointmentViewList = new ArrayList<>();

        for (Calendar timestamp : appointmentList) {
            TimestampViewKt view = new TimestampViewKt(timestamp);
            appointmentViewList.add(view);
        }

        FlexibleAdapter<TimestampViewKt> adapterAppointments = new FlexibleAdapter<>(appointmentViewList);
        recyclerViewAvailableApps.setAdapter(adapterAppointments);
        adapterAppointments.addListener((FlexibleAdapter.OnItemClickListener) (view, position) -> {
            onAppointmentClick(appointmentViewList.get(position).getCalendarUTC());
            return true;
        });
    }

    private JSONObject getBody() {
        JSONObject out = new JSONObject();

        JSONArray servicesJson = new JSONArray();
        for (ServiceInstance si : serviceSelectionView.getSelectedServices()) {
            servicesJson.put(Integer.parseInt(si.getService().getId()));
        }

        try {
            out.put("services", servicesJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return out;
    }

    private boolean hasSelectedServices() {
        return serviceSelectionView.getSelectedServices().size() > 0;
    }

    private void onAppointmentClick(Calendar calendar) {
        selectedDate = calendar;
        listenerSelectedDateAndTime.onDateAndTimeSelected(selectedDate, selectedServices, dayScheduleId);
    }

    public void setOnDateSelectedListener(AnonymousAppActivity.ListenerSelectedDateAndTime onSelectedAppListener) {
        this.listenerSelectedDateAndTime = onSelectedAppListener;
    }

    public void setOnLabelSelectedListener(AnonymousAppActivity.OnLabelSelectedListener onLabelSelectedListener) {
        this.onLabelSelectedListener = onLabelSelectedListener;
    }

}