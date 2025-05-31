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
                    // Filter out synopsis from other cards
                    var otherCards = cards.filter { !it.title.contains("Synopsis", ignoreCase = true) }

                    // Custom logic for Forecast for Today/Tonight
                    val todayIndex = otherCards.indexOfFirst { it.title.contains("Forecast for Today", ignoreCase = true) }
                    val tonightIndex = otherCards.indexOfFirst { it.title.contains("Forecast for Tonight", ignoreCase = true) }
                    if (todayIndex != -1 && tonightIndex != -1) {
                        val now = java.time.LocalTime.now()
                        val onePm = java.time.LocalTime.of(13, 0)
                        otherCards = if (now.isBefore(onePm)) {
                            // Before 1pm: show only 'Forecast for Today'
                            otherCards.filterIndexed { idx, _ -> idx != tonightIndex }
                        } else {
                            // 1pm or after: show only 'Forecast for Tonight'
                            otherCards.filterIndexed { idx, _ -> idx != todayIndex }
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