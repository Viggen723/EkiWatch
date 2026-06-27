package com.example.ekiwatch.data.local.landmark

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LandmarkDao {

    // All of the queries that we made need when getting the landmarks from the database, as well as delete
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(landmarks: List<LandmarkEntity>)

    @Query("SELECT COUNT(*) FROM landmarks")
    suspend fun count(): Int

    @Query("SELECT * FROM landmarks WHERE id = :id")
    suspend fun getById(id: String): LandmarkEntity?

    @Query("SELECT * FROM landmarks")
    fun getAll(): Flow<List<LandmarkEntity>>

    @Query("SELECT * FROM landmarks WHERE category = :category")
    fun getByCategory(category: String): Flow<List<LandmarkEntity>>

    // A way to get the landmarks that are close to that geofenced area
    @Query(
        """
        SELECT * FROM landmarks
        WHERE lat BETWEEN :minLat AND :maxLat
        AND lon BETWEEN :minLon AND :maxLon
        """
    )
    suspend fun getWithinBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<LandmarkEntity>

    /**
     * The query that actually matters for the "landmarks near station two away"
     * feature - once nearestStationId is populated, this is the lookup that gets
     * called on every notification trigger. O(landmarks at that station), not a
     * full scan.
     */
    @Query("SELECT * FROM landmarks WHERE nearestStationId = :stationId")
    suspend fun getByNearestStation(stationId: String): List<LandmarkEntity>

    @Query("UPDATE landmarks SET nearestStationId = :stationId WHERE id = :landmarkId")
    suspend fun setNearestStation(landmarkId: String, stationId: String)

    @Query("DELETE FROM landmarks")
    suspend fun deleteAll()
}
