package com.weatherpossum.app.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import com.weatherpossum.app.R
import androidx.lifecycle.viewModelScope
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.data.model.Result
import com.weatherpossum.app.data.model.WeatherCard
import com.weatherpossum.app.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.util.Log

// Forecast period enum and data class
enum class ForecastPeriod { MORNING, AFTERNOON, NIGHT, FULL_DAY }

data class ParsedForecastCard(val period: ForecastPeriod, val card: WeatherCard)

fun normalizeTitle(title: String): ForecastPeriod? {
    val t = title.trim().lowercase()
    return when {
        t.contains("today and tonight") -> ForecastPeriod.FULL_DAY
        t.contains("today") -> ForecastPeriod.FULL_DAY
        t.contains("this morning") -> ForecastPeriod.MORNING
        t.contains("this afternoon") -> ForecastPeriod.AFTERNOON
        t.contains("tonight") -> ForecastPeriod.NIGHT
        else -> null
    }
}

class ForecastParser(private val cards: List<WeatherCard>) {
    val parsedCards: List<ParsedForecastCard> = cards.mapNotNull { card ->
        normalizeTitle(card.title)?.let { period -> ParsedForecastCard(period, card) }
    }

    fun getForecastForNow(): WeatherCard? {
        val now = java.time.LocalTime.now()
        
        // Determine which time period we're in and look for that specific forecast
        val currentPeriodForecast = when {
            now < java.time.LocalTime.NOON -> {
                // Morning period - only look for morning forecast
                parsedCards.find { it.period == ForecastPeriod.MORNING }
            }
            now < java.time.LocalTime.of(18, 0) -> {
                // Afternoon period - only look for afternoon forecast
                parsedCards.find { it.period == ForecastPeriod.AFTERNOON }
            }
            else -> {
                // Night period - only look for night forecast
                parsedCards.find { it.period == ForecastPeriod.NIGHT }
            }
        }

        // If we found a forecast for the current time period, show only that
        // Otherwise fall back to full day forecast
        return currentPeriodForecast?.card ?: parsedCards.find { it.period == ForecastPeriod.FULL_DAY }?.card
    }
}

class WeatherViewModel(
    private val application: Application
) : ViewModel(), KoinComponent {
    private val repository: WeatherRepository by inject()
    private val userPreferences: UserPreferences by inject()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _synopsis = MutableStateFlow<String?>(null)
    val synopsis: StateFlow<String?> = _synopsis.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Load user name
            userPreferences.userName
                .distinctUntilChanged()
                .collect { name ->
                    _userName.value = name
                }
        }
        loadWeather()
    }

    fun saveUserName(name: String) {
        viewModelScope.launch {
            userPreferences.saveUserName(name)
        }
    }

    fun loadWeather(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = WeatherUiState.Loading
            val result = repository.getWeatherForecast(forceRefresh)
            when (result) {
                is Result.Success -> {
                    val cards = result.data
                    val synopsisTitle = application.getString(R.string.repository_title_synopsis)
                    val synopsisCard = cards.find { it.title.contains(synopsisTitle, ignoreCase = true) }
                    _synopsis.value = synopsisCard?.value

                    // Filter out synopsis, tomorrow forecasts, and blank titles from other cards
                    val otherCards = cards.filter { 
                        !it.title.contains(synopsisTitle, ignoreCase = true) &&
                        !it.title.contains("Tomorrow", ignoreCase = true) &&
                        it.title.isNotBlank()  // Filter out blank titles
                    }

                    // Use ForecastParser to select the correct forecast card for now
                    val parser = ForecastParser(otherCards)
                    val forecastCard = parser.getForecastForNow()

                    // Debug: Log all forecast and extra cards
                    Log.d("WeatherDebug", "Forecast card: ${forecastCard?.title} | ${forecastCard?.value}")
                    otherCards.forEach { Log.d("WeatherDebug", "Other card: ${it.title} | ${it.value}") }

                    // Only include non-forecast cards as extras, and exclude the main forecast card if present
                    val extraCards = otherCards.filter { normalizeTitle(it.title) == null && it != forecastCard }

                    // Log final cards to show
                    (listOfNotNull(forecastCard) + extraCards).forEach {
                        Log.d("WeatherDebug", "Card to show: ${it.title} | ${it.value}")
                    }

                    // Combine forecast card (if present) and extra cards
                    val cardsToShow = buildList {
                        forecastCard?.let { add(it) }
                        addAll(extraCards)
                    }

                    _uiState.value = WeatherUiState.Success(cardsToShow)
                }
                is Result.Error -> {
                    _uiState.value = WeatherUiState.Error(result.exception.message ?: application.getString(R.string.unknown_error))
                }
                is Result.Loading -> {
                    _uiState.value = WeatherUiState.Loading
                }
            }
        }
    }
}

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weatherCards: List<WeatherCard>) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
} 