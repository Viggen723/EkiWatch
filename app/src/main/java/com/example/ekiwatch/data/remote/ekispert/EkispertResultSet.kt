package com.example.ekiwatch.data.remote.ekispert

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EkispertResultSet(
    @SerialName("ResultSet")
    val resultSet: EkispertResultSetBody
)

@Serializable
data class EkispertResultSetBody(
    @SerialName("Course")
    val courses: List<EkispertCourse> = emptyList()
)

@Serializable
data class EkispertCourse(
    @SerialName("Route")
    val route: EkispertRoute
)

@Serializable
data class EkispertRoute(
    @SerialName("Point")
    val points: List<EkispertPoint> = emptyList()
)

@Serializable
data class EkispertPoint(
    @SerialName("Station")
    val station: EkispertStation? = null,
    @SerialName("GeoPoint")
    val geoPoint: EkispertGeoPoint? = null
)

@Serializable
data class EkispertStation(
    @SerialName("code")
    val code: String,
    @SerialName("Name")
    val name: String,
    @SerialName("Yomi")
    val yomi: String? = null
)

@Serializable
data class EkispertGeoPoint(
    @SerialName("longi_d")
    val longitude: Double? = null,
    @SerialName("lati_d")
    val latitude: Double? = null
)