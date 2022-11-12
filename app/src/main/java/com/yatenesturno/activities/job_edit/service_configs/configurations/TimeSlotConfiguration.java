package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.object_interfaces.ServiceInstance;

import java.util.Calendar;

public class TimeSlotConfiguration extends ServiceConfiguration {

    /**
     * Instance variables
     */
    private Calendar startTime, endTime;

    /**
     * UI References
     */
    private CardView cardViewStart, cardViewEnd;
    private TextView tvEditStart, tvEditEnd;

    public TimeSlotConfiguration(@NonNull Context context) {
        super(context);
    }

    public TimeSlotConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return ConfigurationId.TIME_SLOT;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_time_slot;
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        if (serviceInstance.getStartTime() != null) {
            startTime = (Calendar) serviceInstance.getStartTime().clone();
            endTime = (Calendar) serviceInstance.getEndTime().clone();
        } else {
            init();
        }

        updateTimeLabels();
    }

    private void init() {
        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 6);
        startTime.set(Calendar.MINUTE, 0);

        endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 23);
        endTime.set(Calendar.MINUTE, 0);
    }

    private void configStartTime() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {

                    startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    startTime.set(Calendar.MINUTE, minute);

                    updateTimeLabels();

                },
                startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), true
        );
        timePickerDialog.setCustomTitle(inflateTitleView(tvEditEnd.getContext().getString(R.string.label_start_time)));
        timePickerDialog.show();
    }

    @Override
    protected void onViewInflated(View view) {
        cardViewStart = view.findViewById(R.id.cardViewStart);
        cardViewEnd = view.findViewById(R.id.cardViewEnd);

        tvEditStart = view.findViewById(R.id.tvStartTime);
        tvEditEnd = view.findViewById(R.id.tvEndTime);

        cardViewStart.setOnClickListener(view1 -> configStartTime());
        cardViewEnd.setOnClickListener(view1 -> configEndTime());
    }

    public boolean isAValidStartTime(int hourOfDay, int minute) {
        if (isFollowingDay(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE))) {
            return isOtherDayValid(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE));
        }

        return isAtLeastAnHourEarlierThanCalendar(hourOfDay, endTime)
                || isMidNight(hourOfDay, minute)
                || isMidNight(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE));
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

//        if day start is greater than given time, e.g start: 8:00, end: 00:30
//        thus endtime is in the following day
        return (startTime.get(Calendar.HOUR_OF_DAY) > calendar.get(Calendar.HOUR_OF_DAY));
    }

    /**
     * Chceks wheter following day endtime is valid
     *
     * @param hourOfDay hour to check
     * @param minute    minutes to check
     * @return true if given past midnight time lands below certain threshold
     */
    private boolean isOtherDayValid(int hourOfDay, int minute) {
        int minuteThreshold = 60;

        int inMinutes = hourOfDay * 60 + minute;
        return inMinutes <= minuteThreshold;
    }


    private void configEndTime() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {

                    endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    endTime.set(Calendar.MINUTE, minute);

                    updateTimeLabels();

                },
                endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), true
        );
        timePickerDialog.setCustomTitle(inflateTitleView(tvEditEnd.getContext().getString(R.string.label_end_time)));
        timePickerDialog.show();
    }

    public boolean isAValidEndTime(int hourOfDay, int minute) {
        if (isFollowingDay(hourOfDay, minute)) {
            return isOtherDayValid(hourOfDay, minute);
        }

        return isAtLeastAnHourLaterThanCalendar(hourOfDay, startTime)
                || isMidNight(hourOfDay, minute)
                || isMidNight(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE));
    }

    private boolean isMidNight(int hourOfDay, int minute) {
        return hourOfDay == 0 && minute == 0;
    }

    private boolean isAtLeastAnHourEarlierThanCalendar(int hour, Calendar calendar) {
        return calendar == null || hour < calendar.get(Calendar.HOUR_OF_DAY);
    }

    private boolean isAtLeastAnHourLaterThanCalendar(int hour, Calendar calendar) {
        return calendar == null || hour > calendar.get(Calendar.HOUR_OF_DAY);
    }

    private void updateTimeLabels() {
        String startStr = String.format("%02d:%02d", startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE));
        String endStr = String.format("%02d:%02d", endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE));
        tvEditStart.setText(startStr);
        tvEditEnd.setText(endStr);
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

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    @Override
    public void saveState(Bundle bundle) {
        bundle.putSerializable(getConfigurationId().toString() + "_START", startTime);
        bundle.putSerializable(getConfigurationId().toString() + "_END", endTime);
    }

    @Override
    public void recoverState(Bundle bundle) {
        startTime = (Calendar) bundle.getSerializable(getConfigurationId().toString() + "_START");
        endTime = (Calendar) bundle.getSerializable(getConfigurationId().toString() + "_END");
    }

    @Override
    public ValidationResult validate() {

        if (!isFollowingDay(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE)) && !isAValidStartTime(startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE))) {
            return new ValidationResult(false, R.string.start_time_invalid);
        }
        if (!isFollowingDay(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE)) &&
                !isAValidEndTime(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE))) {
            return new ValidationResult(false, R.string.end_time_invalid);
        } else if (isFollowingDay(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE))) {
            if (!isOtherDayValid(endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE))) {
                return new ValidationResult(false, R.string.limit_bound_upper_Extension_err);
            }
        }

        return new ValidationResult(true);
    }

    @Override
    public void updateUI(Boolean active) {

    }

    @Override
    public void reset() {
        init();
        updateTimeLabels();
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }
}
