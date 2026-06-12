package com.weatherpossum.app.domain.forecast

import com.weatherpossum.app.data.model.ForecastSection
import com.weatherpossum.app.data.model.WeatherCard
import java.time.LocalTime

enum class ForecastPeriod { MORNING, AFTERNOON, NIGHT, FULL_DAY }

data class ParsedForecastCard(val period: ForecastPeriod, val card: WeatherCard)

fun normalizeTitle(title: String): ForecastPeriod? {
    val t = title.trim().lowercase()
    return when {
        t.contains("and tonight") || t.contains("& tonight") -> ForecastPeriod.FULL_DAY
        t.contains("today and tonight") -> ForecastPeriod.FULL_DAY
        t.contains("next 24 hours") -> ForecastPeriod.FULL_DAY
        t.contains("this morning") -> ForecastPeriod.MORNING
        t.contains("this afternoon") && !t.contains("tonight") -> ForecastPeriod.AFTERNOON
        t.contains("today") -> ForecastPeriod.FULL_DAY
        t.contains("tonight") -> ForecastPeriod.NIGHT
        else -> null
    }
}

fun ForecastSection.toDisplayPeriod(title: String): ForecastPeriod = when (this) {
    ForecastSection.TODAY -> ForecastPeriod.MORNING
    ForecastSection.TONIGHT -> ForecastPeriod.NIGHT
    ForecastSection.TODAY_TONIGHT -> {
        if (title.contains("afternoon", ignoreCase = true)) ForecastPeriod.FULL_DAY
        else ForecastPeriod.FULL_DAY
    }
    ForecastSection.TOMORROW -> ForecastPeriod.FULL_DAY
    ForecastSection.TWENTY_FOUR_HOURS -> ForecastPeriod.FULL_DAY
    is ForecastSection.UNKNOWN -> ForecastPeriod.FULL_DAY
}

fun ForecastSection.matchesNow(title: String, now: LocalTime): Boolean = when (this) {
    ForecastSection.TODAY -> now < LocalTime.NOON
    ForecastSection.TONIGHT -> now >= LocalTime.of(18, 0)
    ForecastSection.TODAY_TONIGHT -> {
        if (title.contains("afternoon", ignoreCase = true)) {
            now >= LocalTime.NOON
        } else {
            true
        }
    }
    ForecastSection.TOMORROW -> false
    ForecastSection.TWENTY_FOUR_HOURS -> true
    is ForecastSection.UNKNOWN -> true
}

class ForecastParser(private val cards: List<WeatherCard>) {
    val parsedCards: List<ParsedForecastCard> = cards.mapNotNull { card ->
        val period = card.forecastSection?.toDisplayPeriod(card.title)
            ?: normalizeTitle(card.title)
        period?.let { ParsedForecastCard(it, card) }
    }

    fun getForecastForNow(): WeatherCard? {
        val now = LocalTime.now()
        val forecastCards = cards.filter { it.isForecastCard() || normalizeTitle(it.title) != null }

        forecastCards.firstOrNull { card ->
            card.forecastSection?.matchesNow(card.title, now) == true
        }?.let { return it }

        val currentPeriodForecast = when {
            now < LocalTime.NOON -> parsedCards.find { it.period == ForecastPeriod.MORNING }
            now < LocalTime.of(18, 0) -> parsedCards.find { it.period == ForecastPeriod.AFTERNOON }
            else -> parsedCards.find { it.period == ForecastPeriod.NIGHT }
        }

        return currentPeriodForecast?.card
            ?: parsedCards.find { it.period == ForecastPeriod.FULL_DAY }?.card
            ?: forecastCards.firstOrNull()
    }
}
