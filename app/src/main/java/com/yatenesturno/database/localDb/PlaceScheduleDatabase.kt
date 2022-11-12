package com.yatenesturno.database.localDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [SchedulePlaceTime::class], version = 1, exportSchema = false)
abstract class PlaceScheduleDatabase : RoomDatabase() {

    abstract fun placeScheduleDao() : PlaceScheduleDao

    companion object{

        @Volatile
        private var INSTANCE : PlaceScheduleDatabase? = null

        fun getDb(context: Context) : PlaceScheduleDatabase{
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlaceScheduleDatabase::class.java,
                    "place_schedule_db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}