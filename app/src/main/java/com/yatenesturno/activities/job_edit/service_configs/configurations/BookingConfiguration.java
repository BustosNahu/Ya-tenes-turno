package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.custom_views.NumberCounter;
import com.yatenesturno.object_interfaces.ServiceInstance;

/**
 * Appointment booking limitations for those with the given service
 */
public class BookingConfiguration extends ServiceConfiguration {

    /**
     * UI References
     */
    private SwitchCompat switchMaxAppsPerDay, switchMaxAppsSimultaneously;
    private ViewGroup containerMaxAppsPerDay, containerMaxAppsSimultaneously;
    private NumberCounter numberCounterMaxAppsPerDay, numberCounterMaxAppsSimultaneously;

    public BookingConfiguration(@NonNull Context context) {
        super(context);
    }

    public BookingConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return null;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_booking;
    }

    @Override
    protected void onViewInflated(View view) {
        switchMaxAppsPerDay = view.findViewById(R.id.switchMaxAppsPerDay);
        switchMaxAppsSimultaneously = view.findViewById(R.id.switchMaxAppsSimultaneously);
        containerMaxAppsPerDay = view.findViewById(R.id.containerMaxAppsPerDay);
        containerMaxAppsSimultaneously = view.findViewById(R.id.containerMaxAppsSimultaneously);

        numberCounterMaxAppsPerDay = new NumberCounter(containerMaxAppsPerDay);
        numberCounterMaxAppsSimultaneously = new NumberCounter(containerMaxAppsSimultaneously);

        switchMaxAppsPerDay.setOnCheckedChangeListener((compoundButton, b) -> toggleMaxAppsPerDay());
        switchMaxAppsSimultaneously.setOnCheckedChangeListener((compoundButton, b) -> toggleMaxAppsSimultaneously());
    }

    private void toggleMaxAppsPerDay() {
        if (switchMaxAppsPerDay.isChecked()) {
            containerMaxAppsPerDay.setVisibility(VISIBLE);
        } else {
            containerMaxAppsPerDay.setVisibility(GONE);
        }
    }

    private void toggleMaxAppsSimultaneously() {
        if (switchMaxAppsSimultaneously.isChecked()) {
            containerMaxAppsSimultaneously.setVisibility(VISIBLE);
        } else {
            containerMaxAppsSimultaneously.setVisibility(GONE);
        }
    }

    public int getMaxAppsPerDay() {
        if (switchMaxAppsPerDay.isChecked()) {
            return numberCounterMaxAppsPerDay.getCount();
        }
        return -1;
    }

    public int getMaxAppsSimultaneously() {
        if (switchMaxAppsSimultaneously.isChecked()) {
            return numberCounterMaxAppsSimultaneously.getCount();
        }
        return -1;
    }

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    @Override
    public void saveState(Bundle bundle) {
        bundle.putInt(getConfigurationId() + "_SAME_DAY", getMaxAppsPerDay());
        bundle.putInt(getConfigurationId() + "_MAX", getMaxAppsSimultaneously());
    }

    @Override
    public void recoverState(Bundle bundle) {
        int sameDay = bundle.getInt(getConfigurationId() + "_SAME_DAY");
        int simultaneously = bundle.getInt(getConfigurationId() + "_MAX");

        update(sameDay, simultaneously);
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        int sameDay = serviceInstance.getMaxAppsPerDay();
        int simultaneously = serviceInstance.getMaxAppsSimultaneously();

        update(sameDay, simultaneously);
    }

    private void update(int sameDay, int simultaneously) {
        if (sameDay != -1) {
            numberCounterMaxAppsPerDay.setCount(sameDay);
        } else {
            numberCounterMaxAppsPerDay.setCount(5);
        }

        if (simultaneously != -1) {
            numberCounterMaxAppsSimultaneously.setCount(simultaneously);
        } else {
            numberCounterMaxAppsSimultaneously.setCount(15);
        }

        switchMaxAppsPerDay.setChecked(sameDay != -1);
        switchMaxAppsSimultaneously.setChecked(simultaneously != -1);
    }

    @Override
    public ValidationResult validate() {
        if (switchMaxAppsSimultaneously.isChecked() && switchMaxAppsPerDay.isChecked()) {
            if (getMaxAppsSimultaneously() < getMaxAppsPerDay()) {
                return new ValidationResult(false, R.string.simultaneously_less_than_per_day);
            }
        }
        return new ValidationResult(true);
    }

    @Override
    public void updateUI(Boolean active) {

    }

    @Override
    public void reset() {
        switchMaxAppsPerDay.setChecked(false);
        switchMaxAppsSimultaneously.setChecked(false);

        numberCounterMaxAppsPerDay.setCount(1);
        numberCounterMaxAppsSimultaneously.setCount(1);
    }
}
