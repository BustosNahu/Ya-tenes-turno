package com.yatenesturno.activities.services.step3.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yatenesturno.object_interfaces.Job
import com.yatenesturno.object_interfaces.Service

class Step3ViewModel : ViewModel() {

    private var _service = MutableLiveData<Service>()
    val service : LiveData<Service> = _service

    private var _job = MutableLiveData<Job>()
    val job : LiveData<Job> = _job

    fun saveData(job: Job, service: Service){
        _service.value = service
        _job.value = job
    }

}