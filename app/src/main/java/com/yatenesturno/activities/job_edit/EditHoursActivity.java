package com.yatenesturno.activities.job_edit;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.Job;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity to configure a dayschedule time bounds
 * <p>
 * contemplates midday breaks as well
 */
public class EditHoursActivity extends AppCompatActivity {

    public static final String JOB_KEY = "job";
    public static final String DAY_SCHEDULE_KEY = "daySchedule";
    private static final Map<Integer, String> mapNumberDay = new HashMap<Integer, String>() {{
        put(1, "Domingo");
        put(2, "Lunes");
        put(3, "Martes");
        put(4, "Miércoles");
        put(5, "Jueves");
        put(6, "Viernes");
        put(7, "Sábado");
    }};

    private Job job;
    private DaySchedule daySchedule;
    private TextView labelStart, labelEnd, labelPauseStart, labelPauseEnd;
    private int dayNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_hours);

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            job = (Job) bundle.getSerializable("job");
            dayNumber = bundle.getInt("day_number");
            daySchedule = job.getDaySchedule(dayNumber);

        } else {
            recoverState(savedInstanceState);
        }

        initViews();
    }

    private void initViews() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("Edición del día " + mapNumberDay.get(dayNumber));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        new Handler().postDelayed(this::initUI, 100);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }

    private void recoverState(Bundle bundle) {
        job = bundle.getParcelable(JOB_KEY);
        daySchedule = bundle.getParcelable(DAY_SCHEDULE_KEY);
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable(JOB_KEY, job);
        bundle.putSerializable(DAY_SCHEDULE_KEY, daySchedule);

        return bundle;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        Button btnConfirm = findViewById(R.id.btnConfirm);

        labelStart = findViewById(R.id.labelStart);
        labelEnd = findViewById(R.id.labelEnd);
        labelPauseStart = findViewById(R.id.labelPauseStart);
        labelPauseEnd = findViewById(R.id.labelPauseEnd);
        updateTimeLabels();

        findViewById(R.id.labelStartWrapper).setOnClickListener(view -> configStartTime());
        findViewById(R.id.labelEndWrapper).setOnClickListener(view -> configEndTime());

        findViewById(R.id.labelPauseStartWrapper).setOnClickListener(view -> configPauseStartTime());
        findViewById(R.id.labelPauseEndWrapper).setOnClickListener(view -> configPauseEndTime());

        btnConfirm.setOnClickListener(view -> {
            if (validateTimes()) {
                returnOK();
            }
        });

        CheckBox checkBox = findViewById(R.id.checkBoxPause);

        if (daySchedule.hasPause()) {
            checkBox.setChecked(true);
            displayPauseView();
        }

        checkBox.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                Calendar calendarPauseStart, calendarPauseEnd;

                calendarPauseStart = Calendar.getInstance();
                calendarPauseStart.set(Calendar.HOUR_OF_DAY, 12);
                calendarPauseStart.set(Calendar.MINUTE, 0);

                calendarPauseEnd = Calendar.getInstance();
                calendarPauseEnd.set(Calendar.HOUR_OF_DAY, 13);
                calendarPauseEnd.set(Calendar.MINUTE, 0);

                daySchedule.setPauseStart(calendarPauseStart);
                daySchedule.setPauseEnd(calendarPauseEnd);

                updateTimeLabels();
                displayPauseView();
            } else {
                daySchedule.setPauseStart(null);
                daySchedule.setPauseEnd(null);
                hidePauseView();
            }
        });

    }

    private boolean validateTimes() {

        if (!isAValidStartTime(
                daySchedule.getDayStart().get(Calendar.HOUR_OF_DAY),
                daySchedule.getDayStart().get(Calendar.MINUTE))
                ||
                !isAValidEndTime(
                        daySchedule.getDayEnd().get(Calendar.HOUR_OF_DAY),
                        daySchedule.getDayEnd().get(Calendar.MINUTE))) {
            return false;
        }

        if (daySchedule.hasPause()) {
            return isAValidPauseStartTime(daySchedule.getPauseStart().get(Calendar.HOUR_OF_DAY))
                    &&
                    isAValidPauseEndTime(daySchedule.getPauseEnd().get(Calendar.HOUR_OF_DAY));
        }

        return true;
    }

    private void updateTimeLabels() {
        String startStr = String.format("%02d:%02d", daySchedule.getDayStart().get(Calendar.HOUR_OF_DAY), daySchedule.getDayStart().get(Calendar.MINUTE));
        String endStr = String.format("%02d:%02d", daySchedule.getDayEnd().get(Calendar.HOUR_OF_DAY), daySchedule.getDayEnd().get(Calendar.MINUTE));
        labelStart.setText(startStr);
        labelEnd.setText(endStr);

        if (daySchedule.getPauseStart() != null) {
            String pauseStartStr = String.format("%02d:%02d", daySchedule.getPauseStart().get(Calendar.HOUR_OF_DAY), daySchedule.getPauseStart().get(Calendar.MINUTE));
            labelPauseStart.setText(pauseStartStr);
        }

        if (daySchedule.getPauseEnd() != null) {
            String pauseEndStr = String.format("%02d:%02d", daySchedule.getPauseEnd().get(Calendar.HOUR_OF_DAY), daySchedule.getPauseEnd().get(Calendar.MINUTE));
            labelPauseEnd.setText(pauseEndStr);
        }

    }

    private void configStartTime() {
        final Calendar dayStart = daySchedule.getDayStart();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {

                    if (isAValidStartTime(hourOfDay, minute)) {
                        dayStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        dayStart.set(Calendar.MINUTE, minute);

                        updateTimeLabels();
                    }
                },
                dayStart.get(Calendar.HOUR_OF_DAY), dayStart.get(Calendar.MINUTE), true
        );
        timePickerDialog.setCustomTitle(inflateTitleView("Horario de apertura"));
        timePickerDialog.show();
    }

    public boolean isAValidStartTime(int hourOfDay, int minute) {
        if (isFollowingDay(daySchedule.getDayEnd().get(Calendar.HOUR_OF_DAY), daySchedule.getDayEnd().get(Calendar.MINUTE))) {
            return isOtherDayValid(daySchedule.getDayEnd().get(Calendar.HOUR_OF_DAY), daySchedule.getDayEnd().get(Calendar.MINUTE));
        }

        if (!isMidNight(hourOfDay, minute) &&
                !isMidNight(daySchedule.getDayEnd().get(Calendar.HOUR_OF_DAY), daySchedule.getDayEnd().get(Calendar.MINUTE))) {

            if (!isAnHourEarlierThanCalendar(hourOfDay, daySchedule.getPauseStart())) {
                showSnackBar("El horario de apertura debe ser anterior al comienzo de la pausa");
                return false;
            }

            if (!isAnHourEarlierThanCalendar(hourOfDay, daySchedule.getPauseEnd())) {
                showSnackBar("El horario de apertura debe ser anterior al fin de pausa");
                return false;
            }

            if (!isAnHourEarlierThanCalendar(hourOfDay, daySchedule.getDayEnd())) {
                showSnackBar("El horario de apertura debe ser anterior al horario de cierre");
                return false;
            }
        }

        return true;
    }

    private boolean isMidNight(int hourOfDay, int minute) {
        return hourOfDay == 0 && minute == 0;
    }

    private void configEndTime() {
        final Calendar dayEnd = daySchedule.getDayEnd();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {

                    if (isAValidEndTime(hourOfDay, minute)) {
                        dayEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        dayEnd.set(Calendar.MINUTE, minute);

                        updateTimeLabels();
                    }
                },
                dayEnd.get(Calendar.HOUR_OF_DAY), dayEnd.get(Calendar.MINUTE), true
        );
        timePickerDialog.setCustomTitle(inflateTitleView("Horario de cierre"));
        timePickerDialog.show();
    }

    public boolean isAValidEndTime(int hourOfDay, int minute) {
        if (isFollowingDay(hourOfDay, minute)) {
            return isOtherDayValid(hourOfDay, minute);
        }

        if (!isMidNight(hourOfDay, minute)
                && !isMidNight(daySchedule.getDayStart().get(Calendar.HOUR_OF_DAY), daySchedule.getDayStart().get(Calendar.MINUTE))) {

            if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule.getDayStart())) {
                showSnackBar("El horario de cierre debe ser posterior a la apertura");
                return false;
            }

            if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule.getPauseEnd())) {
                showSnackBar("El horario de cierre debe ser posterior al fin de la pausa");
                return false;
            }

            if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule.getPauseStart())) {
                showSnackBar("El horario de cierre debe ser posterior al comienzo de la pausa");
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether the given time time is in the following
     *
     * @param hourOfDay hour to check
     * @param minute    minute to check
     * @return true if the given hour and minute is in the following day
     */
    private boolean isFollowingDay(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        Calendar startTime = daySchedule.getDayStart();

//        if day start is greater than given time, e.g start: 8:00, end: 00:30
//        thus endtime is in the following day
        return (startTime.get(Calendar.HOUR_OF_DAY) > calendar.get(Calendar.HOUR_OF_DAY));
    }

    /**
     * Checks whether following day endtime is valid
     *
     * @param hourOfDay hour to check
     * @param minute    minutes to check
     * @return true if given past midnight time lands below certain threshold
     */
    private boolean isOtherDayValid(int hourOfDay, int minute) {
        int minuteThreshold = 60;

        int inMinutes = hourOfDay * 60 + minute;
        if (inMinutes <= minuteThreshold) {
            return true;
        }

        showSnackBar(String.format("Sólo puedes limitarte a las %s:%s AM para el horario de cierre", (int) minuteThreshold / 60, minuteThreshold % 60));
        return false;
    }

    private void configPauseStartTime() {
        Calendar calendarPauseStart = daySchedule.getPauseStart();
        final Calendar finalCalendarPauseStart = calendarPauseStart;
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {

                    boolean isAValidTime = isAValidPauseStartTime(hourOfDay);

                    if (isAValidTime) {
                        finalCalendarPauseStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        finalCalendarPauseStart.set(Calendar.MINUTE, minute);

                        daySchedule.setPauseStart(finalCalendarPauseStart);
                        updateTimeLabels();
                    }
                },
                calendarPauseStart.get(Calendar.HOUR_OF_DAY), calendarPauseStart.get(Calendar.MINUTE), true
        );
        timePickerDialog.setCustomTitle(inflateTitleView("Inicio de pausa"));
        timePickerDialog.show();
    }

    public boolean isAValidPauseStartTime(int hourOfDay) {
        if (!isMidNight(daySchedule.getDayEnd().get(Calendar.HOUR_OF_DAY), daySchedule.getDayEnd().get(Calendar.MINUTE)) &&
                !isFollowingDay(daySchedule.getDayEnd().get(Calendar.HOUR_OF_DAY), daySchedule.getDayEnd().get(Calendar.MINUTE))) {

            if (!isAnHourEarlierThanCalendar(hourOfDay, daySchedule.getDayEnd())) {
                showSnackBar("El horario de comienzo de pausa debe ser anterior al cierre");
                return false;
            }

        }

        if (!isAnHourEarlierThanCalendar(hourOfDay, daySchedule.getPauseEnd())) {
            showSnackBar("El horario de comienzo de pausa debe ser anterior al fin de la pausa");
            return false;
        }


        if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule.getDayStart())) {
            showSnackBar("El horario de comienzo de pausa debe ser posterior a la apertura");
            return false;
        }


        return true;
    }

    private void configPauseEndTime() {
        Calendar calendarPauseEnd = daySchedule.getPauseEnd();
        if (!daySchedule.hasPause()) {
            calendarPauseEnd = Calendar.getInstance();
            calendarPauseEnd.set(Calendar.HOUR_OF_DAY, 13);
            calendarPauseEnd.set(Calendar.MINUTE, 0);
        }

        final Calendar finalCalendarPauseEnd = calendarPauseEnd;
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        boolean isAValidTime = isAValidPauseEndTime(hourOfDay);

                        if (isAValidTime) {
                            finalCalendarPauseEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            finalCalendarPauseEnd.set(Calendar.MINUTE, minute);

                            daySchedule.setPauseEnd(finalCalendarPauseEnd);
                            updateTimeLabels();
                        }
                    }

                },
                calendarPauseEnd.get(Calendar.HOUR_OF_DAY), calendarPauseEnd.get(Calendar.MINUTE), true
        );
        timePickerDialog.setCustomTitle(inflateTitleView("Fin de pausa"));
        timePickerDialog.show();
    }

    public boolean isAValidPauseEndTime(int hourOfDay) {
        if (!isMidNight(daySchedule.getDayEnd().get(Calendar.HOUR_OF_DAY), daySchedule.getDayEnd().get(Calendar.MINUTE)) &&
                !isFollowingDay(daySchedule.getDayEnd().get(Calendar.HOUR_OF_DAY), daySchedule.getDayEnd().get(Calendar.MINUTE))) {

            if (!isAnHourEarlierThanCalendar(hourOfDay, daySchedule.getDayEnd())) {
                showSnackBar("El horario de fin de pausa debe ser anterior al cierre");
                return false;
            }
        }

        if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule.getPauseStart())) {
            showSnackBar("El horario de fin de pausa debe ser posterior al comienzo de la pausa");
            return false;
        }

        if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule.getDayStart())) {
            showSnackBar("El horario de fin de pausa debe ser posterior a la apertura");
            return false;
        }

        return true;
    }

    private boolean isAnHourEarlierThanCalendar(int hour, Calendar calendar) {
        return calendar == null || hour < calendar.get(Calendar.HOUR_OF_DAY);
    }

    private boolean isAnHourLaterThanCalendar(int hour, Calendar calendar) {
        return calendar == null || hour > calendar.get(Calendar.HOUR_OF_DAY);
    }

    private void displayPauseView() {
        findViewById(R.id.containerPause).setVisibility(View.VISIBLE);
    }

    private void hidePauseView() {
        findViewById(R.id.containerPause).setVisibility(View.GONE);
    }

    public void returnOK() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("job", job);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(R.id.textView3), message, Snackbar.LENGTH_SHORT).show();
    }

    private TextView inflateTitleView(String title) {
        TextView textViewTitle = new TextView(this);
        textViewTitle.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        textViewTitle.setTextColor(getResources().getColor(R.color.white));
        textViewTitle.setGravity(Gravity.CENTER);
        textViewTitle.setText(title);
        textViewTitle.setPadding(10, 20, 10, 10);
        textViewTitle.setTextSize(18);
        return textViewTitle;
    }
}