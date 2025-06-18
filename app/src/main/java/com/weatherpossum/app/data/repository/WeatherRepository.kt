package com.weatherpossum.app.data.repository

import android.content.Context
import com.weatherpossum.app.R
import com.weatherpossum.app.data.api.WeatherForecastApi
import com.weatherpossum.app.data.model.Result
import com.weatherpossum.app.data.model.WeatherCard
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.weatherpossum.app.data.UserPreferences
import java.io.IOException
import java.util.Calendar

private const val TAG = "WeatherRepository"
private const val CACHE_DURATION_MILLIS = 30 * 60 * 1000L // 30 minutes
private const val MAX_RETRIES = 3
private const val INITIAL_RETRY_DELAY = 2000L // 2 seconds

class WeatherRepository(
    private val context: Context,
    private val weatherApi: WeatherForecastApi,
    private val userPreferences: UserPreferences
) {
    private var cachedCards: List<WeatherCard>? = null
    private var lastFetchTime: Long = 0

    private val sectionMarkers = listOf(
        "Synopsis:", "Forecast for Today:", "Forecast for Tonight:",
        "Forecast for this Afternoon:", "Forecast for this Morning:",
        "Forecast for Today and Tonight:", "Forecast for this Afternoon and Tonight:",
        "Sea Conditions:", "Waves:", "Wind:", "Winds:",
        "Sunrise:", "Sunset:", "Weather Outlook:",
        "Weather Outlook for Dominica and the Lesser Antilles:",
        "Warning/Advisory:", "Valid from:",
        "Low Tide:", "High Tide:"
    )

    suspend fun getWeatherForecast(forceRefresh: Boolean = false): Result<List<WeatherCard>> {
        return try {
            if (!forceRefresh && isCacheValid()) {
                return Result.Success(cachedCards!!)
            }

            val html = retryWithTimeout {
                weatherApi.getWeatherForecast()
            }

            val doc = Jsoup.parse(html)
            val cards = parseWeatherCards(doc)

            cachedCards = cards
            lastFetchTime = System.currentTimeMillis()
            userPreferences.saveWeatherCache(serializeCards(cards), lastFetchTime)

            Result.Success(cards)
        } catch (e: Exception) {
            cachedCards?.let {
                return Result.Error(Exception("${e.message}. Showing cached data.", e))
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
            } catch (e: Exception) {
                delay(delayTime)
                delayTime *= 2
            }
        }
        throw IOException("All retries failed")
    }

    private fun parseWeatherCards(doc: Document): List<WeatherCard> {
        val cards = mutableListOf<WeatherCard>()
        val elements = doc.select("div, p, h4, strong, span")
        var currentTitle: String? = null
        val currentContent = StringBuilder()

        val sunTimes = mutableSetOf<String>()
        val seaConditions = mutableSetOf<String>()
        val seenTitles = mutableSetOf<String>()
        val forecastCards = mutableListOf<WeatherCard>()
        var outlookCard: WeatherCard? = null

        fun flush(nextElementText: String? = null) {
            if (!currentTitle.isNullOrBlank() && currentContent.isNotBlank()) {
                val cleaned = cleanText(currentContent.toString(), currentTitle!!)
                if (cleaned.isNotBlank()) {
                    val normalizedTitle = currentTitle!!.trim(':').trim()
                    when {
                        normalizedTitle.lowercase().contains("weather outlook") -> {
                            val outlookContent = if (cleaned.isBlank() || cleaned.equals(normalizedTitle, true)) {
                                nextElementText?.takeIf { it.isNotBlank() } ?: cleaned
                            } else {
                                cleaned
                            }
                            outlookCard = WeatherCard(
                                context.getString(R.string.repository_title_weather_outlook),
                                outlookContent
                            )
                        }
                        normalizedTitle.lowercase().contains("valid from") -> {}
                        normalizedTitle.lowercase().contains("sunrise") -> sunTimes.add("Sunrise: $cleaned")
                        normalizedTitle.lowercase().contains("sunset") -> sunTimes.add("Sunset: $cleaned")
                        normalizedTitle.lowercase().contains("low tide") -> seaConditions.add("Low Tide: $cleaned")
                        normalizedTitle.lowercase().contains("high tide") -> seaConditions.add("High Tide: $cleaned")
                        normalizedTitle.lowercase().contains("waves") -> seaConditions.add("Waves: $cleaned")
                        normalizedTitle.lowercase().contains("sea conditions") -> seaConditions.add("Sea Conditions: $cleaned")
                        normalizedTitle.lowercase().contains("warning/advisory") -> if (seenTitles.add("warning/advisory")) {
                            cards.add(WeatherCard("Warning/Advisory", cleaned))
                        }
                        else -> {
                            if (normalizedTitle.lowercase().contains("forecast")) {
                                // Special handling for "Forecast for Tonight and Tomorrow"
                                if (normalizedTitle.contains("Forecast for Tonight and Tomorrow", ignoreCase = true) ||
                                    cleaned.trimStart().startsWith("Forecast for Tonight and Tomorrow", ignoreCase = true)) {
                                    val finalTitle = "Forecast for Tonight"
                                    
                                    // More aggressive cleaning for the repeated title
                                    var result = cleaned
                                    
                                    // Try multiple regex patterns to catch different variations
                                    val patterns = listOf(
                                        Regex("(?i)Forecast for Tonight and Tomorrow:?\\s*"),
                                        Regex("(?i)Forecast for Tonight and Tomorrow\\s*"),
                                        Regex("(?i)Forecast for Tonight and Tomorrow:?\\s*\\n?"),
                                        Regex("(?i)^\\s*Forecast for Tonight and Tomorrow:?\\s*"),
                                        Regex("(?i)Forecast for Tonight and Tomorrow:?\\s*\\n\\s*")
                                    )
                                    
                                    patterns.forEach { pattern ->
                                        result = result.replace(pattern, "")
                                    }
                                    
                                    // Also try removing any remaining instances
                                    result = result.replace("Forecast for Tonight and Tomorrow:", "")
                                    result = result.replace("Forecast for Tonight and Tomorrow", "")
                                    
                                    // Clean up any double newlines or extra whitespace
                                    result = result.replace(Regex("\\n\\s*\\n"), "\n").trim()
                                    
                                    forecastCards.add(WeatherCard(finalTitle, result))
                                } else {
                                    // Only split if there are truly multiple, different forecast sections
                                    val forecastTitles = sectionMarkers.filter {
                                        it.startsWith("Forecast") && cleaned.contains(it, true)
                                    }.distinct()
                                    if (forecastTitles.size > 1 && forecastTitles.map { it.lowercase() }.toSet().size > 1) {
                                        forecastTitles.forEach { splitTitle ->
                                            val forecastText = cleaned.substringAfter(splitTitle).trim()
                                            forecastCards.add(
                                                WeatherCard(splitTitle.removeSuffix(":"), forecastText)
                                            )
                                        }
                                    } else {
                                        val finalTitle = normalizedTitle
                                        val finalValue = cleaned
                                        forecastCards.add(WeatherCard(finalTitle, finalValue))
                                    }
                                }
                            } else {
                                if (seenTitles.add(normalizedTitle.lowercase())) {
                                    cards.add(WeatherCard(normalizedTitle, cleaned))
                                }
                            }
                        }
                    }
                }
                currentContent.clear()
            }
        }

        for ((i, el) in elements.withIndex()) {
            val text = el.text().trim()
            if (text.isBlank()) continue

            // Detect the outlook header
            if (el.tagName().equals("h4", ignoreCase = true) && text.contains("Weather Outlook for Dominica and the Lesser Antilles", ignoreCase = true)) {
                flush()
                // Get parent div and all <p> children
                val parentDiv = el.parent()
                val paragraphs = parentDiv?.select("p")?.map { it.text().trim() }
                    ?.filter { it.isNotBlank() && !it.contains("valid from", ignoreCase = true) }
                    ?: emptyList()
                if (paragraphs.isNotEmpty()) {
                    outlookCard = WeatherCard(
                        context.getString(R.string.repository_title_weather_outlook),
                        paragraphs.joinToString("\n\n")
                    )
                }
                currentTitle = null
                continue
            }
            val marker = sectionMarkers.firstOrNull { text.startsWith(it.removeSuffix(":"), true) }
            if (marker != null && marker.contains("Weather Outlook for Dominica and the Lesser Antilles")) {
                flush()
                currentTitle = marker.removeSuffix(":").trim()
                // Collect all subsequent siblings' text until the next marker
                val sb = StringBuilder()
                var sibling = el.nextElementSibling()
                while (sibling != null) {
                    val siblingText = sibling.text().trim()
                    if (sectionMarkers.any { siblingText.startsWith(it.removeSuffix(":"), ignoreCase = true) }) break
                    if (siblingText.isNotBlank()) sb.append(siblingText).append("\n\n")
                    sibling = sibling.nextElementSibling()
                }
                if (sb.isNotBlank()) {
                    outlookCard = WeatherCard(
                        context.getString(R.string.repository_title_weather_outlook),
                        sb.toString().trim()
                    )
                }
                currentTitle = null
                continue
            } else if (marker != null) {
                val nextText = elements.getOrNull(i + 1)?.text()?.trim()
                flush(nextText)
                currentTitle = marker.removeSuffix(":").trim()
                val remaining = text.removePrefix(marker).trim()
                if (remaining.isNotBlank()) currentContent.append(remaining).append("\n\n")
            } else if (currentTitle == null && el.tagName().startsWith("h") && text.lowercase().contains("weather outlook")) {
                // fallback for header tag
                val sb = StringBuilder()
                var sibling = el.nextElementSibling()
                while (sibling != null) {
                    val siblingText = sibling.text().trim()
                    if (sectionMarkers.any { siblingText.startsWith(it.removeSuffix(":"), ignoreCase = true) }) break
                    if (siblingText.isNotBlank()) sb.append(siblingText).append("\n\n")
                    sibling = sibling.nextElementSibling()
                }
                if (sb.isNotBlank()) {
                    outlookCard = WeatherCard(
                        context.getString(R.string.repository_title_weather_outlook),
                        sb.toString().trim()
                    )
                }
                currentTitle = null
                continue
            } else {
                currentContent.append(text).append("\n\n")
            }
        }
        flush()

        if (seaConditions.isNotEmpty()) {
            cards.add(
                WeatherCard(
                    context.getString(R.string.repository_title_sea_conditions),
                    seaConditions.joinToString("\n")
                )
            )
        }

        if (sunTimes.isNotEmpty()) {
            cards.add(
                WeatherCard(
                    context.getString(R.string.repository_title_sun_times),
                    sunTimes.joinToString("\n")
                )
            )
        }

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val displayForecast = when (hour) {
            in 12..17 -> listOf("Forecast for this Afternoon", "Forecast for Today")
            in 18..23, in 0..5 -> listOf("Forecast for Tonight")
            else -> listOf("Forecast for Today", "Forecast for this Morning")
        }

        val combinedCard = forecastCards.find {
            it.title.contains("Today and Tonight", true) || it.title.contains("this Afternoon and Tonight", true)
        }

        if (combinedCard != null) {
            val selected = displayForecast.firstOrNull() ?: combinedCard.title
            val selectedContent = combinedCard.value
                .lineSequence()
                .dropWhile { !it.contains(selected, ignoreCase = true) }
                .takeWhile { !it.contains("Forecast for", ignoreCase = true) || it.contains(selected, ignoreCase = true) }
                .joinToString("\n")
            cards.add(WeatherCard(selected, selectedContent))
        } else {
            cards.addAll(forecastCards.filter { card ->
                displayForecast.any { sel -> card.title.contains(sel, true) }
            })
        }

        outlookCard?.let { cards.add(it) }

        return cards
    }

    private fun cleanText(text: String, sectionTitle: String): String {
        var cleaned = text.lines()
            .map { it.trim() }
            .distinct()
            .filterNot {
                it.equals(sectionTitle, true) ||
                it.equals("Valid from:", true) ||
                (sectionTitle.contains("forecast", true) && it.contains("forecast for", true) && !it.contains(sectionTitle, true))
            }
            .joinToString("\n")
            .replace(Regex("[ \t]+"), " ")
            .trim()
        
        // Additional cleaning for forecast titles
        if (sectionTitle.contains("Forecast for Tonight and Tomorrow", ignoreCase = true)) {
            // Remove any remaining instances of the full title
            cleaned = cleaned.replace(Regex("(?i)Forecast for Tonight and Tomorrow:?\\s*"), "")
            cleaned = cleaned.replace("Forecast for Tonight and Tomorrow:", "")
            cleaned = cleaned.replace("Forecast for Tonight and Tomorrow", "")
            
            // Clean up any resulting double newlines
            cleaned = cleaned.replace(Regex("\\n\\s*\\n"), "\n").trim()
        }
        
        return cleaned
    }

    private fun serializeCards(cards: List<WeatherCard>): String {
        val json = org.json.JSONArray()
        for (card in cards) {
            json.put(org.json.JSONObject().apply {
                put("title", card.title)
                put("value", card.value)
            })
        }
        return json.toString()
    }
}
