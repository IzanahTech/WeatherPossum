package com.weatherpossum.app.data.repository

import android.util.Log
import com.weatherpossum.app.data.api.WeatherForecastApi
import com.weatherpossum.app.data.model.Result
import com.weatherpossum.app.data.model.WeatherCard
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.weatherpossum.app.data.parser.DominicaWeatherParser
import com.weatherpossum.app.data.parser.DMOForecastParser
import com.weatherpossum.app.data.parser.ForecastSection

private const val TAG = "WeatherRepository"
private const val CACHE_DURATION_MILLIS = 30 * 60 * 1000L // 30 minutes
private const val MAX_RETRIES = 3
private const val INITIAL_RETRY_DELAY = 1000L

class WeatherRepository(
    private val weatherApi: WeatherForecastApi,
    private val userPreferences: com.weatherpossum.app.data.UserPreferences
) {
    private var cachedCards: List<WeatherCard>? = null
    private var lastFetchTime: Long = 0

    suspend fun getWeatherForecast(forceRefresh: Boolean = false): Result<List<WeatherCard>> {
        return try {
            if (!forceRefresh && isCacheValid()) {
                return Result.Success(cachedCards!!)
            }

            val html = retryWithTimeout {
                weatherApi.getWeatherForecast()
            }

            if (html.isBlank()) {
                Log.w(TAG, "Received empty HTML response")
                cachedCards?.let {
                    return Result.Error(Exception("Empty response. Showing cached data."))
                }
                return Result.Error(Exception("Empty response and no cached data available"))
            }

            val doc = Jsoup.parse(html)
            val cards = parseWeatherCardsWithSpecializedParser(doc)

            if (cards.isEmpty()) {
                Log.w(TAG, "No cards parsed from HTML")
                cachedCards?.let {
                    return Result.Error(Exception("Failed to parse data. Showing cached data."))
                }
                return Result.Error(Exception("Failed to parse weather data"))
            }

            // Cache the results
            cachedCards = cards
            lastFetchTime = System.currentTimeMillis()
            
            try {
            userPreferences.saveWeatherCache(serializeCards(cards), lastFetchTime)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save weather cache", e)
            }

            Result.Success(cards)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather forecast", e)
            cachedCards?.let {
                return Result.Error(Exception("Network error. Showing cached data."))
            }
            Result.Error(e)
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
            } catch (_: Exception) {
                delay(delayTime)
                delayTime *= 2
            }
        }
        throw java.io.IOException("All retries failed")
    }

    private fun parseWeatherCardsWithSpecializedParser(doc: Document): List<WeatherCard> {
        val cards = mutableListOf<WeatherCard>()
        
        try {
            // New robust parser call
            val dmo = DMOForecastParser.parse(doc.html())

            // Decide display title based on section
            val forecastTitle = when (dmo.section) {
                is ForecastSection.TODAY_TONIGHT -> "Forecast for Today & Tonight"
                is ForecastSection.TODAY -> "Forecast for Today"
                is ForecastSection.TONIGHT -> "Forecast for Tonight"
                is ForecastSection.TOMORROW -> "Forecast for Tomorrow"
                is ForecastSection.TWENTY_FOUR_HOURS -> "Forecast for the Next 24 Hours"
                is ForecastSection.UNKNOWN -> dmo.titleRaw.ifBlank { "Forecast" }
            }

            // Add the forecast card with the new parser
            cards.add(WeatherCard(forecastTitle, dmo.body))
            
            // Use the specialized Dominica weather parser for other data
            val parsedData = DominicaWeatherParser.parseDominicaArticleBody(doc.html())
            
            parsedData.synopsis?.let { 
                cards.add(WeatherCard("Synopsis", it))
            }
            
            parsedData.wind?.let { 
                cards.add(WeatherCard("Wind", it))
            }
            
            // Combine sea conditions, waves, and tides into a single card
            val seaAndTideInfo = mutableListOf<String>()
            parsedData.seaConditions?.let { seaAndTideInfo.add("Sea Conditions: $it") }
            parsedData.waves?.let { seaAndTideInfo.add("Waves: $it") }
            parsedData.lowTide?.let { seaAndTideInfo.add("Low Tide: $it") }
            parsedData.highTide?.let { seaAndTideInfo.add("High Tide: $it") }
            if (seaAndTideInfo.isNotEmpty()) {
                cards.add(WeatherCard("Sea & Tides", seaAndTideInfo.joinToString("\n")))
            }
            
            // Combine sunrise and sunset
            val sunInfo = mutableListOf<String>()
            parsedData.sunrise?.let { sunInfo.add("Sunrise: $it") }
            parsedData.sunset?.let { sunInfo.add("Sunset: $it") }
            if (sunInfo.isNotEmpty()) {
                cards.add(WeatherCard("Sun Times", sunInfo.joinToString("\n")))
            }
            
            parsedData.advisory?.let { advisory ->
                // Skip if this is actually weather outlook content (not a real advisory)
                val isWeatherOutlook = advisory.contains("tropical wave", ignoreCase = true) ||
                                      advisory.contains("unstable conditions", ignoreCase = true) ||
                                      advisory.contains("seas are also expected", ignoreCase = true)
                
                if (!isWeatherOutlook && 
                    !advisory.lowercase().contains("none at this time") &&
                    !advisory.lowercase().contains("none") &&
                    advisory.isNotBlank()) {
                    cards.add(WeatherCard("Warning/Advisory", advisory))
                }
            }
            
            // Weather outlook
            val outlookContent = parsedData.outlookText ?: run {
                // If outlookText is empty but advisory contains weather outlook content, use that
                parsedData.advisory?.let { advisory ->
                    if (advisory.contains("tropical wave", ignoreCase = true) ||
                        advisory.contains("unstable conditions", ignoreCase = true) ||
                        advisory.contains("seas are also expected", ignoreCase = true)) {
                        advisory
                    } else null
                }
            }
            
            outlookContent?.let { 
                val title = parsedData.outlookTitle ?: "Weather Outlook"
                cards.add(WeatherCard(title, it))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing weather data with specialized parser", e)
            // Fallback to empty list - could implement fallback parsing here if needed
        }

        return cards
    }

    private fun serializeCards(cards: List<WeatherCard>): String {
        return cards.joinToString("|||") { "${it.title}:::${it.value}" }
    }
}
