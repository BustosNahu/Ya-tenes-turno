package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ObservableServiceConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.ObserverServiceConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.custom_views.DayPicker;
import com.yatenesturno.custom_views.TimePicker;
import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.ServiceInstance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DaysAndDurationConfiguration extends ServiceConfiguration implements ObservableServiceConfiguration {

    /**
     * UI References
     */
    private DayPicker dayPicker;
    private TimePicker durationPicker;
    private List<ObserverServiceConfiguration> observers;
    private Job job;
    private AppCompatTextView labelDays, labelDuration;
    private ViewGroup containerDayPicker;

    public DaysAndDurationConfiguration(@NonNull Context context) {
        super(context);
    }

    public DaysAndDurationConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        observers = new ArrayList<>();
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return ConfigurationId.DAYS_AND_DURATION;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_days_and_duration;
    }

    @Override
    protected void onViewInflated(View view) {
        durationPicker = new TimePicker(view.findViewById(R.id.durationPicker));
        dayPicker = new DayPicker(view.findViewById(R.id.dayPicker));
        durationPicker.setStep(5);
        durationPicker.setOnChangeListener(this::notifyObservers);
        labelDays = view.findViewById(R.id.labelDays);
        labelDuration = view.findViewById(R.id.labelDuration);
        containerDayPicker = view.findViewById(R.id.dayPicker);
    }

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    @Override
    public void saveState(Bundle bundle) {
        ArrayList<Integer> selectedDays = new ArrayList<>(dayPicker.getSelectedDays());

        bundle.putSerializable(getConfigurationId().toString(), durationPicker.getTime());
        bundle.putSerializable(getConfigurationId().toString() + "_DAYS", selectedDays);
    }

    @Override
    public void recoverState(Bundle bundle) {
        durationPicker.setTime((Calendar) bundle.getSerializable(getConfigurationId().toString()));

        List<Integer> selectedDays = (List<Integer>) bundle.getSerializable(getConfigurationId().toString() + "_DAYS");
        for (int day : selectedDays) {
            dayPicker.selectDay(day);
        }
    }

    public Calendar getDuration() {
        return durationPicker.getTime();
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        durationPicker.setTime(serviceInstance.getDuration());

        for (DaySchedule ds : job.getDaySchedules()) {
            ServiceInstance si = ds.getServiceInstanceForService(serviceInstance.getService()
                    .getId());

            if (si != null && !si.isEmergency()) {
                dayPicker.selectDay(ds.getDayOfWeek());
            }
        }
    }

    @Override
    public ValidationResult validate() {

        VisitorIsEmergency visitor = new VisitorIsEmergency();
        runVisitor(visitor);
        if (!visitor.isEmergency() && dayPicker.getSelectedDays().size() == 0) {
            return new ValidationResult(false, R.string.req_select_at_least_one_day);
        }

        if (!hasSelectedDate(durationPicker.getTime())) {
            return new ValidationResult(false, R.string.req_select_duration);
        }

        return new ValidationResult(true);
    }

    @Override
    public void updateUI(Boolean active) {

    }

    private boolean hasSelectedDate(Calendar calendar) {
        return calendar.get(Calendar.MINUTE) != 0 ||
                calendar.get(Calendar.HOUR_OF_DAY) != 0;
    }

    @Override
    public void reset() {
        dayPicker.clearSelection();
        durationPicker.reset();
    }

    @Override
    public void attach(ObserverServiceConfiguration observer) {
        this.observers.add(observer);
    }

    @Override
    public void detach(ObserverServiceConfiguration observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (ObserverServiceConfiguration observer : observers) {
            observer.onUpdate();
        }
    }

    public void showDaysSelection() {
        labelDays.setVisibility(VISIBLE);
        containerDayPicker.setVisibility(VISIBLE);
    }

    public void hideDaysSelection() {
        labelDays.setVisibility(GONE);
        containerDayPicker.setVisibility(GONE);
    }


    public void setJob(Job job) {
        this.job = job;
    }

    public List<Integer> getSelectedDays() {
        return dayPicker.getSelectedDays();
    }

    public static class VisitorIsEmergency extends VisitorServiceConfiguration {

        private boolean isEmergency = false;

        public boolean isEmergency() {
            return isEmergency;
        }

        @Override
        public void visit(EmergencyConfiguration emergencyConfiguration) {
            isEmergency = emergencyConfiguration.isEmergency();
        }
    }
}
