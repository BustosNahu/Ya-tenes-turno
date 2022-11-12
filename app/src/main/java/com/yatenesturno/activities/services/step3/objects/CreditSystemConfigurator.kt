package com.yatenesturno.activities.services.step3.objects

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import com.yatenesturno.R
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration
import com.yatenesturno.activities.job_edit.service_configs.configurations.ServiceConfiguration
import com.yatenesturno.activities.services.CreateServiceActivity
import com.yatenesturno.object_interfaces.ServiceInstance

class CreditSystemConfigurator  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ServiceConfigurationKt(context, attrs){

    private lateinit var creditSystemTitle : TextView
    private lateinit var mainCardVIew : CardView
    private lateinit var completedIcon : ImageView
    private lateinit var container : ConstraintLayout
    private lateinit var creditSystemInfo : ImageButton
    private lateinit var closePopUp: ImageButton

    private lateinit var switchCompatCredits: SwitchCompat
    private lateinit var switchCompatWithoutCredits: SwitchCompat
    private lateinit var labelCanBookWithoutCredits: AppCompatTextView
    private var nextService = false

    override fun configurationId(): ConfigurationId {
        return ConfigurationId.CREDIT_SYSTEM
    }

    override fun getLayoutResourceId(): Int {
       return R.layout.credit_system_config
    }


    override fun onViewInflated(view: View?) {
        creditSystemTitle = view!!.findViewById(R.id.creditSystemTitle)
        mainCardVIew = view.findViewById(R.id.credits_system_cardView)
        container = view.findViewById(R.id.service_credit_sys)
        completedIcon = view.findViewById(R.id.creditSystemCompletedIcon)


        switchCompatCredits = view.findViewById(R.id.service_credit_sys_subject_switch)
        switchCompatWithoutCredits    = view.findViewById(R.id.service_credit_sys_withoutcredit_switch)
        creditSystemInfo = view.findViewById(R.id.creditSystemInfo)
        creditSystemInfo.setOnClickListener {
            inflatePopupBtn(view, InfoPopUp(
                "Sistema de crédito",
                SpannableString(
                        "Si seleccionas esta opción, la reserva de un turno descontará un crédito de los asignados al cliente.\n" +
                        "Podés cargar créditos en la pantalla de información del cliente.")
            ))
        }

    }

    private fun inflatePopupBtn(view: View, iInfoPopUp: InfoPopUp) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams") val popupView =
            inflater.inflate(R.layout.info_btn_pupup, null)
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        closePopUp = popupView.findViewById<ImageButton>(R.id.imageButton2)
        val popupTitle: TextView = popupView.findViewById(R.id.info_popup_title)
        val popupDescription: TextView = popupView.findViewById(R.id.info_popUp_description)
        popupTitle.text = iInfoPopUp.title
        popupDescription.text = iInfoPopUp.description
        popupView.elevation = 10f
        closePopUp.setOnClickListener(OnClickListener {
            popupWindow.dismiss()
        })
    }

    override fun acceptVisitor(visitor: VisitorServiceConfiguration?) {
    }

    override fun saveState(bundle: Bundle?) {
        bundle!!.putBoolean(configurationId().toString(), switchCompatCredits.isChecked)
        bundle.putBoolean(
            configurationId().toString() + "_CAN_BOOK",
            switchCompatWithoutCredits.isChecked
        )
    }

    override fun recoverState(bundle: Bundle?) {
        switchCompatCredits.isChecked = bundle!!.getBoolean(configurationId().toString())
        switchCompatWithoutCredits.isChecked = bundle.getBoolean(configurationId().toString() + "_CAN_BOOK")
    }

    override fun initFromServiceInstance(serviceInstance: ServiceInstance?) {
    }

    fun isCredits(): Boolean {
        return switchCompatCredits.isChecked
    }
    fun getCanBookWithoutCredits(): Boolean {
        return switchCompatWithoutCredits.isChecked
    }


    override fun validate(): ValidationResult {
        return ValidationResult(true)
    }



    override fun updateUI(active: Boolean?) {

    }
    override fun hideContainer() {
        super.hideContainer()
        container.visibility =View.GONE
    }

    override fun setActive() {
        super.setActive()
        creditSystemTitle.setTextColor(Color.parseColor("#FF8672"))
        mainCardVIew.foreground = rootViewV.context.getDrawable(R.drawable.border_outline_work_here_cv_selected)
        val act = context as CreateServiceActivity
        creditSystemTitle.setOnClickListener {
            container.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())
            setCompleted()
            if (!nextService){
                act.nextServiceFragment()
                nextService = true
            }

        }
        mainCardVIew.setOnClickListener {
            container.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())
            setCompleted()
            if (!nextService){
                act.nextServiceFragment()
                nextService = true
            }
        }
        rootViewV.setOnClickListener {
            container.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())
            setCompleted()
            if (!nextService){
                act.nextServiceFragment()
                nextService = true
            }

        }

    }

    private fun showSnackBar(stringResourceId: Int) {
        val act = context as CreateServiceActivity
        try {
            val string : String = context.resources.getString(stringResourceId)
            string.let { str ->
                act.window?.decorView?.rootView?.let { Snackbar.make(it, str, Snackbar.LENGTH_SHORT).show() }
                Log.d("showScnackerror", str)
            }
        }catch (e : Exception){

        }
    }



    override fun setCompleted() {
        super.setCompleted()
        completedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF8672"))
    }

    override fun reset() {
        switchCompatCredits.isChecked = false
        switchCompatWithoutCredits.isChecked = false
    }


    }