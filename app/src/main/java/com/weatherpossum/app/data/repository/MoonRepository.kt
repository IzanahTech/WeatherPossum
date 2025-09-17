package com.weatherpossum.app.data.repository

import com.weatherpossum.app.data.MoonData
import com.weatherpossum.app.util.MoonPhaseCalculator
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate

private const val TAG = "MoonRepository"

class MoonRepository() {
    private val _moonData = MutableStateFlow<MoonData?>(null)
    val moonData: StateFlow<MoonData?> = _moonData.asStateFlow()

    suspend fun getMoonPhase(lat: Double, long: Double): Result<MoonData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Calculating moon phase data locally for lat: $lat, long: $long")
            
            val currentDate = LocalDate.now()
            val phase = MoonPhaseCalculator.calculateMoonPhase()
            val illumination = MoonPhaseCalculator.calculateIllumination()
            val (moonrise, moonset) = MoonPhaseCalculator.calculateMoonTimes(currentDate, lat, long)
            
            val moonData = MoonData(
                phase = phase,
                moonrise = MoonPhaseCalculator.formatTimeTo12Hour(moonrise),
                moonset = MoonPhaseCalculator.formatTimeTo12Hour(moonset),
                illumination = illumination
            )
            
            Log.d(TAG, "Local moon phase calculation completed: phase=$phase, illumination=${illumination * 100}%")
            _moonData.value = moonData
            Result.success(moonData)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating moon phase data locally", e)
            Result.failure(e)
        }
    }

    suspend fun refreshMoonData(): Result<MoonData> {
        Log.d(TAG, "refreshMoonData: called - using local calculation")
        // Using Dominica's coordinates as default
        return getMoonPhase(15.414999, -61.370976)
    }
} 