package com.weatherpossum.app.data.parser

import okhttp3.OkHttpClient
import okhttp3.Request

class DMOForecastSource(
    private val client: OkHttpClient
) {
    fun fetchHtml(): String {
        val req = Request.Builder()
            .url("https://weather.gov.dm/forecast")
            .header("User-Agent", "WeatherPossum/1.0 (+Android)")
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("DMO HTTP ${resp.code}")
            return resp.body?.string().orEmpty()
        }
    }
}
