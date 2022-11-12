package com.yatenesturno.activities.place_register.step3

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.yatenesturno.R
import com.yatenesturno.activities.place_register.step_1.itemsViewPager.NewPlaceVPAdapter
import com.yatenesturno.objects.NewPlaceIntroItem

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewPlaceIntroStep3.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewPlaceIntroStep3 : Fragment() {
    private  lateinit var adapter: NewPlaceVPAdapter
    lateinit var layoutNewPlaceIndicator: LinearLayout
    lateinit var forwardArrow: ImageButton
    lateinit var backwardArrow: ImageButton
    var finished = false
    lateinit var btnStart: CardView
    lateinit var viewPager: ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_new_place_intro_step3, container, false)
        setupNewPlaceItems()
        viewPager = view.findViewById(R.id.new_place_intro_step3_viewpager)
        viewPager.adapter = adapter
        layoutNewPlaceIndicator = view.findViewById(R.id.linearLayout_indicator_new_place_3_container)
        forwardArrow = view.findViewById(R.id.forward_btn_new_place_3_container)
        backwardArrow = view.findViewById(R.id.backward_btn_new_place_3_container)
        btnStart = view.findViewById<CardView>(R.id.btnStart_intro_step_3)


        setupOnboardingIndicators()
        setCurrentOnboardingIndicator(0)
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentOnboardingIndicator(position)
            }
        })

        forwardArrow.setOnClickListener {
            viewPager.currentItem = viewPager.currentItem + 1
        }

        backwardArrow.setOnClickListener {
            viewPager.currentItem = viewPager.currentItem - 1
        }

        btnStart.setOnClickListener {
            val fragment: Fragment = NewPlaceStep3()
            parentFragmentManager.beginTransaction()
                .replace(R.id.new_place_fragment_main, fragment)
                .addToBackStack("a")
                .commit()
        }

        if (!finished) {
            btnStart.setCardBackgroundColor(requireContext().getColor(R.color.darker_grey))
        }
        return view
    }

    private fun setupOnboardingIndicators() {
        val indicators = arrayOfNulls<ImageView>(adapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        for (i in indicators.indices) {
            indicators[i] = ImageView(requireContext())
            indicators[i]!!.setPadding(3, 0, 10, 0)

            indicators[i]!!.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.new_place_intro_indicador_inactive
                )
            )
            //            indicators[i].setLayoutParams(layoutParams);
            layoutNewPlaceIndicator.addView(indicators[i])
        }
    }

    private fun setCurrentOnboardingIndicator(index: Int) {
        val childCount = layoutNewPlaceIndicator.childCount
        for (i in 0 until childCount) {
            val imageView = layoutNewPlaceIndicator.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.new_place_intro_indicador_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.new_place_intro_indicador_inactive
                    )
                )
            }
        }
        if (index == adapter.itemCount - 1) {
            finished = true
            forwardArrow.visibility = View.GONE
            backwardArrow.visibility = View.VISIBLE
            btnStart.isEnabled = true
            btnStart.setCardBackgroundColor(requireContext().getColor(R.color.colorPrimary))
        } else if (index >= 1) {
            forwardArrow.visibility = View.VISIBLE
            backwardArrow.visibility = View.VISIBLE
        } else if (index == 0) {
            forwardArrow.visibility = View.VISIBLE
            backwardArrow.visibility = View.GONE
        }
    }

    private fun setupNewPlaceItems() {
        val introItems: MutableList<NewPlaceIntroItem> = ArrayList()
        val description1: Spannable =
            SpannableString("Aquí necesitamos que nos aclares si sos vos el que presta el servicio al cliente en la tienda o lo hace un empleado.")



        val introItem1 = NewPlaceIntroItem()
        introItem1.description = description1
        introItem1.image = R.drawable.place_shop_step3_vp_1
        val description2: Spannable =
            SpannableString("Si prestás servicios a clientes en tu empresa, da click en “Si, atiendo a mis clientes” y podrás configurar tu primer servicio.\n")
        description2.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            ), 59, 87, Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )

        val introItem2 = NewPlaceIntroItem()
        introItem2.description = description2
        introItem2.image = R.drawable.place_shop_step3_vp_2

        val description3: Spannable =
            SpannableString("Si no, da click en “Atienden mis empleados” y deberás ir a solicitudes de trabajo para poder incluir colaboradores que sí presten servicios en tu tienda")
        description3.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            ), 19, 43, Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        description3.setSpan(StyleSpan(Typeface.BOLD), 85, 121, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        description3.setSpan(
            StyleSpan(Typeface.BOLD),
            133,
            description3.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        val introItem3 = NewPlaceIntroItem()
        introItem3.description = description3
        introItem3.image = R.drawable.place_shop_step3_vp_3
        introItems.add(introItem1)
        introItems.add(introItem2)
        introItems.add(introItem3)
        adapter = NewPlaceVPAdapter(introItems)
    }

}