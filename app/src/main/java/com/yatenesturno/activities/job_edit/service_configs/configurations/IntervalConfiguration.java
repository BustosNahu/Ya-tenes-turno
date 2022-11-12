package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.custom_views.TimePicker;
import com.yatenesturno.object_interfaces.ServiceInstance;

import java.util.Calendar;

public class IntervalConfiguration extends ServiceConfiguration {

    /**
     * Interval selector
     */
    private TimePicker intervalSelector;

    public IntervalConfiguration(@NonNull Context context) {
        super(context);
    }

    public IntervalConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return ConfigurationId.INTERVAL;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_interval;
    }

    @Override
    protected void onViewInflated(View view) {
        intervalSelector = new TimePicker(view.findViewById(R.id.intervalSelector));
        intervalSelector.setStep(5);
        intervalSelector.setMax(1);
    }

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    @Override
    public void saveState(Bundle bundle) {
        bundle.putSerializable(getConfigurationId().toString(), intervalSelector.getTime());
    }

    @Override
    public void recoverState(Bundle bundle) {
        intervalSelector.setTime((Calendar) bundle.getSerializable(getConfigurationId().toString()));
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        intervalSelector.setTime(serviceInstance.getInterval());
    }

    @Override
    public ValidationResult validate() {
        VisitorIsFixedSchedule visitorIsFixedSchedule = new VisitorIsFixedSchedule();
        if (!visitorIsFixedSchedule.isFixedSchedule()) {
            if (!hasSelectedDate(intervalSelector.getTime())) {
                return new ValidationResult(false, R.string.select_app_interval);
            }
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
        intervalSelector.reset();
    }

    public Calendar getInterval() {
        return intervalSelector.getTime();
    }

    private static class VisitorIsFixedSchedule extends VisitorServiceConfiguration {
        private boolean isFixedSchedule;

        public boolean isFixedSchedule() {
            return isFixedSchedule;
        }

        @Override
        public void visit(FixedScheduleConfiguration fixedScheduleConfiguration) {
            isFixedSchedule = fixedScheduleConfiguration.isFixedSchedule();
        }
    }
}
