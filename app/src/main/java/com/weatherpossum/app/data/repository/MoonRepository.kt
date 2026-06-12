package com.weatherpossum.app.data.repository

import android.util.Log
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.data.model.MoonData
import com.weatherpossum.app.util.MoonPhaseCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

private const val TAG = "MoonRepository"

class MoonRepository(
    private val userPreferences: UserPreferences
) {
    private val _moonData = MutableStateFlow<MoonData?>(null)
    val moonData: StateFlow<MoonData?> = _moonData.asStateFlow()

    private var diskCacheHydrated = false

    suspend fun getMoonPhase(lat: Double, long: Double): Result<MoonData> = withContext(Dispatchers.IO) {
        hydrateFromDiskIfNeeded()
        try {
            val currentDate = LocalDate.now()
            val moonState = MoonPhaseCalculator.computeMoonState(lat, long)
            val (moonrise, moonset) = MoonPhaseCalculator.calculateMoonTimes(currentDate, lat, long)

            val moonData = MoonData(
                phase = moonState.phase,
                moonrise = MoonPhaseCalculator.formatTimeTo12Hour(moonrise),
                moonset = MoonPhaseCalculator.formatTimeTo12Hour(moonset),
                illumination = moonState.illumination
            )

            persistMoonData(moonData)
            Result.success(moonData)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating moon phase data locally", e)
            Result.failure(e)
        }
    }

    suspend fun refreshMoonData(): Result<MoonData> {
        hydrateFromDiskIfNeeded()
        return getMoonPhase(15.414999, -61.370976)
    }

    suspend fun warmDiskCache() {
        hydrateFromDiskIfNeeded()
    }

    private suspend fun hydrateFromDiskIfNeeded() {
        if (diskCacheHydrated) return
        diskCacheHydrated = true
        if (_moonData.value != null) return

        userPreferences.loadCachedMoonData()?.let { cached ->
            _moonData.value = cached
            Log.d(TAG, "Hydrated moon data from disk cache")
        }
    }

    private suspend fun persistMoonData(data: MoonData) {
        _moonData.value = data
        userPreferences.saveCachedMoonData(data)
    }
}

/** Daily refresh schedule for moon data (4 AM local). */
object MoonFetchSchedule {
    private const val FETCH_HOUR = 4

    fun shouldFetchMoonData(lastFetchTime: Long?): Boolean {
        if (lastFetchTime == null) return true

        val lastFetch = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(lastFetchTime),
            ZoneId.systemDefault()
        )
        val now = LocalDateTime.now()

        return lastFetch.toLocalDate().isBefore(now.toLocalDate()) &&
            now.toLocalTime().isAfter(LocalTime.of(FETCH_HOUR, 0))
    }
}
