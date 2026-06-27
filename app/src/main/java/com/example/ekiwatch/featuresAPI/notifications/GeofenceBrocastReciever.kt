package com.example.ekiwatch.featuresAPI.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ekiwatch.data.local.EkiWatchDatabase
import com.example.ekiwatch.data.local.recentPlaces.RecentPlaceRepository
import com.example.ekiwatch.data.local.settings.SettingsManager
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val event = GeofencingEvent.fromIntent(intent) ?: return
            if (event.hasError()) {
                android.util.Log.e("EkiWatch", "Geofence Error: ${event.errorCode}")
                return
            }

            val triggeringGeofence = event.triggeringGeofences?.firstOrNull() ?: return
            val stationId = triggeringGeofence.requestId
            android.util.Log.d("EkiWatch", "Geofence enter event for $stationId")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = SettingsManager.getInstance(context)
                    if (stationId == GeofenceManager.SELECTED_DESTINATION_REQUEST_ID) {
                        if (settings.notificationsEnabled.value) {
                            NotificationHelper.showStationArrivalNotification(
                                context = context,
                                stationId = stationId,
                                title = "Arriving soon",
                                body = "You are close to your selected destination."
                            )
                        }
                        return@launch
                    }

                    val db = EkiWatchDatabase.getDatabase(context)
                    val landmarks = db.landmarkDao().getByNearestStation(stationId)
                    val station = db.stationDao().getById(stationId)

                    // --- DEBUG: Print how many landmarks we found ---
                    android.util.Log.d("EkiWatch", "Found ${landmarks.size} landmarks for $stationId")

                    val notificationText = if (landmarks.isNotEmpty()) {
                        "Arriving soon! Nearby: ${landmarks.first().name}"
                    } else {
                        "Arriving soon. Prepare to exit shortly!"
                    }

                    // Always record the visit, regardless of whether the user
                    // has notifications turned on - "Visited" is a history
                    // feature, not a notification.
                    RecentPlaceRepository(db.recentPlaceDao()).logVisit(stationId)

                    if (settings.notificationsEnabled.value) {
                        NotificationHelper.showStationArrivalNotification(
                            context = context,
                            stationId = stationId,
                            title = station?.name ?: "Arriving soon",
                            body = notificationText
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EkiWatch", "Database/Notification error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("EkiWatch", "Receiver error: ${e.message}")
        }
    }
}
