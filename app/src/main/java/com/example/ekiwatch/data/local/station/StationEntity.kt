package com.example.ekiwatch.data.local.station

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a single train station.
 *
 * The [ekispertCode] field is the crosswalk discussed when designing the route
 * model: Ekispert's route-search responses identify stations by their own
 * internal code (a "Point" code), which is almost certainly NOT the same id
 * scheme used here or in any geojson/OSM source. Rather than fuzzy-matching
 * station names at runtime (unreliable across romanization differences -
 * "Shinjuku" vs "Shinjuku-eki" vs 新宿), this column lets a one-time mapping
 * step resolve "Ekispert says Point code X" -> "our StationEntity with id Y"
 * via a simple indexed lookup.
 *
 * id is our own canonical StationId (e.g. "odakyu_shinjuku"), not OSM's id and
 * not Ekispert's code - see LandmarkEntity for the same reasoning applied to
 * landmarks via osmId.
 */
@Entity(
    tableName = "stations",
    indices = [Index(value = ["ekispertCode"], unique = true)]
)
data class StationEntity(
    @PrimaryKey
    val id: String,                 // our StationId (Name that is going to be like the above example
    val ekispertCode: String,       // crosswalk to Ekispert's Point code
    val name: String,               // display name, English
    val nameJa: String? = null,
    val lat: Double,
    val lon: Double
)
