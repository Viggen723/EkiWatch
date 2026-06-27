package com.example.ekiwatch.data.local.landmark

import androidx.room.Entity
import androidx.room.PrimaryKey

// See the JSON file that is in assets folder to see the format in detail
@Entity(tableName = "landmarks")
data class LandmarkEntity(
    @PrimaryKey
    val id: String,
    val osmId: String,
    val name: String,
    val nameJa: String?,
    val category: String,
    val lat: Double,
    val lon: Double,
    val wikidata: String? = null,
    val wikipedia: String? = null,
    val website: String? = null,
    val openingHours: String? = null,
    val description: String? = null,
    val phone: String? = null,
    val fee: String? = null,
    val wheelchair: String? = null,
    val nearestStationId: String? = null
)

// All of the categories that were included in the OverpassAPI export
// TODO Note: about 75 percent of them are TEMPLE, so maybe we make some way to balance this?
object LandmarkCategory {
    const val TEMPLE = "TEMPLE"
    const val SHRINE = "SHRINE"
    const val SHRINE_TEMPLE = "SHRINE_TEMPLE"
    const val MUSEUM = "MUSEUM"
    const val ARTWORK = "ARTWORK"
    const val VIEWPOINT = "VIEWPOINT"
    const val ATTRACTION = "ATTRACTION"
    const val HISTORIC = "HISTORIC"
    const val THEME_PARK = "THEME_PARK"
    const val ZOO = "ZOO"
    const val OTHER = "OTHER"
}
