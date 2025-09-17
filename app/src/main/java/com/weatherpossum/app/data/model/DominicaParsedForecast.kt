package com.weatherpossum.app.data.model

data class DominicaParsedForecast(
    val validFrom: String?,
    val synopsis: String?,
    val forecastTonight: String?,
    val forecastTonightTitle: String?,
    val forecastTomorrow: String?,
    val forecastTomorrowTitle: String?,
    val wind: String?,
    val seaConditions: String?,
    val waves: String?,
    val advisory: String?,
    val sunrise: String?,
    val sunset: String?,
    val lowTide: String?,
    val highTide: String?,
    val outlookTitle: String?,
    val outlookValidFrom: String?,
    val outlookText: String?
)
