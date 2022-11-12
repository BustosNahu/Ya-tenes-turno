package com.yatenesturno.database.localDb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface PlaceScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPlaceScheduleDao(schedule : SchedulePlaceTime)

    @Query( "SELECT * FROM scheduled_place WHERE place_id =:placeId ")
    fun readPlacePrevSchedule(placeId : Int) : Flow<SchedulePlaceTime>

    @Query( "SELECT * FROM scheduled_place")
    fun readAllPlacesWithoutPlaceId() : Flow<List<SchedulePlaceTime>>
}