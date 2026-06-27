package com.example.ekiwatch.featuresAPI.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.ekiwatch.data.local.station.StationEntity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class GeofenceManager(private val context: Context) {

    companion object {
        const val SELECTED_DESTINATION_REQUEST_ID = "selected_destination"
    }

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun registerStationGeofences(stations: List<StationEntity>, radiusMeters: Float = 500f) {
        if (stations.isEmpty()) return

        val geofenceList = stations.map { station ->
            Geofence.Builder()
                .setRequestId(station.id) // Key used to crosswalk back in receiver
                .setCircularRegion(station.lat, station.lon, radiusMeters)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setNotificationResponsiveness(5000) // 5 seconds limits battery load
                .build()
        }

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnSuccessListener {
                android.util.Log.d("EkiWatch", "Successfully anchored ${geofenceList.size} station geofences.")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("EkiWatch", "Failed to register geofences: ${e.message}", e)
            }
    }

    fun clearActiveGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }

    @SuppressLint("MissingPermission")
    fun registerSelectedDestinationGeofence(destination: LatLng, radiusMeters: Float = 500f) {
        val geofence = Geofence.Builder()
            .setRequestId(SELECTED_DESTINATION_REQUEST_ID)
            .setCircularRegion(destination.latitude, destination.longitude, radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setNotificationResponsiveness(5000)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.removeGeofences(listOf(SELECTED_DESTINATION_REQUEST_ID))
            .addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener {
                        android.util.Log.d("EkiWatch", "Successfully registered selected destination geofence.")
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.d("EkiWatch", "Failed to register selected destination geofence: ${e.message}", e)
                    }
            }
    }
}
