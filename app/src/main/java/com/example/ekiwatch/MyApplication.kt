package com.example.ekiwatch // Make sure this matches your folder path

import android.app.Application
import com.example.ekiwatch.data.local.EkiWatchDatabase
import com.example.ekiwatch.data.local.seed.LandmarkSeeder
import com.example.ekiwatch.featuresAPI.notifications.NotificationHelper
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyApplication : Application() {

    // Making it so the database is globally accessible
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var placesClient: PlacesClient

    override fun onCreate() {
        super.onCreate()

        // Channel must exist before any geofence can trigger a notification
        NotificationHelper.createNotificationChannel(this)

        Places.initialize(applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
        placesClient = Places.createClient(applicationContext)

        // application context used to make it thread safe
        val database = EkiWatchDatabase.getDatabase(this)
        val landmarkDao = database.landmarkDao()

        val seeder = LandmarkSeeder(this, landmarkDao)

        // Always use a coroutine to have it running in the background
        applicationScope.launch {
            try {
                seeder.seedIfEmpty()
            } catch (e: Exception) {
                // If JSON is bad, spit out an error
                e.printStackTrace()
            }
        }
    }
}