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
import com.yatenesturno.custom_views.NumberCounter;
import com.yatenesturno.object_interfaces.ServiceInstance;

public class ConcurrencyConfiguration extends ServiceConfiguration {

    /**
     * UI References
     */
    private NumberCounter concurrencyCounter;

    public ConcurrencyConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return ConfigurationId.CONCURRENCY;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_concurrency;
    }

    @Override
    protected void onViewInflated(View view) {
        concurrencyCounter = new NumberCounter(view.findViewById(R.id.concurrencyPicker));
    }

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    @Override
    public void saveState(Bundle bundle) {
        bundle.putInt(getConfigurationId().toString(), concurrencyCounter.getCount());
    }

    @Override
    public void recoverState(Bundle bundle) {
        concurrencyCounter.setCount(bundle.getInt(getConfigurationId().toString()));
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        concurrencyCounter.setCount(serviceInstance.getConcurrency());
    }

    @Override
    public ValidationResult validate() {
        if (concurrencyCounter.getCount() >= 1) {
            return new ValidationResult(true);
        }

        return new ValidationResult(false);
    }

    @Override
    public void updateUI(Boolean active) {

    }

    @Override
    public void reset() {
        concurrencyCounter.reset();
    }

    public int getConcurrency() {
        return concurrencyCounter.getCount();
    }
}
