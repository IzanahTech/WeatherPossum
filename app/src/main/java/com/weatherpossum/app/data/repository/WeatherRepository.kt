package com.weatherpossum.app.data.repository

import android.util.Log
import com.weatherpossum.app.data.api.Message
import com.weatherpossum.app.data.api.OpenAIApi
import com.weatherpossum.app.data.api.OpenAIRequest
import com.weatherpossum.app.data.api.WeatherForecastApi
import com.weatherpossum.app.data.model.Result
import com.weatherpossum.app.data.model.WeatherCard
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.json.JSONArray
import com.weatherpossum.app.data.UserPreferences
import kotlin.text.buildString
import kotlin.text.Regex
import java.net.SocketTimeoutException
import java.io.IOException

private const val TAG = "WeatherRepository"
private const val CACHE_DURATION_MILLIS = 30 * 60 * 1000L // 30 minutes
private const val NETWORK_TIMEOUT_MILLIS = 60000L // 60 seconds
private const val OPENAI_TIMEOUT_MILLIS = 60000L // 60 seconds for OpenAI calls
private const val MAX_RETRIES = 3
private const val INITIAL_RETRY_DELAY = 2000L // 2 seconds

class WeatherRepository(
    private val weatherApi: WeatherForecastApi,
    private val openAIApi: OpenAIApi,
    private val openAIApiKey: String,
    private val userPreferences: UserPreferences
) {
    private var cachedCards: List<WeatherCard>? = null
    private var lastFetchTime: Long = 0

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
                return withTimeout(timeoutMillis) {
                    withContext(Dispatchers.IO) {
                        block()
                    }
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
                        is SocketTimeoutException -> currentDelay * 3 // More aggressive backoff for timeouts
                        else -> currentDelay * 2 // Standard backoff for other errors
                    }.toLong().coerceAtMost(30000L) // Cap at 30 seconds
                    
                    Log.d(TAG, "Retrying in ${nextDelay}ms...")
                    delay(nextDelay)
                    currentDelay = nextDelay
                } else {
                    Log.e(TAG, "All $maxRetries attempts failed", e)
                    // Try to use cached data if available
                    if (cachedCards != null) {
                        Log.d(TAG, "Using cached data due to network failure")
                        return block() // This will be caught by the outer try-catch and return cached data
                    }
                }
            }
        }
        
        throw lastException ?: IllegalStateException("All retries failed")
    }

    /**
     * Extracts and formats the forecast text from the HTML response.
     */
    private fun extractForecastTextFromHtml(html: String): String {
        return try {
            val doc: Document = Jsoup.parse(html)
            val forecastDiv = doc.select(".forecast_for_today").first()
                ?: throw IOException("Could not find forecast content")
            
            val synopsisText = doc.select("p:contains(Synopsis)").first()?.text() ?: ""
            val windText = doc.select("p:contains(Wind)").first()?.text() ?: ""
            val seaConditions = doc.select("p:contains(Sea Conditions)").first()?.text() ?: ""
            val waves = doc.select("p:contains(Waves)").first()?.text() ?: ""
            
            buildString {
                append(synopsisText)
                append("\n\n")
                forecastDiv.select("p").forEach { paragraph ->
                    append(paragraph.text())
                    append("\n")
                }
                append("\n")
                append(windText)
                append("\n")
                append(seaConditions)
                append("\n")
                append(waves)
            }.trim()
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting forecast text", e)
            throw IOException("Error extracting forecast: ${e.message}", e)
        }
    }

    /**
     * Strips markdown code block (e.g., ```json ... ```) from a string.
     */
    private fun extractJsonFromMarkdown(response: String): String {
        return response
            .replace(Regex("^```json\\s*"), "")
            .replace(Regex("^```\\s*"), "")
            .replace(Regex("\\s*```$"), "")
            .trim()
    }

    suspend fun getWeatherForecast(forceRefresh: Boolean = false): Result<List<WeatherCard>> {
        return try {
            if (!forceRefresh && isCacheValid()) {
                return Result.Success(cachedCards!!)
            }

            coroutineScope {
                // Fetch weather data with retry
                val html = retryWithTimeout(timeoutMillis = NETWORK_TIMEOUT_MILLIS) {
                    Log.d(TAG, "Fetching weather forecast...")
                    weatherApi.getWeatherForecast()
                }

                val forecastText = extractForecastTextFromHtml(html)
                
                // If we got this far, update the cache immediately
                val cards = parseWeatherCards(forecastText)
                cachedCards = cards
                lastFetchTime = System.currentTimeMillis()

                // Only make the OpenAI request if we have valid forecast text
                val prompt = createPrompt(forecastText)
                try {
                    // OpenAI request with longer timeout
                    val response = retryWithTimeout(
                        timeoutMillis = OPENAI_TIMEOUT_MILLIS,
                        maxRetries = MAX_RETRIES
                    ) {
                        Log.d(TAG, "Making OpenAI API request...")
                        openAIApi.parseForecast(
                            apiKey = "Bearer $openAIApiKey",
                            request = OpenAIRequest(
                                model = "gpt-3.5-turbo",
                                messages = listOf(
                                    Message(role = "system", content = "You are a weather data parser. Parse the given text into a structured JSON format."),
                                    Message(role = "user", content = prompt)
                                ),
                                temperature = 0.3
                            )
                        )
                    }

                    val contentString = response.choices.firstOrNull()?.message?.content
                        ?: throw IllegalStateException("Empty response from OpenAI")

                    val cleanedContent = extractJsonFromMarkdown(contentString)
                    Log.d(TAG, "OpenAI cleanedContent: $cleanedContent")

                    val aiCards = parseWeatherCards(cleanedContent)
                    cachedCards = aiCards
                    Result.Success(aiCards)
                } catch (e: Exception) {
                    Log.w(TAG, "OpenAI parsing failed, using basic parsing", e)
                    Result.Success(cards)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather data", e)
            if (cachedCards != null) {
                Result.Error(Exception("Failed to fetch new data: ${e.message}. Showing cached data.", e))
            } else {
                Result.Error(e)
            }
        }
    }

    private fun parseWeatherCards(cleanedContent: String): List<WeatherCard> {
        val cards = mutableListOf<WeatherCard>()
        
        try {
            // Try parsing as JSON array first
            val jsonArray = JSONArray(cleanedContent)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                cards.add(
                    WeatherCard(
                        title = obj.optString("title", ""),
                        value = obj.optString("value", obj.optString("forecast", ""))
                    )
                )
            }
        } catch (e: Exception) {
            try {
                // Fallback to parsing as JSON object
                val jsonObject = org.json.JSONObject(cleanedContent)
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jsonObject.get(key)
                    cards.add(
                        WeatherCard(
                            title = key,
                            value = value.toString()
                        )
                    )
                }
            } catch (ex: Exception) {
                throw IllegalStateException("OpenAI response is not a valid JSON array or object: $cleanedContent", ex)
            }
        }
        
        return cards
    }

    private fun createPrompt(forecastText: String): String = """
        Parse the following weather forecast text into a JSON array of cards. Each card should have a title and value.
        
        Rules for parsing:
        1. Each card should be a separate entry in the JSON array
        2. Use consistent titles for similar information:
           - "Synopsis" for the synopsis section
           - "Weather Outlook for Dominica and the Lesser Antilles" for the outlook section
           - "Forecast for Today" for today's forecast
           - "Forecast for Tonight" for tonight's forecast
           - "Forecast for Today and Tonight" when both are combined
           - "Wind Conditions" for wind information
           - "Sea Conditions" for all sea-related information (combine high tide, low tide, sea state, and waves)
           - "Sun Times" for both sunrise and sunset times
        3. For sea conditions, combine all sea-related information into one card with format:
           "Sea State: [state]\nWaves: [wave conditions]\nHigh Tide: [time]\nLow Tide: [time]"
        4. For sun times, combine sunrise and sunset into a single card with format:
           "Sunrise: [time], Sunset: [time]"
        5. Keep the forecast text as is, only split into appropriate cards
        6. Ensure the JSON is properly formatted with no trailing commas
        
        Example format:
        [
            {
                "title": "Synopsis",
                "value": "[synopsis text]"
            },
            {
                "title": "Forecast for Today",
                "value": "[today's forecast]"
            },
            {
                "title": "Weather Outlook for Dominica and the Lesser Antilles",
                "value": "[outlook text]"
            },
            {
                "title": "Sea Conditions",
                "value": "Sea State: Moderate\nWaves: 2-3 meters\nHigh Tide: 8:30 AM\nLow Tide: 2:45 PM"
            },
            {
                "title": "Sun Times",
                "value": "Sunrise: 6:30 AM, Sunset: 7:45 PM"
            }
        ]
        
        Forecast text to parse:
        $forecastText
    """.trimIndent()
} 