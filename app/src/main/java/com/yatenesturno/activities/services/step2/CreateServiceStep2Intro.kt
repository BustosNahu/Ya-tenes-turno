package com.yatenesturno.activities.services.step2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.yatenesturno.R
import com.yatenesturno.activities.job_edit.ServiceSelectionFragment
import com.yatenesturno.activities.services.CreateServiceActivity


class CreateServiceStep2Intro : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_service_step2_intro, container, false)
        val createServiceBtn : CardView = view.findViewById(R.id.btnCreate_service_step2_1)

        createServiceBtn.setOnClickListener {

        findNavController().navigate(R.id.action_createServiceStep2Intro_to_createServiceStep2)

        }

        return view
    }
}