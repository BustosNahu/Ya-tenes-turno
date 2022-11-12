package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;

import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.object_interfaces.ServiceInstance;

public class CreditsConfiguration extends ServiceConfiguration {

    /**
     * UI References
     */
    private AppCompatCheckBox checkBoxCredits, checkBoxCanBookWithoutCredits;
    private AppCompatTextView labelCanBookWithoutCredits;

    public CreditsConfiguration(@NonNull Context context) {
        super(context);
    }

    public CreditsConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return ConfigurationId.CREDITS;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_credits;
    }

    @Override
    protected void onViewInflated(View view) {
        checkBoxCredits = view.findViewById(R.id.checkBoxCredits);
        checkBoxCanBookWithoutCredits = view.findViewById(R.id.checkBoxCanBookWithoutCredits);
        labelCanBookWithoutCredits = view.findViewById(R.id.labelCanBookWithoutCredits);

        checkBoxCredits.setOnCheckedChangeListener((compoundButton, b) -> toggleCreditsSelector());
    }

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    @Override
    public void saveState(Bundle bundle) {
        bundle.putBoolean(getConfigurationId().toString(), checkBoxCredits.isChecked());
        bundle.putBoolean(getConfigurationId().toString() + "_CAN_BOOK", checkBoxCanBookWithoutCredits.isChecked());
    }

    private void toggleCreditsSelector() {
        if (checkBoxCredits.isChecked()) {
            checkBoxCanBookWithoutCredits.setVisibility(View.VISIBLE);
            labelCanBookWithoutCredits.setVisibility(View.VISIBLE);
        } else {
            checkBoxCanBookWithoutCredits.setVisibility(View.GONE);
            labelCanBookWithoutCredits.setVisibility(View.GONE);
        }
    }

    @Override
    public void recoverState(Bundle bundle) {
        checkBoxCredits.setChecked(bundle.getBoolean(getConfigurationId().toString()));
        checkBoxCanBookWithoutCredits.setChecked(bundle.getBoolean(getConfigurationId().toString() + "_CAN_BOOK"));
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        checkBoxCredits.setChecked(serviceInstance.isCredits());
        checkBoxCanBookWithoutCredits.setChecked(serviceInstance.isCanBookWithoutCredits());
        toggleCreditsSelector();
    }

    @Override
    public ValidationResult validate() {
        return new ValidationResult(true);
    }

    @Override
    public void updateUI(Boolean active) {

    }

    public boolean isCredits() {
        return checkBoxCredits.isChecked();
    }

    public boolean getCanBookWithoutCredits() {
        return checkBoxCanBookWithoutCredits.isChecked();
    }

    @Override
    public void reset() {
        checkBoxCredits.setChecked(false);
        checkBoxCanBookWithoutCredits.setChecked(false);
    }
}
