package com.example.ekiwatch // Make sure this matches your folder path

import android.app.Application
import com.example.ekiwatch.data.local.AppDatabase
import com.example.ekiwatch.data.local.seed.LandmarkSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyApplication : Application() {

    // Making it so the database is globally accessible
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // application context used to make it thread safe
        val database = AppDatabase.getInstance(this)
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