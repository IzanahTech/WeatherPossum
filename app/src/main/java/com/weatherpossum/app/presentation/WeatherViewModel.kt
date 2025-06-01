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

    val isRefreshing: StateFlow<Boolean> = uiState.map { it is WeatherUiState.Loading }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )

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
        viewModelScope.launch(Dispatchers.IO) {
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
                    // Extract synopsis and other cards
                    val synopsisTitle = application.getString(R.string.repository_title_synopsis)
                    val synopsisCard = cards.find { it.title.contains(synopsisTitle, ignoreCase = true) }
                    _synopsis.value = synopsisCard?.value

                    // Filter out synopsis and tomorrow forecasts from other cards
                    var otherCards = cards.filter { 
                        !it.title.contains(synopsisTitle, ignoreCase = true) &&
                        !it.title.contains("Tomorrow", ignoreCase = true) // "Tomorrow" is a keyword, not a string resource yet
                    }

                    // Define 'now' once for use below
                    val now = java.time.LocalTime.now()

                    // Custom logic for Forecast for Today/Tonight
                    val todayKeyword = application.getString(R.string.repository_title_forecast_today)
                    val tonightKeyword = application.getString(R.string.repository_title_forecast_tonight)
                    val morningKeyword = application.getString(R.string.viewmodel_forecast_morning) // More specific than just "Morning"
                    val afternoonKeyword = application.getString(R.string.viewmodel_forecast_afternoon) // More specific than just "Afternoon"

                    val todayIndex = otherCards.indexOfFirst {
                        it.title.contains(todayKeyword, ignoreCase = true) ||
                        it.title.contains(afternoonKeyword, ignoreCase = true) ||
                        it.title.contains(morningKeyword, ignoreCase = true)
                    }
                    val tonightIndex = otherCards.indexOfFirst { it.title.contains(tonightKeyword, ignoreCase = true) }

                    if (todayIndex != -1 || tonightIndex != -1) {
                        val sixPm = java.time.LocalTime.of(18, 0)
                        otherCards = otherCards.filterIndexed { idx, card ->
                            when {
                                card.title.contains(tonightKeyword, ignoreCase = true) -> now.isAfter(sixPm) || now == sixPm
                                card.title.contains(todayKeyword, ignoreCase = true) ||
                                card.title.contains(afternoonKeyword, ignoreCase = true) ||
                                card.title.contains(morningKeyword, ignoreCase = true) -> now.isBefore(sixPm)
                                else -> true
                            }
                        }
                    }

                    // Assign default title if missing or if title is 'Forecast for Today and Tonight'
                    val defaultTitle = when {
                        now.isBefore(java.time.LocalTime.NOON) -> application.getString(R.string.viewmodel_forecast_morning)
                        now.isBefore(java.time.LocalTime.of(18, 0)) -> application.getString(R.string.viewmodel_forecast_afternoon)
                        else -> application.getString(R.string.viewmodel_forecast_tonight)
                    }
                    otherCards = otherCards.map { card ->
                        val trimmedTitle = card.title.trim()
                        if (trimmedTitle.isEmpty() || trimmedTitle.equals(application.getString(R.string.repository_title_forecast_today_tonight), ignoreCase = true)) {
                            card.copy(title = defaultTitle)
                        } else {
                            card
                        }
                    }

                    _uiState.value = WeatherUiState.Success(otherCards)
                }
                is Result.Error -> {
                    _uiState.value = WeatherUiState.Error(result.exception.message ?: application.getString(R.string.unknown_error))
                }
                // else -> {} // Not needed as sealed class should be exhaustive or have a fallback
            }
        }
    }
}

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weatherCards: List<WeatherCard>) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
} 