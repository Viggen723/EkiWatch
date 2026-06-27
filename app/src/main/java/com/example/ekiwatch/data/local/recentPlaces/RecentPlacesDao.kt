package com.example.ekiwatch.data.local.recentPlaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPlaceDao {

    @Insert
    suspend fun insert(visit: RecentPlaceEntity)

    @Query("SELECT COUNT(*) FROM recent_places")
    suspend fun count(): Int

    /**
     * One row per station, showing only the most recent visit time, ordered
     * newest-first. This is what backs the "Visited" tab - a log of every
     * geofence trigger would just be noise to a user scrolling their history.
     */
    @Query(
        """
        SELECT
            stations.id AS id,
            stations.name AS name,
            stations.nameJa AS nameJa,
            stations.lat AS lat,
            stations.lon AS lon,
            MAX(recent_places.visitedAtEpochMillis) AS lastVisitedAtEpochMillis
        FROM recent_places
        INNER JOIN stations ON stations.id = recent_places.stationId
        GROUP BY stations.id
        ORDER BY lastVisitedAtEpochMillis DESC
        """
    )
    fun getRecentStations(): Flow<List<RecentStationView>>

    @Query("DELETE FROM recent_places")
    suspend fun deleteAll()
}