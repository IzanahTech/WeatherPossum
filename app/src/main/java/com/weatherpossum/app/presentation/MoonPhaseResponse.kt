package com.weatherpossum.app.presentation

data class MoonPhaseResponse(
    val location: LocationData,
    val moon_phase: String,
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String
)

data class LocationData(
    val latitude: String,
    val longitude: String
) 