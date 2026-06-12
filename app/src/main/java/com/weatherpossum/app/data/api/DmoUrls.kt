package com.weatherpossum.app.data.api

/**
 * Canonical Dominica Meteorological Service forecast page URLs.
 *
 * @see [Daily forecast](https://weather.gov.dm/forecast)
 * @see [Extended forecast](https://weather.gov.dm/forecast/extended-forecast)
 */
object DmoUrls {
    const val BASE = "https://weather.gov.dm/"
    const val DAILY_FORECAST = "https://weather.gov.dm/forecast"

    /** Retrofit path segment for [DAILY_FORECAST]. */
    const val DAILY_FORECAST_PATH = "forecast"

    /** Retrofit path for the extended forecast page. */
    const val EXTENDED_FORECAST_PATH = "forecast/extended-forecast"
}
