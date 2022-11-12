package com.yatenesturno.database.localDb

import kotlinx.coroutines.flow.Flow

class PlaceScheduleRepository(private val placeSchDao : PlaceScheduleDao) {


    fun readPreviousScheduled(placeId : Int) : Flow<SchedulePlaceTime> {
        return placeSchDao.readPlacePrevSchedule(placeId)
    }

    suspend fun addPlaceSchedule(placeTime: SchedulePlaceTime) {
        placeSchDao.addPlaceScheduleDao(placeTime)
    }

    fun readAllPlacesWithoutPlaceId() : Flow<List<SchedulePlaceTime>> {
        return placeSchDao.readAllPlacesWithoutPlaceId()
    }
}