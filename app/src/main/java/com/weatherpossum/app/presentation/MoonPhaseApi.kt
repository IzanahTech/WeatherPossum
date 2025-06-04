package com.weatherpossum.app.presentation

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