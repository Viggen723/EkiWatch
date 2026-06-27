package com.example.ekiwatch.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ekiwatch.data.local.landmark.LandmarkDao
import com.example.ekiwatch.data.local.landmark.LandmarkEntity

@Database(
    entities = [
        LandmarkEntity::class
        // StationEntity::class  Once stations is ready with the Ekispert API, add it here and change the version
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun landmarkDao(): LandmarkDao
    // abstract fun stationDao(): StationDao  Add here too

    companion object {
        private const val DATABASE_NAME = "ekiwatch.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { INSTANCE = it }
            }
    }
}