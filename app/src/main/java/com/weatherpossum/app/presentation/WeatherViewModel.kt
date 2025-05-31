package com.weatherpossum.app.presentation

import androidx.lifecycle.ViewModel
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

class WeatherViewModel : ViewModel(), KoinComponent {
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
                    val synopsisCard = cards.find { it.title.contains("Synopsis", ignoreCase = true) }
                    _synopsis.value = synopsisCard?.value
                    // Filter out synopsis and tomorrow forecasts from other cards
                    var otherCards = cards.filter { 
                        !it.title.contains("Synopsis", ignoreCase = true) &&
                        !it.title.contains("Tomorrow", ignoreCase = true)
                    }

                    // Define 'now' once for use below
                    val now = java.time.LocalTime.now()

                    // Custom logic for Forecast for Today/Tonight
                    val todayIndex = otherCards.indexOfFirst { it.title.contains("Forecast for Today", ignoreCase = true) || it.title.contains("Afternoon", ignoreCase = true) || it.title.contains("Morning", ignoreCase = true) }
                    val tonightIndex = otherCards.indexOfFirst { it.title.contains("Forecast for Tonight", ignoreCase = true) }
                    if (todayIndex != -1 || tonightIndex != -1) {
                        val sixPm = java.time.LocalTime.of(18, 0)
                        otherCards = otherCards.filterIndexed { idx, card ->
                            when {
                                card.title.contains("Forecast for Tonight", ignoreCase = true) -> now.isAfter(sixPm) || now == sixPm
                                card.title.contains("Forecast for Today", ignoreCase = true) || card.title.contains("Afternoon", ignoreCase = true) || card.title.contains("Morning", ignoreCase = true) -> now.isBefore(sixPm)
                                else -> true
                            }
                        }
                    }

                    // Assign default title if missing or if title is 'Forecast for Today and Tonight'
                    val defaultTitle = when {
                        now.isBefore(java.time.LocalTime.NOON) -> "Forecast for this Morning"
                        now.isBefore(java.time.LocalTime.of(18, 0)) -> "Forecast for this Afternoon"
                        else -> "Forecast for Tonight"
                    }
                    otherCards = otherCards.map { card ->
                        val trimmedTitle = card.title.trim()
                        if (trimmedTitle.isEmpty() || trimmedTitle.equals("Forecast for Today and Tonight", ignoreCase = true)) {
                            card.copy(title = defaultTitle)
                        } else {
                            card
                        }
                    }

                    _uiState.value = WeatherUiState.Success(otherCards)
                }
                is Result.Error -> {
                    _uiState.value = WeatherUiState.Error(result.exception.message ?: "Unknown error")
                }
                else -> {}
            }
        }
    }
}

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weatherCards: List<WeatherCard>) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
} 