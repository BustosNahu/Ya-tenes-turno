package com.yatenesturno.activities.services.step2

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatenesturno.Constants
import com.yatenesturno.R
import com.yatenesturno.activities.services.CreateServiceActivity
import com.yatenesturno.custom_views.LoadingButton
import com.yatenesturno.database.DatabaseDjangoRead
import com.yatenesturno.listeners.DatabaseCallback
import com.yatenesturno.object_interfaces.Job
import com.yatenesturno.object_interfaces.Service
import com.yatenesturno.object_interfaces.ServiceInstance
import com.yatenesturno.object_views.ServiceObject
import com.yatenesturno.object_views.ViewService2Adapter
import com.yatenesturno.serializers.BuilderListService
import com.yatenesturno.utils.TaskRunner
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.util.*


class CreateServiceStep2 : Fragment() {

    val SERVICE_LIST_KEY = "serviceIsSelectedMap"
    val JOB_KEY = "job"
    private lateinit var job: Job
    var servicesRvDisplayed = false

    private var serviceClickListener: ServiceClickListener? = null
    private var serviceListAdapter: ViewService2Adapter? = null
    private lateinit var recyclerViewServices: RecyclerView
    private lateinit var searchViewDoableServices: SearchView
    private lateinit var continueWithServiceBtn : CardView
    private lateinit var loadingBtn : ProgressBar
    private var serviceList: List<Service> = emptyList()
    private var serviceListObject: MutableList<ServiceObject> = mutableListOf()
    private var serviceListObjectTemp: MutableList<ServiceObject> = mutableListOf()
    private lateinit var rvContainerCl: ConstraintLayout
    var tempVar: MutableList<ServiceObject> = mutableListOf()


    lateinit var listAdapter: ArrayAdapter<String>

    private lateinit var openCloseRv: ImageButton

    //lateinit var loadingOverlay: LoadingOverlay



    fun setServiceClickListener(serviceClickListener: ServiceClickListener?) {
        this.serviceClickListener = serviceClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_service_step2, container, false)
        continueWithServiceBtn =view.findViewById(R.id.btnSelect_service_step2_2)
        loadingBtn = view.findViewById(R.id.progressBar_step2_service)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        val mainActivity = requireActivity() as CreateServiceActivity
        job = mainActivity.getJob()!!
        openCloseRv.setOnClickListener {
            displayRv()
        }



