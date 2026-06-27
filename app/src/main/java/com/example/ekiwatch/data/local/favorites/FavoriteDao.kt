package com.example.ekiwatch.data.local.favorites

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ekiwatch.data.local.landmark.LandmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE landmarkId = :landmarkId")
    suspend fun deleteByLandmarkId(landmarkId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE landmarkId = :landmarkId)")
    fun isFavorite(landmarkId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun count(): Int

    @Query(
        """
        SELECT landmarks.* FROM landmarks
        INNER JOIN favorites ON landmarks.id = favorites.landmarkId
        ORDER BY favorites.savedAtEpochMillis DESC
        """
    )
    fun getFavoriteLandmarks(): Flow<List<LandmarkEntity>>

    @Query("DELETE FROM favorites")
    suspend fun deleteAll()
}