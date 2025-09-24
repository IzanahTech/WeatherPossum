package com.weatherpossum.app.data.repository

import android.util.Log
import com.weatherpossum.app.data.api.HurricaneFeedsApi
import com.weatherpossum.app.data.model.HurricaneData
import com.weatherpossum.app.data.model.Hurricane
import com.weatherpossum.app.data.model.CurrentStormsDto
import com.weatherpossum.app.data.model.StormDto
import com.weatherpossum.app.data.parser.TwoTextParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.squareup.moshi.Moshi
import java.io.IOException

private const val TAG = "HurricaneRepository"
private const val CACHE_DURATION_MILLIS = 60 * 60 * 1000L // 1 hour
private const val MAX_RETRIES = 2
private const val INITIAL_RETRY_DELAY = 500L

class HurricaneRepository {
    private val nhcApi: HurricaneFeedsApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "WeatherPossum/1.4.8 (Android)")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
        Retrofit.Builder()
            .baseUrl("https://www.nhc.noaa.gov/")
            .client(client)
            .build()
            .create(HurricaneFeedsApi::class.java)
    }
    
    private val moshi = Moshi.Builder().build()
    private val stormsAdapter = moshi.adapter(CurrentStormsDto::class.java)
    
    
    private val _hurricaneData = MutableStateFlow<HurricaneData?>(null)
    val hurricaneData: StateFlow<HurricaneData?> = _hurricaneData.asStateFlow()
    
    private var lastFetchTime: Long = 0
    
    suspend fun getActiveHurricanes(forceRefresh: Boolean = false): Result<HurricaneData> =
        withContext(Dispatchers.IO) {
            try {
                if (!forceRefresh && isCacheValid()) {
                    return@withContext Result.success(_hurricaneData.value!!)
                }

                // Fetch & parse CurrentStorms.json (fast attempt first)
                val stormsJson = try {
                    Log.d(TAG, "Attempting to fetch CurrentStorms.json")
                    val response = nhcApi.currentStorms()
                    Log.d(TAG, "Got response, converting to string")
                    response.string()
                } catch (e: Exception) {
                    Log.w(TAG, "First attempt failed for storms, retrying: ${e.message}", e)
                    retryWithTimeout { 
                        Log.d(TAG, "Retry attempt for CurrentStorms.json")
                        nhcApi.currentStorms().string() 
                    }
                }
                val storms = parseCurrentStorms(stormsJson)

                // Fetch & parse TWO text (fast attempt first)
                val twoHtml = try {
                    Log.d(TAG, "Attempting to fetch TWO text")
                    val response = nhcApi.atlanticTwoText()
                    Log.d(TAG, "Got TWO response, converting to string")
                    response.string()
                } catch (e: Exception) {
                    Log.w(TAG, "First attempt failed for TWO, retrying: ${e.message}", e)
                    retryWithTimeout { 
                        Log.d(TAG, "Retry attempt for TWO text")
                        nhcApi.atlanticTwoText().string() 
                    }
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

                _hurricaneData.value = data
                lastFetchTime = System.currentTimeMillis()
                Result.success(data)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching hurricane data", e)
                // Return cached data if available, otherwise return failure
                _hurricaneData.value?.let { 
                    Log.w(TAG, "Network error. Returning cached data.")
                    return@withContext Result.success(it.copy(isFromCache = true)) 
                }
                // If no cached data, create a minimal error response instead of failing completely
                Log.w(TAG, "No cached data available. Creating empty hurricane data.")
                val emptyData = HurricaneData(
                    activeStorms = emptyList(),
                    tropicalOutlook = "Unable to fetch hurricane data. Please check your internet connection and try again.",
                    forecaster = null,
                    issued = null,
                    lastUpdated = System.currentTimeMillis(),
                    isFromCache = false
                )
                Result.success(emptyData)
            }
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
            } catch (e: Exception) {
                Log.w(TAG, "Retry attempt ${it + 1} failed: ${e.message}")
                
                // Only retry on specific network errors, not all exceptions
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
                    throw e // Don't retry if it's not a network error
                }
            }
        }
        throw IOException("All retries failed after $MAX_RETRIES attempts")
    }
    
    private fun parseCurrentStorms(json: String): List<Hurricane> {
        val dto = stormsAdapter.fromJson(json) ?: return emptyList()
        return dto.activeStorms
            .filter { isAtlanticStorm(it) } // Only include Atlantic storms
            .map { s ->
                val windSpeed = s.intensity?.toIntOrNull() ?: 0
                val category = calculateHurricaneCategory(s.classification, windSpeed)
                val location = formatLocation(s.latitude, s.longitude)
                val status = formatStormStatus(s.classification, windSpeed)
                
                Hurricane(
                    id = s.id ?: "unknown",
                    name = s.name ?: "Unknown",
                    category = category,
                    status = status,
                    location = location,
                    windSpeed = windSpeed,
                    pressure = s.pressure?.toIntOrNull() ?: 0,
                    lastUpdated = s.lastUpdate ?: System.currentTimeMillis().toString()
                )
            }
    }
    
    private fun isAtlanticStorm(storm: StormDto): Boolean {
        // Atlantic storms have IDs starting with "AL" (e.g., "al072025")
        // Pacific storms have IDs starting with "EP" (e.g., "ep142025")
        val stormId = storm.id?.lowercase() ?: return false
        return stormId.startsWith("al")
    }
    
    private fun calculateHurricaneCategory(classification: String?, windSpeed: Int): Int {
        return when {
            classification != "HU" -> 0 // Not a hurricane
            windSpeed >= 157 -> 5
            windSpeed >= 130 -> 4
            windSpeed >= 111 -> 3
            windSpeed >= 96 -> 2
            windSpeed >= 74 -> 1
            else -> 0
        }
    }
    
    private fun formatLocation(latitude: String?, longitude: String?): String {
        return if (!latitude.isNullOrBlank() && !longitude.isNullOrBlank()) {
            "$latitude, $longitude"
        } else {
            "Location not available"
        }
    }
    
    private fun formatStormStatus(classification: String?, windSpeed: Int): String {
        return when (classification) {
            "HU" -> "Hurricane (Cat ${calculateHurricaneCategory(classification, windSpeed)})"
            "TS" -> "Tropical Storm"
            "TD" -> "Tropical Depression"
            "PTC" -> "Post-Tropical Cyclone"
            else -> classification ?: "Unknown"
        }
    }
    
}
