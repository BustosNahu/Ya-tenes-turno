package com.yatenesturno.activities.place_register.step_2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yatenesturno.R
import com.yatenesturno.activities.place_register.NewPlaceActivity
import com.yatenesturno.activities.place_register.step3.NewPlaceIntroStep3
import com.yatenesturno.activities.place_register.step_2.OpenHoursItem.OhRevisionAdapter
import com.yatenesturno.database.localDb.PlaceScheduleDatabase
import com.yatenesturno.database.localDb.PlaceScheduleRepository
import com.yatenesturno.database.localDb.SchedulePlaceTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ConfirmScheduledHours : Fragment() {
    private var dbRepo: PlaceScheduleRepository? = null

    private var scheduledHoursList: MutableList<SchedulePlaceTime> = mutableListOf()
    private lateinit var revisionOhAdapter: OhRevisionAdapter
    private lateinit var rv: RecyclerView
    private lateinit var editScheduledHoursBtn: AppCompatImageButton
    private lateinit var editScheduledHoursCv: CardView
    private lateinit var confirmScheduledHoursBtn: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val placeSchDao = PlaceScheduleDatabase.getDb(requireContext()).placeScheduleDao()
        Log.d("dbRepo", "Created")

        dbRepo = PlaceScheduleRepository(placeSchDao)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("firsTimeSchedule", false)
    }


    private fun readALlSchedule() {
        lifecycleScope.launch(context = Dispatchers.IO) {
            Log.d("dbRepo", "read all coroutine")
            dbRepo?.readAllPlacesWithoutPlaceId()?.collect { list ->
                Log.d("dbRepo", "Collected + $list")
                list.sortedBy { it.weekDayIndex }.let {
                    scheduledHoursList.addAll(it)
                }
                activity?.runOnUiThread {
                    revisionOhAdapter = OhRevisionAdapter(scheduledHoursList)
                    rv.adapter = revisionOhAdapter
                }

            }
        }
    }


    private fun init(view: View) {
        editScheduledHoursBtn = view.findViewById(R.id.editScheduledHours_Ib)
        editScheduledHoursCv = view.findViewById(R.id.editScheduledHours_Cv)
        confirmScheduledHoursBtn = view.findViewById(R.id.btnStep2_confirmScheduletHours)
        rv = view.findViewById(R.id.revision_to_confirm_hours_rv)
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_confirm_scheduled_hours, container, false)
        init(view)
        readALlSchedule()
        val bundle = Bundle()
        confirmScheduledHoursBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.new_place_fragment_main, NewPlaceIntroStep3()).commit()
        }
        editScheduledHoursBtn.setOnClickListener {
            bundle.putBoolean("firsTimeSchedule", false)
            val activity = requireActivity() as NewPlaceActivity
            activity.saveBundleFromStep2(bundle)
            parentFragmentManager.beginTransaction()
                .replace(R.id.new_place_fragment_main, NewPlaceStep2()).commit()
        }
        return view
    }

}