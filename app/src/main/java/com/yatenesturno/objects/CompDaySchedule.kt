package com.yatenesturno.objects

import java.util.*

data class CompDaySchedule(
    var dayStart : Calendar?=null,
    var dayEnd : Calendar?=null,
    var pauseStart : Calendar? = null,
    var pauseEnd : Calendar?= null
)
