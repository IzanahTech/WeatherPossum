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
import org.jsoup.nodes.Element
import com.weatherpossum.app.data.UserPreferences
import java.net.SocketTimeoutException
import java.io.IOException
import java.util.regex.Pattern

private const val TAG = "WeatherRepository"
private const val CACHE_DURATION_MILLIS = 30 * 60 * 1000L // 30 minutes
private const val NETWORK_TIMEOUT_MILLIS = 60000L // 60 seconds
private const val MAX_RETRIES = 3
private const val INITIAL_RETRY_DELAY = 2000L // 2 seconds

class WeatherRepository(
    private val weatherApi: WeatherForecastApi,
    private val userPreferences: UserPreferences
) {
    private var cachedCards: List<WeatherCard>? = null
    private var lastFetchTime: Long = 0

    // Common patterns for text cleaning
    private val commonPrefixes = listOf(
        "Synopsis:", "Wind:", "Sea Conditions:", "Waves:",
        "Weather Outlook for Dominica and the Lesser Antilles:",
        "Forecast for Today:", "Forecast for Tonight:",
        "Sunrise:", "Sunset:"
    )

    private fun isCacheValid(): Boolean {
        return cachedCards != null && System.currentTimeMillis() - lastFetchTime < CACHE_DURATION_MILLIS
    }

    private suspend fun <T> retryWithTimeout(
        timeoutMillis: Long = NETWORK_TIMEOUT_MILLIS,
        maxRetries: Int = MAX_RETRIES,
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        var currentDelay = INITIAL_RETRY_DELAY

        for (attempt in 1..maxRetries) {
            try {
                return withContext(Dispatchers.IO) {
                    block()
                }
            } catch (e: Exception) {
                lastException = e
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Socket timeout"
                    is IOException -> "IO error: ${e.message}"
                    else -> e.message ?: "Unknown error"
                }
                
                Log.w(TAG, "Attempt $attempt/$maxRetries failed: $errorMessage")
                
                if (attempt < maxRetries) {
                    val nextDelay = when (e) {
                        is SocketTimeoutException -> currentDelay * 3
                        else -> currentDelay * 2
                    }.toLong().coerceAtMost(30000L)
                    
                    Log.d(TAG, "Retrying in ${nextDelay}ms...")
                    delay(nextDelay)
                    currentDelay = nextDelay
                }
            }
        }
        
        throw lastException ?: IllegalStateException("All retries failed")
    }

    private fun cleanText(text: String): String {
        var cleaned = text.trim()
        // Remove common prefixes
        commonPrefixes.forEach { prefix ->
            if (cleaned.startsWith(prefix, ignoreCase = true)) {
                cleaned = cleaned.substring(prefix.length).trim()
            }
        }
        // Remove extra whitespace
        cleaned = cleaned.replace(Regex("\\s+"), " ")
        // Remove any HTML entities
        cleaned = cleaned.replace("&nbsp;", " ")
        return cleaned
    }

    private fun findElementWithFallbacks(doc: Document, selectors: List<String>, textPattern: String? = null): Element? {
        for (selector in selectors) {
            try {
                val elements = doc.select(selector)
                if (elements.isNotEmpty()) {
                    if (textPattern != null) {
                        // If we're looking for specific text, find the element containing it
                        val pattern = Pattern.compile(textPattern, Pattern.CASE_INSENSITIVE)
                        elements.find { element -> 
                            pattern.matcher(element.text()).find() 
                        }?.let { return it }
                    } else {
                        // Otherwise return the first matching element
                        return elements.first()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Selector '$selector' failed: ${e.message}")
            }
        }
        return null
    }

    private fun parseWeatherCardsFromHtml(doc: Document): List<WeatherCard> {
        val cards = mutableListOf<WeatherCard>()
        
        try {
            // Log the HTML structure for debugging
            Log.d(TAG, "Parsing HTML structure: ${doc.select("body").html().take(500)}...")

            // Parse Synopsis with multiple fallback selectors
            val synopsisSelectors = listOf(
                "p:contains(Synopsis)",
                "div:contains(Synopsis)",
                "h2:contains(Synopsis) + p",
                "h3:contains(Synopsis) + p",
                "div.forecast_synopsis",
                "div.synopsis"
            )
            findElementWithFallbacks(doc, synopsisSelectors, "Synopsis")?.let { synopsis ->
                val text = cleanText(synopsis.text())
                if (text.isNotBlank()) {
                    cards.add(WeatherCard(
                        title = "Synopsis",
                        value = text
                    ))
                    Log.d(TAG, "Successfully parsed Synopsis")
                }
            }

            // Enhanced Forecast parsing with support for different formats
            val forecastSelectors = listOf(
                ".forecast_for_today",
                "div.forecast",
                "div.forecast-content",
                "div.forecast_today",
                "div:contains(Forecast for Today)",
                "div:contains(Forecast for Tonight)",
                "div:contains(Forecast for Today and Tonight)",
                "h2:contains(Forecast) + div",
                "h3:contains(Forecast) + div"
            )

            // Try to find the forecast div
            findElementWithFallbacks(doc, forecastSelectors)?.let { forecastDiv ->
                // Look for strong tags containing forecast titles
                val forecastElements = forecastDiv.select("p > strong")
                val forecasts = mutableMapOf<String, String>()
                var foundTodaySpecific = false
                
                forecastElements.forEach { strong ->
                    val rawTitle = cleanText(strong.text())
                    val lowerTitle = rawTitle.lowercase()
                    // Get the next paragraph after this strong tag
                    val nextParagraph = strong.parent()?.nextElementSibling()
                    if (nextParagraph != null && nextParagraph.tagName() == "p") {
                        val content = cleanText(nextParagraph.text())
                        if (content.isNotBlank()) {
                            // Use the exact title for today/afternoon/morning, and always use 'Forecast for Tonight' for night
                            when {
                                lowerTitle.contains("afternoon") || lowerTitle.contains("morning") || lowerTitle == "forecast for today".lowercase() -> {
                                    forecasts[rawTitle] = content
                                    foundTodaySpecific = true
                                }
                                lowerTitle.contains("tonight") -> {
                                    forecasts["Forecast for Tonight"] = content
                                }
                                else -> {
                                    // fallback: add any other forecast titles as-is
                                    forecasts[rawTitle] = content
                                }
                            }
                        }
                    }
                }

                // If no specific today/afternoon/morning title was found, fallback to 'Forecast for Today'
                if (!foundTodaySpecific) {
                    // Try to find a generic today forecast
                    val todayForecast = forecastElements.find { cleanText(it.text()).lowercase().contains("today") }
                    todayForecast?.let { strong ->
                        val nextParagraph = strong.parent()?.nextElementSibling()
                        if (nextParagraph != null && nextParagraph.tagName() == "p") {
                            val content = cleanText(nextParagraph.text())
                            if (content.isNotBlank()) {
                                forecasts["Forecast for Today"] = content
                            }
                        }
                    }
                }

                // Add each forecast as a separate card
                forecasts.forEach { (title, content) ->
                    cards.add(WeatherCard(
                        title = title.trim(),
                        value = content
                    ))
                    Log.d(TAG, "Successfully parsed $title")
                }

                // If no forecasts were found using strong tags, try the old method
                if (forecasts.isEmpty()) {
                    // Try to find combined forecast
                    val combinedForecast = findElementWithFallbacks(
                        doc,
                        forecastSelectors.filter { it.contains("Today and Tonight") },
                        "Forecast for Today and Tonight"
                    )

                    if (combinedForecast != null) {
                        // Handle combined forecast format
                        val paragraphs = combinedForecast.select("p").map { cleanText(it.text()) }
                            .filter { it.isNotBlank() }
                        if (paragraphs.isNotEmpty()) {
                            cards.add(WeatherCard(
                                title = "Forecast for Today and Tonight",
                                value = paragraphs.joinToString("\n")
                            ))
                            Log.d(TAG, "Successfully parsed combined Forecast for Today and Tonight")
                        }
                    } else {
                        // Try to find separate today and tonight forecasts
                        val todayForecast = findElementWithFallbacks(
                            doc,
                            forecastSelectors.filter { it.contains("Today") && !it.contains("Tonight") },
                            "Forecast for Today"
                        )
                        val tonightForecast = findElementWithFallbacks(
                            doc,
                            forecastSelectors.filter { it.contains("Tonight") && !it.contains("Today") },
                            "Forecast for Tonight"
                        )

                        // Process today's forecast
                        todayForecast?.let { forecast ->
                            val paragraphs = forecast.select("p").map { cleanText(it.text()) }
                                .filter { it.isNotBlank() }
                            if (paragraphs.isNotEmpty()) {
                                cards.add(WeatherCard(
                                    title = "Forecast for Today",
                                    value = paragraphs.joinToString("\n")
                                ))
                                Log.d(TAG, "Successfully parsed Forecast for Today")
                            }
                        }

                        // Process tonight's forecast
                        tonightForecast?.let { forecast ->
                            val paragraphs = forecast.select("p").map { cleanText(it.text()) }
                                .filter { it.isNotBlank() }
                            if (paragraphs.isNotEmpty()) {
                                cards.add(WeatherCard(
                                    title = "Forecast for Tonight",
                                    value = paragraphs.joinToString("\n")
                                ))
                                Log.d(TAG, "Successfully parsed Forecast for Tonight")
                            }
                        }
                    }
                }
            }

            // Parse Wind Conditions with multiple fallback selectors
            val windSelectors = listOf(
                "p:contains(Wind)",
                "div:contains(Wind)",
                "h3:contains(Wind) + p",
                "div.wind-conditions",
                "div.forecast_wind"
            )
            findElementWithFallbacks(doc, windSelectors, "Wind")?.let { wind ->
                val text = cleanText(wind.text())
                if (text.isNotBlank()) {
                    cards.add(WeatherCard(
                        title = "Wind Conditions",
                        value = text
                    ))
                    Log.d(TAG, "Successfully parsed Wind Conditions")
                }
            }

            // Parse Sea Conditions with multiple fallback selectors
            val seaConditions = mutableListOf<String>()
            val seaSelectors = listOf(
                "p:contains(Sea Conditions)",
                "div:contains(Sea Conditions)",
                "h3:contains(Sea Conditions) + p",
                "div.sea-conditions",
                "div.forecast_sea"
            )
            findElementWithFallbacks(doc, seaSelectors, "Sea Conditions")?.let { sea ->
                seaConditions.add(cleanText(sea.text()))
            }

            val waveSelectors = listOf(
                "p:contains(Waves)",
                "div:contains(Waves)",
                "h3:contains(Waves) + p",
                "div.wave-conditions",
                "div.forecast_waves"
            )
            findElementWithFallbacks(doc, waveSelectors, "Waves")?.let { waves ->
                seaConditions.add(cleanText(waves.text()))
            }

            if (seaConditions.isNotEmpty()) {
                cards.add(WeatherCard(
                    title = "Sea Conditions",
                    value = seaConditions.joinToString("\n")
                ))
                Log.d(TAG, "Successfully parsed Sea Conditions")
            }

            // Parse Sun Times with multiple fallback selectors
            val sunTimes = mutableListOf<String>()
            val sunSelectors = listOf(
                "p:contains(Sunrise)",
                "div:contains(Sunrise)",
                "h3:contains(Sunrise) + p",
                "div.sun-times",
                "div.forecast_sun"
            )
            findElementWithFallbacks(doc, sunSelectors, "Sunrise")?.let { sunrise ->
                sunTimes.add(cleanText(sunrise.text()))
            }

            findElementWithFallbacks(doc, sunSelectors.map { it.replace("Sunrise", "Sunset") }, "Sunset")?.let { sunset ->
                sunTimes.add(cleanText(sunset.text()))
            }

            if (sunTimes.isNotEmpty()) {
                cards.add(WeatherCard(
                    title = "Sun Times",
                    value = sunTimes.joinToString(", ")
                ))
                Log.d(TAG, "Successfully parsed Sun Times")
            }

            // Parse Weather Outlook with specific structure handling
            val outlookSelectors = listOf(
                "div.outlook_da_la",
                "div.outlook_da_la.col-sm-6",
                "div:contains(Weather Outlook for Dominica and the Lesser Antilles)",
                "h4.no_padding:contains(Weather Outlook) + p",
                "div.weather-outlook",
                "div.forecast_outlook"
            )

            findElementWithFallbacks(doc, outlookSelectors, "Weather Outlook")?.let { outlook ->
                val outlookText = StringBuilder()
                
                // Get the valid from date if available
                outlook.select("p:contains(Valid from:)").firstOrNull()?.let { validFrom ->
                    outlookText.append(cleanText(validFrom.text())).append("\n\n")
                }
                
                // Get all paragraphs with text-align: justify
                outlook.select("p[style*='text-align: justify']").forEach { paragraph ->
                    outlookText.append(cleanText(paragraph.text())).append("\n\n")
                }
                
                // If no justified paragraphs found, try getting all paragraphs
                if (outlookText.isEmpty()) {
                    outlook.select("p").forEach { paragraph ->
                        // Skip the valid from paragraph as it's already handled
                        if (!paragraph.text().contains("Valid from:")) {
                            outlookText.append(cleanText(paragraph.text())).append("\n\n")
                        }
                    }
                }
                
                // If still empty, use the entire text
                if (outlookText.isEmpty()) {
                    outlookText.append(cleanText(outlook.text()))
                }

                if (outlookText.isNotEmpty()) {
                    cards.add(WeatherCard(
                        title = "Weather Outlook for Dominica and the Lesser Antilles",
                        value = outlookText.toString().trim()
                    ))
                    Log.d(TAG, "Successfully parsed Weather Outlook")
                }
            }

            // Validate that we have at least some weather information
            if (cards.isEmpty()) {
                Log.w(TAG, "No weather cards were parsed from the HTML")
                throw IOException("No weather information found in the response")
            }

            // Log the number of cards parsed
            Log.d(TAG, "Successfully parsed ${cards.size} weather cards")

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing weather cards from HTML", e)
            throw IOException("Error parsing weather data: ${e.message}", e)
        }

        return cards
    }

    suspend fun getWeatherForecast(forceRefresh: Boolean = false): Result<List<WeatherCard>> {
        return try {
            if (!forceRefresh && isCacheValid()) {
                Log.d(TAG, "Using cached weather data")
                return Result.Success(cachedCards!!)
            }

            // Fetch weather data with retry
            val html = retryWithTimeout(timeoutMillis = NETWORK_TIMEOUT_MILLIS) {
                Log.d(TAG, "Fetching weather forecast...")
                weatherApi.getWeatherForecast()
            }

            // Parse HTML directly
            val doc = Jsoup.parse(html)
            val cards = parseWeatherCardsFromHtml(doc)
            
            // Update cache
            cachedCards = cards
            lastFetchTime = System.currentTimeMillis()
            Log.d(TAG, "Successfully updated weather cache")
            
            Result.Success(cards)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather data", e)
            if (cachedCards != null) {
                Log.w(TAG, "Using cached data due to error: ${e.message}")
                Result.Error(Exception("Failed to fetch new data: ${e.message}. Showing cached data.", e))
            } else {
                Result.Error(e)
            }
        }
    }
} 