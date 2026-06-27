package com.example.ekiwatch.data.remote.ekispert

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Builds the Retrofit client for EkispertApi, using kotlinx.serialization as
 * the JSON converter (Taking the recieved JSON and then converting it to make the other classes)
 */
object EkispertClient {

    private val json = Json { ignoreUnknownKeys = true }

    val api: EkispertApi by lazy {
        Retrofit.Builder()
            .baseUrl(EkispertApi.BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(EkispertApi::class.java)
    }
}