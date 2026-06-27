package com.example.ekiwatch.data.local.recentPlaces

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_places")
data class RecentPlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stationId: String, // matches StationEntity.id
    val visitedAtEpochMillis: Long
)