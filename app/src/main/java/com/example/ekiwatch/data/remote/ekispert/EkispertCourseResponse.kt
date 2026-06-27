package com.example.ekiwatch.data.remote.ekispert

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// https://docs.ekispert.com/v1/en/api/search/course/ -check for the structure is right for all of the data classes
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

// A single stop which is going to contain a below defined station as well as the coords
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
    val code: String,           // This is the code that will be in the crosswalk
    @SerialName("Name")
    val name: String,
    @SerialName("Yomi")
    val yomi: String? = null
)

// Each value here is the decimal longitude and latitude points denoted by the serial name
@Serializable
data class EkispertGeoPoint(
    @SerialName("longi_d")
    val longitude: Double? = null,
    @SerialName("lati_d")
    val latitude: Double? = null
)