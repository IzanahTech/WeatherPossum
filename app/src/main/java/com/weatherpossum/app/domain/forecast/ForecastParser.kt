package com.weatherpossum.app.domain.forecast

import com.weatherpossum.app.data.model.WeatherCard
import java.time.LocalTime

/**
 * Represents different periods of a forecast day
 */
enum class ForecastPeriod {
    MORNING,
    AFTERNOON,
    NIGHT,
    FULL_DAY
}

/**
 * Data class representing a parsed forecast card with its associated time period
 */
data class ParsedForecastCard(
    val period: ForecastPeriod,
    val card: WeatherCard
)

/**
 * Utility function to normalize forecast titles into standardized periods
 * @param title The raw forecast title to normalize
 * @return The corresponding ForecastPeriod, or null if the title doesn't match any known pattern
 */
fun normalizeTitle(title: String): ForecastPeriod? {
    val t = title.trim().lowercase()
    return when {
        t.contains("today and tonight") -> ForecastPeriod.FULL_DAY
        t.contains("today") -> ForecastPeriod.FULL_DAY
        t.contains("this morning") -> ForecastPeriod.MORNING
        t.contains("this afternoon") -> ForecastPeriod.AFTERNOON
        t.contains("tonight") -> ForecastPeriod.NIGHT
        else -> null
    }
}

/**
 * Parser for weather forecast cards that handles time-based forecast selection
 * and period normalization
 */
class ForecastParser(private val cards: List<WeatherCard>) {
    /**
     * List of parsed forecast cards with their associated time periods
     */
    val parsedCards: List<ParsedForecastCard> = cards.mapNotNull { card ->
        normalizeTitle(card.title)?.let { period -> ParsedForecastCard(period, card) }
    }

    /**
     * Gets the most appropriate forecast card for the current time of day
     * @return The most relevant WeatherCard for the current time period, or null if no suitable card is found
     */
    fun getForecastForNow(): WeatherCard? {
        val now = LocalTime.now()
        
        // Determine which time period we're in and look for that specific forecast
        val currentPeriodForecast = when {
            now < LocalTime.NOON -> {
                // Morning period - only look for morning forecast
                parsedCards.find { it.period == ForecastPeriod.MORNING }
            }
            now < LocalTime.of(18, 0) -> {
                // Afternoon period - only look for afternoon forecast
                parsedCards.find { it.period == ForecastPeriod.AFTERNOON }
            }
            else -> {
                // Night period - only look for night forecast
                parsedCards.find { it.period == ForecastPeriod.NIGHT }
            }
        }

        // If we found a forecast for the current time period, show only that
        // Otherwise fall back to full day forecast
        return currentPeriodForecast?.card ?: parsedCards.find { it.period == ForecastPeriod.FULL_DAY }?.card
    }

    /**
     * Gets all non-forecast cards (cards that don't match any forecast period)
     * @return List of WeatherCards that are not forecast cards
     */
    fun getNonForecastCards(): List<WeatherCard> {
        return cards.filter { normalizeTitle(it.title) == null }
    }
} 