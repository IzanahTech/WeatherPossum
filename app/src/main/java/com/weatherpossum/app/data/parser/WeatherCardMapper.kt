package com.weatherpossum.app.data.parser

import com.weatherpossum.app.data.model.DominicaParsedForecast
import com.weatherpossum.app.data.model.ForecastSection
import com.weatherpossum.app.data.model.WeatherCard

object WeatherCardMapper {

    fun toWeatherCards(parsed: DominicaParsedForecast): List<WeatherCard> {
        val cards = mutableListOf<WeatherCard>()

        parsed.shortTermForecast?.let { forecast ->
            if (forecast.body.isNotBlank()) {
                cards.add(
                    WeatherCard(
                        title = displayTitleFor(forecast.section, forecast.titleRaw),
                        value = formatWithValidFrom(parsed.validFrom, forecast.body),
                        forecastSection = forecast.section,
                        validFrom = parsed.validFrom
                    )
                )
            }
        }

        parsed.synopsis?.let { cards.add(WeatherCard("Synopsis", it)) }
        parsed.wind?.let { cards.add(WeatherCard("Wind", it)) }

        val seaAndTideInfo = buildList {
            parsed.seaConditions?.let { add("Sea Conditions: $it") }
            parsed.waves?.let { add("Waves: $it") }
            parsed.lowTide?.let { add("Low Tide: $it") }
            parsed.highTide?.let { add("High Tide: $it") }
        }
        if (seaAndTideInfo.isNotEmpty()) {
            cards.add(WeatherCard("Sea & Tides", seaAndTideInfo.joinToString("\n")))
        }

        val sunInfo = buildList {
            parsed.sunrise?.let { add("Sunrise: $it") }
            parsed.sunset?.let { add("Sunset: $it") }
        }
        if (sunInfo.isNotEmpty()) {
            cards.add(WeatherCard("Sun Times", sunInfo.joinToString("\n")))
        }

        parsed.advisory?.let { advisory ->
            cards.add(WeatherCard("Warning/Advisory", advisory))
        }

        parsed.outlookText?.let { outlook ->
            val title = parsed.outlookTitle ?: "Weather Outlook"
            val value = formatWithValidFrom(parsed.outlookValidFrom, outlook)
            cards.add(WeatherCard(title, value, validFrom = parsed.outlookValidFrom))
        }

        return cards
    }

    private fun formatWithValidFrom(validFrom: String?, body: String): String {
        if (validFrom.isNullOrBlank()) return body
        return "Valid from: $validFrom\n\n$body"
    }

    private fun displayTitleFor(section: ForecastSection, titleRaw: String): String = when (section) {
        ForecastSection.TODAY_TONIGHT -> {
            if (titleRaw.contains("afternoon", ignoreCase = true)) {
                "Forecast for This Afternoon & Tonight"
            } else {
                "Forecast for Today & Tonight"
            }
        }
        ForecastSection.TODAY -> "Forecast for Today"
        ForecastSection.TONIGHT -> "Forecast for Tonight"
        ForecastSection.TOMORROW -> "Forecast for Tomorrow"
        ForecastSection.TWENTY_FOUR_HOURS -> "Forecast for the Next 24 Hours"
        is ForecastSection.UNKNOWN -> titleRaw.ifBlank { "Forecast" }
    }
}
