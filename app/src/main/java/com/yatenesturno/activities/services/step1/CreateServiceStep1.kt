package com.yatenesturno.activities.services.step1

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.yatenesturno.R


class CreateServiceStep1 : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var  generalServiceBtn : ImageView
    private lateinit var  emergencyBtn : ImageView

    private var param1: String? = null
    private var param2: String? = null
    private var primaryColor : String = "#FF8672"
    private var primaryColorLight : String = "#B2FF8672"
    private var greyColor : String = "#717171"
    private var greyInactive : String = "#c4c4c4"
// gs = general service
    private lateinit var gsText1 : TextView
    private lateinit var gsText2 : TextView
    private lateinit var gsIcon : ImageButton
    private lateinit var gsMainCardVIew : CardView

//e = emergency
    private lateinit var eText1 : TextView
    private lateinit var eText2 : TextView
    private lateinit var eIcon : ImageButton
    private lateinit var eMainCardVIew : CardView
    lateinit var btnFinish: CardView

    //TRUE == GENERAL , FALSE == EMERGENCY
    private var serviceType : Boolean? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_service_step1, container, false)
        inflateViews(view)

        if (serviceType != null){
            btnFinish.setCardBackgroundColor(Color.parseColor(primaryColor))
            btnFinish.isEnabled = false
        }else{
            btnFinish.isEnabled = true
            btnFinish.setCardBackgroundColor(Color.parseColor(greyInactive))
        }

        generalServiceBtn.setOnClickListener {
            serviceType = true
            btnFinish.setCardBackgroundColor(Color.parseColor(primaryColor))
            generalServiceView()
        }
//        emergencyBtn.setOnClickListener {
//            serviceType = false
//            btnFinish.setCardBackgroundColor(Color.parseColor(primaryColor))
//            emergencyServiceView()
//        }

        //TODO FUTURO EMERGENCY IMPLEMENTAR BUNDLE
        btnFinish.setOnClickListener {
            if (serviceType != null){
                Navigation.findNavController(view).navigate(R.id.action_createServiceStep1_to_createServiceStep2Intro)
            }
        }

        return  view
    }

    private fun emergencyServiceView() {



        eText1.setTextColor(Color.parseColor(primaryColor))
        eText2.setTextColor(Color.parseColor(primaryColor))
        eIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(primaryColor))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            eMainCardVIew.outlineAmbientShadowColor = Color.parseColor(primaryColor)
            eMainCardVIew.outlineSpotShadowColor = Color.parseColor(primaryColor)
        }
        eMainCardVIew.foreground = requireContext().getDrawable(R.drawable.bo_service_selection_cv_selected)
        eMainCardVIew.cardElevation = 25F
        eMainCardVIew.elevation = 25F


        gsText1.setTextColor(Color.parseColor(greyInactive) )
        gsText2.setTextColor(Color.parseColor(greyInactive) )
        gsMainCardVIew.foreground = null
        gsIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(greyInactive))

        gsMainCardVIew.cardElevation = 0F
        gsMainCardVIew.elevation = 0F
    }

    private fun generalServiceView() {
        gsText1.setTextColor(Color.parseColor(primaryColor) )
        gsText2.setTextColor(Color.parseColor(primaryColor) )
        gsIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(primaryColor))
        gsMainCardVIew.foreground = requireContext().getDrawable(R.drawable.bo_service_selection_cv_selected)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            gsMainCardVIew.outlineAmbientShadowColor = Color.parseColor("#FF2400")
            gsMainCardVIew.outlineSpotShadowColor = Color.parseColor("#FF2400")
        }
        gsMainCardVIew.cardElevation = 25F
        gsMainCardVIew.elevation = 25F


//        eText1.setTextColor(Color.parseColor(greyInactive))
//        eText2.setTextColor(Color.parseColor(greyInactive))
//        eIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(greyInactive))
//
//        eMainCardVIew.foreground = null
//        eMainCardVIew.cardElevation = 0F
//        eMainCardVIew.elevation = 0F
    }


    private fun inflateViews(view: View){
        generalServiceBtn = view.findViewById(R.id.generalServiceBtn)
        emergencyBtn = view.findViewById(R.id.emergencyServiceBtn)
        ////////////////////////////////////////////////////
        gsText1 = view.findViewById(R.id.normal_service_cv_title)
        gsText2 = view.findViewById(R.id.normal_service_cv_description)
        gsIcon = view.findViewById(R.id.gsIconCv)
        gsMainCardVIew = view.findViewById(R.id.gsCardView)
        ///////////////////////////////////////////////////////
        eText1 = view.findViewById(R.id.emergency_cv_title)
        eText2 = view.findViewById(R.id.emergency_cv_escription)
        eIcon = view.findViewById(R.id.cardView4)
        eMainCardVIew = view.findViewById(R.id.emergency_cv)

        btnFinish = view.findViewById(R.id.btnCreate_service_step1_2)

    }










    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateServiceStep1().apply {
                arguments = Bundle().apply {
                }
            }
    }
}