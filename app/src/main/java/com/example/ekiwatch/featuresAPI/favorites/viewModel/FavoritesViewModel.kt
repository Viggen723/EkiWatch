package com.example.ekiwatch.featuresAPI.favorites.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ekiwatch.data.local.EkiWatchDatabase
import com.example.ekiwatch.data.local.favorite.FavoriteRepository
import com.example.ekiwatch.data.local.landmark.LandmarkEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = EkiWatchDatabase.getDatabase(application)
    private val favoriteRepository = FavoriteRepository(database.favoriteDao())

    val favoriteLandmarks: StateFlow<List<LandmarkEntity>> = favoriteRepository
        .getFavoriteLandmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(landmarkId: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(landmarkId)
        }
    }
}