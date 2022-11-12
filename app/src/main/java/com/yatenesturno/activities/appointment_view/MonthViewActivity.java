package com.yatenesturno.activities.appointment_view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.custom_views.JobDateSelection;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.functionality.ManagerAppointment;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.user_auth.UserManagement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Display month & date selection and a daily grid with appointments if any
 */
public class MonthViewActivity extends AppCompatActivity {

    public static final String JOB = "job";
    public static final String PLACE_ID = "placeId";
    public static final String SELECTED_DAY = "selectedDay";
    public static final String APPOINTMENTS = "appointments";

    private static final int RC_VIEW_APPOINTMENT = 1;

    private LoadingOverlay loadingOverlay;
    private DailyCalendar dailyCalendar;

    private Job job;
    private Calendar selectedDay;
    private String placeId;

    private String jobId;
    private String pushNotificationApp;


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable(JOB, job);
        bundle.putString(PLACE_ID, placeId);
        bundle.putSerializable(SELECTED_DAY, selectedDay);
        bundle.putParcelableArrayList(APPOINTMENTS, (ArrayList<Appointment>) dailyCalendar.getAppointments());

        return bundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");

        loadingOverlay = new LoadingOverlay(findViewById(R.id.coordinator));

        if (savedInstanceState == null) {
            init();
        } else {
            recoverState(savedInstanceState);
        }
    }

    private void init() {
        selectedDay = (Calendar) getIntent().getSerializableExtra("selectedDay");
        jobId = getIntent().getStringExtra("jobId");
        placeId = getIntent().getStringExtra("placeId");

        loadingOverlay.show();
        boolean fromPush = getIntent().getBooleanExtra("fromPush", false);
        if (fromPush) {
            fromNotification();
        } else {
            getPlaces();
        }
    }

    private void fromNotification() {
        ManagerAppointment.getInstance().invalidateJob(jobId);
        pushNotificationApp = getIntent().getStringExtra("appointmentId");

        UserManagement.getInstance().authenticate(
                this,
                new UserManagement.UserManagementAuthenticateListener() {
                    @Override
                    public void userAuthenticated() {
                        loadingOverlay.hide();
                        getPlaces();
                    }

                    @Override
                    public void userNotAuthenticated() {
                        finish();
                    }
                });
    }

    private void getPlaces() {
        new Handler(Looper.myLooper()).postDelayed(() ->
                ManagerPlace.getInstance().getPlaces(
                        new ManagerPlace.OnPlaceListFetchListener() {
                            @Override
                            public void onFetch(List<Place> placeList) {
                                job = findJob(placeList, jobId);
                                initUI();
                            }

                            @Override
                            public void onFailure() {
                                showConnectionError();
                                hideLoadingOverlay();
                            }
                        }), 100);
    }

    private void recoverState(Bundle bundle) {
        job = bundle.getParcelable(JOB);
        placeId = bundle.getString(PLACE_ID);
        selectedDay = (Calendar) bundle.getSerializable(SELECTED_DAY);

        if (dailyCalendar != null) {
            dailyCalendar.setAppointments(bundle.getParcelableArrayList(APPOINTMENTS));
        } else {
            initUI();
        }

        loadingOverlay.show();
        new Handler(Looper.myLooper()).postDelayed(() -> {
            loadingOverlay.hide();
            getAppsForSelectedDay();
        }, 500);
    }

    private Job findJob(List<Place> list, String jobId) {
        for (Place p : list) {
            if (p.getId().equals(placeId)) {
                for (Job j : p.getJobList()) {
                    if (j.getId().equals(jobId)) {
                        return j;
                    }
                }
            }
        }
        return null;
    }

    private void initUI() {
        if (pushNotificationApp != null && !GetPremiumActivity.hasPremiumInPlaceOrShowScreen(this, placeId, UserManagement.getInstance().getUser().getId())) {
            onBackPressed();
            return;
        }

        dailyCalendar = new DailyCalendar(
                findViewById(R.id.containerDailyCalendar),
                this::startViewAppointmentActivity
        );

        JobDateSelection jobDateSelection = new JobDateSelection(job, findViewById(R.id.jobDateSelection), selectedDay, true);

        jobDateSelection.setLoadingOverlay(loadingOverlay);
        jobDateSelection.setOnDateSelectedListener(new OnDateSelectedListenerImpl());

        getAppsForSelectedDay();
    }

    private void startViewAppointmentActivity(Appointment appointment) {
        Intent intent = new Intent(getApplicationContext(), ViewAppointmentActivity.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable("appointment", appointment);
        bundle.putString("placeId", placeId);
        bundle.putString("jobId", job.getId());

        intent.putExtras(bundle);

        startActivityForResult(intent, RC_VIEW_APPOINTMENT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAppsForSelectedDay() {
        showLoadingOverlay();
        ManagerAppointment.getInstance().getAppointmentForJob(job, selectedDay, new FetchAppsListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_VIEW_APPOINTMENT && resultCode == RESULT_OK) {
            ManagerAppointment.getInstance().invalidateJob(job.getId());
            getAppsForSelectedDay();
            setResult(RESULT_OK);
        }
    }

    private void populateAppsForDay(List<Appointment> appointmentList) {
        if (appointmentList.size() > 0) {
            displayHasAppointments(appointmentList);
        } else {
            displayNoAppointments();
        }
    }

    private void displayHasAppointments(List<Appointment> appointmentList) {
        dailyCalendar.setAppointments(appointmentList);

        if (pushNotificationApp != null) {
            dailyCalendar.animateAppointment(pushNotificationApp);
            pushNotificationApp = null;
        }

        findViewById(R.id.noAppointmentsContainer).setVisibility(View.INVISIBLE);
        findViewById(R.id.containerDailyCalendar).setVisibility(View.VISIBLE);
    }

    private void displayNoAppointments() {
        findViewById(R.id.noAppointmentsContainer).setVisibility(View.VISIBLE);
        findViewById(R.id.containerDailyCalendar).setVisibility(View.INVISIBLE);
    }

    public void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    private void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }

    public void showConnectionError() {
        Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
    }

    private class OnDateSelectedListenerImpl implements JobDateSelection.OnDateSelectedListener {

        @Override
        public void onDateSelected(Calendar date) {
            selectedDay = date;
            getAppsForSelectedDay();
        }
    }

    private class FetchAppsListener implements ManagerAppointment.OnAppointmentFetchListener {
        @Override
        public void onFetch(List<Appointment> appointmentList) {
            hideLoadingOverlay();
            populateAppsForDay(appointmentList);
        }

        @Override
        public void onFailure() {
            showConnectionError();
            hideLoadingOverlay();
        }
    }
}