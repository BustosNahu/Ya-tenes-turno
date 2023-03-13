package com.yatenesturno.activities.place_register.step_2

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.yatenesturno.R
import com.yatenesturno.activities.place_register.NewPlaceActivity
import com.yatenesturno.activities.place_register.step_2.OpenHoursItem.OhRevisionAdapter
import com.yatenesturno.activities.place_register.step_2.OpenHoursItem.OpenHoursAdapter
import com.yatenesturno.database.localDb.PlaceScheduleDatabase
import com.yatenesturno.database.localDb.PlaceScheduleRepository
import com.yatenesturno.database.localDb.SchedulePlaceTime
import com.yatenesturno.objects.CompDaySchedule
import com.yatenesturno.objects.open_hour_cardView.OpenHoursObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class NewPlaceStep2 : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var hoursList: ArrayList<OpenHoursObject> = arrayListOf()
    private var dayScheduleList: ArrayList<CompDaySchedule> = arrayListOf()
    private lateinit var hoursAdapter: OpenHoursAdapter
    private var dbRepo: PlaceScheduleRepository? = null
    private lateinit var nextBtn: CardView
    private var firstTime: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val placeSchDao = PlaceScheduleDatabase.getDb(requireContext()).placeScheduleDao()
        dbRepo = PlaceScheduleRepository(placeSchDao)
        val activity = requireActivity() as NewPlaceActivity
        if (activity.step2Bundle != null) {
            firstTime = activity.step2Bundle.get("firsTimeSchedule") as Boolean?
            if (firstTime != null && firstTime == true) {
                hoursList = arrayListOf()
            } else {
                firstTime = false
            }
            Log.d("bundle not null", firstTime.toString())
        } else {
            firstTime = true
        }


    }

    private fun readALlSchedule() {
        lifecycleScope.launch(context = Dispatchers.IO) {
            dbRepo?.readAllPlacesWithoutPlaceId()?.collect { list ->
                for (i in list) {
                    hoursList.add(
                        OpenHoursObject(
                            day_name = i.day,
                            active = i.active,
                            isEditing = false,
                            isAnotherEditing = false,
                            restTime = i.openMidTime != "13:00"  || i.closeMidTime != "14:00" ,
                            open_time = i.openTime,
                            close_time = i.closeTime,
                            rest_end_time = i.closeMidTime!!,
                            rest_start_time = i.openMidTime,
                            weekDayIndex = i.weekDayIndex
                        )
                    )
                    Log.e("RestTime", "${i.openMidTime}  & ${i.closeMidTime}")
                }
                hoursList.sortBy { it.weekDayIndex }
                Log.d("hourlistFromDbRead", hoursList.toString())
                activity?.runOnUiThread {
                    if (hoursList.isNotEmpty()) {
                        hoursAdapter = OpenHoursAdapter(hoursList, clicksListener)
                        recyclerView.adapter = hoursAdapter
                        addScheduleList()
                    } else {
                        addDaysToList()
                        addScheduleList()
                        hoursAdapter = OpenHoursAdapter(hoursList, clicksListener)
                        recyclerView.adapter = hoursAdapter
                    }

                }

            }
        }
    }


    private fun addSchedule(placeSchedule: SchedulePlaceTime) {
        lifecycleScope.launch(context = Dispatchers.IO) {
            dbRepo?.addPlaceSchedule(placeSchedule)
        }
    }


    private fun init(view: View) {
        nextBtn = view.findViewById(R.id.btnStep2_continue)
        recyclerView = view.findViewById(R.id.attention_hours_rv)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        hoursList = ArrayList()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_place_step2, container, false)
        init(view)
        if (firstTime == true || firstTime == null) {
            addDaysToList()
            addScheduleList()
            hoursAdapter = OpenHoursAdapter(hoursList, clicksListener)
            recyclerView.adapter = hoursAdapter
        } else {
            nextBtn.setCardBackgroundColor(Color.parseColor("#FF8672"))
            readALlSchedule()
        }


        val animator = recyclerView.itemAnimator
        animator?.changeDuration = 0L
        if (canNextFragment()) {
            nextBtn.setCardBackgroundColor(Color.parseColor("#FF8672"))

        }

        nextBtn.setOnClickListener {
            if (canNextFragment()) {
                nextBtn.setCardBackgroundColor(Color.parseColor("#FF8672"))
                val mockList = mutableListOf<OpenHoursObject>()
                mockList.addAll(hoursList)
                for (i in mockList) {
                    addSchedule(
                        SchedulePlaceTime(
                            active = i.active,
                            day = i.day_name,
                            openTime = i.open_time,
                            closeTime = i.close_time,
                            closeMidTime = i.rest_end_time,
                            openMidTime = i.rest_start_time,
                            haveMidTime = i.restTime,
                            weekDayIndex = i.weekDayIndex
                        )
                    )
                }

                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.new_place_fragment_main, ConfirmScheduledHours())
                    .commit()
                Log.d("mockList", mockList.toString())

            } else {
                nextBtn.setCardBackgroundColor(Color.parseColor("#F3F3F3"))
            }

        }
        return view
    }


    private fun canNextFragment(): Boolean {
        val filteredList = hoursList.filter { it.open_time != "00:00" && it.close_time != "00:00" }
        if (filteredList.isNotEmpty()) {
            return true
        }
        return false
    }


    private val clicksListener = (object : OpenHoursAdapter.ClicksAdapterListener {

        @SuppressLint("NotifyDataSetChanged")
        override fun editBtnClick(v: View?, position: Int) {
            hoursList[position].isEditing = !hoursList[position].isEditing!!
            if (hoursList[position].isEditing == true) {
                for (i in hoursList) {
                    if (i.day_name != hoursList[position].day_name) i.isAnotherEditing = true
                }
            } else {
                for (i in hoursList) {
                    if (i.day_name != hoursList[position].day_name) i.isAnotherEditing = false
                }
            }
            hoursAdapter.notifyDataSetChanged()
        }

        override fun turnBtnClick(v: View?, position: Int) {
            hoursList[position].active = !hoursList[position].active
            hoursAdapter.notifyDataSetChanged()

        }

        override fun cancelBtnClick(v: View?, position: Int) {
            TODO("Not yet implemented")
        }

        override fun saveBtnClick(v: View?, position: Int) {
            hoursList[position].isEditing = false
            for (i in hoursList) {
                if (i.day_name != hoursList[position].day_name) i.isAnotherEditing = false
            }
            if (canNextFragment()) {
                nextBtn.setCardBackgroundColor(Color.parseColor("#FF8672"))
            }
            hoursAdapter.notifyDataSetChanged()
        }

        override fun midTimeCBClick(v: View?, position: Int) {
            hoursList[position].restTime = !hoursList[position].restTime
            hoursAdapter.notifyItemChanged(position)
        }

        override fun openHourCvClick(v: View?, position: Int) {
            configStartTime(dayScheduleList[position], hoursList[position], position)
            hoursAdapter.notifyItemChanged(position)
        }

        override fun closeHourCvClick(v: View?, position: Int) {
            configEndTime(dayScheduleList[position], hoursList[position], position)
            hoursAdapter.notifyItemChanged(position)
        }

        override fun closeMidTimeCvClick(v: View?, position: Int) {
            if (hoursList[position].restTime) {
                configPauseStartTime(dayScheduleList[position], hoursList[position], position)
            }
        }

        override fun openMidTimeCvClick(v: View?, position: Int) {
            if (hoursList[position].restTime) {
                configPauseEndTime(dayScheduleList[position], hoursList[position], position)
            }
        }
    })


    private fun updateTimeLabels(
        daySchedule: CompDaySchedule,
        day: OpenHoursObject,
        position: Int
    ) {
        val startStr = String.format(
            "%02d:%02d",
            daySchedule.dayStart?.get(Calendar.HOUR_OF_DAY),
            daySchedule.dayStart?.get(Calendar.MINUTE)
        )
        val endStr = String.format(
            "%02d:%02d",
            daySchedule.dayEnd?.get(Calendar.HOUR_OF_DAY),
            daySchedule.dayEnd?.get(Calendar.MINUTE)
        )

        day.open_time = if ("null:null" == startStr) {
            daySchedule.dayStart?.set(Calendar.HOUR_OF_DAY, 8)
            "08:00"
        } else startStr

        day.close_time = if ("null:null" == endStr) {
            daySchedule.dayEnd?.set(Calendar.HOUR_OF_DAY, 20)
            "20:00"
        } else endStr

        if (daySchedule.pauseStart != null) {
            val pauseStartStr = String.format(
                "%02d:%02d",
                daySchedule.pauseStart?.get(Calendar.HOUR_OF_DAY),
                daySchedule.pauseStart?.get(Calendar.MINUTE)
            )
            day.rest_start_time = if ("null:null" == pauseStartStr) "00:00" else pauseStartStr
        }
        if (daySchedule.pauseEnd != null) {
            val pauseEndStr = String.format(
                "%02d:%02d",
                daySchedule.pauseEnd!!.get(Calendar.HOUR_OF_DAY),
                daySchedule.pauseEnd!![Calendar.MINUTE]
            )
            day.rest_end_time = if ("null:null" == pauseEndStr) "00:00" else pauseEndStr
        }
        Log.d("listUpdated?", hoursList.toString())
        hoursAdapter.notifyItemChanged(position)


    }


    fun configStartTime(daySchedule: CompDaySchedule, day: OpenHoursObject, position: Int) {
        val cal = Calendar.getInstance()
        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTitleText("Ingresa el horario de  apertura")
            .setHour(8)
            .setPositiveButtonText("Seleccionar")
            .setNegativeButtonText("Salir")
            .setMinute(0)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .build()
        materialTimePicker.show(childFragmentManager, "")
        materialTimePicker.addOnPositiveButtonClickListener {
            cal.set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
            cal.set(Calendar.MINUTE, materialTimePicker.minute)
            daySchedule.dayStart = cal
            if (isAValidStartTime(
                    materialTimePicker.hour,
                    materialTimePicker.minute,
                    daySchedule
                )
            ) {
                updateTimeLabels(daySchedule, day, position)
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 8)
                cal.set(Calendar.MINUTE, 0)
                daySchedule.dayStart = cal
                updateTimeLabels(daySchedule, day, position)
            }

        }
    }

    private fun configEndTime(daySchedule: CompDaySchedule, day: OpenHoursObject, position: Int) {
        val cal = Calendar.getInstance()
        val calMock = Calendar.getInstance()
        calMock.set(Calendar.HOUR_OF_DAY, 8)
        calMock.set(Calendar.MINUTE, 0)
        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTitleText("Ingresa el horario de cierre")
            .setHour(20)
            .setPositiveButtonText("Seleccionar")
            .setNegativeButtonText("Salir")
            .setMinute(0)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .build()
        materialTimePicker.show(childFragmentManager, "")
        materialTimePicker.addOnPositiveButtonClickListener {
            cal.set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
            cal.set(Calendar.MINUTE, materialTimePicker.minute)
            daySchedule.dayEnd = cal
            if (daySchedule.dayStart == null) {
                daySchedule.dayStart = calMock
            }
            if (isAValidEndTime(materialTimePicker.hour, materialTimePicker.minute, daySchedule)) {
                updateTimeLabels(daySchedule, day, position)
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 20)
                cal.set(Calendar.MINUTE, 0)
                daySchedule.dayEnd = cal
                updateTimeLabels(daySchedule, day, position)
            }
        }
    }


    private fun configPauseStartTime(
        daySchedule: CompDaySchedule,
        day: OpenHoursObject,
        position: Int
    ) {
        val cal = Calendar.getInstance()
        val calMock = Calendar.getInstance()
        calMock.set(Calendar.HOUR_OF_DAY, 13)
        calMock.set(Calendar.MINUTE, 0)
        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTitleText("Ingresa el horario del comienzo de descanso")
            .setHour(13)
            .setPositiveButtonText("Seleccionar")
            .setNegativeButtonText("Salir")
            .setMinute(0)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .build()
        materialTimePicker.show(childFragmentManager, "")
        materialTimePicker.addOnPositiveButtonClickListener {
            cal.set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
            cal.set(Calendar.MINUTE, materialTimePicker.minute)
            daySchedule.pauseStart = cal

            if (isAValidPauseStartTime(materialTimePicker.hour, daySchedule)) {
                updateTimeLabels(daySchedule, day, position)
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 13)
                cal.set(Calendar.MINUTE, 0)
                daySchedule.pauseStart = cal
                updateTimeLabels(daySchedule, day, position)
            }
        }
    }

    private fun configPauseEndTime(
        daySchedule: CompDaySchedule,
        day: OpenHoursObject,
        position: Int
    ) {
        val cal = Calendar.getInstance()
        val materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
            .setTitleText("Ingresa el horario de fin de descanso")
            .setHour(14)
            .setPositiveButtonText("Seleccionar")
            .setNegativeButtonText("Salir")
            .setMinute(0)
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .build()
        materialTimePicker.show(childFragmentManager, "")
        materialTimePicker.addOnPositiveButtonClickListener {
            cal.set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
            cal.set(Calendar.MINUTE, materialTimePicker.minute)
            daySchedule.pauseEnd = cal
            if (isAValidPauseEndTime(materialTimePicker.hour, daySchedule)) {
                updateTimeLabels(daySchedule, day, position)
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 14)
                cal.set(Calendar.MINUTE, 0)
                daySchedule.pauseEnd = cal
                updateTimeLabels(daySchedule, day, position)
            }
        }
    }


    fun isAValidStartTime(hourOfDay: Int, minute: Int, daySchComp: CompDaySchedule): Boolean {
        if (isFollowingDay(
                daySchComp.dayEnd?.get(Calendar.HOUR_OF_DAY) ?: Calendar.HOUR_OF_DAY,
                daySchComp.dayEnd?.get(Calendar.MINUTE) ?: Calendar.MINUTE,
                daySchComp
            )
        ) {
            return isOtherDayValid(
                daySchComp.dayEnd?.get(Calendar.HOUR_OF_DAY) ?: Calendar.HOUR_OF_DAY,
                daySchComp.dayEnd?.get(Calendar.MINUTE) ?: Calendar.MINUTE
            )
        }
        if (!isMidNight(hourOfDay, minute) &&
            !isMidNight(
                daySchComp.dayEnd?.get(Calendar.HOUR_OF_DAY) ?: Calendar.HOUR_OF_DAY,
                daySchComp.dayEnd?.get(Calendar.MINUTE) ?: Calendar.MINUTE
            )
        ) {
            if (!isAnHourEarlierThanCalendar(hourOfDay, daySchComp.pauseStart)) {
                showSnackBar("El horario de apertura debe ser anterior al comienzo de la pausa")
                return false
            }
            if (!isAnHourEarlierThanCalendar(hourOfDay, daySchComp.pauseEnd)) {
                showSnackBar("El horario de apertura debe ser anterior al fin de pausa")
                return false
            }
            if (!isAnHourEarlierThanCalendar(hourOfDay, daySchComp.dayEnd)) {
                showSnackBar("El horario de apertura debe ser anterior al horario de cierre")
                return false
            }
        }
        return true
    }

    fun isAValidPauseStartTime(hourOfDay: Int, daySchedule: CompDaySchedule): Boolean {
        if (!isMidNight(
                daySchedule.dayEnd?.get(Calendar.HOUR_OF_DAY) ?: Calendar.HOUR_OF_DAY,
                daySchedule.dayEnd?.get(Calendar.MINUTE) ?: Calendar.MINUTE
            ) &&
            !isFollowingDay(
                daySchedule.dayEnd?.get(Calendar.HOUR_OF_DAY) ?: Calendar.HOUR_OF_DAY,
                daySchedule.dayEnd?.get(Calendar.MINUTE) ?: Calendar.MINUTE,
                daySchedule
            )
        ) {
            if (!isAnHourEarlierThanCalendar(hourOfDay, daySchedule!!.dayEnd)) {
                showSnackBar("El horario de comienzo de pausa debe ser anterior al cierre")
                return false
            }
        }
        if (!isAnHourEarlierThanCalendar(hourOfDay, daySchedule!!.pauseEnd)) {
            showSnackBar("El horario de comienzo de pausa debe ser anterior al fin de la pausa")
            return false
        }
        if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule!!.dayStart)) {
            showSnackBar("El horario de comienzo de pausa debe ser posterior a la apertura")
            return false
        }
        return true
    }

    private fun isAValidPauseEndTime(hourOfDay: Int, daySchedule: CompDaySchedule): Boolean {
        if (!isMidNight(
                daySchedule!!.dayEnd?.get(Calendar.HOUR_OF_DAY) ?: Calendar.HOUR_OF_DAY,
                daySchedule!!.dayEnd?.get(Calendar.MINUTE) ?: Calendar.MINUTE,
            ) &&
            !isFollowingDay(
                daySchedule!!.dayEnd?.get(Calendar.HOUR_OF_DAY) ?: Calendar.HOUR_OF_DAY,
                daySchedule!!.dayEnd?.get(Calendar.MINUTE) ?: Calendar.MINUTE,
                daySchedule
            )
        ) {
            if (!isAnHourEarlierThanCalendar(hourOfDay, daySchedule.dayEnd)) {
                showSnackBar("El horario de fin de pausa debe ser anterior al cierre")
                return false
            }
        }
        if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule.pauseStart)) {
            showSnackBar("El horario de fin de pausa debe ser posterior al comienzo de la pausa")
            return false
         }
        if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule.dayStart)) {
            showSnackBar("El horario de fin de pausa debe ser posterior a la apertura")
            return false
        }
        return true
    }

    private fun isAnHourEarlierThanCalendar(hour: Int, calendar: Calendar?): Boolean {
        return calendar == null || hour < calendar[Calendar.HOUR_OF_DAY]
    }

    private fun isAnHourLaterThanCalendar(hour: Int, calendar: Calendar?): Boolean {
        return calendar == null || hour > calendar[Calendar.HOUR_OF_DAY]
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun isMidNight(hourOfDay: Int, minute: Int): Boolean {
        return hourOfDay == 0 && minute == 0
    }

    private fun isOtherDayValid(hourOfDay: Int, minute: Int): Boolean {
        val minuteThreshold = 60
        val inMinutes = hourOfDay * 60 + minute
        if (inMinutes <= minuteThreshold) {
            return true
        }
        showSnackBar(
            String.format(
                "Sólo puedes limitarte a las %s:%s AM para el horario de cierre",
                minuteThreshold / 60,
                minuteThreshold % 60
            )
        )
        return false
    }


    private fun isFollowingDay(hourOfDay: Int, minute: Int, daySchedule: CompDaySchedule): Boolean {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = hourOfDay
        calendar[Calendar.MINUTE] = minute
        val startTime = daySchedule.dayStart

//        if day start is greater than given time, e.g start: 8:00, end: 00:30
//        thus endtime is in the following day
        return (startTime?.get(Calendar.HOUR_OF_DAY)
            ?: Calendar.HOUR_OF_DAY) > calendar[Calendar.HOUR_OF_DAY]
    }

    fun isAValidEndTime(hourOfDay: Int, minute: Int, daySchedule: CompDaySchedule): Boolean {
        if (isFollowingDay(hourOfDay, minute, daySchedule)) {
            return isOtherDayValid(hourOfDay, minute)
        }
        if (!isMidNight(hourOfDay, minute)
            && !isMidNight(
                daySchedule.dayEnd!!.get(Calendar.HOUR_OF_DAY), daySchedule.dayStart!!.get(
                    Calendar.MINUTE
                )
            )
        ) {
            if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule.dayStart!!)) {
                showSnackBar("El horario de cierre debe ser posterior a la apertura")
                return false
            }
            if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule.pauseEnd)) {
                showSnackBar("El horario de cierre debe ser posterior al fin de la pausa")
                return false
            }
            if (!isAnHourLaterThanCalendar(hourOfDay, daySchedule.pauseStart)) {
                showSnackBar("El horario de cierre debe ser posterior al comienzo de la pausa")
                return false
            }
        }
        return true
    }

    private fun addDaysToList() {
        hoursList.add(
            OpenHoursObject(
                "Lunes",
                open_time = "08:00",
                close_time = "20:00",
                rest_start_time = "13:00",
                rest_end_time = "14:00",
                active = true,
                isEditing = false,
                restTime = false,
                weekDayIndex = 0
            )
        )
        hoursList.add(
            OpenHoursObject(
                "Martes",
                open_time = "08:00",
                close_time = "20:00",
                rest_start_time = "13:00",
                rest_end_time = "14:00",
                active = true,
                isEditing = false,
                restTime = false,
                        weekDayIndex = 1
            )
        )
        hoursList.add(
            OpenHoursObject(
                "Miércoles",
                open_time = "08:00",
                close_time = "20:00",
                rest_start_time = "13:00",
                rest_end_time = "14:00",
                active = true,
                isEditing = false,
                restTime = false,
                weekDayIndex = 2
            )
        )
        hoursList.add(
            OpenHoursObject(
                "Jueves",
                open_time = "08:00",
                close_time = "20:00",
                rest_start_time = "13:00",
                rest_end_time = "14:00",
                active = true,
                isEditing = false,
                restTime = false,
                weekDayIndex = 3
            )
        )
        hoursList.add(
            OpenHoursObject(
                "Viernes",
                open_time = "08:00",
                close_time = "20:00",
                rest_start_time = "13:00",
                rest_end_time = "14:00",
                active = true,
                isEditing = false,
                restTime = false,
                        weekDayIndex = 4
            )
        )
        hoursList.add(
            OpenHoursObject(
                "Sábado",
                open_time = "08:00",
                close_time = "13:00",
                rest_start_time = "13:00",
                rest_end_time = "14:00",
                active = false,
                isEditing = false,
                restTime = false,
                weekDayIndex = 5
            )
        )
        hoursList.add(
            OpenHoursObject(
                "Domingo",
                open_time = "08:00",
                close_time = "13:00",
                rest_start_time = "13:00",
                rest_end_time = "14:00",
                active = false,
                isEditing = false,
                restTime = false,
                weekDayIndex = 6
            )
        )
    }

    private fun addScheduleList() {
        dayScheduleList.add(
            CompDaySchedule(
                null,
                null,
                null,
                null
            )
        )
        dayScheduleList.add(
            CompDaySchedule(
                null,
                null,
                null,
                null
            )
        )
        dayScheduleList.add(
            CompDaySchedule(
                null,
                null,
                null,
                null
            )
        )
        dayScheduleList.add(
            CompDaySchedule(
                null,
                null,
                null,
                null
            )
        )
        dayScheduleList.add(
            CompDaySchedule(
                null,
                null,
                null,
                null
            )
        )
        dayScheduleList.add(
            CompDaySchedule(
                null,
                null,
                null,
                null
            )
        )
        dayScheduleList.add(
            CompDaySchedule(
                null,
                null,
                null,
                null
            )
        )

    }





}