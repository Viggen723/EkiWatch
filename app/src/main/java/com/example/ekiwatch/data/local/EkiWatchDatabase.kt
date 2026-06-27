package com.example.ekiwatch.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ekiwatch.data.local.station.StationEntity
import com.example.ekiwatch.data.local.station.StationDao
import com.example.ekiwatch.data.local.landmark.LandmarkEntity
import com.example.ekiwatch.data.local.landmark.LandmarkDao
import com.example.ekiwatch.data.local.favorites.FavoriteEntity
import com.example.ekiwatch.data.local.favorites.FavoriteDao
import com.example.ekiwatch.data.local.recentPlaces.RecentPlaceEntity
import com.example.ekiwatch.data.local.recentPlaces.RecentPlaceDao

@Database(
    entities = [
        StationEntity::class,
        LandmarkEntity::class,
        FavoriteEntity::class,
        RecentPlaceEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class EkiWatchDatabase : RoomDatabase() {

    abstract fun stationDao(): StationDao
    abstract fun landmarkDao(): LandmarkDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentPlaceDao(): RecentPlaceDao

    companion object {
        @Volatile
        private var INSTANCE: EkiWatchDatabase? = null

        fun getDatabase(context: Context): EkiWatchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EkiWatchDatabase::class.java,
                    "ekiwatch_database"
                )
                    // No migrations written yet for this early-stage schema -
                    // rebuilding the DB on a version bump is fine while
                    // landmarks/stations are reseeded from JSON anyway.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}