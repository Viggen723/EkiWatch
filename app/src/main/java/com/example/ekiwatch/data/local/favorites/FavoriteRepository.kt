package com.example.ekiwatch.data.local.favorite

import com.example.ekiwatch.data.local.favorites.FavoriteDao
import com.example.ekiwatch.data.local.favorites.FavoriteEntity
import com.example.ekiwatch.data.local.landmark.LandmarkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FavoriteRepository(private val favoriteDao: FavoriteDao) {

    fun getFavoriteLandmarks(): Flow<List<LandmarkEntity>> =
        favoriteDao.getFavoriteLandmarks()

    fun isFavorite(landmarkId: String): Flow<Boolean> =
        favoriteDao.isFavorite(landmarkId)

    suspend fun addFavorite(landmarkId: String) {
        withContext(Dispatchers.IO) {
            favoriteDao.insert(
                FavoriteEntity(
                    landmarkId = landmarkId,
                    savedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun removeFavorite(landmarkId: String) {
        withContext(Dispatchers.IO) {
            favoriteDao.deleteByLandmarkId(landmarkId)
        }
    }

    suspend fun toggleFavorite(landmarkId: String, currentlyFavorite: Boolean) {
        if (currentlyFavorite) removeFavorite(landmarkId) else addFavorite(landmarkId)
    }
}