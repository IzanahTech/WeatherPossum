package com.weatherpossum.app.data.repository

import com.weatherpossum.app.data.api.ApiClient
import com.weatherpossum.app.data.api.MoonPhaseResponse
import com.weatherpossum.app.data.MoonData
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

private const val TAG = "MoonRepository"

class MoonRepository {
    private val api = ApiClient.moonPhaseApi
    private val _moonData = MutableStateFlow<MoonData?>(null)
    val moonData: StateFlow<MoonData?> = _moonData.asStateFlow()

    suspend fun getMoonPhase(lat: Double, long: Double): Result<MoonData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching moon phase data for lat: $lat, long: $long")
            val response = api.getMoonPhase(
                apiKey = "8ab2b02ea2e14dd185ea10dba53902de",
                lat = lat,
                long = long
            )
            Log.d(TAG, "Moon phase data received: $response")
            
            val moonData = MoonData(
                phase = response.moon_phase,
                moonrise = MoonData.formatTimeTo12Hour(response.moonrise),
                moonset = MoonData.formatTimeTo12Hour(response.moonset),
                illumination = 0.0 // TODO: Add illumination calculation if available from API
            )
            
            _moonData.value = moonData
            Result.success(moonData)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching moon phase data", e)
            Result.failure(e)
        }
    }

    suspend fun refreshMoonData() {
        Log.d(TAG, "refreshMoonData: called")
        // Using Dominica's coordinates as default
        getMoonPhase(15.414999, -61.370976)
    }
} 