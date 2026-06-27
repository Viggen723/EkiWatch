package com.example.ekiwatch.data.remote.ekispert

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * TODO Read this description for how this is going to be implemented
 * Retrofit interface for Ekispert's route-search endpoint.
 *
 * Verify the base URL below (api.ekispert.jp)
 * is what's referenced in third-party Ekispert SDK source,
 *
 * Endpoint used: search/course/light - the simplest "from one station to
 * another" search (see https://docs.ekispert.com/v1/api/search/course/light.html).
 * Ekispert also has search/course/extreme (supports multi-stop viaList,
 * landmark-based points, etc.) and search/course/plain (average-wait-time
 * search) - swap the @GET path if the other is needed
 */
interface EkispertApi {

    @GET("v1/json/search/course/light")
    suspend fun searchCourseLight(
        @Query("key") apiKey: String,
        @Query("from") fromStationCode: String,
        @Query("to") toStationCode: String
    ): EkispertResultSet

    companion object {
        const val BASE_URL = "https://api.ekispert.jp/"
    }
}