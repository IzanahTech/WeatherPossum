package com.weatherpossum.app.data.repository

import android.util.Log
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.data.api.WeatherForecastApi
import com.weatherpossum.app.data.model.Result
import com.weatherpossum.app.data.model.WeatherCard
import com.weatherpossum.app.data.parser.DominicaWeatherParser
import com.weatherpossum.app.data.parser.WeatherCardMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "WeatherRepository"
private const val CACHE_DURATION_MILLIS = 30 * 60 * 1000L // 30 minutes
private const val MAX_RETRIES = 2
private const val INITIAL_RETRY_DELAY = 750L

class WeatherRepository(
    private val weatherApi: WeatherForecastApi,
    private val userPreferences: UserPreferences
) {
    private var cachedCards: List<WeatherCard>? = null
    private var lastFetchTime: Long = 0
    private var diskCacheHydrated = false

    suspend fun readCachedForecast(): Pair<List<WeatherCard>, Boolean>? {
        hydrateFromDiskIfNeeded()
        val cards = cachedCards ?: return null
        return cards to !isCacheValid()
    }

    suspend fun getWeatherForecast(forceRefresh: Boolean = false): Result<List<WeatherCard>> {
        hydrateFromDiskIfNeeded()

        return try {
            if (!forceRefresh && isCacheValid()) {
                return Result.Success(cachedCards!!)
            }

            val html = retryWithTimeout {
                weatherApi.getWeatherForecast()
            }

            if (html.isBlank()) {
                Log.w(TAG, "Received empty HTML response")
                return staleCacheOrError("Empty response and no cached data available")
            }

            val cards = parseWeatherCards(html)

            if (cards.isEmpty()) {
                Log.w(TAG, "No cards parsed from HTML")
                return staleCacheOrError("Failed to parse weather data")
            }

            persistCache(cards)
            Result.Success(cards)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather forecast", e)
            staleCacheOrError(e.message ?: "Network error", e)
        }
    }

    private suspend fun hydrateFromDiskIfNeeded() {
        if (diskCacheHydrated) return
        diskCacheHydrated = true
        if (cachedCards != null) return

        userPreferences.loadCachedWeatherCards()?.let { (cards, fetchedAt) ->
            cachedCards = cards
            lastFetchTime = fetchedAt
            Log.d(TAG, "Hydrated ${cards.size} weather cards from disk cache")
        }
    }

    private suspend fun persistCache(cards: List<WeatherCard>) {
        cachedCards = cards
        lastFetchTime = System.currentTimeMillis()
        userPreferences.saveCachedWeatherCards(cards, lastFetchTime)
    }

    private fun staleCacheOrError(
        noCacheMessage: String,
        cause: Exception? = null
    ): Result<List<WeatherCard>> {
        val cached = cachedCards
        return if (cached != null) {
            Log.w(TAG, "Serving stale cache: $noCacheMessage")
            Result.Success(cached, isStale = true)
        } else {
            Result.Error(cause ?: Exception(noCacheMessage))
        }
    }

    private fun isCacheValid(): Boolean {
        return cachedCards != null && System.currentTimeMillis() - lastFetchTime < CACHE_DURATION_MILLIS
    }

    private suspend fun <T> retryWithTimeout(block: suspend () -> T): T {
        var delayTime = INITIAL_RETRY_DELAY
        repeat(MAX_RETRIES) {
            try {
                return withContext(Dispatchers.IO) { block() }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                delay(delayTime)
                delayTime *= 2
            }
        }
        throw java.io.IOException("All retries failed")
    }

    private fun parseWeatherCards(html: String): List<WeatherCard> {
        return try {
            val parsed = DominicaWeatherParser.parse(html)
            WeatherCardMapper.toWeatherCards(parsed)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing weather data", e)
            emptyList()
        }
    }
}
