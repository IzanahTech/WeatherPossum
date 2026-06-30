package com.weatherpossum.app.widget

import com.weatherpossum.app.data.model.WeatherCard

object WidgetMetricsParser {

    fun seaConditionsFromCards(cards: List<WeatherCard>): String? {
        val seaTidesCard = cards.firstOrNull {
            it.title.contains("Sea", ignoreCase = true) && it.title.contains("Tide", ignoreCase = true)
        } ?: return null

        val seaParts = seaTidesCard.value.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                when {
                    line.startsWith("Sea Conditions:", ignoreCase = true) ->
                        line.substringAfter(":").trim()
                    line.startsWith("Waves:", ignoreCase = true) ->
                        line.substringAfter(":").trim()
                    else -> null
                }
            }

        return seaParts.joinToString(" • ").takeIf { it.isNotBlank() }
    }
}
