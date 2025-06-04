package com.weatherpossum.app.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

// Data model
data class ForecastDay(
    val date: String,
    val maxTemp: String,
    val minTemp: String,
    val weather: String,
    val wind: String,
    val seas: String,
    val waves: String
)

class ExtendedForecastViewModel : ViewModel() {
    private val _forecast = MutableStateFlow<List<ForecastDay>>(emptyList())
    val forecast: StateFlow<List<ForecastDay>> = _forecast.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadForecast() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val html = fetchHtml()
                Log.d("ExtendedForecast", "Raw HTML: ${html.take(1000)}")
                _forecast.value = parseExtendedForecast(html)
            } catch (e: Exception) {
                _error.value = "Failed to load forecast"
                Log.e("ExtendedForecast", "Error loading forecast", e)
            }
            _isLoading.value = false
        }
    }

    private suspend fun fetchHtml(): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://weather.gov.dm/forecast/extended-forecast")
            .build()
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                response.body?.string() ?: throw IOException("Empty response")
            }
        }
    }

    private fun parseExtendedForecast(html: String): List<ForecastDay> {
        val doc: Document = Jsoup.parse(html)
        val days = mutableListOf<ForecastDay>()
        val extForecastDiv = doc.selectFirst("div#ext_forecast") ?: return emptyList()
        val dayDivs = extForecastDiv.select("div.third")

        for (div in dayDivs) {
            val h3 = div.selectFirst("h3")
            val date = h3?.ownText()?.trim() + ", " + h3?.selectFirst("span strong")?.text().orEmpty()

            val entry = div.selectFirst("div.entry")
            val maxTemp = entry?.select("p:contains(Max Temp.)")?.text()?.substringAfter(":")?.trim() ?: ""
            val minTemp = entry?.select("p:contains(Min Temp.)")?.text()?.substringAfter(":")?.trim() ?: ""
            val weather = entry?.select("p:contains(Weather)")?.text()?.substringAfter(":")?.trim() ?: ""
            val wind = entry?.select("p:contains(Wind)")?.text()?.substringAfter(":")?.trim() ?: ""
            val seas = entry?.select("p:contains(Seas)")?.text()?.substringAfter(":")?.trim() ?: ""
            val waves = entry?.select("p:contains(Waves)")?.text()?.substringAfter(":")?.trim() ?: ""

            days.add(
                ForecastDay(
                    date = date,
                    maxTemp = maxTemp,
                    minTemp = minTemp,
                    weather = weather,
                    wind = wind,
                    seas = seas,
                    waves = waves
                )
            )
        }
        return days
    }
} 