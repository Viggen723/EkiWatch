package com.example.ekiwatch.featuresAPI.map.routing

import kotlinx.serialization.Serializable

@Serializable
data class RouteRequest(
    val origin: Waypoint,
    val destination: Waypoint,
    val travelMode: String = "WALK"
)

@Serializable
data class Waypoint(val location: LocationData)

@Serializable
data class LocationData(val latLng: LatLngData)

@Serializable
data class LatLngData(val latitude: Double, val longitude: Double)

@Serializable
data class RouteResponse(val routes: List<Route> = emptyList())

@Serializable
data class Route(val polyline: PolylineData)

@Serializable
data class PolylineData(val encodedPolyline: String)
