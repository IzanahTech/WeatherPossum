package com.weatherpossum.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CurrentStormsDto(
    @Json(name = "activeStorms") val activeStorms: List<StormDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class StormDto(
    val id: String?,
    val name: String?,
    val classification: String?,    // HU, TS, TD, etc.
    val intensity: String?,        // Wind speed in mph
    val pressure: String?,         // Pressure in mb
    val latitude: String?,         // e.g., "33.2N"
    val longitude: String?,        // e.g., "59.7W"
    val latitudeNumeric: Double?,  // 33.2
    val longitudeNumeric: Double?, // -59.7
    val movementDir: Int?,         // Direction in degrees
    val movementSpeed: Int?,       // Speed in mph
    val lastUpdate: String?,       // ISO timestamp
    val publicAdvisory: AdvisoryDto?
)

@JsonClass(generateAdapter = true)
data class AdvisoryDto(
    val advNum: String?,           // Advisory number like "026"
    val issuance: String?,         // ISO timestamp
    val url: String?               // Link to advisory
)
