package com.yatenesturno.activities.services.step1

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yatenesturno.R
import com.yatenesturno.activities.main_screen.MainActivity
import com.yatenesturno.activities.services.CreateServiceActivity


class CreateServiceStep1Intro : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_create_service_step1_intro, container, false)
        val createServiceBtn : CardView = view.findViewById(R.id.btnCreate_service_step1)
        val backBtn : ImageButton = view.findViewById(R.id.back_btn_new_service_intro)

        createServiceBtn.setOnClickListener {
            findNavController()
                .navigate(R.id.action_createServiceStep1Intro_to_createServiceConfirmAndCreate)
        }

        backBtn.setOnClickListener {
            val act : CreateServiceActivity= requireActivity() as CreateServiceActivity
           act.finish()
        }
        return view
    }



}