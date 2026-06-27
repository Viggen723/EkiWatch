package com.example.ekiwatch.featuresAPI.map.routing

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// The actual HTTP request interface that is used in the RoutingRepository
interface RoutesApiService {
    @POST("directions/v2:computeRoutes")
    suspend fun computeRoutes(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String = "routes.polyline.encodedPolyline",
        @Body request: RouteRequest
    ): RouteResponse
}