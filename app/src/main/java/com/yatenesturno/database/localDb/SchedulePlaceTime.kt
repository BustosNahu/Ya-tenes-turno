package com.yatenesturno.database.localDb

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "scheduled_place")
data class SchedulePlaceTime(
    val place_id : Int? = null,
    @PrimaryKey(autoGenerate = false)
    val day : String,
    val active : Boolean,
    val openTime : String,
    val closeTime : String,
    val haveMidTime : Boolean,
    val closeMidTime : String? = null,
    val openMidTime : String? = null,
    val weekDayIndex : Int
)
