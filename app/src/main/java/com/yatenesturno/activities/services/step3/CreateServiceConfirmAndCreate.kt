package com.yatenesturno.activities.services.step3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatenesturno.R
import com.yatenesturno.activities.job_edit.ServiceConfigureFragment
import com.yatenesturno.activities.services.CreateServiceActivity
import com.yatenesturno.activities.services.step3.ServiceRevision.ActiveDaysAdapter
import com.yatenesturno.object_interfaces.DaySchedule
import com.yatenesturno.object_interfaces.Job
import com.yatenesturno.object_interfaces.Place
import com.yatenesturno.object_interfaces.Service
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

const val SERVICE_KEY = "service_confirm_and_create"



class CreateServiceConfirmAndCreate : Fragment() {
    private var service: Service? = null
    private var job: Job? = null
    private var place: Place? = null

    private lateinit var activeDaysRv : RecyclerView
    private lateinit var scheduledClassesRv : RecyclerView
    var selectedDays: ArrayList<Int> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            job = it.getParcelable("job")
            selectedDays = it.getIntegerArrayList("selected_days") as ArrayList<Int>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_service_confirm_and_create, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActiveDays()
    }



    fun serviceConfirmFragment(place: Place, job: Job, service: Service) {
        this.job = job
        this.service = service
    }


    fun saveState(bundle: Bundle){
        bundle.putParcelable(SERVICE_KEY ,service)

    }
    fun recoverState(bundle: Bundle) {
        service = bundle.getParcelable(SERVICE_KEY)
    }

    private fun setActiveDays(){
        view?.let{
            activeDaysRv = it.findViewById(R.id.active_days_rv)
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            activeDaysRv.layoutManager = layoutManager
        }

        val dummyList = listOf("Lun","Mar","Mie","Jue")
        val adapter = ActiveDaysAdapter(dummyList)
        activeDaysRv.adapter = adapter
    }

    private fun removeEmptyDaySchedules() {
        val toRemove: MutableList<DaySchedule> = ArrayList()
        for (ds in job?.daySchedules!!) {
            if (ds.serviceInstances.isEmpty()) {
                toRemove.add(ds)
            }
        }
        for (ds in toRemove) {
            job?.daySchedules!!.remove(ds)
        }
    }

    private fun finishConfiguration() {
        removeUnselectedDays()
        removeEmptyDaySchedules()

        val act = activity as CreateServiceActivity
        act.commitJob(job!!)
    }



    private fun removeUnselectedDays() {
        for (ds in job?.daySchedules!!) {
            val si = ds.getServiceInstanceForService(service?.id)
            if (si != null && !getSelectedDays().contains(ds.dayOfWeek)) {
                ds.removeServiceInstance(si)
            }
        }
    }

    private fun getSelectedDays(): List<Int> {
        val act = activity as CreateServiceActivity
        return if (act.isEmergency) {
            listOf(1, 2, 3, 4, 5, 6, 7)
        } else selectedDays
    }

}