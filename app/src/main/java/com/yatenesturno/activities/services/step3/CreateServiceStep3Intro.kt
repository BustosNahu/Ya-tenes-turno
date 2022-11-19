package com.yatenesturno.activities.services.step3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.NavigatorProvider
import androidx.navigation.fragment.findNavController
import com.yatenesturno.R
import com.yatenesturno.activities.services.CreateServiceActivity
import com.yatenesturno.activities.services.step2.CreateServiceStep2


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateServiceStep3Intro.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateServiceStep3Intro : Fragment() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(com.yatenesturno.R.layout.fragment_create_service_step3_intro, container, false)

        val continueBtn : CardView = view.findViewById(com.yatenesturno.R.id.btnContinue_service_step3_1)
        val backBtn : ImageButton = view.findViewById(com.yatenesturno.R.id.back_btn_step3_intro)

        continueBtn.setOnClickListener {
            val mainActivity = requireActivity() as CreateServiceActivity
            mainActivity.navigateToStep3(null, false)
        }
        return view
    }


}