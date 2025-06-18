package com.weatherpossum.app.data.repository

object Selectors {
    // Selectors for parsing the synopsis section from the Met Office HTML
    val SYNOPSIS_SELECTORS = listOf(
        "p:contains(Synopsis)", "div:contains(Synopsis)", "h2:contains(Synopsis)", "h3:contains(Synopsis)",
        "div.forecast_synopsis", "div.synopsis", ":has(strong:containsOwn(Synopsis))"
    )

    // Selectors for parsing warning/advisory messages
    val WARNING_SELECTORS = listOf(
        "p:contains(Warning/Advisory)", "div:contains(Warning/Advisory)", "h3:contains(Warning/Advisory)",
        ":has(strong:containsOwn(Warning/Advisory))"
    )

    // Selectors for parsing the main forecast (today, tonight, etc.)
    val FORECAST_CONTAINER_SELECTORS = listOf( // Selectors for the OVERALL forecast area
        ".forecast_for_today", "div.forecast", "div.forecast-content", "div.forecast_today",
        "div:contains(Forecast for Today)", "div:contains(Forecast for Tonight)",
        "h2:contains(Forecast) + div", "h3:contains(Forecast) + div", "article" // Broader fallbacks
    )

    // Selectors for parsing wind conditions
    val WIND_SELECTORS = listOf(
        "p:contains(Wind:)", "div:contains(Wind:)", "h3:contains(Wind:)", "p:contains(Winds:)", // More specific with colon
        "div.wind-conditions", "div.forecast_wind", ":has(strong:containsOwn(Wind))"
    )

    // Selectors for parsing sea conditions
    val SEA_SELECTORS = listOf( // For "Sea Conditions"
        "p:contains(Sea Conditions:)", "div:contains(Sea Conditions:)", "h3:contains(Sea Conditions:)",
        "div.sea-conditions", "div.forecast_sea", ":has(strong:containsOwn(Sea Conditions))"
    )

    // Selectors for parsing wave conditions
    val WAVE_SELECTORS = listOf( // For "Waves"
        "p:contains(Waves:)", "div:contains(Waves:)", "h3:contains(Waves:)",
        "div.wave-conditions", "div.forecast_waves", ":has(strong:containsOwn(Waves))"
    )

    // Selectors for parsing sunrise times
    val SUN_SELECTORS_SUNRISE = listOf(
        "p:contains(Sunrise:)", "div:contains(Sunrise:)", "h3:contains(Sunrise:)",
        "div.sun-times:contains(Sunrise)", "div.forecast_sun:contains(Sunrise)", ":has(strong:containsOwn(Sunrise))"
    )

    // Selectors for parsing sunset times
    val SUN_SELECTORS_SUNSET = listOf(
        "p:contains(Sunset:)", "div:contains(Sunset:)", "h3:contains(Sunset:)",
        "div.sun-times:contains(Sunset)", "div.forecast_sun:contains(Sunset)", ":has(strong:containsOwn(Sunset))"
    )

    // Selectors for parsing the weather outlook section
    val OUTLOOK_SELECTORS = listOf(
        "div.outlook_da_la", "div:contains(Weather Outlook for Dominica and the Lesser Antilles)",
        "h4:contains(Weather Outlook) + p", "div.weather-outlook", "div.forecast_outlook",
        ":has(strong:containsOwn(Weather Outlook))"
    )
} 