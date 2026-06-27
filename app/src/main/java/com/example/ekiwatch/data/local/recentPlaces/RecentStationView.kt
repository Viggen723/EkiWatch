package com.example.ekiwatch.data.local.recentPlaces

/**
 * One row for the "Visited" screen: a station's display data plus when the
 * user most recently passed through its geofence. Room can populate this
 * directly from a query as long as the column names line up (see
 * RecentPlaceDao.getRecentStations).
 */
data class RecentStationView(
    val id: String,
    val name: String,
    val nameJa: String?,
    val lat: Double,
    val lon: Double,
    val lastVisitedAtEpochMillis: Long
)