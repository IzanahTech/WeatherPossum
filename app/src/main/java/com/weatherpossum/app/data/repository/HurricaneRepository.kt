package com.weatherpossum.app.data.repository

import android.util.Log
import com.weatherpossum.app.data.api.HurricaneApi
import com.weatherpossum.app.data.model.HurricaneData
import com.weatherpossum.app.data.model.Hurricane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException

private const val TAG = "HurricaneRepository"
private const val CACHE_DURATION_MILLIS = 60 * 60 * 1000L // 1 hour

class HurricaneRepository {
    private val api: HurricaneApi by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        
        Retrofit.Builder()
            .baseUrl("https://www.nhc.noaa.gov/") // NHC RSS feed URL
            .client(okHttpClient)
            .build()
            .create(HurricaneApi::class.java)
    }
    
    
    private val _hurricaneData = MutableStateFlow<HurricaneData?>(null)
    val hurricaneData: StateFlow<HurricaneData?> = _hurricaneData.asStateFlow()
    
    private var lastFetchTime: Long = 0
    
    suspend fun getActiveHurricanes(forceRefresh: Boolean = false): Result<HurricaneData> = withContext(Dispatchers.IO) {
        try {
            if (!forceRefresh && isCacheValid()) {
                Log.d(TAG, "Using cached hurricane data")
                return@withContext Result.success(_hurricaneData.value!!)
            }
            
            Log.d(TAG, "Fetching fresh hurricane data from NHC RSS feed")
            
            // Fetch RSS feed from NHC
            val responseBody = api.getHurricaneRssFeed()
            val xmlContent = responseBody.string()
            
            if (xmlContent.isBlank()) {
                Log.w(TAG, "Received empty RSS response")
                _hurricaneData.value?.let { cachedData ->
                    return@withContext Result.success(cachedData.copy(isFromCache = true))
                }
                return@withContext Result.failure(IOException("Empty RSS response"))
            }
            
            // Parse hurricanes and outlook from RSS feed using JSoup
            val (hurricanes, tropicalOutlook) = parseRssXml(xmlContent)
            
            val hurricaneData = HurricaneData(
                activeStorms = hurricanes,
                tropicalOutlook = tropicalOutlook,
                lastUpdated = System.currentTimeMillis(),
                isFromCache = false
            )
            
            _hurricaneData.value = hurricaneData
            lastFetchTime = System.currentTimeMillis()
            
            Log.d(TAG, "Successfully parsed ${hurricanes.size} hurricanes from RSS feed")
            Result.success(hurricaneData)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching hurricane data", e)
            _hurricaneData.value?.let { cachedData ->
                return@withContext Result.success(cachedData.copy(isFromCache = true))
            }
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
    
    /**
     * Parse RSS XML to extract only the Atlantic Tropical Weather Outlook
     * Ignore all individual storm advisories to prevent chaotic display
     */
    private fun parseRssXml(xmlContent: String): Pair<List<Hurricane>, String?> {
        val hurricanes = mutableListOf<Hurricane>()
        var tropicalOutlook: String? = null
        
        try {
            val doc: Document = Jsoup.parse(xmlContent)
            val items = doc.select("item")
            
            for (item in items) {
                val title = item.select("title").text()
                val description = item.select("description").text()
                
                if (title.isBlank()) continue
                
                // ONLY extract the Atlantic Tropical Weather Outlook
                // Ignore all individual storm advisories (Hurricane, Tropical Storm, Tropical Depression)
                if (title.contains("Atlantic Tropical Weather Outlook", ignoreCase = true)) {
                    tropicalOutlook = cleanTropicalOutlookText(description)
                    Log.d(TAG, "Found Atlantic Tropical Weather Outlook")
                    break // Only need this one item
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing RSS XML", e)
        }
        
        Log.d(TAG, "Parsed tropical outlook from RSS feed, ignoring individual storm advisories")
        return Pair(hurricanes, tropicalOutlook)
    }
    
    private fun extractHurricaneName(title: String): String {
        // Extract hurricane name from title like "Hurricane Franklin Advisory Number 15"
        val regex = Regex("(Hurricane|Tropical Storm|Tropical Depression)\\s+([A-Za-z]+)")
        val match = regex.find(title)
        return match?.groupValues?.get(2) ?: "Unknown"
    }
    
    private fun extractHurricaneCategory(title: String): String {
        return when {
            title.contains("Hurricane", ignoreCase = true) -> "Hurricane"
            title.contains("Tropical Storm", ignoreCase = true) -> "Tropical Storm"
            title.contains("Tropical Depression", ignoreCase = true) -> "Tropical Depression"
            else -> "Unknown"
        }
    }
    
    private fun extractHurricaneCategoryInt(title: String): Int {
        // Try to extract category number from title
        val categoryRegex = Regex("Category\\s+(\\d+)")
        val match = categoryRegex.find(title)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }
    
    private fun extractLocation(description: String): String {
        // Simple extraction - look for coordinates or location mentions
        val coordRegex = Regex("(\\d+\\.\\d+[NS]\\s+\\d+\\.\\d+[EW])")
        val match = coordRegex.find(description)
        return match?.value ?: "Location not specified"
    }
    
    private fun extractWindSpeedInt(description: String): Int {
        val windRegex = Regex("(\\d+)\\s*MPH")
        val match = windRegex.find(description)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }
    
    private fun extractPressureInt(description: String): Int {
        val pressureRegex = Regex("(\\d+)\\s*MB")
        val match = pressureRegex.find(description)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }
    
    /**
     * Clean and format the Atlantic Tropical Weather Outlook text
     * Extract only the relevant sections: Active Systems and Eastern Tropical Atlantic
     */
    private fun cleanTropicalOutlookText(rawText: String): String? {
        if (rawText.isBlank()) return null
        
        try {
            // First, properly parse HTML to extract clean text
            val doc = Jsoup.parse(rawText)
            var cleanText = doc.text()
            
            // Remove HTML artifacts
            cleanText = cleanText.replace(Regex("<[^>]+>"), "") // Remove HTML tags
            cleanText = cleanText.replace("&nbsp;", " ") // Replace HTML entities
            cleanText = cleanText.replace("&amp;", "&")
            cleanText = cleanText.replace("&lt;", "<")
            cleanText = cleanText.replace("&gt;", ">")
            
            // Remove meteorological codes and headers
            cleanText = cleanText.replace(Regex("\\d{3}\\s*ABNT\\d+\\s*KNHC\\s*\\d+\\s*TWOAT"), "")
            cleanText = cleanText.replace("NWS National Hurricane Center Miami FL", "")
            cleanText = cleanText.replace(Regex("\\d+\\s*AM\\s*EDT\\s*\\w+\\s*\\w+\\s*\\d+\\s*\\d+"), "")
            cleanText = cleanText.replace(Regex("Tropical Weather Outlook", RegexOption.IGNORE_CASE), "")
            
            // Extract only the relevant sections
            val sections = mutableListOf<String>()
            
            // Extract Active Systems section - fix the regex pattern
            val activeSystemsRegex = Regex("Active Systems:(.*?)(?=Eastern Tropical Atlantic|$)", RegexOption.DOT_MATCHES_ALL)
            val activeMatch = activeSystemsRegex.find(cleanText)
            if (activeMatch != null) {
                val activeText = activeMatch.groupValues[1]
                    .trim()
                if (activeText.isNotBlank()) {
                    sections.add("Active Systems:\n$activeText")
                }
            }
            
            // Extract Eastern Tropical Atlantic section (including formation chances)
            val easternRegex = Regex("Eastern Tropical Atlantic:(.*?)(?=\\$\\$|$)", RegexOption.DOT_MATCHES_ALL)
            val easternMatch = easternRegex.find(cleanText)
            if (easternMatch != null) {
                val easternText = easternMatch.groupValues[1]
                    .trim()
                if (easternText.isNotBlank()) {
                    sections.add("Eastern Tropical Atlantic:\n$easternText")
                }
            }
            
            // If we found specific sections, use them
            if (sections.isNotEmpty()) {
                cleanText = sections.joinToString("\n\n")
            } else {
                // Fallback: extract meaningful content after removing headers
                cleanText = cleanText.replace(Regex("For the North Atlantic.*?Caribbean Sea and the Gulf of America:", RegexOption.IGNORE_CASE), "")
                cleanText = cleanText.replace(Regex("\\$+"), "") // Remove dollar signs
                cleanText = cleanText.replace(Regex("\\*+"), "") // Remove asterisks
                cleanText = cleanText.replace(Regex("\\s+"), " ") // Normalize whitespace
                cleanText = cleanText.trim()
            }
            
            // Remove forecaster names and technical details at the end
            cleanText = cleanText.replace(Regex("\\n\\nForecaster.*$"), "")
            cleanText = cleanText.replace(Regex("Forecaster.*$"), "")
            cleanText = cleanText.replace(Regex("Public Advisories.*?AWIPS.*?\\$\\$", RegexOption.DOT_MATCHES_ALL), "")
            
            // Remove WMO header and AWIPS header technical information
            cleanText = cleanText.replace(Regex("Public Advisories.*?WTNT.*?KNHC.*?AWIPS.*?MIATCPAT2", RegexOption.DOT_MATCHES_ALL), "")
            cleanText = cleanText.replace(Regex("Forecast/Advisories.*?WTNT.*?KNHC.*?AWIPS.*?MIATCMAT2", RegexOption.DOT_MATCHES_ALL), "")
            cleanText = cleanText.replace(Regex("WMO header.*?KNHC", RegexOption.IGNORE_CASE), "")
            cleanText = cleanText.replace(Regex("AWIPS header.*?MIATCPAT2", RegexOption.IGNORE_CASE), "")
            cleanText = cleanText.replace(Regex("AWIPS header.*?MIATCMAT2", RegexOption.IGNORE_CASE), "")
            
            // Remove remaining technical artifacts
            cleanText = cleanText.replace(Regex("\\$\\$"), "")
            cleanText = cleanText.replace(Regex("WTNT\\d+"), "")
            cleanText = cleanText.replace(Regex("KNHC"), "")
            cleanText = cleanText.replace(Regex("MIATCPAT2"), "")
            cleanText = cleanText.replace(Regex("MIATCMAT2"), "")
            
            // Remove specific artifacts like ".&&." and similar patterns
            cleanText = cleanText.replace(Regex("\\.\\s*&&\\s*\\."), "")
            cleanText = cleanText.replace(Regex("&&"), "")
            cleanText = cleanText.replace(Regex("\\.\\s*&&"), "")
            cleanText = cleanText.replace(Regex("&&\\s*\\."), "")
            
            // Final cleanup
            cleanText = cleanText.replace(Regex("\\n\\s*\\n"), "\n\n") // Normalize line breaks
            cleanText = cleanText.trim()
            
            return cleanText.takeIf { it.isNotBlank() && it.length > 20 }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning tropical outlook text", e)
            return null
        }
    }
}