        if (savedInstanceState == null) {
            showLoadingOverlay()
            fetchServices()
        } else {
            recoverState(savedInstanceState)
        }
    }

    fun recoverState(bundle: Bundle) {
        serviceListObject = (bundle.getSerializable(SERVICE_LIST_KEY) as MutableList<ServiceObject>?)!!
        job = bundle.getParcelable(JOB_KEY)!!
        populateServices()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(saveState())

    }

    private fun saveState(): Bundle? {
        val bundle = Bundle()
        val serviceListObject = ArrayList<ServiceObject>()
        for (viewService in serviceListObject) {
            serviceListObject.add(viewService)
        }
        bundle.putParcelable(JOB_KEY, job)
        bundle.putSerializable(SERVICE_LIST_KEY, serviceListObject)
        return bundle
    }

    fun showLoadingOverlay() {
       if (loadingBtn != null){
            loadingBtn.visibility = View.VISIBLE
        }
    }
    fun hideLoadingOverlay() {
        if (loadingBtn != null){
            loadingBtn.visibility = View.GONE

        }

    }

    fun initViews() {
        searchViewDoableServices = requireView().findViewById(R.id.searchView_cs_step2)
        recyclerViewServices = requireView().findViewById(R.id.recyclerView_step2_cs)
        val layoutManagerServices: RecyclerView.LayoutManager = LinearLayoutManager(context)
        recyclerViewServices.layoutManager = layoutManagerServices
        openCloseRv = requireView().findViewById(R.id.rv_open_close_arrow)
        rvContainerCl = requireView().findViewById(R.id.rv_create_service_step2_layout)


    }


    private fun displayRv() {
        if (servicesRvDisplayed) {
            openCloseRv.setImageDrawable(requireContext().getDrawable(R.drawable.ic_arrow_closerv))
            rvContainerCl.visibility = View.GONE
            servicesRvDisplayed = false
        } else {
            openCloseRv.setImageDrawable(requireContext().getDrawable(R.drawable.ic_arrow_bottom))
            rvContainerCl.visibility = View.VISIBLE
            servicesRvDisplayed = true
        }
    }

    private fun fetchServices() {
        Handler(Looper.myLooper()!!).postDelayed({
            val body: MutableMap<String, String> =
                HashMap()
            body["job_id"] = job!!.id
            DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_DOABLE_SERVICES,
                body,
                callbackGetDoableServices
            )
        }, 300)
    }

    private fun populateServices() {

        serviceList.let { Collections.sort(it, comparatorIsServiceProvided) }

        for (s in serviceList) {
            serviceListObject.add(
                ServiceObject(
                    s,
                    false,
                    providesService(s),
                    isCredits(s),
                    false,
                    isEmergency(s)
                )
            )
        }

        serviceListAdapter = ViewService2Adapter(serviceListObject,listener)
        recyclerViewServices.adapter = serviceListAdapter
        searchViewDoableServices.setOnQueryTextListener(listenerQueryDoableServiceType)
    }

    private fun isEmergency(s: Service): Boolean {
        val si = getServiceInstanceForService(s) ?: return false
        return si.isEmergency
    }

    private fun providesService(s: Service): Boolean {
        return getServiceInstanceForService(s) != null
    }



    private fun isCredits(s: Service): Boolean {
        val si = getServiceInstanceForService(s) ?: return false
        return si.isCredits
    }

    private fun getServiceInstanceForService(s: Service): ServiceInstance? {
        return job.getServiceInstanceForServiceId(s.id)
    }

     fun getProvidedServiceInstances(): MutableList<ServiceInstance> {
        val servicesBeingDone: MutableList<ServiceInstance> = ArrayList()
         for (si in serviceListObject) {
            if (si.isProvided) {
                getServiceInstanceForService(si.service)?.let { servicesBeingDone.add(it) }
            }
        }
        return servicesBeingDone
    }

    override fun onResume() {
        super.onResume()
        if (serviceListAdapter != null) {
            checkForUpdates()
        }
        searchViewDoableServices.clearFocus()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun checkForUpdates() {
        serviceListAdapter!!.notifyDataSetChanged()
    }


    interface ServiceClickListener {
        fun onServiceViewClick(service: Service?)
    }


    /////////////                 CALLBACKS             ////////////////////////////
    val callbackGetDoableServices = object : DatabaseCallback() {

        override fun onSuccess(statusCode: Int, headers: Array<Header>, response: JSONObject) {
            TaskRunner().executeAsync(
                {
                    BuilderListService().build(
                        response
                    )
                }
            ) { result: List<Service> ->
                serviceList = result
                Log.d("step2Job",serviceList.toString())
                populateServices()
            }
            hideLoadingOverlay()
        }

        override fun onFailure(
            statusCode: Int,
            headers: Array<Header>,
            responseString: String,
            throwable: Throwable
        ) {
            hideLoadingOverlay()
            Toast.makeText(
                activity,
                activity?.getString(R.string.error_de_conexion), Toast.LENGTH_SHORT
            ).show()

        }

    }


    var first = true
    private  lateinit var lastObject : ServiceObject
    private  lateinit var currentObject : ServiceObject

    val listener = (object : ViewService2Adapter.OnSelectServiceListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(v: View?, position: Int) {
            currentObject = serviceListObject[position]

            if(first){
                lastObject = serviceListObject[position]
               first = false
            }

            if (lastObject.isSelected){
                lastObject.isSelected = false
                currentObject.isSelected = true
            }else{
                currentObject.isSelected = !currentObject.isSelected
            }



            serviceListAdapter!!.notifyDataSetChanged()
            enableButton(serviceListObject)

            lastObject = currentObject

        }

    })


    private fun enableButton(serviceListObject: MutableList<ServiceObject>){
        if (serviceListObject.isNotEmpty()){
            if (serviceListObject.any { it.isSelected == true }){
                continueWithServiceBtn.setCardBackgroundColor(Color.parseColor("#FF8672"))
                continueWithServiceBtn.isEnabled = true
                if (serviceListObject.isNotEmpty()){
                    serviceListObject.forEach { obj ->
                        obj.isSelected.let {
                            if (it == true){
                                continueWithServiceBtn.setOnClickListener {
                                    navigateToStep3(obj)
                                }
                            }
                        }
                    }
                }
            }else{
                continueWithServiceBtn.isEnabled = false
                continueWithServiceBtn.setCardBackgroundColor(Color.parseColor("#717171"))
            }
        }

    }

    private fun navigateToStep3(obj: ServiceObject){
        findNavController().navigate(R.id.action_createServiceStep2_to_createServiceStep3Intro)
        val mainActivity = requireActivity() as CreateServiceActivity
        mainActivity.navigateToStep3(obj , true)
    }

    val listenerQueryDoableServiceType = (object : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String): Boolean {
            return onQueryTextChange(query)
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onQueryTextChange(newText: String): Boolean {


            serviceListObjectTemp.clear()
            val searchText = newText.toLowerCase(Locale.getDefault())


            if (searchText.isNotEmpty() && searchText != "" ){
                serviceListObject.forEach {
                    if (it.service.name.toLowerCase(Locale.getDefault()).contains(searchText)){
                        serviceListObjectTemp.add(it)
                    }
                }

                serviceListObject.clear()
                serviceListObject.addAll(serviceListObjectTemp)
                recyclerViewServices.adapter?.notifyDataSetChanged()
            }else{
                Log.d("seriveListTempvar", tempVar.toString())
                serviceListObject.clear()
                serviceListObject.addAll(tempVar)
                Log.d("seriveListServiceListObject", serviceListObject.toString())

                recyclerViewServices.adapter?.notifyDataSetChanged()
            }
            return false
        }
    })




    val comparatorIsServiceProvided = (object : Comparator<Service?> {
        override fun compare(p0: Service?, p1: Service?): Int {
            val providesServiceS1: Boolean = providesService(p0!!)
            val providesServiceS2: Boolean = providesService(p1!!)
            if (providesServiceS1 && !providesServiceS2) {
                return -1
            }
            return if (providesServiceS2 && !providesServiceS1) {
                1
            } else 0
        }

    })


}


