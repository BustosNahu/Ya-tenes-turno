package com.yatenesturno.activities.place_register.step3

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.yatenesturno.R
import com.yatenesturno.activities.main_screen.MainActivity
import com.yatenesturno.activities.place_register.NewPlaceActivity
import com.yatenesturno.custom_views.LoadingOverlay


class NewPlaceStep3 : Fragment() {

    private var workHere : Boolean? = null
    private var primaryColor : String = "#FF8672"
    private var primaryColorLight : String = "#B2FF8672"
    private var greyColor : String = "#717171"
    private var greyInactive : String = "#c4c4c4"

    private var sharedPreferences: SharedPreferences? = null

    //VIEWS
    private lateinit var  workHereBtn : ImageView
    private lateinit var  dontWorkHereBtn : ImageView

    //wh = Work Here
    private lateinit var whText1 : TextView
    private lateinit var whText2 : TextView
    private lateinit var whText3 : TextView
    private lateinit var whMainCardVIew : CardView

    //dwh = Don't work here
    private lateinit var dwhText1 : TextView
    private lateinit var dwhText2 : TextView
    private lateinit var dwhText3 : TextView
    private lateinit var dwhText4 : TextView
    private lateinit var dwhText5 : TextView
    private lateinit var dwhMainCardVIew : CardView

    private lateinit var createSpace : CardView
    lateinit var btnFinish: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_place_step3, container, false) // Inflate the layout for this fragment
        inflateViews(view)

        if (workHere != null){
            createSpace.setCardBackgroundColor(Color.parseColor(primaryColor))
            createSpace.isEnabled = false
        }else{
            createSpace.isEnabled = true
            createSpace.setCardBackgroundColor(Color.parseColor(greyInactive))
        }

        workHereBtn.setOnClickListener {
            workHere = true
            createSpace.setCardBackgroundColor(Color.parseColor(primaryColor))
            workHere()
        }

        dontWorkHereBtn.setOnClickListener {
            workHere = false
            createSpace.setCardBackgroundColor(Color.parseColor(primaryColor))
            dontWorkHere()
        }


        createSpace.setOnClickListener {
            if (workHere != null){
                createPlace()
            }else Toast.makeText(requireContext(),"Selecciona una opcion!", Toast.LENGTH_LONG).show()
        }




        return view
    }

    private fun createPlace(){

        val activity = requireActivity() as NewPlaceActivity
        if (!activity.isLoading){
            val bd = Bundle()
            bd.putBoolean("worksHere", workHere!!)
            activity.saveBundleFromStep3(bd)
            activity.createNewPlace()
            sharedPreferences = requireActivity().getSharedPreferences("firstShop", Context.MODE_PRIVATE)
            sharedPreferences.let {
                it?.edit()?.putBoolean("firstShop", false)?.apply()
            }
        }

    }


    private fun dontWorkHere() {
        whText1.setTextColor(Color.parseColor(greyInactive) )
        whText2.setTextColor(Color.parseColor(greyInactive) )
        whText3.setTextColor(Color.parseColor(greyInactive) )

        whMainCardVIew.foreground = null
        whMainCardVIew.cardElevation = 0F
        whMainCardVIew.elevation = 0F

        dwhText1.setTextColor(Color.parseColor(primaryColorLight))
        dwhText2.setTextColor(Color.parseColor(primaryColorLight))

        dwhText3.setTextColor(Color.parseColor("#5E5E5E"))
        dwhText4.setTextColor(Color.parseColor(greyColor))
        dwhText5.setTextColor(Color.parseColor(primaryColor))


        dwhMainCardVIew.foreground = requireContext().getDrawable(R.drawable.border_outline_work_here_cv_selected)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            dwhMainCardVIew.outlineAmbientShadowColor = Color.parseColor(primaryColor)
            dwhMainCardVIew.outlineSpotShadowColor = Color.parseColor(primaryColor)
        }
        dwhMainCardVIew.cardElevation = 15F
        dwhMainCardVIew.elevation = 15F


    }

    private fun workHere(){

        whText1.setTextColor(Color.parseColor(primaryColor) )
        whText2.setTextColor(Color.parseColor(primaryColor) )
        whText3.setTextColor(Color.parseColor(greyColor) )
        whMainCardVIew.foreground = requireContext().getDrawable(R.drawable.border_outline_work_here_cv_selected)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            whMainCardVIew.outlineAmbientShadowColor = Color.parseColor(primaryColor)
            whMainCardVIew.outlineSpotShadowColor = Color.parseColor(primaryColor)
        }
        whMainCardVIew.cardElevation = 15F
        whMainCardVIew.elevation = 15F


        dwhText1.setTextColor(Color.parseColor(greyInactive))
        dwhText2.setTextColor(Color.parseColor(greyInactive))
        dwhText3.setTextColor(Color.parseColor(greyInactive))
        dwhText4.setTextColor(Color.parseColor(greyInactive))
        dwhText5.setTextColor(Color.parseColor(greyInactive))
        dwhMainCardVIew.foreground = null
        dwhMainCardVIew.cardElevation = 0F

        dwhMainCardVIew.elevation = 0F
    }

    private fun inflateViews(view: View){

        workHereBtn = view.findViewById(R.id.workHereStep3_btn)
        dontWorkHereBtn = view.findViewById(R.id.dwhMainImgView)
        ////////////////////////////////////////////////////
        whText1 = view.findViewById(R.id.whText1)
        whText2 = view.findViewById(R.id.whText2)
        whText3 = view.findViewById(R.id.whText3)
        whMainCardVIew = view.findViewById(R.id.whMainCardVIew)
        ///////////////////////////////////////////////////////
        dwhText1 = view.findViewById(R.id.dwhText1)
        dwhText2 = view.findViewById(R.id.dwhText2)
        dwhText3 = view.findViewById(R.id.dwhText3)
        dwhText4 = view.findViewById(R.id.dwhText4)
        dwhText5 = view.findViewById(R.id.dwhText5)
        dwhMainCardVIew = view.findViewById(R.id.dwhMainCardVIew)

        createSpace = view.findViewById(R.id.btnStep2_confirmScheduletHours)

    }


    fun onButtonShowPopupWindowClick(view: View?) {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams") val popupView = inflater.inflate(R.layout.create_popup, null)

        // create the popup window
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        btnFinish = popupView.findViewById(R.id.finish_place_creation)
        popupView.elevation = 10f
        // dismiss the popup window when touched
        btnFinish.setOnClickListener(View.OnClickListener {
            val i = Intent(requireActivity() as NewPlaceActivity,  MainActivity::class.java)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            startActivity(i)
        })

    }




}