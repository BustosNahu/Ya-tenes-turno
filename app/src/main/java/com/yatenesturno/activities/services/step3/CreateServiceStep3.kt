package com.yatenesturno.activities.services.step3

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.yatenesturno.R
import com.yatenesturno.activities.job_edit.ServiceConfigureFragment
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult
import com.yatenesturno.activities.services.CreateServiceActivity
import com.yatenesturno.activities.services.step3.objects.*
import com.yatenesturno.activities.services.step3.viewModel.Step3ViewModel
import com.yatenesturno.objects.DayScheduleImpl
import com.yatenesturno.objects.ServiceInstanceImpl
import java.util.*


class CreateServiceStep3 : Fragment(), ServiceConfigCoordinatorKt {
    /**
     * Argument keys
     */
    private val sharedVm : Step3ViewModel by activityViewModels()

    var handler = Handler(Looper.getMainLooper())
    private lateinit var job: Job
    private var place: Place? = null
    private var service: Service? = null
    private var serviceConfigurationList: MutableList<ServiceConfigurationKt> = mutableListOf()





    //------------------------------------VIEWS-----------------------------------------\\


    private lateinit var basicServiceInfo: BasicServiceInfoConfigurator
    private lateinit var simultShifts: SimultShiftsConfigurator
    private lateinit var clientPermissions: ClientPermissions
    private lateinit var creditSystem: CreditSystemConfigurator
    private lateinit var additionals: AdditionalsConfigurator
    private lateinit var serviceName: TextView

    var mActivity : Context? = null
    val newList: MutableList<ServiceConfigurationKt> = mutableListOf()

    private lateinit var createServiceBtn: CardView
    private lateinit var backBtn: ImageButton
    private lateinit var servicesList : java.util.ArrayList<ServiceConfigurationKt>

