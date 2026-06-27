package com.example.ekiwatch.featuresAPI.recent.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ekiwatch.data.local.EkiWatchDatabase
import com.example.ekiwatch.data.local.recentPlaces.RecentPlaceRepository
import com.example.ekiwatch.data.local.recentPlaces.RecentStationView
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RecentPlacesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = EkiWatchDatabase.getDatabase(application)
    private val recentPlaceRepository = RecentPlaceRepository(database.recentPlaceDao())

    val recentStations: StateFlow<List<RecentStationView>> = recentPlaceRepository
        .getRecentStations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}