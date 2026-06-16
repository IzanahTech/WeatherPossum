package com.weatherpossum.app.data.repository

import android.util.Log
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.data.api.HurricaneFeedsApi
import com.weatherpossum.app.data.model.HurricaneData
import com.weatherpossum.app.data.model.Hurricane
import com.weatherpossum.app.data.model.CurrentStormsDto
import com.squareup.moshi.Moshi
import com.weatherpossum.app.data.model.StormDto
import com.weatherpossum.app.data.parser.TwoTextParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import com.weatherpossum.app.data.api.HttpClients
import retrofit2.Retrofit
import java.io.IOException
import kotlinx.coroutines.withTimeout

private const val TAG = "HurricaneRepository"
private const val CACHE_DURATION_MILLIS = 60 * 60 * 1000L // 1 hour
private const val MAX_RETRIES = 2
private const val INITIAL_RETRY_DELAY = 500L
private const val FETCH_TIMEOUT_MS = 45_000L

class HurricaneRepository(
    private val userPreferences: UserPreferences
) {
    private val nhcApi: HurricaneFeedsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.nhc.noaa.gov/")
            .client(HttpClients.default())
            .build()
            .create(HurricaneFeedsApi::class.java)
    }

    private val stormsAdapter = Moshi.Builder().build().adapter(CurrentStormsDto::class.java)

    private val _hurricaneData = MutableStateFlow<HurricaneData?>(null)
    val hurricaneData: StateFlow<HurricaneData?> = _hurricaneData.asStateFlow()

    private var lastFetchTime: Long = 0
    private var diskCacheHydrated = false

    suspend fun getActiveHurricanes(forceRefresh: Boolean = false): Result<HurricaneData> =
        withContext(Dispatchers.IO) {
            hydrateFromDiskIfNeeded()
            try {
                withTimeout(FETCH_TIMEOUT_MS) {
                    fetchActiveHurricanes(forceRefresh)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching hurricane data", e)
                handleHurricaneFetchFailure(e)
            }
        }

    private suspend fun hydrateFromDiskIfNeeded() {
        if (diskCacheHydrated) return
        diskCacheHydrated = true
        if (_hurricaneData.value != null) return

        userPreferences.loadCachedHurricaneData()?.let { (data, fetchedAt) ->
            _hurricaneData.value = data
            lastFetchTime = fetchedAt
            Log.d(TAG, "Hydrated hurricane data from disk cache")
        }
    }

    private suspend fun fetchActiveHurricanes(forceRefresh: Boolean): Result<HurricaneData> {
        try {
            if (!forceRefresh && isCacheValid()) {
                return Result.success(_hurricaneData.value!!)
            }

            val stormsJson = try {
                Log.d(TAG, "Attempting to fetch CurrentStorms.json")
                nhcApi.currentStorms().string()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "First attempt failed for storms, retrying: ${e.message}", e)
                retryWithTimeout { nhcApi.currentStorms().string() }
            }
            val storms = parseCurrentStorms(stormsJson)

            val twoHtml = try {
                Log.d(TAG, "Attempting to fetch TWO text")
                nhcApi.atlanticTwoText().string()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "First attempt failed for TWO, retrying: ${e.message}", e)
                retryWithTimeout { nhcApi.atlanticTwoText().string() }
            }
            val two = TwoTextParser.parse(twoHtml)

            val data = HurricaneData(
                activeStorms = storms,
                tropicalOutlook = two.cleaned,
                forecaster = two.forecaster,
                issued = two.issued,
                lastUpdated = System.currentTimeMillis(),
                isFromCache = false
            )

            persistCache(data)
            return Result.success(data)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return handleHurricaneFetchFailure(e)
        }
    }

    private suspend fun persistCache(data: HurricaneData) {
        _hurricaneData.value = data
        lastFetchTime = System.currentTimeMillis()
        userPreferences.saveCachedHurricaneData(data, lastFetchTime)
    }

    private fun handleHurricaneFetchFailure(e: Exception): Result<HurricaneData> {
        _hurricaneData.value?.let {
            Log.w(TAG, "Network error. Returning cached data.", e)
            return Result.success(it.copy(isFromCache = true))
        }
        return Result.failure(e)
    }

    private fun isCacheValid(): Boolean {
        return _hurricaneData.value != null &&
            System.currentTimeMillis() - lastFetchTime < CACHE_DURATION_MILLIS
    }

    suspend fun refreshHurricaneData(): Result<HurricaneData> {
        Log.d(TAG, "refreshHurricaneData: called")
        return getActiveHurricanes(forceRefresh = true)
    }

    private suspend fun <T> retryWithTimeout(block: suspend () -> T): T {
        var delayTime = INITIAL_RETRY_DELAY
        repeat(MAX_RETRIES) {
            try {
                return withContext(Dispatchers.IO) { block() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Retry attempt ${it + 1} failed: ${e.message}")

                val shouldRetry = when {
                    e.message?.contains("UnknownHostException") == true -> true
                    e.message?.contains("timeout") == true -> true
                    e.message?.contains("Connection") == true -> true
                    e is IOException -> true
                    else -> false
                }

                if (it < MAX_RETRIES - 1 && shouldRetry) {
                    delay(delayTime)
                    delayTime *= 2
                } else {
                    throw e
                }
            }
        }
        throw IOException("All retries failed after $MAX_RETRIES attempts")
    }

    private fun parseCurrentStorms(json: String): List<Hurricane> {
        val dto = stormsAdapter.fromJson(json) ?: return emptyList()
        return dto.activeStorms
            .filter { isAtlanticStorm(it) }
            .map { s ->
                val windSpeed = s.intensity?.toIntOrNull() ?: 0
                val category = calculateHurricaneCategory(s.classification, windSpeed)
                Hurricane(
                    id = s.id ?: "unknown",
                    name = s.name.orEmpty(),
                    category = category,
                    classification = s.classification,
                    location = formatCoordinates(s.latitude, s.longitude),
                    windSpeed = windSpeed,
                    pressure = s.pressure?.toIntOrNull() ?: 0,
                    lastUpdated = s.lastUpdate ?: System.currentTimeMillis().toString()
                )
            }
    }

    private fun isAtlanticStorm(storm: StormDto): Boolean {
        val stormId = storm.id?.lowercase() ?: return false
        return stormId.startsWith("al")
    }

    private fun calculateHurricaneCategory(classification: String?, windSpeed: Int): Int {
        return when {
            classification != "HU" -> 0
            windSpeed >= 157 -> 5
            windSpeed >= 130 -> 4
            windSpeed >= 111 -> 3
            windSpeed >= 96 -> 2
            windSpeed >= 74 -> 1
            else -> 0
        }
    }

    private fun formatCoordinates(latitude: String?, longitude: String?): String {
        return if (!latitude.isNullOrBlank() && !longitude.isNullOrBlank()) {
            "$latitude, $longitude"
        } else {
            ""
        }
    }
}
