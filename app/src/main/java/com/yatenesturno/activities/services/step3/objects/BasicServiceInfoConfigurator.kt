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
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.play.core.internal.t
import com.yatenesturno.R
import com.yatenesturno.activities.job_edit.service_configs.*
import com.yatenesturno.activities.services.CreateServiceActivity
import com.yatenesturno.activities.services.step3.objects.objects_views.NewDayPicker
import com.yatenesturno.activities.services.step3.objects.objects_views.NewTimePicker
import com.yatenesturno.activities.services.step3.objects.objects_views.NumberCounter.ListenerOnChange
import com.yatenesturno.object_interfaces.Job
import com.yatenesturno.object_interfaces.ServiceInstance
import java.util.*
import com.yatenesturno.activities.services.step3.objects.classConfigs.AdapterClassTimes as AdapterClassTimes1


class BasicServiceInfoConfigurator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ServiceConfigurationKt(context, attrs),
    ObservableServiceConfiguration {

    val DEBUG = true

    private val observers: MutableList<ObserverServiceConfiguration>? = null
    private var job: Job? = null
    private var adapterClassTimes: AdapterClassTimes1? = null
    private var startTime: Calendar? = null;
    private var endTime: Calendar? = null

    private var classTimes: MutableList<Calendar> = mutableListOf()
    private lateinit var servicePriceEt: EditText
    private lateinit var title: TextView
    private lateinit var basicServiceInfoCompletedIcon: ImageView

    /////////////////////////////////////////////////
    private lateinit var daysLayout: LinearLayout
    private  var serviceDuration: AppCompatTextView? = null
    private lateinit var serviceDurationContraintLayout: LinearLayout
    private lateinit var serviceDurationInfo: ImageButton

    //////////////////////////////////////////////////////
    private var serviceWithFixedTimeInfo: ImageButton? = null
    private lateinit var serviceWithFixedTimeYesBtn: CardView
    private lateinit var serviceWithFixedTimeYesBtnTv: TextView
    private lateinit var serviceWithFixedTimeNoBtn: CardView
    private lateinit var serviceWithFixedTimeNoBtnTv: TextView
    private lateinit var bringClassesTitle: TextView


    ////////////////////////////////////////////////////////
    private var serviceClassFormatInfo: ImageButton? = null
    private lateinit var serviceClassFormatLayout: ConstraintLayout
    private lateinit var serviceClassFormatYesBtn: CardView
    private lateinit var serviceClassFormatYesBtnTv: TextView
    private lateinit var serviceClassFormatNoBtn: CardView
    private lateinit var serviceClassFormatNoBtnTv: TextView
    private lateinit var NoClassesTitle: TextView
    private lateinit var attentionDaysTitle: TextView
    private lateinit var serviceDurationTitle : TextView
    private lateinit var fixedSchedules : TextView

    private lateinit var recyclerViewClassTimes: RecyclerView

    private lateinit var contraintServiceClass: ConstraintLayout

    //////////////////////////////////////////////////////////////////
    private lateinit var serviceClassIntervalsFormat: ConstraintLayout
    private lateinit var serviceIntervalsDurationLayout: LinearLayout
    private lateinit var serviceIntervalsDurationInfo: ImageButton


    //////////////////////////////////////////////////////////////////
    private lateinit var contraintNoServiceClass: ConstraintLayout
    private var serviceNoClassFormatInfo: ImageButton? = null
    private lateinit var serviceNoClassFormatStartTimeCv: CardView
    private lateinit var serviceNoClassFormatStartTimeTv: TextView
    private lateinit var serviceNoClassFormatEndTimeCv: CardView
    private lateinit var serviceNoClassFormatEndTimeTv: TextView
    private lateinit var mainCardVIew: CardView
    private lateinit var scrollContainer: ScrollView

    ///////////////////////////////////////////////////////
    private var classScheduledTimesInfo: ImageButton? = null
    private lateinit var closePopUp: ImageButton

    ////////////////////////////////////////////////////////
    var serviceWithFixedTimeBool: Boolean = false
    var serviceClassFormat: Boolean = false


    private var infoPopupViewsList = mutableListOf<ImageButton>()

    private lateinit var serviceDurationPicker: NewTimePicker
    private lateinit var serviceIntervalDurationPicker: NewTimePicker
    private lateinit var serviceDaysPicker: NewDayPicker

    override fun configurationId(): ConfigurationId {
        return ConfigurationId.BASIC_SERVICE_CONFIG // tODO change
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.basic_service_info_card
    }


    override fun onViewInflated(view: View?) {

        infoPopupViewsList = mutableListOf()
        servicePriceEt = view!!.findViewById(R.id.servicePriceEt)
        daysLayout = view.findViewById(R.id.daysConfigLayout)
        title = view.findViewById(R.id.textView9)
        serviceDurationContraintLayout = view.findViewById(R.id.serviceDurationConstraintLayout)
        serviceDurationInfo = view.findViewById(R.id.service_duration_info)
        serviceWithFixedTimeInfo = view.findViewById(R.id.service_fixed_time_info)
        bringClassesTitle = view.findViewById(R.id.tv_bring_classes_title)


        serviceWithFixedTimeYesBtn = view.findViewById(R.id.service_fixed_time_yes_btn)
        serviceWithFixedTimeNoBtn = view.findViewById(R.id.service_fixed_time_no_btn)
        serviceWithFixedTimeNoBtnTv = view.findViewById(R.id.service_fixed_time_no_btn_tv)
        serviceWithFixedTimeYesBtnTv = view.findViewById(R.id.service_fixed_time_yes_btn_tv)

        serviceClassFormatLayout = view.findViewById(R.id.clServiceClassFormatFixedSch)

        serviceClassFormatInfo = view.findViewById(R.id.service_class_info)
        serviceClassFormatYesBtn = view.findViewById(R.id.service_class_format_yes)
        serviceClassFormatNoBtn = view.findViewById(R.id.service_class_format_no)
        serviceClassFormatYesBtnTv = view.findViewById(R.id.service_class_format_yes_tv)
        serviceClassFormatNoBtnTv = view.findViewById(R.id.service_class_format_no_tv)
        contraintServiceClass = view.findViewById(R.id.contraintServiceClass)
        recyclerViewClassTimes = view.findViewById(R.id.classConfigRecycler)

        serviceClassIntervalsFormat = view.findViewById(R.id.clServiceClassIntervalsFormat)
        serviceIntervalsDurationLayout = view.findViewById(R.id.serviceAppointmentIntervals)
        serviceIntervalsDurationInfo = view.findViewById(R.id.serviceAppointmentIntervalsInfo)
        attentionDaysTitle = view.findViewById(R.id.textView11)
        serviceDurationTitle = view.findViewById(R.id.textView16)
        fixedSchedules = view.findViewById(R.id.textView13)





        contraintNoServiceClass = view.findViewById(R.id.contraintNoServiceClass)
        serviceNoClassFormatInfo = view.findViewById(R.id.service_no_class_schedules_info)
        serviceNoClassFormatStartTimeCv = view.findViewById(R.id.service_start_time_btn)
        serviceNoClassFormatStartTimeTv = view.findViewById(R.id.service_start_time_tv)
        serviceNoClassFormatEndTimeTv = view.findViewById(R.id.service_end_time_tv)
        serviceNoClassFormatEndTimeCv = view.findViewById(R.id.service_end_time_btn)
        scrollContainer = view.findViewById(R.id.needed_service_info_scrollV)
        NoClassesTitle = view.findViewById(R.id.tv_no_classes_title_2)
        basicServiceInfoCompletedIcon = view.findViewById(R.id.basicServiceInfoCompletedIcon)

        serviceNoClassFormatStartTimeTv.setOnClickListener {
            configStartTime()
        }

        serviceNoClassFormatEndTimeTv.setOnClickListener {
            configEndTime(view)
        }

        classScheduledTimesInfo = view.findViewById(R.id.service_add_class_schedules_info)
        mainCardVIew = view.findViewById(R.id.basicServiceInfocardview)



        serviceDurationPicker = NewTimePicker(serviceDurationContraintLayout)
        serviceIntervalDurationPicker = NewTimePicker(serviceIntervalsDurationLayout)
        serviceDaysPicker = NewDayPicker(daysLayout)


        setPickers()

        serviceWithFixedTimeYesBtn.setOnClickListener {
            serviceWithFixedTimeBool = true
            setClicksListener()

        }
        serviceWithFixedTimeNoBtn.setOnClickListener {
            serviceWithFixedTimeBool = false
            setClicksListener()
        }

        serviceClassFormatNoBtn.setOnClickListener {
            serviceClassFormat = false
            setClicksListener()
        }
        serviceClassFormatYesBtn.setOnClickListener {
            serviceClassFormat = true
            setClicksListener()
        }

        setInfoPopUpViews()
        setInfoPopups(view)
        classTimes = ArrayList()
        initRecyclerViewClassTimes()
        setClicksListener()
        initTimeSlot()
        active = true

        val noClassesTitleText = "Determina horarios de atención en que brinda este servicio "
        val noClassesTitleText2 = "<font color='#FF8672'>*</font>"
        NoClassesTitle.text = Html.fromHtml(noClassesTitleText + noClassesTitleText2)
        val attentionDaysTitleText = "Días de atención para este servicio"
        attentionDaysTitle.text =Html.fromHtml(attentionDaysTitleText + noClassesTitleText2)
        val serviceDurationTitleText = "Duracion del servicio"
        serviceDurationTitle.text =Html.fromHtml(serviceDurationTitleText + noClassesTitleText2)
        val fixedSchedulesText = "Servicio con horarios fijos"
        fixedSchedules.text = Html.fromHtml(fixedSchedulesText + noClassesTitleText2)
        val bringClassesTitleText = "¿Este servicio se brinda en formato de clases?"
        bringClassesTitle.text = Html.fromHtml(bringClassesTitleText + noClassesTitleText2)


    }

    private fun setClick(){
        val act = context as CreateServiceActivity
        title.setOnClickListener {
            scrollContainer.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())

        }
        mainCardVIew.setOnClickListener {
            scrollContainer.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())
        }
        rootViewV.setOnClickListener {
            scrollContainer.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())

        }
    }

    fun getPrice(): String {
        return if (servicePriceEt.text.isNullOrEmpty()) {
            "0"
        } else servicePriceEt.text.toString()
    }

    private fun setPickers() {
        serviceIntervalDurationPicker.setStep(5)
        serviceIntervalDurationPicker.setMax(1)
        serviceIntervalDurationPicker.setOnChangeListener(object : ListenerOnChange {
            override fun onChange() {
                if (serviceIntervalDurationPicker.isDisplayingHours()) {
                    serviceIntervalsDurationInfo.visibility = View.INVISIBLE
                } else {
                    serviceIntervalsDurationInfo.visibility = View.VISIBLE

                }
                notifyObservers()
            }
        })

        serviceDurationPicker.setStep(5)
        serviceDurationPicker.setOnChangeListener(object : ListenerOnChange {
            override fun onChange() {
                if (serviceDurationPicker.isDisplayingHours()) {
                    serviceDurationInfo.visibility = View.INVISIBLE
                } else {
                    serviceDurationInfo.visibility = View.VISIBLE

                }
                adapterClassTimes = AdapterClassTimes1(context, serviceDurationPicker.time!!, classTimes){
                    Log.d("updated in setpickers","Set picers")
                        updateValidation()
                    }
                recyclerViewClassTimes.adapter = adapterClassTimes
                notifyObservers()
            }
        })

    }


    fun getClassTimes(): List<Calendar?> {
        return classTimes
    }


    private fun initRecyclerViewClassTimes() {
        val lm = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recyclerViewClassTimes.layoutManager = lm
        recyclerViewClassTimes.itemAnimator = null
        adapterClassTimes = AdapterClassTimes1(context, serviceDurationPicker.time!!, classTimes){
            updateValidation()
        }
        recyclerViewClassTimes.adapter = adapterClassTimes
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setClicksListener() {
        if (serviceWithFixedTimeBool) {
            ///button pressed UI
            serviceWithFixedTimeYesBtn.setCardBackgroundColor(Color.parseColor("#FF8672"))
            serviceWithFixedTimeYesBtnTv.setTextColor(Color.parseColor("#FFFFFF"))
            serviceWithFixedTimeNoBtnTv.setTextColor(Color.parseColor("#717171"))
            serviceWithFixedTimeNoBtn.setCardBackgroundColor(Color.parseColor("#FFFFFF")) /////

            serviceClassFormatLayout.visibility = View.VISIBLE
            serviceClassIntervalsFormat.visibility = View.INVISIBLE
        } else {
            serviceWithFixedTimeYesBtn.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            serviceWithFixedTimeYesBtnTv.setTextColor(Color.parseColor("#717171"))

            serviceWithFixedTimeNoBtnTv.setTextColor(Color.parseColor("#FFFFFF"))
            serviceWithFixedTimeNoBtn.setCardBackgroundColor(Color.parseColor("#FF8672"))
            serviceClassIntervalsFormat.visibility = View.VISIBLE

            serviceClassFormatLayout.visibility = View.INVISIBLE
        }

        if (serviceClassFormat) {
            recyclerViewClassTimes.visibility = View.VISIBLE
            contraintServiceClass.visibility = View.VISIBLE
            contraintNoServiceClass.visibility = View.INVISIBLE
            serviceClassFormatYesBtn.setCardBackgroundColor(Color.parseColor("#FF8672"))
            serviceClassFormatYesBtnTv.setTextColor(Color.parseColor("#FFFFFF"))

            serviceClassFormatNoBtnTv.setTextColor(Color.parseColor("#717171"))
            serviceClassFormatNoBtn.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
        } else {
            classTimes.clear()
            adapterClassTimes?.notifyDataSetChanged()


            recyclerViewClassTimes.visibility = View.INVISIBLE
            contraintServiceClass.visibility = View.INVISIBLE
            contraintNoServiceClass.visibility = View.VISIBLE
            serviceClassFormatYesBtn.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            serviceClassFormatYesBtnTv.setTextColor(Color.parseColor("#717171"))
            serviceClassFormatNoBtnTv.setTextColor(Color.parseColor("#FFFFFF"))
            serviceClassFormatNoBtn.setCardBackgroundColor(Color.parseColor("#FF8672"))
        }

        updateValidation()
        Log.d("updated in setClicksListener","Set picers")

    }

    override fun acceptVisitor(visitor: VisitorServiceConfiguration?) {
        visitor!!.visit(this);
    }

    fun getDuration(): Calendar? {
        return serviceDurationPicker.time
    }

    fun setJob(job: Job) {
        this.job = job
    }


    override fun saveState(bundle: Bundle?) {

        //TODO REVISAR
        val selectedDays: ArrayList<Int> = ArrayList(serviceDaysPicker.getSelectedDays()!!)
        bundle!!.putSerializable(configurationId().toString(), serviceIntervalDurationPicker.time)
        bundle.putSerializable(configurationId().toString(), serviceDurationPicker.time)
        bundle.putSerializable(configurationId().toString() + "_DAYS", selectedDays)
        bundle.putString(configurationId().toString(), servicePriceEt.text.toString())
        bundle.putBoolean(configurationId().toString(), serviceClassFormat)
        bundle.putSerializable(
            configurationId().toString() + "_CLASS_TIMES",
            classTimes as ArrayList<Calendar?>
        )
    }

    override fun recoverState(bundle: Bundle?) {
        serviceDurationPicker.time =
            (bundle!!.getSerializable(configurationId().toString()) as Calendar?)
        serviceIntervalDurationPicker.time =
            bundle.getSerializable(configurationId().toString()) as Calendar?

        val selectedDays = bundle.getSerializable(
            configurationId().toString() + "_DAYS"
        ) as List<*>?
        for (day in selectedDays!!) {
            serviceDaysPicker.selectDay(day as Int)
        }
        serviceClassFormat = bundle.getBoolean(configurationId().toString())
        classTimes =
            bundle.getSerializable(configurationId().toString() + "_CLASS_TIMES") as MutableList<Calendar>
    }

    fun isFixedSchedule(): Boolean {
        return serviceWithFixedTimeBool
    }

    fun getStartTime(): Calendar? {
        return startTime
    }

    fun getEndTime(): Calendar? {
        return endTime
    }
    ////////////////////////////// CLASS TIMES CONFIG ///////////////////////////////////


    override fun initFromServiceInstance(serviceInstance: ServiceInstance?) {
        servicePriceEt.setText(serviceInstance!!.price.toString())
        serviceDurationPicker.time = (serviceInstance.duration)

        for (ds in job!!.daySchedules) {
            val si = ds.getServiceInstanceForService(
                serviceInstance.service
                    .id
            )
            if (si != null && !si.isEmergency) {
                serviceDaysPicker.selectDay(ds.dayOfWeek)
            }
        }

        if (serviceInstance.isClassType) {
            if (serviceInstance.classTimes != null) {
                classTimes = ArrayList(serviceInstance.classTimes)
            }
        }
        updateValidation()
    }

    private fun initTimeSlot() {
        startTime = Calendar.getInstance()
        startTime!!.set(Calendar.HOUR_OF_DAY, 6)
        startTime!!.set(Calendar.MINUTE, 0)

        endTime = Calendar.getInstance()
        endTime!!.set(Calendar.HOUR_OF_DAY, 23)
        endTime!!.set(Calendar.MINUTE, 0)
        updateTimeLabels()
    }

    private fun configStartTime() {
        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTitleText("Horario de comienzo")
            .setHour(6)
            .setPositiveButtonText("Seleccionar")
            .setNegativeButtonText("Salir")
            .setMinute(0)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .build()
        materialTimePicker.show((context as CreateServiceActivity).supportFragmentManager, "")
        materialTimePicker.addOnPositiveButtonClickListener {
            startTime?.set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
            startTime?.set(Calendar.MINUTE, materialTimePicker.minute)
            updateTimeLabels()
            updateValidation()

        }
    }

    private fun configEndTime(view: View) {
        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTitleText("Horario de fin")
            .setHour(23)
            .setPositiveButtonText("Seleccionar")
            .setNegativeButtonText("Salir")
            .setMinute(0)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .build()
        materialTimePicker.show((context as CreateServiceActivity).supportFragmentManager, "")
        materialTimePicker.addOnPositiveButtonClickListener {
            endTime?.set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
            endTime?.set(Calendar.MINUTE, materialTimePicker.minute)
            updateTimeLabels()
            updateValidation()

        }
    }

    fun isAValidEndTime(hourOfDay: Int, minute: Int): Boolean {
        return if (isFollowingDay(hourOfDay, minute)) {
            isOtherDayValid(hourOfDay, minute)

        } else isAtLeastAnHourLaterThanCalendar(hourOfDay, startTime)
                || isMidNight(hourOfDay, minute)
                || isMidNight(
            startTime!![Calendar.HOUR_OF_DAY],
            startTime!![Calendar.MINUTE]
        )
    }

    private fun isFollowingDay(hourOfDay: Int, minute: Int): Boolean {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = hourOfDay
        calendar[Calendar.MINUTE] = minute

//        if day start is greater than given time, e.g start: 8:00, end: 00:30
//        thus endtime is in the following day
        return startTime!![Calendar.HOUR_OF_DAY] > calendar[Calendar.HOUR_OF_DAY]
    }


    private fun isOtherDayValid(hourOfDay: Int, minute: Int): Boolean {
        val minuteThreshold = 60
        val inMinutes = hourOfDay * 60 + minute
        return inMinutes <= minuteThreshold
    }

    private fun isMidNight(hourOfDay: Int, minute: Int): Boolean {
        return hourOfDay == 0 && minute == 0
    }

    private fun isAtLeastAnHourEarlierThanCalendar(hour: Int, calendar: Calendar?): Boolean {
        return calendar == null || hour < calendar[Calendar.HOUR_OF_DAY]
    }

    private fun isAtLeastAnHourLaterThanCalendar(hour: Int, calendar: Calendar?): Boolean {
        return calendar == null || hour > calendar[Calendar.HOUR_OF_DAY]
    }

    private fun updateTimeLabels() {
        val startStr = String.format(
            "%02d:%02d",
            startTime!![Calendar.HOUR_OF_DAY], startTime!![Calendar.MINUTE]
        )
        val endStr = String.format(
            "%02d:%02d",
            endTime!![Calendar.HOUR_OF_DAY], endTime!![Calendar.MINUTE]
        )
        serviceNoClassFormatStartTimeTv.text = startStr
        serviceNoClassFormatEndTimeTv.text = endStr

    }

    private fun hasSelectedDate(calendar: Calendar): Boolean {
        return calendar[Calendar.MINUTE] != 0 ||
                calendar[Calendar.HOUR_OF_DAY] != 0
    }

    fun getSelectedDays(): List<Int> {
        return serviceDaysPicker.getSelectedDays()
    }

    fun isAValidStartTime(hourOfDay: Int, minute: Int): Boolean {
        return if (isFollowingDay(
                endTime!![Calendar.HOUR_OF_DAY],
                endTime!![Calendar.MINUTE]
            )
        ) {
            isOtherDayValid(
                endTime!![Calendar.HOUR_OF_DAY],
                endTime!![Calendar.MINUTE]
            )
        } else isAtLeastAnHourEarlierThanCalendar(hourOfDay, endTime)
                || isMidNight(hourOfDay, minute)
                || isMidNight(
            endTime!![Calendar.HOUR_OF_DAY],
            endTime!![Calendar.MINUTE]
        )
    }

    override fun validate(): ValidationResult {
        hideKeyBoard();
        if (serviceDaysPicker.getSelectedDays()?.size == 0) {
            return ValidationResult(false, R.string.req_select_at_least_one_day)
        }
        if (serviceClassFormat) {
            if (classTimes.size == 0) {
                return ValidationResult(false, R.string.req_select_at_least_one_class_time)
            }
        }

        if (!serviceDurationPicker.time?.let { hasSelectedDate(it) }!!) {
            return ValidationResult(false, R.string.req_select_duration)
        }
        if (serviceWithFixedTimeBool && serviceClassFormat) {
            if (classTimes.isEmpty()){
                return ValidationResult(false, R.string.need_to_add_class_time)
            }
        }
        if (servicePriceEt.text.equals("0") ) {
            ValidationResult(false, R.string.req_select_price)
        } else if (serviceDuration?.text == "0") {
            ValidationResult(false, R.string.duration)
        }else if(servicePriceEt.text == null){
            ValidationResult(false, R.string.req_select_price)
        }
        if (!serviceWithFixedTimeBool) {
            if (!serviceIntervalDurationPicker.time?.let { hasSelectedDate(it) }!!) {
                return ValidationResult(false, R.string.select_app_interval)
            }
        }

        //TODO validate algo mas porque ta fallando con las clases
        if (endTime != null && startTime != null){
            if (!isFollowingDay(
                    endTime!![Calendar.HOUR_OF_DAY],
                    endTime!![Calendar.MINUTE]
                ) && !isAValidStartTime(
                    startTime!![Calendar.HOUR_OF_DAY], startTime!![Calendar.MINUTE]
                )
            ) {
                return ValidationResult(false, R.string.start_time_invalid)
            }
        }

        if (endTime != null && startTime != null) {
            if (!isFollowingDay(
                    endTime!![Calendar.HOUR_OF_DAY],
                    endTime!![Calendar.MINUTE]
                ) &&
                !isAValidEndTime(endTime!![Calendar.HOUR_OF_DAY], endTime!![Calendar.MINUTE])
            ) {
                return ValidationResult(false, R.string.end_time_invalid)
            } else if (isFollowingDay(
                    endTime!![Calendar.HOUR_OF_DAY],
                    endTime!![Calendar.MINUTE]
                )
            ) {
                if (!isOtherDayValid(
                        endTime!![Calendar.HOUR_OF_DAY],
                        endTime!![Calendar.MINUTE]
                    )
                ) {
                    return ValidationResult(false, R.string.limit_bound_upper_Extension_err)
                }
            }
        }
        return ValidationResult(true)
    }
    fun getInterval(): Calendar? {
        return serviceIntervalDurationPicker.time
    }
    override fun hideContainer() {
        super.hideContainer()
        scrollContainer.visibility =View.GONE
    }
    private fun showSnackBar(stringResourceId: Int) {
        try {
            val string : String = root.context.resources.getString(stringResourceId)
            string.let { str ->
               Snackbar.make(root, str, Snackbar.LENGTH_SHORT).show()
            }
        }catch (e : Exception){

        }
    }



    private fun hideKeyBoard() {
        servicePriceEt.clearFocus()
        val imm = servicePriceEt.context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(servicePriceEt.windowToken, 0)
    }

    override fun reset() {
        Log.d("resetReset", "reset")
        serviceDaysPicker.clearSelection()
        serviceDurationPicker.reset()
    }

    override fun attach(observer: ObserverServiceConfiguration?) {
        if (observer != null) {
            this.observers!!.add(observer)
            Log.d("attached", "atachh")
        };
    }

    override fun detach(observer: ObserverServiceConfiguration?) {
        if (observer != null) {
            this.observers!!.remove(observer)
        };
    }

    override fun notifyObservers() {
        if (observers != null) {
            for (observer in observers) {
                observer.onUpdate()
            }
        }
    }

    private fun updateValidation(){

        if (validate().result){
            basicServiceInfoCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF8672"))
            val act = context as CreateServiceActivity
            if(!completed){
                act.nextServiceFragment()
            }
        }else{
            basicServiceInfoCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#D8F7C2C5"))
            showSnackBar(validate().messageResourceId)
        }
    }


    private fun setInfoPopups(view: View) {
        val tempList = mutableListOf<InfoPopUp>()

        val serviceDurationSpan =
            SpannableString("Ingresa días en los que brindes tus servicios y el tiempo que demora en realizarse dicho servicio")
        tempList.add(
            InfoPopUp(
                title = "Duración de servicio",
                description = serviceDurationSpan
            )
        )
        val serviceFixedDurationSpan =
            SpannableString("Selecciona esta opción si los horarios en que brindas tu servicio siempre serán fijos y no habrán turnos intermedios. Por ejemplo: se podrán configurar turnos a las 8 am, 9 am, 10 am, etc., por lo que un cliente no será capaz de sacar un turno a las 9:20 am.")
        serviceFixedDurationSpan.setSpan(
            StyleSpan(Typeface.BOLD),
            118, // start
            130, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        tempList.add(
            InfoPopUp(
                title = "Servicio con horarios fijos",
                description = serviceFixedDurationSpan
            )
        )

        val serviceClassFormatSpan =
            SpannableString("Seleccioná esta opción si brindás este servicio en formato de clase. La cantidad de clientes en la clase será determinada por la cantidad de personas que puedas atender al mismo tiempo.")

        tempList.add(
            InfoPopUp(
                title = "Servicio en formato de clases",
                description = serviceClassFormatSpan
            )
        )
        val classTimesSpan =
            SpannableString("Seleccioná esta opción si brindás este servicio en formato de clases. La cantidad de clientes en cada clase será determinada por la cantidad de personas seleccionadas con anterioridad. Este modo es recomendado para lugares que brinden este servicio a más de  2 clientes por turno Podés remover un horario del listado al mantenerlo presionado.")
        classTimesSpan.setSpan(
            StyleSpan(Typeface.BOLD),
            185, // start
            279, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )

        tempList.add(
            InfoPopUp(
                title = "Horarios de las clases",
                description = classTimesSpan
            )
        )

        val noClassTimesSpan =
            SpannableString("Ingresá el horario en que comenzas y finalizas la atención de tu servicio")

        tempList.add(
            InfoPopUp(
                title = "Horarios de atención en que se brinda el servicio",
                description = noClassTimesSpan
            )
        )

        val intervalAppointments =
            SpannableString("Ingresá el tiempo de intervalo que se debe esperar para tomar un turno al siguiente del mismo servicio")

        tempList.add(
            InfoPopUp(
                title = "Intervalos de tiempo entre turnos de este servicio",
                description = intervalAppointments
            )
        )

        val infoPopupList: MutableList<InfoPopUp> = mutableListOf()
        infoPopupList.addAll(tempList).let {
            for (i in infoPopupViewsList.withIndex()) {
                infoPopupViewsList[i.index].setOnClickListener {
                    inflatePopupBtn(view, infoPopupList[i.index])
                    if (DEBUG){
                        val act = context as CreateServiceActivity
                        act.nextServiceFragment()
                    }

                }
            }
        }


    }
    fun isClass(): Boolean {
        return serviceClassFormat
    }

    private fun inflatePopupBtn(view: View, iInfoPopUp: InfoPopUp) {
        val inflater = root.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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


    private fun setInfoPopUpViews() {
        serviceDurationInfo.let {
            infoPopupViewsList.add(it)
        }
        serviceWithFixedTimeInfo?.let {
            infoPopupViewsList.add(it)
        }
        serviceClassFormatInfo?.let {
            infoPopupViewsList.add(it)

        }
        classScheduledTimesInfo?.let {
            infoPopupViewsList.add(it)
        }
        serviceNoClassFormatInfo?.let {
            infoPopupViewsList.add(it)
        }

        serviceIntervalsDurationInfo.let {
            infoPopupViewsList.add(it)
        }


    }


    override fun updateUI(active: Boolean?) {

        if (active == true){
                scrollContainer.visibility = View.VISIBLE
            val act = context as CreateServiceActivity
            act.serviceConfigureFragment?.hideAllServices(configurationId())
            wasEdited = true
        }else{
                scrollContainer.visibility = View.GONE
        }

        //basicServiceInfoCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF8672"))
    }

    override fun setInactive() {
        super.setInactive()
        scrollContainer = rootViewV.findViewById(R.id.needed_service_info_scrollV)
        scrollContainer.visibility = View.GONE
//        rootView.setOnClickListener {
//            updateUI(true)
//        }
        Log.d("settedInactive","BasicSericviceIfnfo")

    }

    override fun setActive() {
        title = rootViewV.findViewById(R.id.textView9)
        mainCardVIew = rootViewV.findViewById(R.id.basicServiceInfocardview)
        scrollContainer = rootViewV.findViewById(R.id.needed_service_info_scrollV)
        val act = context as CreateServiceActivity

        basicServiceInfoCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#D8F7C2C5"))
        title.setTextColor(Color.parseColor("#FF8672"))
        mainCardVIew.foreground = ContextCompat.getDrawable(context,R.drawable.border_outline_work_here_cv_selected)

        title.setOnClickListener {
            scrollContainer.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())

        }
        mainCardVIew.setOnClickListener {
            scrollContainer.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())

        }
        rootViewV.setOnClickListener {
            scrollContainer.visibility =View.VISIBLE
            act.serviceConfigureFragment?.hideAllServices(configurationId())

        }
        wasEdited = true
    }

    override fun setCompleted() {
        super.setCompleted()
        basicServiceInfoCompletedIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF8672"))
    }


}