package com.yatenesturno.activities.services.step3.objects

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.yatenesturno.R
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration
import com.yatenesturno.activities.services.CreateServiceActivity
import com.yatenesturno.object_interfaces.ServiceInstance

class SimultShiftsConfigurator  @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ServiceConfigurationKt(context, attrs){


    // VARS
    private lateinit var btnIncrease : ImageButton
    private lateinit var btnDecrease :  ImageButton
    private lateinit var clientsTv :  TextView
    private lateinit var title :  TextView
    private lateinit var simultClientsText :  TextView
    private lateinit var mainCardVIew :  CardView
    private lateinit var simultShiftsInfo :  ImageButton
    lateinit var simultShiftsCompletedIcon :  ImageView
    lateinit var cointainer :  ConstraintLayout
    var inited = false

    private var counter = 0

    override fun configurationId(): ConfigurationId {
        return ConfigurationId.SIMULT_SHIFTS
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.simult_shifts
    }

    fun getMaxAppsSimultaneously(): Int {
        return if (counter > 0) {
            counter
        } else -1
    }


    override fun onViewInflated(view: View?) {

       btnDecrease = view!!.findViewById(R.id.btnDecreaseSimultShift)
       btnIncrease = view.findViewById(R.id.btnIncreaseSimultShift)
        clientsTv = view.findViewById(R.id.simultShift_client_tv)
        simultShiftsInfo = view.findViewById(R.id.simultaneousShiftsInfo)
        simultShiftsCompletedIcon = view.findViewById(R.id.simultaneousShiftsCompletedIcon)
        title = view.findViewById(R.id.simultShiftTitle)
        mainCardVIew = view.findViewById(R.id.simultShift_cardview)
        cointainer = view.findViewById(R.id.containerViewSimultShifts)
        simultClientsText  = view.findViewById(R.id.textView11)
        val noClassesTitleText = "¿A cuántos clientes puede brindar este servicio al mismo tiempo?"
        val noClassesTitleText2 = "<font color='#FF8672'>*</font>"
        simultClientsText.text = Html.fromHtml(noClassesTitleText + noClassesTitleText2)



        btnIncrease.setOnClickListener {
            increaseCounter()
            updateCounterUi()
        }

        btnDecrease.setOnClickListener {
            decreaseCounter()
            updateCounterUi()
        }

        simultShiftsInfo.setOnClickListener {
            inflatePopupBtn(view)
        }


    }

    override fun acceptVisitor(visitor: VisitorServiceConfiguration?) {

    }

    override fun saveState(bundle: Bundle?) {
        bundle!!.putInt("simult_shift" + "_SAME_APPOINTMENT", getMaxAppsPerDay())
    }

    override fun recoverState(bundle: Bundle?) {
        counter = bundle!!.getInt("simult_shift" + "_SAME_APPOINTMENT")
        updateCounterUi()
    }

    override fun initFromServiceInstance(serviceInstance: ServiceInstance?) {
        counter = serviceInstance!!.maxAppsSimultaneously
        updateCounterUi()
    }

    override fun validate(): ValidationResult {
        return ValidationResult(counter >= 1, R.string.min_appointment)
    }

    override fun updateUI(active: Boolean?) {
        title = rootViewV.findViewById(R.id.simultShiftTitle)
        mainCardVIew = rootViewV.findViewById(R.id.simultShift_cardview)
        cointainer = rootViewV.findViewById(R.id.containerViewSimultShifts)

        if(active!!){

            title.setTextColor(Color.parseColor("#FF8672"))
            mainCardVIew.foreground = ContextCompat.getDrawable(context,R.drawable.border_outline_work_here_cv_selected)
            cointainer.visibility =View.VISIBLE
        }else{
            cointainer = rootViewV.findViewById(R.id.containerViewSimultShifts)
            cointainer.visibility = View.GONE
        }

    }
    override fun hideContainer() {
        super.hideContainer()
        cointainer.visibility =View.GONE
    }
    fun getConcurrency(): Int {
        return counter
    }
    override fun setActive() {
        title = rootViewV.findViewById(R.id.simultShiftTitle)
        mainCardVIew = rootViewV.findViewById(R.id.simultShift_cardview)
        cointainer = rootViewV.findViewById(R.id.containerViewSimultShifts)
        val act = context as CreateServiceActivity

        simultShiftsCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#D8F7C2C5"))
        title.setTextColor(Color.parseColor("#FF8672"))
        mainCardVIew.foreground = ContextCompat.getDrawable(context,R.drawable.border_outline_work_here_cv_selected)

        title.setOnClickListener {
            cointainer.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())

        }
        mainCardVIew.setOnClickListener {
            cointainer.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())

        }
        rootViewV.setOnClickListener {
            cointainer.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())

        }
        wasEdited = true

    }

    override fun setInactive() {
        super.setInactive()
        cointainer = rootViewV.findViewById(R.id.containerViewSimultShifts)
        cointainer.visibility = View.GONE
    }



    override fun reset() {
        counter = 0
    }


    fun increaseCounter() {
        updateValidation()
        counter++
    }

    fun decreaseCounter() {
        counter--
    }

    fun init() {
        counter = 0
    }

    fun getDisplayableText(): String {
        return counter.toString() + ""
    }
    private fun updateValidation(){
        if (validate().result){
            Log.d("ValidatedTrue", "valdated")
            simultShiftsCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF8672"))
            val act = context as CreateServiceActivity
            if(!completed){
                act.nextServiceFragment()
            }
        }else{
            Log.d("ValidatedFalse", "not validated")
            simultShiftsCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#D8F7C2C5"))
            showSnackBar(validate().messageResourceId)
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

    fun updateCounterUi(){
        updateValidation()
        clientsTv.text = getDisplayableText()
        if (isAtBottomLimit()) {
            btnDecrease.visibility = INVISIBLE
        } else {
            btnDecrease.visibility = VISIBLE
        }
    }

     fun isAtBottomLimit(): Boolean {
        return counter <= 1
    }
    fun getMaxAppsPerDay(): Int {
        return counter
    }
    private fun inflatePopupBtn(view: View ){
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
        popupTitle.text = "Cantidad de clientes que puedes atender al mismo tiempo"
        val descriptionSpannable = SpannableString("Seleccioná la cantidad de clientes que vos, como prestador de este servicio, puedes atender al mismo tiempo. Habrán turnos disponibles para dicho servicio en ese horario según se determine en este items. Por ejemplo: si sos peluquero y haces un corte, podrías atender a una sola persona; pero si tenés dos o más canchas de padel, podés atender a todas en simultáneo.")
        descriptionSpannable.setSpan( StyleSpan(Typeface.BOLD),
            109, // start
            216, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        popupDescription.text = descriptionSpannable
        popupView.elevation = 10f
        closePopUp.setOnClickListener(OnClickListener {
            popupWindow.dismiss()
        })
    }

    override fun setCompleted() {
        super.setCompleted()
        simultShiftsCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF8672"))

    }






}