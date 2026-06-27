package com.example.ekiwatch.data.local.station

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stations: List<StationEntity>)

    @Query("SELECT COUNT(*) FROM stations")
    suspend fun count(): Int

    @Query("SELECT * FROM stations WHERE id = :id")
    suspend fun getById(id: String): StationEntity?

    /**
     * The crosswalk lookup: given an Ekispert Point code from a route-search
     * response, resolve it to our canonical StationEntity. This is the query
     * that turns Ekispert's ordered station list into something the rest of
     * the app can use (see the ActiveRoute / orderedStations design).
     */
    @Query("SELECT * FROM stations WHERE ekispertCode = :ekispertCode")
    suspend fun getByEkispertCode(ekispertCode: String): StationEntity?

    /**
     * Bounding box search: pulls candidate stations in a local rectangular frame.
     * Used as a high-performance pre-filter before calculating exact pedestrian routes.
     */
    @Query("""
        SELECT * FROM stations 
        WHERE lat BETWEEN :minLat AND :maxLat 
        AND lon BETWEEN :minLon AND :maxLon
    """)
    suspend fun getStationsInBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<StationEntity>

    @Query("SELECT * FROM stations")
    suspend fun getAll(): List<StationEntity>

    @Query("DELETE FROM stations")
    suspend fun deleteAll()
}