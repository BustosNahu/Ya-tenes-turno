package com.yatenesturno.activities.services.step3.objects.objects_views

import android.util.Log
import android.view.ViewGroup
import java.util.*

class NewTimePicker(parent: ViewGroup) : com.yatenesturno.activities.services.step3.objects.objects_views.NumberCounter(parent) {
    private var MAX_HOURS = 3
    private var MINUTE_STEP = 15
    private lateinit var  calendar: Calendar
    private var showMinutes = true

    override fun init() {
        calendar = Calendar.getInstance()
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
    }


    override fun increaseCounter() {
        Log.e("asdasd", "increased")
        calendar.add(Calendar.MINUTE, MINUTE_STEP)
    }

    override fun decreaseCounter() {
        Log.e("asdasd", "decreased")
        calendar.add(Calendar.MINUTE, -MINUTE_STEP)
    }

    override fun isAtBottomLimit(): Boolean {
        return calendar[Calendar.HOUR_OF_DAY] == 0 && calendar[Calendar.MINUTE] == 0
    }


    fun setStep(minutes: Int) {
        MINUTE_STEP = minutes
    }

    override fun isAtTopLimit(): Boolean {
        return calendar!![Calendar.HOUR_OF_DAY] == MAX_HOURS
    }

    fun setMax(hours: Int) {
        MAX_HOURS = hours
    }

    fun isDisplayingHours() : Boolean{
        return  calendar[Calendar.HOUR_OF_DAY] >= 1
    }

    override fun getDisplayableText(): String {
        Log.d("calendarValueMinute", calendar[Calendar.MINUTE].toString())
        Log.d("calendarValueHourofdaySetted", calendar[Calendar.HOUR_OF_DAY].toString())

        if (calendar[Calendar.HOUR_OF_DAY] >= 1) {
            return calendar[Calendar.HOUR_OF_DAY].toString() + "h" + calendar[Calendar.MINUTE].toString() + "m"
        } else {
            return calendar[Calendar.MINUTE].toString() + "m"
         }
    }

    var time: Calendar?
        get() = calendar
        set(calendar) {
            if (calendar != null) {
                this.calendar = calendar.clone() as Calendar
            }
            updateUI()
        }
}