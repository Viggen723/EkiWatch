package com.example.ekiwatch.data.local.seed

import android.content.Context
import com.example.ekiwatch.data.local.landmark.LandmarkDao
import com.example.ekiwatch.data.local.landmark.LandmarkJson
import com.example.ekiwatch.data.local.landmark.toEntity
import kotlinx.serialization.json.Json

// This is called then in the MyApplication class upon startup.
class LandmarkSeeder(
    private val context: Context,
    private val dao: LandmarkDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedIfEmpty(assetFileName: String = "landmarks.json") {
        if (dao.count() > 0) return

        val jsonText = context.assets.open(assetFileName)
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        val landmarks: List<LandmarkJson> = json.decodeFromString(jsonText)
        dao.insertAll(landmarks.map { it.toEntity() })
    }
}