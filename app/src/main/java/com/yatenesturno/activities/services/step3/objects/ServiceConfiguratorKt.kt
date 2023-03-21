package com.yatenesturno.activities.services.step3.objects

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import com.yatenesturno.R
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration
import com.yatenesturno.object_interfaces.ServiceInstance


abstract class ServiceConfigurationKt @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) :
    LinearLayoutCompat(context, attrs) {
    /**
     * UI References
     */
    val root: ViewGroup
    var rootViewV: View
    var active: Boolean
    var completed: Boolean
    var wasEdited: Boolean

    /**
     * Instance variables
     */
    private lateinit var configCoordinator: ServiceConfigCoordinatorKt
    fun setConfigCoordinator(configCoordinator: ServiceConfigCoordinatorKt?) {
        this.configCoordinator = configCoordinator!!
    }

    /**
     * Run a given visitor among the other configurations
     *
     * @param visitor VisitorServiceConfiguration implementation to run
     */
    protected fun runVisitor(visitor: VisitorServiceConfiguration?) {
        for (serviceConfiguration in configCoordinator.getServiceConfigurationList()) {
            serviceConfiguration.acceptVisitor(visitor)
        }
    }

    /**
     * Return an identifier for this service configuration
     *
     * @return configuration id enum describing this service config
     */
    abstract fun configurationId(): ConfigurationId


    /**
     * Get layout id to be inflated
     *
     * @return resource id of the layout
     */
    protected abstract fun getLayoutResourceId(): Int

    /**
     * Method called once the view gets inflated into root view
     *
     * @param view inflated view
     */
    protected abstract fun onViewInflated(view: View?)

    /**
     * Accept visitor
     *
     * @param visitor incoming visitor
     */
    abstract fun acceptVisitor(visitor: VisitorServiceConfiguration?)
    abstract fun saveState(bundle: Bundle?)
    abstract fun recoverState(bundle: Bundle?)

    /**
     * Init configuration for when the ServiceInstance already exists
     *
     * @param serviceInstance servince instance to get info from
     */
    abstract fun initFromServiceInstance(serviceInstance: ServiceInstance?)

    /**
     * Validate configuration
     *
     * @return Validation result with relevant info of the validation process
     */
    abstract fun validate(): ValidationResult?
    abstract fun updateUI(active: Boolean?)

    /**
     * Reset state to initial one
     */
    abstract fun reset()

    /**
     * hide the configuration
     */
    fun hide() {
        root.visibility = GONE
    }

    /**
     * Show the configuration
     */
    fun show() {
        root.visibility = VISIBLE
    }

    val isVisible: Boolean
        get() = root.visibility == VISIBLE

    open fun setCompleted() {
        completed = true
    }

    open fun hideContainer() {}
    open fun setActive() {
        active = true
    }

    open fun setInactive() {
        active = false
    }

    /**
     * Apply a visual effect when the validation failed
     */
    fun setError() {
        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            this,
            PropertyValuesHolder.ofFloat("scaleX", 1.03f),
            PropertyValuesHolder.ofFloat("scaleY", 1.03f)
        )
        scaleDown.duration = 200
        scaleDown.startDelay = 300
        scaleDown.repeatCount = 3
        scaleDown.repeatMode = ObjectAnimator.REVERSE
        scaleDown.start()

        //root.setBackground(getContext().getDrawable(R.drawable.rounded_background_error));
    }

    /**
     * Remove the visual effect for when the validation fails
     */
    fun removeError() {
        //root.setBackground(getContext().getDrawable(R.drawable.rounded_background_grey));
    }

    init {
        root =
            LayoutInflater.from(context).inflate(R.layout.service_configurator_new_view, this, true)
                .findViewById(R.id.root)
        active = false
        completed = false
        wasEdited = false
        val view = LayoutInflater.from(context).inflate(getLayoutResourceId(), root, true)
        rootViewV = view
        onViewInflated(view)
    }
}
