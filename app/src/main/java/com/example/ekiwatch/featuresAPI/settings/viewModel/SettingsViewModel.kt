package com.example.ekiwatch.featuresAPI.settings.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.ekiwatch.data.local.settings.DistanceUnit
import com.example.ekiwatch.data.local.settings.SettingsManager
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // Settings manager was set up in the local files. Maybe should move it here?
    private val settingsManager = SettingsManager.getInstance(application)

    val notificationsEnabled: StateFlow<Boolean> = settingsManager.notificationsEnabled
    val geofenceRadiusMeters: StateFlow<Float> = settingsManager.geofenceRadiusMeters
    val distanceUnit: StateFlow<DistanceUnit> = settingsManager.distanceUnit

    fun setNotificationsEnabled(enabled: Boolean) {
        settingsManager.setNotificationsEnabled(enabled)
    }

    fun setGeofenceRadiusMeters(radius: Float) {
        settingsManager.setGeofenceRadiusMeters(radius)
    }

    fun setDistanceUnit(unit: DistanceUnit) {
        settingsManager.setDistanceUnit(unit)
    }

    fun resetToDefaults() {
        settingsManager.resetToDefaults()
    }
}