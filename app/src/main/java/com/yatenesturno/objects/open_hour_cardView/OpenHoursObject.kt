package com.yatenesturno.objects.open_hour_cardView

data class OpenHoursObject(
    val day_name : String,
    var active : Boolean,
    var isEditing : Boolean?,
    var isAnotherEditing : Boolean? = false,
    var restTime : Boolean,
    var open_time : String,
    var close_time : String,
    var rest_start_time : String?,
    var rest_end_time : String,
    var weekDayIndex : Int
)
