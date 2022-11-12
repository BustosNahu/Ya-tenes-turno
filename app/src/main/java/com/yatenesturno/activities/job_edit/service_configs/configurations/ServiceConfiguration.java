package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ServiceConfigCoordinator;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.object_interfaces.ServiceInstance;

/**
 * Abstract class for a serviceInstance configurator
 * <p>
 * Allows usage of the Visitor pattern
 */
public abstract class ServiceConfiguration extends LinearLayoutCompat  {

    /**
     * UI References
     */
    private final ViewGroup root;
    public View rootViewV;
    public Boolean active;
    public Boolean completed;
    public Boolean wasEdited;

    /**
     * Instance variables
     */
    private ServiceConfigCoordinator configCoordinator;

    public ServiceConfiguration(@NonNull Context context) {
        this(context, null);
    }

    public ServiceConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);

        root = LayoutInflater.from(context).inflate(R.layout.service_configurator_view, this, true).findViewById(R.id.root);
        active = false;
        completed = false;
        wasEdited = false;
        View view = LayoutInflater.from(context).inflate(getLayoutResourceId(), root, true);
        rootViewV = view;
        onViewInflated(view);
    }

    protected final ServiceConfigCoordinator getConfigCoordinator() {
        return configCoordinator;
    }

    public final void setConfigCoordinator(ServiceConfigCoordinator configCoordinator) {
        this.configCoordinator = configCoordinator;
    }

    /**
     * Run a given visitor among the other configurations
     *
     * @param visitor VisitorServiceConfiguration implementation to run
     */
    protected final void runVisitor(VisitorServiceConfiguration visitor) {
        for (ServiceConfiguration serviceConfiguration : getConfigCoordinator().getServiceConfigurationList()) {
            serviceConfiguration.acceptVisitor(visitor);
        }
    }

    /**
     * Return an identifier for this service configuration
     *
     * @return configuration id enum describing this service config
     */
    public abstract ConfigurationId getConfigurationId();

    /**
     * Get layout id to be inflated
     *
     * @return resource id of the layout
     */
    protected abstract int getLayoutResourceId();

    /**
     * Method called once the view gets inflated into root view
     *
     * @param view inflated view
     */
    protected abstract void onViewInflated(View view);

    /**
     * Accept visitor
     *
     * @param visitor incoming visitor
     */
    public abstract void acceptVisitor(VisitorServiceConfiguration visitor);

    public abstract void saveState(Bundle bundle);

    public abstract void recoverState(Bundle bundle);

    /**
     * Init configuration for when the ServiceInstance already exists
     *
     * @param serviceInstance servince instance to get info from
     */
    public abstract void initFromServiceInstance(ServiceInstance serviceInstance);

    /**
     * Validate configuration
     *
     * @return Validation result with relevant info of the validation process
     */
    public abstract ValidationResult validate();

    public abstract void updateUI(Boolean active);

    /**
     * Reset state to initial one
     */
    public abstract void reset();

    /**
     * hide the configuration
     */
    public void hide() {
        root.setVisibility(GONE);
    }

    /**
     * Show the configuration
     */
    public void show() {
        root.setVisibility(VISIBLE);
    }

    /**
     * Return a boolean indicating if the configuration is visible
     *
     * @return true if this configuration is visible, false otherwise
     */
    public boolean isVisible() {
        return root.getVisibility() == VISIBLE;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(){
        completed = true;
    }

    public void hideContainer(){}


    public void setActive(){
        active = true;
    }
    public void setInactive(){
        active = false;
    }


    /**
     * Apply a visual effect when the validation failed
     */
    public final void setError() {
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                this,
                PropertyValuesHolder.ofFloat("scaleX", 1.03f),
                PropertyValuesHolder.ofFloat("scaleY", 1.03f));
        scaleDown.setDuration(200);
        scaleDown.setStartDelay(300);
        scaleDown.setRepeatCount(3);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);

        scaleDown.start();

        //root.setBackground(getContext().getDrawable(R.drawable.rounded_background_error));
    }

    /**
     * Remove the visual effect for when the validation fails
     */
    public final void removeError() {
        //root.setBackground(getContext().getDrawable(R.drawable.rounded_background_grey));
    }

    public ViewGroup getRoot() {
        return root;
    }
}
