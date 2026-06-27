package com.example.ekiwatch.featuresAPI.map.routing

import kotlinx.serialization.Serializable

// All of the route requests that are made through the Google API. Takes the JSON and makes it niice
// See how the Landmark entities also work in this way with the serialization
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
