package com.example.ekiwatch.data.local.station

import com.example.ekiwatch.data.remote.ekispert.EkispertCourse
import com.example.ekiwatch.data.remote.ekispert.EkispertStation
import java.security.MessageDigest

// Maps the station id from Ekispert to the entity id
class StationCrosswalk(
    private val stationDao: StationDao
) {

    // Gives back a list of the needed stations on a route (Ekispert course response)
    suspend fun resolveOrderedStations(course: EkispertCourse): List<StationEntity> {
        val resolved = mutableListOf<StationEntity>()

        for (point in course.route.points) {
            val station = point.station ?: continue // walking only points have no Station

            val existing = stationDao.getByEkispertCode(station.code)
            val entity = existing ?: createAndInsert(
                station = station,
                lat = point.geoPoint?.latitude,
                lon = point.geoPoint?.longitude
            )
            resolved += entity
        }

        return resolved
    }

    private suspend fun createAndInsert(
        station: EkispertStation,
        lat: Double?,
        lon: Double?
    ): StationEntity {
        val entity = StationEntity(
            id = canonicalIdFor(station),
            ekispertCode = station.code,
            name = station.name,
            nameJa = station.name, // Ekispert names are Japanese by default for most;
            // We will have to revisit this to make sure we get the English names working
            lat = lat ?: 0.0, // TODO: fill in real coordinates - see note above
            lon = lon ?: 0.0
        )
        stationDao.insertAll(listOf(entity))
        return entity
    }

    private fun canonicalIdFor(station: EkispertStation): String {
        val digest = MessageDigest.getInstance("SHA-1")
            .digest(station.code.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(12)
        return "stn_$digest"
    }
}