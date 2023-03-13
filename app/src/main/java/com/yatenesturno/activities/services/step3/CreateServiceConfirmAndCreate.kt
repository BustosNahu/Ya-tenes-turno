package com.yatenesturno.activities.services.step3

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatenesturno.R
import com.yatenesturno.activities.services.CreateServiceActivity
import com.yatenesturno.activities.services.step3.ServiceRevision.ActiveDaysAdapter
import com.yatenesturno.activities.services.step3.ServiceRevision.ServiceClassesAdapter
import com.yatenesturno.activities.services.step3.viewModel.Step3ViewModel
import com.yatenesturno.object_interfaces.DaySchedule
import com.yatenesturno.object_interfaces.Job
import com.yatenesturno.object_interfaces.Place
import com.yatenesturno.object_interfaces.Service
import com.yatenesturno.object_interfaces.ServiceInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

const val SERVICE_KEY = "service_confirm_and_create"


class CreateServiceConfirmAndCreate : Fragment() {
    private val sharedVm: Step3ViewModel by activityViewModels()


    private var service: Service? = null
    private var job: Job? = null

    private lateinit var activeDaysRv: RecyclerView
    private lateinit var scheduledClassesRv: RecyclerView
    private lateinit var scheduledClassesLayout: LinearLayout
    private lateinit var intervalsServiceLayout: LinearLayout


    private lateinit var fixedTimeLayoutOpen: LinearLayout
    private lateinit var fixedTimeLayoutClose: LinearLayout
    private lateinit var serviceTitle: TextView

    private lateinit var servicePriceTv: TextView
    private lateinit var serviceDurationTv: TextView
    private lateinit var serviceIntervalsDurationTv: TextView
    private lateinit var serviceFixedTime: TextView
    private lateinit var serviceClass: TextView
    private lateinit var serviceOpenTime: TextView
    private lateinit var serviceCloseTime: TextView
    private lateinit var confirmAndCreateBtn: CardView
    private lateinit var backBtn: ImageButton

    private lateinit var serviceClientsShiftsDay: TextView
    private lateinit var serviceClientsSimultaneous: TextView
    private lateinit var serviceClientsSimultaneousShift: TextView
    private lateinit var serviceCreditSystem: TextView
    private lateinit var serviceReminder: TextView
    val classTimesStrings = mutableListOf<String>()


    var selectedDays: ArrayList<Int> = arrayListOf()

