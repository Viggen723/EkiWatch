package com.example.ekiwatch.data.remote.ekispert

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface EkispertApi {

    companion object {
        const val BASE_URL = "https://api.ekispert.jp/"
    }

    /**
     * Fetches full train route nodes including coordinates
     */
    @GET("v1/json/search/course/light")
    suspend fun getRoute(
        @Query("key") apiKey: String,
        @Query("from") fromStation: String,
        @Query("to") toStation: String
    ): Response<EkispertResultSet>
}