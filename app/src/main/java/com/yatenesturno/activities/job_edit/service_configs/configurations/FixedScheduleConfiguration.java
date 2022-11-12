package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.object_interfaces.ServiceInstance;

public class FixedScheduleConfiguration extends ServiceConfiguration {

    /**
     * UI References
     */
    private CheckBox checkBoxFixedSchedule;

    public FixedScheduleConfiguration(@NonNull Context context) {
        super(context);
    }

    public FixedScheduleConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return ConfigurationId.FIXED_SCHEDULE;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_fixed_schedule;
    }

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    @Override
    public void saveState(Bundle bundle) {
        bundle.putBoolean(getConfigurationId().toString(), checkBoxFixedSchedule.isChecked());
    }

    @Override
    public void recoverState(Bundle bundle) {
        checkBoxFixedSchedule.setChecked(bundle.getBoolean(getConfigurationId().toString()));
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        checkBoxFixedSchedule.setChecked(serviceInstance.isFixedSchedule());
    }

    @Override
    public ValidationResult validate() {
        return new ValidationResult(true);
    }

    @Override
    public void updateUI(Boolean active) {

    }

    @Override
    public void reset() {
        checkBoxFixedSchedule.setChecked(false);
    }

    public boolean isFixedSchedule() {
        return checkBoxFixedSchedule.isChecked();
    }

    @Override
    protected void onViewInflated(View view) {
        checkBoxFixedSchedule = view.findViewById(R.id.checkBoxFixedSchedule);

        checkBoxFixedSchedule.setOnCheckedChangeListener((buttonView, isChecked) -> {
            VisitorFixedScheduleVisibility visitor = new VisitorFixedScheduleVisibility(isChecked);
            runVisitor(visitor);
        });
    }

    private static class VisitorFixedScheduleVisibility extends VisitorServiceConfiguration {

        private final boolean active;

        public VisitorFixedScheduleVisibility(boolean active) {
            this.active = active;
        }

        @Override
        public void visit(ClassConfiguration serviceConfiguration) {
            if (active) {
                serviceConfiguration.show();
            } else {
                serviceConfiguration.hide();
            }
        }

        @Override
        public void visit(IntervalConfiguration serviceConfiguration) {
            if (active) {
                serviceConfiguration.hide();
            } else {
                serviceConfiguration.show();
            }
        }

        @Override
        public void visit(TimeSlotConfiguration serviceConfiguration) {
            if (!active) {
                serviceConfiguration.show();
            }
        }
    }
}
