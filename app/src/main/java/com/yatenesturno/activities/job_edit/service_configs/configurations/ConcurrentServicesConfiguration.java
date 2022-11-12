package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.custom_views.ServiceSelectionView;
import com.yatenesturno.object_interfaces.Service;
import com.yatenesturno.object_interfaces.ServiceInstance;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentServicesConfiguration extends ServiceConfiguration {
    /**
     * Instance variables
     */
    private List<ServiceInstance> otherProvidedServices;

    /**
     * UI Ref
     */
    private ServiceSelectionView serviceSelectionView;
    private ViewGroup container;

    public ConcurrentServicesConfiguration(@NonNull Context context) {
        super(context);
    }

    public ConcurrentServicesConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOtherProvidedServices(List<ServiceInstance> otherProvidedServices) {
        this.otherProvidedServices = otherProvidedServices;
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return ConfigurationId.CONCURRENT_SERVICES;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_concurrent_services;
    }

    @Override
    protected void onViewInflated(View view) {
        container = view.findViewById(R.id.containerConcurrentServices);
        serviceSelectionView = view.findViewById(R.id.serviceSelectionView);
        if (otherProvidedServices != null) {
            serviceSelectionView.setServices(otherProvidedServices);
        }
    }

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    private boolean shouldShowOtherServiceConc() {
        return otherProvidedServices != null && otherProvidedServices.size() > 0;
    }

    private void hideOtherServicesConcurrency() {
        hide();
    }

    private void showOtherServicesConcurrency() {
        show();
    }

    @Override
    public void saveState(Bundle bundle) {
        bundle.putParcelableArrayList(getConfigurationId().toString(), (ArrayList<ServiceInstance>) otherProvidedServices);
    }

    @Override
    public void recoverState(Bundle bundle) {
        otherProvidedServices = bundle.getParcelableArrayList(getConfigurationId().toString());
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        if (shouldShowOtherServiceConc()) {
            showOtherServicesConcurrency();
            serviceSelectionView.setServices(otherProvidedServices);

            List<ServiceInstance> previouslySelectedServices = new ArrayList<>();

            for (Service concurrentService : serviceInstance.getConcurrentServices()) {
                for (ServiceInstance si : otherProvidedServices) {
                    if (si.getService().getId().equals(concurrentService.getId())) {
                        previouslySelectedServices.add(si);
                    }
                }
            }

            serviceSelectionView.setSelectedServices(previouslySelectedServices);

        } else {
            hideOtherServicesConcurrency();
            hide();
        }
    }

    public List<ServiceInstance> getOtherProvidedServices() {
        return otherProvidedServices;
    }

    public List<Service> getSelectedConcurrentServices() {
        List<Service> selectedConcurrentServices = new ArrayList<>();
        if (otherProvidedServices != null) {
            for (ServiceInstance concurrentService : serviceSelectionView.getSelectedServices()) {
                selectedConcurrentServices.add(concurrentService.getService());
            }
        }
        return selectedConcurrentServices;
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
        serviceSelectionView.invalidate();

        if (shouldShowOtherServiceConc()) {
            showOtherServicesConcurrency();
            serviceSelectionView.setServices(otherProvidedServices);
        } else {
            hideOtherServicesConcurrency();
        }
    }
}
