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
import android.view.View.OnClickListener
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import com.yatenesturno.R
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration
import com.yatenesturno.activities.services.CreateServiceActivity
import com.yatenesturno.object_interfaces.ServiceInstance

class ClientPermissions@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ServiceConfigurationKt(context, attrs){

    private var numberCounterMaxAppsPerDay = 0
    private  var numberCounterMaxAppsSimultaneously =  0
    private lateinit var maxAppsPerDayTv : AppCompatTextView
    private lateinit var maxAppsSimultaneouslyTv : AppCompatTextView
    private lateinit var maxAppsPerDayIncrBtn : ImageButton
    private lateinit var maxAppsPerDayDecrBtn : ImageButton
    private lateinit var maxAppsPerDaInfo : ImageButton
    private lateinit var maxAppsSimultaneouslyIncrBtn : ImageButton
    private lateinit var maxAppsSimultaneouslyDecrBtn : ImageButton
    private lateinit var maxAppsSimultaneouslyInfo : ImageButton
    private lateinit var clientpermissionsCompletedIcon : ImageView
    private lateinit var title : TextView
    private var infoPopupViewsList = mutableListOf<ImageButton>()
    private lateinit var mainCardVIew : CardView



    override fun configurationId(): ConfigurationId {
        return ConfigurationId.CLIENT_PERMISSIONS
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.client_permissions
    }


    override fun onViewInflated(view: View?) {
        maxAppsPerDayTv = view!!.findViewById(R.id.custom_clients_shifs_tv)
        maxAppsSimultaneouslyTv = view.findViewById(R.id.custom_clients_simult_shifts_tv)
        maxAppsPerDayDecrBtn = view.findViewById(R.id.custom_clients_shifs_decrease_btn)
        maxAppsPerDaInfo = view.findViewById(R.id.service_custom_clients_info)
        maxAppsPerDayIncrBtn = view.findViewById(R.id.custom_clients_shifs_increase_btn)
        maxAppsSimultaneouslyIncrBtn = view.findViewById(R.id.custom_clients_increase_btn)
        maxAppsSimultaneouslyDecrBtn = view.findViewById(R.id.custom_clients_decrease_btn)
        maxAppsSimultaneouslyInfo = view.findViewById(R.id.service_additional_shift_simultaneous)
        clientpermissionsCompletedIcon = view.findViewById(R.id.clientpermissionsCompletedIcon)
        title = view.findViewById(R.id.title_clientPermission)

        mainCardVIew = view.findViewById(R.id.customers_permits_cardview)

        setClicksListeners()
        updateCounterUi()
        addPopUps(view)



    }

    private fun addPopUps(view: View) {
        val tempList = mutableListOf<InfoPopUp>()

        tempList.add(
            InfoPopUp(
                title = "Turnos de clientes por día",
                description = SpannableString("Limita la cantidad de turnos que podrá solicitar un cliente por día")
            )
        )
        tempList.add(
            InfoPopUp(
                title = "Turnos simultanéos de clientes por día",
                description = SpannableString("Limita la cantidad de turnos que podrá solicitar un cliente a futuro")
            )
        )


        val infoPopupList  : MutableList<InfoPopUp> = mutableListOf()
        infoPopupList.addAll(tempList).let {
            for (i in  infoPopupViewsList.withIndex()){
                infoPopupViewsList[i.index].setOnClickListener {
                    inflatePopupBtn(view,infoPopupList[i.index])
                }
            }
        }
    }

    private fun setClicksListeners() {
        infoPopupViewsList = mutableListOf()
        maxAppsPerDayDecrBtn.setOnClickListener {
            numberCounterMaxAppsPerDay=  decreaseCounter(numberCounterMaxAppsPerDay)
            updateCounterUi()

        }
        maxAppsPerDayIncrBtn.setOnClickListener {
           numberCounterMaxAppsPerDay=  increaseCounter(numberCounterMaxAppsPerDay)
            updateCounterUi()
        }

        maxAppsSimultaneouslyDecrBtn.setOnClickListener {
            numberCounterMaxAppsSimultaneously = decreaseCounter(numberCounterMaxAppsSimultaneously)
            updateCounterUi()
        }
        maxAppsSimultaneouslyIncrBtn.setOnClickListener {
            numberCounterMaxAppsSimultaneously = increaseCounter(numberCounterMaxAppsSimultaneously)
            updateCounterUi()
        }
        infoPopupViewsList.addAll(listOf(maxAppsSimultaneouslyInfo,maxAppsPerDaInfo))

    }

    override fun acceptVisitor(visitor: VisitorServiceConfiguration?) {
        visitor?.visit(this)
    }

    override fun saveState(bundle: Bundle?) {

        bundle!!.putInt(configurationId().toString() + "_SAME_DAY", numberCounterMaxAppsPerDay)
        bundle.putInt(configurationId().toString() + "_MAX", numberCounterMaxAppsSimultaneously)


    }

    override fun recoverState(bundle: Bundle?) {
        numberCounterMaxAppsPerDay = bundle!!.getInt(configurationId().toString() + "_SAME_DAY")
        numberCounterMaxAppsSimultaneously = bundle.getInt(configurationId().toString() + "_MAX")
        updateCounterUi()
    }

    fun increaseCounter(counter : Int) : Int {
        updateValidation()
        return counter + 1
    }

    fun decreaseCounter(counter : Int) : Int {
        updateValidation()
        return counter - 1
    }

