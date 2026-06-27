package com.example.ekiwatch.data.local.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val landmarkId: String,
    val savedAtEpochMillis: Long   // System.currentTimeMillis() at the time of saving
)