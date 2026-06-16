package com.weatherpossum.app.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import com.weatherpossum.app.R
import androidx.lifecycle.viewModelScope
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.data.model.ForecastSection
import com.weatherpossum.app.data.model.Result
import com.weatherpossum.app.data.model.WeatherCard
import com.weatherpossum.app.data.repository.WeatherRepository
import com.weatherpossum.app.domain.forecast.ForecastParser
import com.weatherpossum.app.domain.forecast.normalizeTitle
import com.weatherpossum.app.widget.WeatherWidgetUpdateManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import android.util.Log

class WeatherViewModel(
    private val application: Application,
    private val repository: WeatherRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    companion object {
        private const val TAG = "WeatherViewModel"
        private const val LOAD_TIMEOUT_MS = 60_000L
    }

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _synopsis = MutableStateFlow<String?>(null)
    val synopsis: StateFlow<String?> = _synopsis.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.userName
                .distinctUntilChanged()
                .collect { name ->
                    _userName.value = name
                }
        }
        viewModelScope.launch {
            hydrateFromCacheIfAvailable()
            refreshWeather(forceRefresh = false)
        }
    }

    private suspend fun hydrateFromCacheIfAvailable() {
        val cached = withContext(Dispatchers.IO) {
            repository.readCachedForecast()
        } ?: return
        if (_uiState.value is WeatherUiState.Loading) {
            applySuccess(
                cards = cached.first,
                synopsisTitle = application.getString(R.string.repository_title_synopsis),
                isStale = cached.second
            )
        }
    }

    fun saveUserName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferences.saveUserName(name)
            WeatherWidgetUpdateManager.updateAllWidgets(application)
        }
    }

    fun loadWeather(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            refreshWeather(forceRefresh)
        }
    }

    suspend fun refreshWeather(forceRefresh: Boolean = false) {
        val showingContent = _uiState.value is WeatherUiState.Success
        if (!showingContent) {
            val cached = withContext(Dispatchers.IO) { repository.readCachedForecast() }
            if (cached != null) {
                applySuccess(
                    cards = cached.first,
                    synopsisTitle = application.getString(R.string.repository_title_synopsis),
                    isStale = cached.second
                )
            } else {
                _uiState.value = WeatherUiState.Loading
            }
        }

        try {
            val result = withContext(Dispatchers.IO) {
                withTimeout(LOAD_TIMEOUT_MS) {
                    repository.getWeatherForecast(forceRefresh)
                }
            }
            val synopsisTitle = application.getString(R.string.repository_title_synopsis)
            when (result) {
                is Result.Success -> applySuccess(
                    cards = result.data,
                    synopsisTitle = synopsisTitle,
                    isStale = result.isStale
                )
                is Result.Error -> {
                    _uiState.value = WeatherUiState.Error(
                        result.exception.message ?: application.getString(R.string.unknown_error)
                    )
                }
                is Result.Loading -> {
                    _uiState.value = WeatherUiState.Loading
                }
            }
        } catch (_: TimeoutCancellationException) {
            _uiState.value = WeatherUiState.Error(
                application.getString(R.string.repository_error_socket_timeout)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error loading weather", e)
            _uiState.value = WeatherUiState.Error(
                e.message ?: application.getString(R.string.unknown_error)
            )
        }
    }

    private fun applySuccess(
        cards: List<WeatherCard>,
        synopsisTitle: String,
        isStale: Boolean = false
    ) {
        val synopsisCard = cards.find { it.title.contains(synopsisTitle, ignoreCase = true) }
        _synopsis.value = synopsisCard?.value

        val otherCards = cards.filter {
            !it.title.contains(synopsisTitle, ignoreCase = true) &&
                it.forecastSection != ForecastSection.TOMORROW &&
                !it.title.contains("Tomorrow", ignoreCase = true) &&
                it.title.isNotBlank()
        }

        val parser = ForecastParser(otherCards)
        val forecastCard = parser.getForecastForNow()

        val extraCards = otherCards.filter { card ->
            !card.isForecastCard() && normalizeTitle(card.title) == null && card != forecastCard
        }

        val cardsToShow = buildList {
            forecastCard?.let { add(it) }
            addAll(extraCards)
        }

        if (cardsToShow.isEmpty()) {
            _uiState.value = WeatherUiState.Error(
                application.getString(R.string.viewmodel_error_no_weather_data)
            )
        } else {
            _uiState.value = WeatherUiState.Success(cardsToShow, isStale = isStale)
        }

        viewModelScope.launch(Dispatchers.IO) {
            WeatherWidgetUpdateManager.updateAllWidgets(application)
        }
    }
}

sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data class Success(
        val weatherCards: List<WeatherCard>,
        val isStale: Boolean = false
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}