    override fun initFromServiceInstance(serviceInstance: ServiceInstance?) {
        numberCounterMaxAppsPerDay = serviceInstance!!.maxAppsPerDay
        numberCounterMaxAppsSimultaneously = serviceInstance.maxAppsSimultaneously
        updateCounterUi()
    }

    override fun validate(): ValidationResult {
        if (numberCounterMaxAppsSimultaneously > 0 && numberCounterMaxAppsPerDay > 0){
            if (numberCounterMaxAppsSimultaneously < numberCounterMaxAppsPerDay) {
                return ValidationResult(false, R.string.simultaneously_less_than_per_day)
            }
        }

        return ValidationResult(true)

    }



    override fun updateUI(active: Boolean?) {
        val scrollContainer : ConstraintLayout = rootViewV.findViewById(R.id.service_custom_clients_layout)
        if (active!!){
            scrollContainer.visibility = View.VISIBLE
            title.setTextColor(Color.parseColor("#FF8672"))
            mainCardVIew.foreground = rootViewV.context.getDrawable(R.drawable.border_outline_work_here_cv_selected)
            wasEdited = true
        }else{
            scrollContainer.visibility = View.GONE
            title.setTextColor(Color.parseColor("#B4B3B3"))
            mainCardVIew.foreground = rootViewV.context.getDrawable(R.drawable.border_outline_work_here_cv_inactive)
        }
    }

    fun getDisplayableText(counter: Int): String {
        return counter.toString() + ""
    }

    fun updateCounterUi(){
        maxAppsPerDayTv.text = getDisplayableText(numberCounterMaxAppsPerDay)
        maxAppsSimultaneouslyTv.text = getDisplayableText(numberCounterMaxAppsSimultaneously)
        if (isAtBottomLimit(numberCounterMaxAppsSimultaneously)) {
            maxAppsSimultaneouslyDecrBtn.visibility = INVISIBLE
        } else {
            maxAppsSimultaneouslyDecrBtn.visibility = VISIBLE
        }
        if (isAtBottomLimit(numberCounterMaxAppsPerDay)) {
            maxAppsPerDayDecrBtn.visibility = INVISIBLE
        } else {
            maxAppsPerDayDecrBtn.visibility = VISIBLE
        }

    }

    fun isAtBottomLimit(counter: Int): Boolean {
        return counter <= 1
    }

    override fun reset() {
        numberCounterMaxAppsPerDay = 0
        numberCounterMaxAppsSimultaneously = 0
    }




    //popups
    private fun inflatePopupBtn(view: View, iInfoPopUp: InfoPopUp ){
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams") val popupView =
            inflater.inflate(R.layout.info_btn_pupup, null)
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        val closePopUp = popupView.findViewById<ImageButton>(R.id.imageButton2)
        val popupTitle : TextView = popupView.findViewById(R.id.info_popup_title)
        val popupDescription : TextView = popupView.findViewById(R.id.info_popUp_description)
        popupTitle.text = iInfoPopUp.title

        popupDescription.text = iInfoPopUp.description
        popupView.elevation = 10f
        closePopUp.setOnClickListener(OnClickListener {
            popupWindow.dismiss()
        })
    }


    override fun setCompleted() {
        super.setCompleted()

        clientpermissionsCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF8672"))

    }

    private fun updateValidation(){
        Log.d("ValidatedTrue", validate().result.toString())
        Log.d("ValidatedTrue", validate().messageResourceId.toString())

        if (validate().result){
            // setCompleted()
            Log.d("ValidatedTrue", "valdated")
            clientpermissionsCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF8672"))
            val act = context as CreateServiceActivity
            if(!completed){
                act.nextServiceFragment()
            }
        }else{
            Log.d("ValidatedFalse", "not validated")
            clientpermissionsCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#D8F7C2C5"))
            showSnackBar(validate().messageResourceId)
        }
    }


    private fun showSnackBar(stringResourceId: Int) {
        val act = context as CreateServiceActivity
        try {
            val string : String = context.resources.getString(stringResourceId)
            string.let { str ->
                act.window?.decorView?.rootView?.let { Snackbar.make(it, str, Snackbar.LENGTH_SHORT).show() }
            }
        }catch (e : Exception){

        }
    }

    override fun hideContainer() {
        super.hideContainer()
        val scrollContainer : ConstraintLayout = rootViewV.findViewById(R.id.service_custom_clients_layout)
        scrollContainer.visibility = View.GONE
    }

    override fun setActive() {
        super.setActive()
        val act = context as CreateServiceActivity
        Log.d("settedactive", "setedacitv client permission")
        val scrollContainer : ConstraintLayout = rootViewV.findViewById(R.id.service_custom_clients_layout)
        title.setTextColor(Color.parseColor("#FF8672"))
        clientpermissionsCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#D8F7C2C5"))
        mainCardVIew.foreground = context.getDrawable(R.drawable.border_outline_work_here_cv_selected)
        title.setOnClickListener {
            scrollContainer.visibility = View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())
        }
        mainCardVIew.setOnClickListener {
            scrollContainer.visibility = View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())
        }
        rootViewV.setOnClickListener {
            scrollContainer.visibility = View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())
        }
        wasEdited = true

    }
    fun getMaxAppsPerDay(): Int {
        return if (numberCounterMaxAppsPerDay > 0) {
            numberCounterMaxAppsPerDay
        } else -1
    }

    fun getMaxAppsSimultaneously(): Int {
        return if (numberCounterMaxAppsSimultaneously > 0) {
            numberCounterMaxAppsSimultaneously
        } else -1
    }

}