    fun serviceConfigureFragment(place: Place, job: Job, service: Service) {
        this.job = job
        this.place = place
        this.service = service
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_service_step3, container, false)
        createServiceBtn = view.findViewById(R.id.btnCreate_service_step3)
        backBtn = view.findViewById(R.id.back_btn_create_service_3)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler(Looper.myLooper()!!).postDelayed({
            initViews(view)
            savedInstanceState?.let { recoverState(it) }
            initConfigurations()
        }, 300)


    }
    private fun finishConfiguration() {
        removeUnselectedDays()
        removeEmptyDaySchedules()
        val act = activity as CreateServiceActivity
        act.navigateToConfirmation(service, job , getSelectedDays())
        sharedVm.saveData(job, service!!)
    }


    private fun removeEmptyDaySchedules() {
        val toRemove: MutableList<DaySchedule> = ArrayList()
        for (ds in job.daySchedules) {
            if (ds.serviceInstances.isEmpty()) {
                toRemove.add(ds)
            }
        }
        for (ds in toRemove) {
            job.daySchedules.remove(ds)
        }
        sharedVm.saveData(job,service!!)
    }

    private fun removeUnselectedDays() {
        for (ds in job.daySchedules) {
            val si = ds.getServiceInstanceForService(service?.id)
            if (si != null && !getSelectedDays().contains(ds.dayOfWeek)) {
                ds.removeServiceInstance(si)
            }
        }
    }
    private fun createOrUpdateServiceInstance() {
        var serviceInstance = getServiceInstance()
        if (serviceInstance == null) {
            serviceInstance = createServiceInstance()
        }
        updateServiceInstance(serviceInstance)
        val selectedDays: MutableList<Int> = mutableListOf()
        selectedDays.addAll(getSelectedDays())

        for (day in selectedDays) {
            val ds: DaySchedule = getOrCreateDaySchedule(day)
            updateServiceInstanceInDaySchedule(serviceInstance, ds)
        }

        finishConfiguration()
    }

    private fun getOrCreateDaySchedule(day: Int): DaySchedule {
        var  das : DaySchedule? = job.getDaySchedule(day)
        if (das == null) {
            das = createDaySchedule(day)
            Log.d("step3service", das.toString())
                job.addDaySchedule(das)
        }
        return das
    }

    private fun updateServiceInstanceInDaySchedule(
        serviceInstance: ServiceInstance,
        ds: DaySchedule
    ) {
        val it = ds.serviceInstances.iterator()
        var currentSI: ServiceInstance
        while (it.hasNext()) {
            currentSI = it.next()
            if (currentSI.service.id == serviceInstance.service.id) {
                it.remove()
                break
            }
        }
        ds.serviceInstances.add(serviceInstance)
    }
    private fun createDaySchedule(day: Int): DaySchedule {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        start[Calendar.HOUR_OF_DAY] = 8
        start[Calendar.MINUTE] = 0
        end[Calendar.HOUR_OF_DAY] = 20
        end[Calendar.MINUTE] = 0
        return DayScheduleImpl(
            null, day, start, end, null, null, null
        )
    }

    private fun createServiceInstance(): ServiceInstance {
        return ServiceInstanceImpl(null, service)
    }

    private fun getSelectedDays(): List<Int> {
        val act = activity as CreateServiceActivity
        return if (act.isEmergency) {
            Arrays.asList(1, 2, 3, 4, 5, 6, 7)
        } else basicServiceInfo.getSelectedDays()
    }

    fun recoverState(bundle: Bundle) {
        job = bundle.getParcelable(ServiceConfigureFragment.JOB_KEY)!!
        place = bundle.getParcelable(ServiceConfigureFragment.PLACE_KEY)
        service = bundle.getParcelable(ServiceConfigureFragment.SERVICE_KEY)
    }

    fun saveState(bundle: Bundle){
        bundle.putParcelable(ServiceConfigureFragment.JOB_KEY,job)
         bundle.putParcelable(ServiceConfigureFragment.PLACE_KEY,place)
        bundle.putParcelable(ServiceConfigureFragment.SERVICE_KEY,service)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveState(outState)
    }

    private fun initViews(view: View) {

        createServiceBtn = view.findViewById(R.id.btnCreate_service_step3)

        serviceName = view.findViewById(R.id.serviceName)
        simultShifts = view.findViewById(R.id.simultaneous_shifts)
        basicServiceInfo = view.findViewById(R.id.basicServiceInfoConfigurator)
        clientPermissions = view.findViewById(R.id.customers_permits)
        creditSystem = view.findViewById(R.id.credits_system)
        additionals = view.findViewById(R.id.additionals_configurator)

        job.let {
            basicServiceInfo.setJob(it)
        }
        basicServiceInfo

        val act = requireActivity() as CreateServiceActivity
        serviceName.text = act.getService()?.name
        serviceName.width = 700
        serviceName.maxLines = 2

        act.setServicesList(mutableListOf(basicServiceInfo,simultShifts,clientPermissions,creditSystem,additionals))
        createServiceBtn.isEnabled = false
        backBtn.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .add(R.id.CreateServiceFragmentContainer, CreateServiceStep3Intro())
                .commit()
        }
    }

    private fun showSnackBar(stringResourceId: Int) {
        Snackbar.make(requireView(), requireContext().resources.getString(stringResourceId), Snackbar.LENGTH_SHORT).show()
    }


    private fun initConfigurations() {

        serviceConfigurationList = mutableListOf()
        //CHANGE UI SERVICESS
        serviceConfigurationList.add(basicServiceInfo)
        serviceConfigurationList.add(simultShifts)
        serviceConfigurationList.add(clientPermissions)
        serviceConfigurationList.add(creditSystem)
        serviceConfigurationList.add(additionals)


        newList.addAll(serviceConfigurationList)

        for (sc in serviceConfigurationList) {
            sc.setConfigCoordinator(this)
        }

        for (sc in serviceConfigurationList) {
            sc.reset()
        }

    }
    var index = 0
    var lastIndex = 0


    private fun validateContinueAndCreateService(list: MutableList<ServiceConfigurationKt>) : Boolean{
        if (list[0].completed && list[1].completed){
            return true
        }
        return false
    }

    fun hideAllServices(id : ConfigurationId){
        if (newList.isNotEmpty()){
            val removedCurrent = newList.filter { it.configurationId() != id }
            for (i in removedCurrent){
                i.hideContainer()
            }
        }
    }

    fun nextService(list: MutableList<ServiceConfigurationKt>){
        if (newList.isEmpty()){
            newList.clear()
            newList.addAll(list)
        }
        newList[0].setActive()

        Log.d("indexOfCurrentService", lastIndex.toString())
        if (index < 5){
            newList[index].setCompleted()
            newList[index + 1].setActive()
            newList[index + 1].setOnClickListener {
                newList[index + 1].updateUI(true)
            }
            lastIndex = index
            index++
        }

        if (validateContinueAndCreateService(newList)){
            createServiceBtn = this.requireActivity().findViewById(R.id.btnCreate_service_step3)
            createServiceBtn.isEnabled = true
            createServiceBtn.setCardBackgroundColor(android.graphics.Color.parseColor("#FF8672"))
            createServiceBtn.setOnClickListener {
                if (validateData()){
                    createOrUpdateServiceInstance()
                }
            }
        }

    }
    private fun validateData(): Boolean {
        var error = false
        for (sc in newList) {
            if (!error and sc.isVisible) {
                val result = sc.validate()
                if (!result?.result!!) {
                    if (result != null) {
                        showErrorForConfiguration(sc, result)
                    }
                    error = true
                } else {
                    sc.removeError()
                }
            } else {
                sc.removeError()
            }
        }
        return !error
    }

    private fun showErrorForConfiguration(
        configuration: ServiceConfigurationKt,
        result: ValidationResult
    ) {
        showSnackBar(result.messageResourceId)
        configuration.setError()
    }
    fun setServiceToConfigure(
        service: Service,
        otherProvidedServices: List<ServiceInstance?>?
    ) {
        this.service = service
        refreshUI()
    }

    private fun refreshUI() {
        val serviceInstance: ServiceInstance? = getServiceInstance()
        if (serviceInstance != null) {
            for (sc in serviceConfigurationList) {
                sc.initFromServiceInstance(serviceInstance)
            }
        } else {
            serviceConfigurationList.let {
                for (sc in it) {
                    sc.reset()
                }
            }
        }
    }


    private fun getServiceInstance(): ServiceInstance? {
        return job.getServiceInstanceForServiceId(service?.id)
    }



    //MOST IMPORTANT FUNCTION
    private fun updateServiceInstance(si: ServiceInstance) {
        val defaultIntervalTime = Calendar.getInstance()
        defaultIntervalTime.add(Calendar.HOUR_OF_DAY, 4) //Previous Hours advert

        si.price = basicServiceInfo.getPrice().toFloat()
        si.duration = basicServiceInfo.getDuration()
        si.concurrency = simultShifts.getConcurrency()
        si.isReminderSet = additionals.isReminder()
        si.concurrentServices = listOf()
        si.reminderInterval = defaultIntervalTime
        val act = activity as CreateServiceActivity
        si.isEmergency = act.isEmergency
        si.isCredits = creditSystem.isCredits()
        si.isCanBookWithoutCredits = creditSystem.getCanBookWithoutCredits()
        if (si.isEmergency) {
            si.maxAppsSimultaneously = -1
            si.maxAppsPerDay = -1
        } else {
            si.maxAppsSimultaneously = clientPermissions.getMaxAppsSimultaneously()
            si.maxAppsPerDay = clientPermissions.getMaxAppsPerDay()

        }
        if (basicServiceInfo.isClass()) {
            si.isFixedSchedule = true
            si.isClassType = true
            si.startTime = null
            si.endTime = null
            si.classTimes = basicServiceInfo.getClassTimes()
        } else {
            si.isClassType = false
            si.isFixedSchedule = basicServiceInfo.isFixedSchedule()
            si.startTime = basicServiceInfo.getStartTime()
            si.endTime = basicServiceInfo.getEndTime()

            si.classTimes = null
        }
        if (!basicServiceInfo.isFixedSchedule()) {
            si.interval = basicServiceInfo.getInterval()
        }
    }

    override fun getServiceConfigurationList(): MutableList<ServiceConfigurationKt> {
        return newList
    }


}

