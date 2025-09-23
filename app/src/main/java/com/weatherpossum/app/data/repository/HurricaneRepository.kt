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
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.squareup.moshi.Moshi
import java.io.IOException

private const val TAG = "HurricaneRepository"
private const val CACHE_DURATION_MILLIS = 60 * 60 * 1000L // 1 hour

class HurricaneRepository {
    private val nhcApi: HurricaneFeedsApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
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

                // Fetch & parse CurrentStorms.json
                val stormsJson = nhcApi.currentStorms().string()
                val storms = parseCurrentStorms(stormsJson)

                // Fetch & parse TWO text
                val twoHtml = nhcApi.atlanticTwoText().string()
                val two = TwoTextParser.parse(twoHtml)

                val data = HurricaneData(
                    activeStorms = storms,
                    tropicalOutlook = two.cleaned,
                    lastUpdated = System.currentTimeMillis(),
                    isFromCache = false
                )

                _hurricaneData.value = data
                lastFetchTime = System.currentTimeMillis()
                Result.success(data)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching hurricane data", e)
                _hurricaneData.value?.let { return@withContext Result.success(it.copy(isFromCache = true)) }
                Result.failure(e)
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
