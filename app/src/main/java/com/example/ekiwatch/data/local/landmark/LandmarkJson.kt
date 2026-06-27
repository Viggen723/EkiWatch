package com.example.ekiwatch.data.local.landmark

import kotlinx.serialization.Serializable

// This needs to map the JSON layout exactly! Serializer will then do the heavy lifting
@Serializable
data class LandmarkJson(
    val id: String,
    val osmId: String,
    val name: String,
    val nameJa: String? = null,
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
    val wheelchair: String? = null
)

// Makes the DTO into the proper entity
fun LandmarkJson.toEntity(): LandmarkEntity = LandmarkEntity(
    id = id,
    osmId = osmId,
    name = name,
    nameJa = nameJa,
    category = category,
    lat = lat,
    lon = lon,
    wikidata = wikidata,
    wikipedia = wikipedia,
    website = website,
    openingHours = openingHours,
    description = description,
    phone = phone,
    fee = fee,
    wheelchair = wheelchair,
    nearestStationId = null // Have to map when the station is known (by the coordinates
)