package com.weatherpossum.app.data.parser

sealed class ForecastSection {
    data object TODAY : ForecastSection()
    data object TONIGHT : ForecastSection()
    data object TODAY_TONIGHT : ForecastSection()
    data object TOMORROW : ForecastSection()
    data object TWENTY_FOUR_HOURS : ForecastSection()
    data class UNKNOWN(val rawTitle: String) : ForecastSection()
}

data class DMOForecastResult(
    val section: ForecastSection,
    val titleRaw: String,
    val body: String,
    val sourceUrl: String = "https://weather.gov.dm/forecast"
)

class ParseException(msg: String) : RuntimeException(msg)
