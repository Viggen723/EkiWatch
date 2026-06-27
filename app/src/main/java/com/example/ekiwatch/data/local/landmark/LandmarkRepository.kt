package com.example.ekiwatch.data.local.landmark

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LandmarkRepository(private val landmarkDao: LandmarkDao) {

    // Fetch landmarks associated with a specific station code
    suspend fun getLandmarksForStation(stationId: String): List<LandmarkEntity> {
        return withContext(Dispatchers.IO) {
            landmarkDao.getByNearestStation(stationId)
        }
    }

    // Insert mock or fresh data into the database
    suspend fun insertLandmarks(landmarks: List<LandmarkEntity>) {
        withContext(Dispatchers.IO) {
            landmarkDao.insertAll(landmarks)
        }
    }
}