    companion object {
        const val ARG_JOB = "job"
        const val ARG_SERVICE = "service"
        const val ARG_DAYS = "selected_days"


        fun newInstance(job: Job, service: Service): CreateServiceConfirmAndCreate {
            val fragment = CreateServiceConfirmAndCreate()

            val bundle = Bundle().apply {
                putParcelable(ARG_JOB, job)
                putParcelable(ARG_SERVICE, service)
            }

            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            job = it.getParcelable(ARG_JOB)
            service = it.getParcelable(ARG_SERVICE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_create_service_confirm_and_create, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {

        initViews()
        setViews()
    }

    @SuppressLint("SetTextI18n")
    private fun setViews() {
        job?.let { it ->
            if (service != null) {

                val serviceData = it.getServiceInstanceForServiceId(service!!.id)
                serviceTitle.text = service?.name
                servicePriceTv.text = serviceData.price.toString()
                serviceFixedTime.text = if (serviceData.isFixedSchedule) "Si" else "No"
                serviceClass.text = if (serviceData.isClassType) "Si" else "No"

                if (serviceData.isFixedSchedule) {
                    intervalsServiceLayout.visibility = View.GONE
                } else {
                    intervalsServiceLayout.visibility = View.VISIBLE
                }


                if (!serviceData.isClassType) {
                    serviceOpenTime.text = toStringDate(serviceData.startTime.time)
                    serviceCloseTime.text = toStringDate(serviceData.endTime.time)
                    serviceDurationTv.text = toStringDate(serviceData.duration.time)
                    if (!serviceData.isFixedSchedule) serviceIntervalsDurationTv.text =
                        toStringDate(serviceData.interval.time)
                }

                serviceClientsShiftsDay.text =
                    if (serviceData.maxAppsPerDay == -1) "Sin especificar" else serviceData.maxAppsPerDay.toString()
                serviceClientsSimultaneous.text =
                    if (serviceData.concurrency == -1) "Sin especificar" else serviceData.concurrency.toString()
                serviceClientsSimultaneousShift.text =
                    if (serviceData.maxAppsSimultaneously == -1) " Sin especificar" else serviceData.maxAppsSimultaneously.toString()
                serviceCreditSystem.text = if (serviceData.isCredits) "Si" else "No"
                serviceReminder.text = if (serviceData.isReminderSet) "Si" else "No"

                setClassesList(serviceData)
                setActiveDaysList()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Un error ocurrio al cargar el servicio",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    private fun setActiveDaysList() {
        val activeDaysList = job!!.daySchedules.map { it.dayOfWeek }
        val dayLIst = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
        val currentDays = mutableListOf<String>()

        for (i in activeDaysList) {
            currentDays.add(dayLIst[i - 1])
        }
        setActiveDays(currentDays)

        confirmAndCreateBtn.setOnClickListener {
            finishConfiguration()
        }

        backBtn.setOnClickListener {
            val act = activity as CreateServiceActivity
            act.toStep3()
        }


    }

    private fun setClassesList(si: ServiceInstance) {
        if (!si.isClassType) {
            scheduledClassesLayout.visibility = View.GONE
            fixedTimeLayoutOpen.visibility = View.VISIBLE
            fixedTimeLayoutClose.visibility = View.VISIBLE
        } else {
            fixedTimeLayoutOpen.visibility = View.GONE
            fixedTimeLayoutClose.visibility = View.GONE

            scheduledClassesLayout.visibility = View.VISIBLE
            val classesTimes = si.classTimes.map { c -> c.time }
            for (i in classesTimes) {
                classTimesStrings.add(toStringDate(i))
            }
            setClasses(classTimesStrings)
        }

    }


    fun toStringDate(date: Date): String {
        return if (date.hours == 0) {
            if (date.minutes.toString().length > 1) {
                "00:" + date.minutes.toString()
            } else {
                "00:0" + date.minutes.toString()
            }
        } else {
            if (date.minutes.toString().length > 1) {
                if (date.hours.toString().length == 1) {
                    "0" + date.hours.toString() + ":" + date.minutes.toString()
                } else {
                    date.hours.toString() + ":" + date.minutes.toString()

                }
            } else {
                if (date.hours.toString().length == 1) {
                    "0" + date.hours.toString() + ":0" + date.minutes.toString()
                } else {
                    date.hours.toString() + ":0" + date.minutes.toString()

                }
            }
        }
    }

    private fun initViews() {
        view?.let { v ->
            servicePriceTv = v.findViewById(R.id.tv_service_price)
            serviceDurationTv = v.findViewById(R.id.tv_service_duration)
            serviceIntervalsDurationTv = v.findViewById(R.id.tv_service_sameInterval)
            intervalsServiceLayout = v.findViewById(R.id.intervals_service_layout)
            serviceFixedTime = v.findViewById<TextView>(R.id.tv_service_fixed_sch)
            serviceClass = v.findViewById<TextView>(R.id.tv_service_class_format)
            serviceOpenTime = v.findViewById<TextView>(R.id.service_open_tv)
            serviceCloseTime = v.findViewById<TextView>(R.id.service_close_tv)
            serviceClientsShiftsDay = v.findViewById<TextView>(R.id.tv_service_clients_per_day)
            serviceClientsSimultaneous =
                v.findViewById<TextView>(R.id.tv_service_simult_shifts_per_client)
            serviceClientsSimultaneousShift =
                v.findViewById<TextView>(R.id.tv_service_clients_simult_shifts)
            serviceCreditSystem = v.findViewById<TextView>(R.id.tv_credit_system)
            serviceReminder = v.findViewById<TextView>(R.id.tv_send_remember)
            scheduledClassesRv = v.findViewById(R.id.class_schedules_rv)
            scheduledClassesLayout = v.findViewById(R.id.service_class_format_list_layout)
            activeDaysRv = v.findViewById(R.id.active_days_rv)
            confirmAndCreateBtn = v.findViewById(R.id.cs_stp_3_cyc)
            backBtn = v.findViewById(R.id.editService)
            fixedTimeLayoutOpen = v.findViewById(R.id.service_fixed_open_sch)
            fixedTimeLayoutClose = v.findViewById(R.id.service_fixed_close_sch)
            serviceTitle = v.findViewById(R.id.generalServiceTitle)
        }

    }


    fun saveState(bundle: Bundle) {
        bundle.putParcelable(SERVICE_KEY, service)

    }

    fun recoverState(bundle: Bundle) {
        service = bundle.getParcelable(SERVICE_KEY)
    }

    private fun setActiveDays(strList: List<String>) {
        view?.let {
            val layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            activeDaysRv.layoutManager = layoutManager
        }
        val adapter = ActiveDaysAdapter(strList)
        activeDaysRv.adapter = adapter
    }

    private fun setClasses(classesString: List<String>) {
        view?.let {
            val spanCount = if (classesString.size > 3) 2 else 1
            val layoutManager =
                GridLayoutManager(requireContext(), spanCount, GridLayoutManager.HORIZONTAL, false)
            scheduledClassesRv.layoutManager = layoutManager
        }

        val adapter = ServiceClassesAdapter(classesString)
        scheduledClassesRv.adapter = adapter
    }


    private fun finishConfiguration() {
        val act = activity as CreateServiceActivity
        act.commitJob(sharedVm.job.value!!)
    }

}