package com.example.ekiwatch.data.local.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple SharedPreferences-backed store for user-facing app settings.
 *
 * Kept deliberately small and dependency-free (no DataStore) since these are
 * a handful of flags/values, not structured or relational data - that's what
 * Room is for elsewhere in this app (see landmark/, station/, favorite/,
 * recent/). Each setting exposes a StateFlow so Compose screens can collect
 * it directly and recompose on change.
 */
class SettingsManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _geofenceRadiusMeters = MutableStateFlow(
        prefs.getFloat(KEY_GEOFENCE_RADIUS_METERS, DEFAULT_GEOFENCE_RADIUS_METERS)
    )
    val geofenceRadiusMeters: StateFlow<Float> = _geofenceRadiusMeters.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _distanceUnit = MutableStateFlow(
        DistanceUnit.fromStorageValue(prefs.getString(KEY_DISTANCE_UNIT, null))
    )
    val distanceUnit: StateFlow<DistanceUnit> = _distanceUnit.asStateFlow()

    private val _preferredLandmarkCategories = MutableStateFlow(
        prefs.getStringSet(KEY_PREFERRED_CATEGORIES, null) ?: emptySet()
    )
    val preferredLandmarkCategories: StateFlow<Set<String>> = _preferredLandmarkCategories.asStateFlow()

    fun setGeofenceRadiusMeters(radius: Float) {
        prefs.edit().putFloat(KEY_GEOFENCE_RADIUS_METERS, radius).apply()
        _geofenceRadiusMeters.value = radius
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun setDistanceUnit(unit: DistanceUnit) {
        prefs.edit().putString(KEY_DISTANCE_UNIT, unit.storageValue).apply()
        _distanceUnit.value = unit
    }

    fun setPreferredLandmarkCategories(categories: Set<String>) {
        prefs.edit().putStringSet(KEY_PREFERRED_CATEGORIES, categories).apply()
        _preferredLandmarkCategories.value = categories
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _geofenceRadiusMeters.value = DEFAULT_GEOFENCE_RADIUS_METERS
        _notificationsEnabled.value = true
        _distanceUnit.value = DistanceUnit.KILOMETERS
        _preferredLandmarkCategories.value = emptySet()
    }

    companion object {
        private const val PREFS_NAME = "ekiwatch_settings"

        private const val KEY_GEOFENCE_RADIUS_METERS = "geofence_radius_meters"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_DISTANCE_UNIT = "distance_unit"
        private const val KEY_PREFERRED_CATEGORIES = "preferred_landmark_categories"

        const val DEFAULT_GEOFENCE_RADIUS_METERS = 500f

        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context).also { INSTANCE = it }
            }
        }
    }
}

enum class DistanceUnit(val storageValue: String) {
    KILOMETERS("km"),
    MILES("mi");

    companion object {
        fun fromStorageValue(value: String?): DistanceUnit =
            entries.firstOrNull { it.storageValue == value } ?: KILOMETERS
    }
}