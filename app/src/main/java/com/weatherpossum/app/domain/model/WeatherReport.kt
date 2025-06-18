package com.weatherpossum.app.domain.model

import com.weatherpossum.app.data.model.WeatherCard
import com.weatherpossum.app.domain.forecast.ForecastPeriod
import java.time.Instant

/**
 * Extension function to check if a WeatherCard's title contains a keyword (case-insensitive)
 */
private fun WeatherCard.hasTitle(keyword: String): Boolean =
    title.contains(keyword, ignoreCase = true)

/**
 * Represents a complete weather report with all available weather information
 */
data class WeatherReport(
    val synopsis: String?,
    val currentForecast: WeatherCard?,
    val extraCards: List<WeatherCard>,
    val lastUpdated: Instant,
    val isFromCache: Boolean = false
) {
    /**
     * Whether the report contains any weather warnings
     */
    val hasWarnings: Boolean
        get() = extraCards.any { it.hasTitle("warning") }

    /**
     * Whether the report contains sea conditions information
     */
    val hasSeaInfo: Boolean
        get() = extraCards.any { it.hasTitle("sea") }

    /**
     * The moon phase card if available
     */
    val moonPhase: WeatherCard?
        get() = extraCards.find { it.hasTitle("moon") }

    /**
     * Whether the report contains wind conditions information
     */
    val hasWindInfo: Boolean
        get() = extraCards.any { it.hasTitle("wind") }

    /**
     * Whether the report contains sun times information
     */
    val hasSunTimes: Boolean
        get() = extraCards.any { it.hasTitle("sun") }

    /**
     * Whether the report contains extended forecast information
     */
    val hasExtendedForecast: Boolean
        get() = extraCards.any { it.hasTitle("forecast") }

    /**
     * The outlook card if available
     */
    val outlookCard: WeatherCard?
        get() = extraCards.find { it.hasTitle("outlook") }

    /**
     * Gets a specific card by title (case-insensitive)
     * @param title The title to search for
     * @return The matching card or null if not found
     */
    fun getCardByTitle(title: String): WeatherCard? =
        extraCards.find { it.hasTitle(title) }

    /**
     * Groups extra cards by type for future widgets/notifications
     */
    val groupedExtras: Map<String, List<WeatherCard>> by lazy {
        extraCards.groupBy {
            when {
                it.hasTitle("warning") -> "warning"
                it.hasTitle("sea") -> "sea"
                it.hasTitle("sun") -> "sun"
                it.hasTitle("wind") -> "wind"
                it.hasTitle("moon") -> "moon"
                it.hasTitle("outlook") -> "outlook"
                it.hasTitle("forecast") -> "forecast"
                else -> "other"
            }
        }
    }

    companion object {
        /**
         * Creates a WeatherReport from a list of weather cards
         * @param cards List of weather cards to parse
         * @param lastUpdated Timestamp of when the data was last updated
         * @param isFromCache Whether this report is from cached data
         * @return A new WeatherReport instance
         */
        fun fromCards(
            cards: List<WeatherCard>,
            lastUpdated: Instant = Instant.now(),
            isFromCache: Boolean = false
        ): WeatherReport {
            // Find synopsis card
            val synopsisCard = cards.find { it.hasTitle("Synopsis") }
            
            // Filter out synopsis and blank titles
            val otherCards = cards.filter { 
                !it.hasTitle("Synopsis") &&
                !it.hasTitle("Tomorrow") &&
                it.title.isNotBlank()
            }
            
            // Use ForecastParser to get current forecast
            val parser = com.weatherpossum.app.domain.forecast.ForecastParser(otherCards)
            val currentForecast = parser.getForecastForNow()
            
            // Get extra cards (non-forecast cards)
            val extraCards = parser.getNonForecastCards()
            
            return WeatherReport(
                synopsis = synopsisCard?.value,
                currentForecast = currentForecast,
                extraCards = extraCards,
                lastUpdated = lastUpdated,
                isFromCache = isFromCache
            )
        }
    }
} 