package com.weatherpossum.app.data.repository

import android.content.Context
import android.util.Log
import com.weatherpossum.app.R
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
    private val context: Context,
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
                // Using getString for error messages that might be passed up
                val errorMessage = when (e) {
                    is SocketTimeoutException -> context.getString(R.string.repository_error_socket_timeout)
                    is IOException -> context.getString(R.string.repository_error_io, e.message ?: context.getString(R.string.unknown_error))
                    else -> e.message ?: context.getString(R.string.unknown_error)
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
        
        throw lastException ?: IllegalStateException(context.getString(R.string.repository_error_all_retries_failed))
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
                "p:contains(Synopsis)", // Keyword
                "div:contains(Synopsis)", // Keyword
                "h2:contains(Synopsis) + p",
                "h3:contains(Synopsis) + p",
                "div.forecast_synopsis",
                "div.synopsis"
            )
            findElementWithFallbacks(doc, synopsisSelectors, "Synopsis")?.let { synopsisEl -> // "Synopsis" is a keyword here
                val text = cleanText(synopsisEl.text())
                if (text.isNotBlank()) {
                    cards.add(WeatherCard(
                        title = context.getString(R.string.repository_title_synopsis),
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
                                lowerTitle.contains("afternoon") || lowerTitle.contains("morning") || lowerTitle == context.getString(R.string.repository_title_forecast_today).lowercase() -> {
                                    forecasts[rawTitle] = content // Keep rawTitle as key for now, will map to string resource if it's a direct title
                                    foundTodaySpecific = true
                                }
                                lowerTitle.contains("tonight") -> {
                                    // Use string resource for known titles
                                    forecasts[context.getString(R.string.repository_title_forecast_tonight)] = content
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
                    val todayForecastKeyword = "today" // Keep "today" as a keyword for searching
                    val todayForecast = forecastElements.find { cleanText(it.text()).lowercase().contains(todayForecastKeyword) }
                    todayForecast?.let { strong ->
                        val nextParagraph = strong.parent()?.nextElementSibling()
                        if (nextParagraph != null && nextParagraph.tagName() == "p") {
                            val content = cleanText(nextParagraph.text())
                            if (content.isNotBlank()) {
                                forecasts[context.getString(R.string.repository_title_forecast_today)] = content
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
                                title = context.getString(R.string.repository_title_forecast_today_tonight),
                                value = paragraphs.joinToString("\n")
                            ))
                            Log.d(TAG, "Successfully parsed combined Forecast for Today and Tonight")
                        }
                    } else {
                        // Try to find separate today and tonight forecasts
                        // Keep "Forecast for Today" and "Forecast for Tonight" as keywords for findElementWithFallbacks
                        val todayForecastEl = findElementWithFallbacks(
                            doc,
                            forecastSelectors.filter { it.contains("Today") && !it.contains("Tonight") },
                            "Forecast for Today"
                        )
                        val tonightForecastEl = findElementWithFallbacks(
                            doc,
                            forecastSelectors.filter { it.contains("Tonight") && !it.contains("Today") },
                            "Forecast for Tonight"
                        )

                        // Process today's forecast
                        todayForecastEl?.let { forecast ->
                            val paragraphs = forecast.select("p").map { cleanText(it.text()) }
                                .filter { it.isNotBlank() }
                            if (paragraphs.isNotEmpty()) {
                                cards.add(WeatherCard(
                                    title = context.getString(R.string.repository_title_forecast_today),
                                    value = paragraphs.joinToString("\n")
                                ))
                                Log.d(TAG, "Successfully parsed Forecast for Today")
                            }
                        }

                        // Process tonight's forecast
                        tonightForecastEl?.let { forecast ->
                            val paragraphs = forecast.select("p").map { cleanText(it.text()) }
                                .filter { it.isNotBlank() }
                            if (paragraphs.isNotEmpty()) {
                                cards.add(WeatherCard(
                                    title = context.getString(R.string.repository_title_forecast_tonight),
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
                "p:contains(Wind)", // Keyword
                "div:contains(Wind)", // Keyword
                "h3:contains(Wind) + p",
                "div.wind-conditions",
                "div.forecast_wind"
            )
            findElementWithFallbacks(doc, windSelectors, "Wind")?.let { windEl -> // "Wind" is a keyword
                val text = cleanText(windEl.text())
                if (text.isNotBlank()) {
                    cards.add(WeatherCard(
                        title = context.getString(R.string.repository_title_wind_conditions),
                        value = text
                    ))
                    Log.d(TAG, "Successfully parsed Wind Conditions")
                }
            }

            // Parse Sea Conditions with multiple fallback selectors
            val seaConditionsList = mutableListOf<String>()
            val seaSelectors = listOf(
                "p:contains(Sea Conditions)", // Keyword
                "div:contains(Sea Conditions)", // Keyword
                "h3:contains(Sea Conditions) + p",
                "div.sea-conditions",
                "div.forecast_sea"
            )
            findElementWithFallbacks(doc, seaSelectors, "Sea Conditions")?.let { seaEl -> // "Sea Conditions" is a keyword
                seaConditionsList.add(cleanText(seaEl.text()))
            }

            val waveSelectors = listOf(
                "p:contains(Waves)", // Keyword
                "div:contains(Waves)", // Keyword
                "h3:contains(Waves) + p",
                "div.wave-conditions",
                "div.forecast_waves"
            )
            findElementWithFallbacks(doc, waveSelectors, "Waves")?.let { wavesEl -> // "Waves" is a keyword
                seaConditionsList.add(cleanText(wavesEl.text()))
            }

            if (seaConditionsList.isNotEmpty()) {
                cards.add(WeatherCard(
                    title = context.getString(R.string.repository_title_sea_conditions),
                    value = seaConditionsList.joinToString("\n")
                ))
                Log.d(TAG, "Successfully parsed Sea Conditions")
            }

            // Parse Sun Times with multiple fallback selectors
            val sunTimesList = mutableListOf<String>()
            val sunSelectors = listOf(
                "p:contains(Sunrise)", // Keyword
                "div:contains(Sunrise)", // Keyword
                "h3:contains(Sunrise) + p",
                "div.sun-times",
                "div.forecast_sun"
            )
            findElementWithFallbacks(doc, sunSelectors, "Sunrise")?.let { sunriseEl -> // "Sunrise" is a keyword
                sunTimesList.add(cleanText(sunriseEl.text()))
            }

            // "Sunset" is a keyword
            findElementWithFallbacks(doc, sunSelectors.map { it.replace("Sunrise", "Sunset") }, "Sunset")?.let { sunsetEl ->
                sunTimesList.add(cleanText(sunsetEl.text()))
            }

            if (sunTimesList.isNotEmpty()) {
                cards.add(WeatherCard(
                    title = context.getString(R.string.repository_title_sun_times),
                    value = sunTimesList.joinToString(", ")
                ))
                Log.d(TAG, "Successfully parsed Sun Times")
            }

            // Parse Weather Outlook with specific structure handling
            val outlookSelectors = listOf(
                "div.outlook_da_la",
                "div.outlook_da_la.col-sm-6",
                "div:contains(Weather Outlook for Dominica and the Lesser Antilles)", // Keyword-ish, but long
                "h4.no_padding:contains(Weather Outlook) + p",
                "div.weather-outlook",
                "div.forecast_outlook"
            )
            // "Weather Outlook" is a keyword for findElementWithFallbacks
            findElementWithFallbacks(doc, outlookSelectors, "Weather Outlook")?.let { outlookEl ->
                val outlookText = StringBuilder()
                
                outlookEl.select("p:contains(Valid from:)").firstOrNull()?.let { validFrom ->
                    outlookText.append(cleanText(validFrom.text())).append("\n\n")
                }
                
                outlookEl.select("p[style*='text-align: justify']").forEach { paragraph ->
                    outlookText.append(cleanText(paragraph.text())).append("\n\n")
                }
                
                if (outlookText.isEmpty()) {
                    outlookEl.select("p").forEach { paragraph ->
                        if (!paragraph.text().contains("Valid from:")) {
                            outlookText.append(cleanText(paragraph.text())).append("\n\n")
                        }
                    }
                }
                
                if (outlookText.isEmpty()) {
                    outlookText.append(cleanText(outlookEl.text()))
                }

                if (outlookText.isNotEmpty()) {
                    cards.add(WeatherCard(
                        title = context.getString(R.string.repository_title_weather_outlook),
                        value = outlookText.toString().trim()
                    ))
                    Log.d(TAG, "Successfully parsed Weather Outlook")
                }
            }

            // Validate that we have at least some weather information
            if (cards.isEmpty()) {
                Log.w(TAG, "No weather cards were parsed from the HTML")
                throw IOException(context.getString(R.string.repository_error_no_info_in_response))
            }

            // Log the number of cards parsed
            Log.d(TAG, "Successfully parsed ${cards.size} weather cards")

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing weather cards from HTML", e)
            throw IOException(context.getString(R.string.repository_error_parsing_data, e.message ?: context.getString(R.string.unknown_error)), e)
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
            val errorMsg = e.message ?: context.getString(R.string.unknown_error)
            if (cachedCards != null) {
                Log.w(TAG, "Using cached data due to error: $errorMsg")
                Result.Error(Exception(context.getString(R.string.repository_error_fetch_failed_showing_cache, errorMsg), e))
            } else {
                Result.Error(Exception(errorMsg, e)) // Ensure the exception passed up has a message
            }
        }
    }
} 