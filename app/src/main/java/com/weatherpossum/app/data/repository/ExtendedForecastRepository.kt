package com.weatherpossum.app.data.repository

import android.util.Log
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.data.api.ExtendedForecastApi
import com.weatherpossum.app.data.model.ForecastDay
import com.weatherpossum.app.data.model.Result
import com.weatherpossum.app.data.parser.ExtendedForecastParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.util.Calendar

private const val TAG = "ExtendedForecastRepository"
private const val CACHE_DURATION_MILLIS = 30 * 60 * 1000L

class ExtendedForecastRepository(
    private val api: ExtendedForecastApi,
    private val userPreferences: UserPreferences
) {
    private var cachedDays: List<ForecastDay>? = null
    private var lastFetchTime: Long = 0
    private var diskCacheHydrated = false

    fun shouldRefreshAtScheduledBoundary(): Boolean {
        val now = Calendar.getInstance()
        val currentMillis = now.timeInMillis
        val refreshHours = listOf(6, 12, 18)
        for (refreshHour in refreshHours) {
            val refreshTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, refreshHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (currentMillis >= refreshTime.timeInMillis && lastFetchTime < refreshTime.timeInMillis) {
                return true
            }
        }
        return false
    }

    suspend fun getExtendedForecast(forceRefresh: Boolean = false): Result<List<ForecastDay>> {
        hydrateFromDiskIfNeeded()

        return try {
            if (!forceRefresh && isCacheValid()) {
                return Result.Success(cachedDays!!)
            }

            val html = withContext(Dispatchers.IO) {
                api.getExtendedForecastHtml()
            }

            if (html.isBlank()) {
                return staleCacheOrError("Empty extended forecast response")
            }

            val days = ExtendedForecastParser.parse(html)
            if (days.isEmpty()) {
                return staleCacheOrError("No forecast days parsed")
            }

            persistCache(days)
            Result.Success(days)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching extended forecast", e)
            staleCacheOrError(e.message ?: "Extended forecast fetch failed", e)
        }
    }

    private suspend fun hydrateFromDiskIfNeeded() {
        if (diskCacheHydrated) return
        diskCacheHydrated = true
        if (cachedDays != null) return

        userPreferences.loadCachedExtendedForecast()?.let { (days, fetchedAt) ->
            cachedDays = days
            lastFetchTime = fetchedAt
            Log.d(TAG, "Hydrated ${days.size} extended forecast days from disk cache")
        }
    }

    private suspend fun persistCache(days: List<ForecastDay>) {
        cachedDays = days
        lastFetchTime = System.currentTimeMillis()
        userPreferences.saveCachedExtendedForecast(days, lastFetchTime)
    }

    private fun isCacheValid(): Boolean {
        return cachedDays != null && System.currentTimeMillis() - lastFetchTime < CACHE_DURATION_MILLIS
    }

    private fun staleCacheOrError(
        message: String,
        cause: Exception? = null
    ): Result<List<ForecastDay>> {
        val cached = cachedDays
        return if (cached != null) {
            Log.w(TAG, "Serving stale extended forecast: $message")
            Result.Success(cached, isStale = true)
        } else {
            Result.Error(cause ?: Exception(message))
        }
    }
}
