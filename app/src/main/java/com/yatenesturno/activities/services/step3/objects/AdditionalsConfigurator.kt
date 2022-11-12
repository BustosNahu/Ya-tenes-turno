package com.yatenesturno.activities.services.step3.objects

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.yatenesturno.R
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration
import com.yatenesturno.activities.job_edit.service_configs.configurations.ServiceConfiguration
import com.yatenesturno.activities.services.CreateServiceActivity
import com.yatenesturno.object_interfaces.ServiceInstance

class AdditionalsConfigurator constructor(context: Context, attrs: AttributeSet? = null, ) :
    ServiceConfigurationKt(context, attrs){

    private lateinit var completedIcon : ImageView
    private lateinit var title : TextView
    private lateinit var mainCardView: CardView
    private lateinit var checkBoxReminder: CheckBox
    private lateinit var container: ConstraintLayout

    override fun configurationId(): ConfigurationId {
        return ConfigurationId.CONCURRENT_SERVICES //idk
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.additional_configurator_layout
    }

    override fun onViewInflated(view: View?) {
        completedIcon = view!!.findViewById(R.id.additionalsCompletedIcon)
        title = view.findViewById(R.id.additionalsTitle)
        mainCardView = view.findViewById(R.id.additionalCardView)
        checkBoxReminder = view.findViewById(R.id.checkBoxReminder)
        container = view.findViewById(R.id.service_additionals_layout)

    }

    override fun acceptVisitor(visitor: VisitorServiceConfiguration?) {
    }

    override fun saveState(bundle: Bundle?) {
    }

    override fun recoverState(bundle: Bundle?) {
    }

    override fun setActive() {
        super.setActive()
        title.setTextColor(Color.parseColor("#FF8672"))
        completedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF8672"))
        mainCardView.foreground = rootViewV.context.getDrawable(R.drawable.border_outline_work_here_cv_selected)
        val act = context as CreateServiceActivity
        title.setOnClickListener {
            container.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())

        }
        mainCardView.setOnClickListener {
            container.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())
        }
        rootViewV.setOnClickListener {
            container.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())

        }
    }

    override fun setCompleted() {
        super.setCompleted()
        completedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF8672"))
    }

    fun isReminder(): Boolean {
        return checkBoxReminder.isChecked
    }

    override fun initFromServiceInstance(serviceInstance: ServiceInstance?) {
    }

    override fun validate(): ValidationResult {
        return ValidationResult(true)
    }

    override fun updateUI(active: Boolean?) {
    }

    override fun reset() {
    }
}