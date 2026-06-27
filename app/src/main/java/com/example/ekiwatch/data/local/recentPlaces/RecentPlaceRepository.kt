package com.example.ekiwatch.data.local.recentPlaces

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RecentPlaceRepository(private val recentPlaceDao: RecentPlaceDao) {

    fun getRecentStations(): Flow<List<RecentStationView>> =
        recentPlaceDao.getRecentStations()



    /**
     * Called whenever a station's geofence is entered - see
     * GeofenceBroadcastReceiver. Each call appends a new visit row, so the
     * underlying log keeps full history even though the UI only shows the
     * latest visit per station.
     */
    suspend fun logVisit(stationId: String) {
        withContext(Dispatchers.IO) {
            recentPlaceDao.insert(
                RecentPlaceEntity(
                    stationId = stationId,
                    visitedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun removeFavorite(stationId: String) {
        withContext(Dispatchers.IO) {
            recentPlaceDao.deleteByStationId(stationId)
        }
    }
}