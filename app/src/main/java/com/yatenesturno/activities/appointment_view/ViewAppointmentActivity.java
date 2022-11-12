package com.yatenesturno.activities.appointment_view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.yatenesturno.R;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.Place;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewAppointmentActivity extends AppCompatActivity {

    private static final Map<Integer, String> mapNumberDay = new HashMap<Integer, String>() {{
        put(1, "Dom");
        put(2, "Lun");
        put(3, "Mar");
        put(4, "Mie");
        put(5, "Jue");
        put(6, "Vie");
        put(7, "Sab");
    }};
    private Appointment appointment;

    private boolean isRunning;

    private String placeId;
    private String jobId;

    @Override
    public void onResume() {
        super.onResume();
        isRunning = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointment);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        appointment = (Appointment) getIntent().getExtras().getSerializable("appointment");
        placeId = getIntent().getExtras().getString("placeId");
        jobId = getIntent().getExtras().getString("jobId");

        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                ManagerPlace.getInstance().getPlaces(new ManagerPlace.OnPlaceListFetchListener() {
                    @Override
                    public void onFetch(List<Place> placeList) {
                        if (isRunning) {
                            initUI();
                        }
                    }

                    @Override
                    public void onFailure() {
                        showConnectionError();
                    }
                });
            }
        }, 100);
    }

    public void showConnectionError() {
        Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void initUI() {
        getSupportActionBar().setTitle(appointment.getName());

        TextView textViewDayIndicator = findViewById(R.id.textViewDayIndicator);
        TextView textViewDayNumberIndicator = findViewById(R.id.textViewDayNumberIndicator);
        TextView labelStartTime = findViewById(R.id.labelStartTime);
        TextView labelEndTime = findViewById(R.id.labelEndTime);
        ViewGroup containerCredits = findViewById(R.id.containerCredits);
        AppCompatImageView ivCredits = findViewById(R.id.ivCredits);
        AppCompatTextView labelWithoutCredits = findViewById(R.id.labelWithoutCredits);

        if (appointment.usesCredits()) {
            containerCredits.setVisibility(View.VISIBLE);
            ivCredits.setVisibility(View.VISIBLE);
            if (appointment.bookedWithoutCredits()) {
                labelWithoutCredits.setVisibility(View.VISIBLE);
            } else {
                labelWithoutCredits.setVisibility(View.GONE);
            }
        } else {
            containerCredits.setVisibility(View.GONE);
        }

        String strStart = formatCalendar(appointment.getTimeStampStart());
        String strEnd = formatCalendar(appointment.getTimeStampEnd());

        labelStartTime.setText(strStart);
        labelEndTime.setText(strEnd);

        textViewDayIndicator.setText(mapNumberDay.get(appointment.getTimeStampStart().get(Calendar.DAY_OF_WEEK)));
        textViewDayNumberIndicator.setText(appointment.getTimeStampStart().get(Calendar.DATE) + "/" + (1 + appointment.getTimeStampStart().get(Calendar.MONTH)));

        initInnerViewFragments();
    }

    private void initInnerViewFragments() {
        Fragment fragment = appointment.getInnerViewFragment(placeId, jobId);

        getSupportFragmentManager().beginTransaction().replace(
                R.id.containerInnerViewFragment,
                fragment
        ).commit();
    }

    private String formatCalendar(Calendar calendar) {
        return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}