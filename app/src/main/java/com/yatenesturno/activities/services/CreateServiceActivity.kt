package com.yatenesturno.activities.services

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.yatenesturno.R
import com.yatenesturno.activities.services.step2.CreateServiceStep2
import com.yatenesturno.activities.services.step3.CreateServiceConfirmAndCreate
import com.yatenesturno.activities.services.step3.CreateServiceStep3
import com.yatenesturno.activities.services.step3.CreateServiceStep3Intro
import com.yatenesturno.activities.services.step3.objects.ServiceConfigurationKt
import com.yatenesturno.custom_views.LoadingOverlay
import com.yatenesturno.functionality.JobSaver
import com.yatenesturno.listeners.RepositorySaveListener
import com.yatenesturno.object_interfaces.Job
import com.yatenesturno.object_interfaces.Place
import com.yatenesturno.object_interfaces.Service
import com.yatenesturno.object_interfaces.ServiceInstance
import com.yatenesturno.object_views.ServiceObject

class CreateServiceActivity : AppCompatActivity() {

    lateinit var loadingOverlay: LoadingOverlay

    /**
     * Instance varaibles
     */
    private var job: Job? = null
    private var place: Place? = null
    private var mainService: Service? = null
    var isEmergency = false // implementar

    /**
     * UI References
     */
    private  var serviceSelectionFragment: CreateServiceStep2? = null
    var serviceConfigureFragment: CreateServiceStep3? = null
    private var serviceConfirmFragment: CreateServiceConfirmAndCreate? = null

    private var adapter: ServiceConfigPagerAdapter? = null



    val listOfServicesToConfigure = mutableListOf<ServiceConfigurationKt>()

    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_service)
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = getColor(R.color.black)
        val navHostFragment  = supportFragmentManager.findFragmentById(R.id.CreateServiceFragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        loadingOverlay = LoadingOverlay(findViewById(R.id.create_service_root))

        if (savedInstanceState == null) {
            job = intent.extras!!.getSerializable("job") as Job?
            place = intent.extras!!.getSerializable("place") as Place?
        } else {
            recoverState(savedInstanceState)
        }
    }

    fun showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show()
        }
    }
    fun hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide()
        }
    }
    fun showConnectionErrorMessage() {
        Toast.makeText(
            applicationContext,
            getString(R.string.error_de_conexion),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun commitJob(editedJob : Job) {
        showLoadingOverlay()
        JobSaver().saveJob(editedJob, object : RepositorySaveListener {
            override fun onSuccess() {
                hideLoadingOverlay()
                returnOK()
            }

            override fun onFailure() {
                showConnectionErrorMessage()
                hideLoadingOverlay()
            }
        })
    }

    private fun recoverState(bundle: Bundle) {
        job = bundle.getParcelable<Job>("job")
        place = bundle.getParcelable<Place>("place")
        serviceConfigureFragment?.recoverState(bundle)
        serviceSelectionFragment?.recoverState(bundle)
        serviceConfirmFragment?.recoverState(bundle)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(saveState())
        serviceConfigureFragment?.recoverState(outState)
        serviceSelectionFragment?.recoverState(outState)
        serviceConfirmFragment?.recoverState(outState)

    }

    private fun saveState(): Bundle {
        val bundle = Bundle()
        bundle.putParcelable("job", job)
        bundle.putParcelable("place", place)
        return bundle
    }

    fun nextServiceFragment(){
        serviceConfigureFragment?.nextService(listOfServicesToConfigure)
    }


    fun setServicesList(list: MutableList<ServiceConfigurationKt>){
        listOfServicesToConfigure.addAll(list)
    }




    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_delete, menu)
        return true
    }

    override fun onBackPressed() {
        if (navController.backQueue.isNotEmpty()){
            navController.popBackStack()
        }
    }

    fun returnOK() {
        val bundle = Bundle()
        bundle.putSerializable("job", job)
        val intent = Intent()
        intent.putExtras(bundle)
        intent.putExtra("fromCreatedService", true)
        Log.d("returnOKFrom", "lol")
        setResult(RESULT_OK, intent)
        finish()
    }



    fun showConfigureService() {
        mainService?.let {
            Log.d("serviceShow", it.name )
            val otherProvidedServices = removeServiceFromProvidedOnes(it)
            serviceConfigureFragment?.setServiceToConfigure(
                it,
                otherProvidedServices
            )
        }
    }

    fun setService(service: Service) {
        this.mainService = service
        serviceConfigureFragment =  CreateServiceStep3()
        serviceConfigureFragment?.serviceConfigureFragment(place!!,job!!, service)
        Log.d("serviceShow", "main service" )
    }

    private fun setConfirmService(service: Service) {
        this.mainService = service
        serviceConfirmFragment =  CreateServiceConfirmAndCreate()
        serviceConfirmFragment?.serviceConfirmFragment(place!!,job!!, service)

    }

    fun getService() : Service? {
        return mainService
    }


    private fun removeServiceFromProvidedOnes(service: Service): List<ServiceInstance> {
        val providedServices: MutableList<ServiceInstance> = CreateServiceStep2().getProvidedServiceInstances()
        val i = providedServices.iterator()
        while (i.hasNext()) {
            if (i.next().service == service) {
                i.remove()
                break
            }
        }
        return providedServices
    }



    fun getJob(): Job? {

        return job
    }

    var selectedService : ServiceObject? = null
    fun navigateToStep3(obj: ServiceObject?, intro : Boolean) {
        if (selectedService == null){
            selectedService = obj
        }
        if (obj == null){
            setService(selectedService!!.service)
        }else{
            setService(obj.service)
        }
        if (!intro){
            serviceConfigureFragment?.let {
                this
                    .supportFragmentManager
                    .beginTransaction()
                    .add(R.id.CreateServiceFragmentContainer, it)
                    .commit()
            }
        }else{
            CreateServiceStep3Intro().let {
                this
                    .supportFragmentManager
                    .beginTransaction()
                    .add(R.id.CreateServiceFragmentContainer, it)
                    .commit()
            }


        }
        this.supportFragmentManager.executePendingTransactions()
    }

    fun navigateToConfirmation(obj: Service?) {
        if (mainService == null){
            mainService = obj
        }
        if (obj == null){
            setConfirmService(mainService!!)
        }else{
            setConfirmService(obj)
        }

            CreateServiceConfirmAndCreate().let {
                this
                    .supportFragmentManager
                    .beginTransaction()
                    .add(R.id.CreateServiceFragmentContainer, it)
                    .commit()
            }
        this.supportFragmentManager.executePendingTransactions();
    }

    private class ServiceConfigPagerAdapter(
        fm: FragmentManager?,
        private val fragmentList: List<Fragment>
    ) :
        FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }
    }


}