package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.object_interfaces.ServiceInstance;

public class EmergencyConfiguration extends ServiceConfiguration {

    /**
     * UI References
     */
    private CheckBox checkBoxEmergency;

    public EmergencyConfiguration(@NonNull Context context) {
        super(context);
    }

    public EmergencyConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return ConfigurationId.EMERGENCY;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_emergency;
    }

    @Override
    protected void onViewInflated(View view) {
        checkBoxEmergency = view.findViewById(R.id.checkBoxEmergency);
        checkBoxEmergency.setOnCheckedChangeListener(this::onCheckBoxChanged);
    }

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    private void onCheckBoxChanged(CompoundButton compoundButton, boolean b) {
        runVisitor(new VisitorEmergencyVisibility(b));
    }

    @Override
    public void saveState(Bundle bundle) {
        bundle.putBoolean(getConfigurationId().toString(), checkBoxEmergency.isChecked());
    }

    @Override
    public void recoverState(Bundle bundle) {
        checkBoxEmergency.setChecked(bundle.getBoolean(getConfigurationId().toString()));
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        checkBoxEmergency.setChecked(serviceInstance.isEmergency());
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
        checkBoxEmergency.setChecked(false);
    }

    public boolean isEmergency() {
        return checkBoxEmergency.isChecked();
    }

    public static class VisitorEmergencyVisibility extends VisitorServiceConfiguration {

        private final boolean active;

        public VisitorEmergencyVisibility(boolean active) {
            this.active = active;
        }

        @Override
        public void visit(DaysAndDurationConfiguration configuration) {
            if (active) {
                configuration.hideDaysSelection();
            } else {
                configuration.showDaysSelection();
            }
        }

        @Override
        public void visit(ReminderConfiguration configuration) {
            if (active) {
                configuration.hide();
            } else {
                configuration.show();
            }
        }

        @Override
        public void visit(ConcurrentServicesConfiguration configuration) {
            if (active) {
                configuration.hide();
            } else {
                configuration.show();
            }
        }

        @Override
        public void visit(FixedScheduleConfiguration configuration) {
            if (active) {
                configuration.hide();
            } else {
                configuration.show();
            }
        }

        @Override
        public void visit(ClassConfiguration configuration) {
            if (active) {
                configuration.hide();
            } else {
                configuration.show();
            }
        }

        @Override
        public void visit(CreditsConfiguration configuration) {
            if (active) {
                configuration.hide();
            } else {
                configuration.show();
            }
        }

        @Override
        public void visit(IntervalConfiguration configuration) {
            configuration.show();
        }

        @Override
        public void visit(BookingConfiguration configuration) {
            if (active) {
                configuration.hide();
            } else {
                configuration.show();
            }
        }

        @Override
        public void visit(TimeSlotConfiguration configuration) {
            if (active) {
                configuration.hide();
            } else {
                configuration.show();
            }
        }

    }
}
