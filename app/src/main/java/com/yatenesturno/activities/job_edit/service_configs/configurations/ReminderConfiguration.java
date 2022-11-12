package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.custom_views.TimePicker;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.user_auth.UserManagement;

import java.util.Calendar;

public class ReminderConfiguration extends ServiceConfiguration {

    private String placeId;

    /**
     * UI Refs
     */
    private CheckBox checkBoxReminder;
    private LinearLayoutCompat reminderPrevTimeContainer;
    private AppCompatTextView labelReminder;
    private TimePicker reminderPrevTime;

    public ReminderConfiguration(@NonNull Context context) {
        super(context);
    }

    public ReminderConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return ConfigurationId.REMINDER;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_reminder;
    }

    @Override
    protected void onViewInflated(View view) {
        reminderPrevTimeContainer = view.findViewById(R.id.reminderPrevTime);
        labelReminder = view.findViewById(R.id.labelReminder);
        checkBoxReminder = view.findViewById(R.id.checkBoxReminder);


        reminderPrevTime = new TimePicker(view.findViewById(R.id.reminderPrevTime));

        reminderPrevTime.setMax(23);
        reminderPrevTime.setStep(60);
        reminderPrevTime.setShowMinutes(false);
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;


        boolean isPremium = GetPremiumActivity.hasPremiumInPlace(placeId, UserManagement.getInstance().getUser().getId());
        if (isPremium) {
            checkBoxReminder.setOnCheckedChangeListener((compoundButton, isChecked) -> toggleReminderSelector());
        } else {
            CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    checkBoxReminder.setOnCheckedChangeListener(null);
                    checkBoxReminder.setChecked(false);
                    checkBoxReminder.setOnCheckedChangeListener(this);
                    GetPremiumActivity.showPremiumInfoFromActivity((Activity) checkBoxReminder.getContext(), placeId);
                }
            };
            checkBoxReminder.setOnCheckedChangeListener(listener);
        }
    }

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    private void toggleReminderSelector() {
        if (checkBoxReminder.isChecked()) {
            reminderPrevTimeContainer.setVisibility(View.VISIBLE);
            labelReminder.setVisibility(View.VISIBLE);
        } else {
            reminderPrevTimeContainer.setVisibility(View.GONE);
            labelReminder.setVisibility(View.GONE);
        }
    }

    @Override
    public void saveState(Bundle bundle) {
        bundle.putBoolean(getConfigurationId().toString(), checkBoxReminder.isChecked());
        bundle.putSerializable(getConfigurationId().toString() + "_INTERVAL", reminderPrevTime.getTime());
    }

    @Override
    public void recoverState(Bundle bundle) {
        checkBoxReminder.setChecked(bundle.getBoolean(getConfigurationId().toString()));
        reminderPrevTime.setTime((Calendar) bundle.getSerializable(getConfigurationId().toString() + "_INTERVAL"));
    }

    public boolean isReminder() {
        return checkBoxReminder.isChecked();
    }

    public Calendar getReminderInterval() {
        return reminderPrevTime.getTime();
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        checkBoxReminder.setChecked(serviceInstance.isReminderSet());
        reminderPrevTime.setTime(serviceInstance.getReminderInterval());
    }

    @Override
    public ValidationResult validate() {
        VisitorEmergency visitor = new VisitorEmergency();
        runVisitor(visitor);

        if (!visitor.isEmergency() && checkBoxReminder.isChecked()) {
            if (!hasSelectedDate(reminderPrevTime.getTime())) {
                return new ValidationResult(false, R.string.warning_choose_reminder);
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
        checkBoxReminder.setChecked(false);
        reminderPrevTime.reset();
        toggleReminderSelector();
    }

    private static class VisitorEmergency extends VisitorServiceConfiguration {

        private boolean isEmergency = false;

        public boolean isEmergency() {
            return isEmergency;
        }

        @Override
        public void visit(EmergencyConfiguration serviceConfiguration) {
            isEmergency = serviceConfiguration.isEmergency();
        }
    }
}
