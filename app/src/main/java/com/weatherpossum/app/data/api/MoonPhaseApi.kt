package com.weatherpossum.app.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface MoonPhaseApi {
    @GET("astronomy")
    suspend fun getMoonPhase(
        @Query("apiKey") apiKey: String,
        @Query("lat") lat: Double,
        @Query("long") long: Double
    ): MoonPhaseResponse
}

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