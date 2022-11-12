package com.yatenesturno.activities.place_register.step_2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.yatenesturno.R


/**
 * A simple [Fragment] subclass.
 * Use the [NewPlaceIntroStep2.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewPlaceIntroStep2 : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_new_place_intro_step2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nextBtn : CardView = view.findViewById(R.id.btnStep2_continue)

        nextBtn.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(R.id.new_place_fragment_main, NewPlaceStep2()).commit()
        }

    }


}