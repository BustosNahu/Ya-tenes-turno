package com.yatenesturno.activities.job_edit.days_off;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.custom_views.JobDateSelection;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.functionality.ManagerAppointment;
import com.yatenesturno.functionality.days_off.DayOff;
import com.yatenesturno.functionality.days_off.DaysOffManager;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import java.util.Calendar;
import java.util.List;

public class DaysOffActivity extends AppCompatActivity {

    private Job job;

    private Place place;


    private AppCompatTextView labelDayStatus;
    private CardView btnChangeStatus;
    private boolean disabling;
    private Calendar selectedDay;
    private JobDateSelection jobDateSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_days_off);

        if (savedInstanceState != null) {
            setState(savedInstanceState);
        } else {
            setState(getIntent().getExtras());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Días inactivos");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        new Handler(Looper.myLooper()).post(this::initUI);
    }

    private void setState(Bundle extras) {
        job = extras.getParcelable("job");
        place = extras.getParcelable("place");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("job", job);
    }

    private void initUI() {
        LoadingOverlay loadingOverlay = new LoadingOverlay(findViewById(R.id.root));
        labelDayStatus = findViewById(R.id.labelDayStatus);
        btnChangeStatus = findViewById(R.id.btnChangeStatus);

        jobDateSelection = new JobDateSelection(job, findViewById(R.id.jobDateSelection), Calendar.getInstance(), false);
        jobDateSelection.setLoadingOverlay(loadingOverlay);
        jobDateSelection.setOnDateSelectedListener(new OnDateSelectedListenerImpl());
        selectedDay = Calendar.getInstance();
        initDayOffView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putParcelable("job", job);
    }

    private void initDayOffView() {
        DaysOffManager.getInstance().getDaysOff(job, days -> {
            DayOff correspDayOff = null;
            for (DayOff dayOff : days) {
                if (dayOff.hasDate(selectedDay)) {
                    correspDayOff = dayOff;
                    break;
                }
            }
            if (correspDayOff != null) {
                initSelectedDayIsDisabled();
            } else {
                initSelectedDayIsActive();
            }
        });
    }

    private void initSelectedDayIsDisabled() {
        labelDayStatus.setText("Día inactivo");
        ((TextView) btnChangeStatus.findViewById(R.id.btnChangeStatusText)).setText("Activar dia");
        btnChangeStatus.setOnClickListener(view -> enableDay());
        findViewById(R.id.imageViewDayOff).setVisibility(View.INVISIBLE);
        findViewById(R.id.imageViewDayOff).setVisibility(View.VISIBLE);
        ((ImageView) findViewById(R.id.imageViewDayOff)).setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.day_off));
    }


    /**
     * This method is to able a day, but first
     * it checks if the user is or not premium,
     * if not, it shows getPremium screen
     */
    private void enableDay() {
        if (GetPremiumActivity.hasPremiumInPlaceOrShowScreen(this, place.getId(), UserManagement.getInstance().getUser().getId())) {
            showBtnProgressBar();
            DaysOffManager.getInstance().removeFromDayOff(job, selectedDay, new DaysOffManager.OnUpdateDayOffListener() {
                @Override
                public void onUpdate() {
                    jobDateSelection.removeDayOff(selectedDay);
                    hideBtnProgressBar();
                    initDayOffView();
                    setResult(RESULT_OK);
                }

                @Override
                public void onFailure() {
                    hideBtnProgressBar();
                }
            });
        }
    }

    private void initSelectedDayIsActive() {
        labelDayStatus.setText("Día activo");
        ((TextView) btnChangeStatus.findViewById(R.id.btnChangeStatusText)).setText("Desactivar día");
        btnChangeStatus.setOnClickListener(view -> verifyAppointments());
        findViewById(R.id.imageViewDayOff).setVisibility(View.INVISIBLE);
        findViewById(R.id.imageViewDayOff).setVisibility(View.VISIBLE);
        ((ImageView) findViewById(R.id.imageViewDayOff)).setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.day_on));

    }

    /**
     * This method is to enable a day, but first
     * it checks if the user is or not premium,
     * if not, it shows getPremium screen
     */
    private void verifyAppointments() {
        if (GetPremiumActivity.hasPremiumInPlaceOrShowScreen(this, place.getId(), UserManagement.getInstance().getUser().getId())) {
            showBtnProgressBar();
            ManagerAppointment.getInstance().getAppointmentForJob(job, selectedDay, new ManagerAppointment.OnAppointmentFetchListener() {
                @Override
                public void onFetch(List<Appointment> appointmentList) {
                    if (appointmentList.size() > 0) {
                        showWarning();
                    } else {
                        disableDay();
                    }
                }

                @Override
                public void onFailure() {
                    showWarning();
                }
            });
        }
    }

    private void showWarning() {
        showBtnProgressBar();
        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this);

        builder.setTitle("Tienes turnos reservados en este día")
                .setMessage("Serán cancelados si continuás")
                .setPositiveButton(R.string.continue_anyway, (dialogInterface, i) -> disableDay())
                .setNeutralButton(R.string.cancel, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    hideBtnProgressBar();
                })
                .setOnDismissListener(dialogInterface -> {
                    if (!disabling) {
                        hideBtnProgressBar();
                    }
                })
                .show();
    }

    private void showBtnProgressBar() {
        findViewById(R.id.btnChangeStatusProgressBar).setVisibility(View.VISIBLE);
    }

    private void hideBtnProgressBar() {
        findViewById(R.id.btnChangeStatusProgressBar).setVisibility(View.GONE);
    }

    private void disableDay() {
        disabling = true;

        showBtnProgressBar();
        DaysOffManager.getInstance().setDayOff(job, selectedDay, new DaysOffManager.OnUpdateDayOffListener() {
            @Override
            public void onUpdate() {
                disabling = false;
                jobDateSelection.addDayOff(selectedDay);
                hideBtnProgressBar();
                initDayOffView();
                setResult(RESULT_OK);
            }

            @Override
            public void onFailure() {
                disabling = false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class OnDateSelectedListenerImpl implements JobDateSelection.OnDateSelectedListener {

        @Override
        public void onDateSelected(Calendar date) {
            selectedDay = date;
            initDayOffView();
        }
    }
}