package com.weatherpossum.app.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherpossum.app.R
import com.weatherpossum.app.data.model.ForecastDay
import com.weatherpossum.app.data.model.Result
import com.weatherpossum.app.data.repository.ExtendedForecastRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class ExtendedForecastViewModel(
    private val application: Application,
    private val repository: ExtendedForecastRepository
) : ViewModel() {
    private val _forecast = MutableStateFlow<List<ForecastDay>>(emptyList())
    val forecast: StateFlow<List<ForecastDay>> = _forecast.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isStale = MutableStateFlow(false)
    val isStale: StateFlow<Boolean> = _isStale.asStateFlow()

    fun shouldRefreshForecast(): Boolean = repository.shouldRefreshAtScheduledBoundary()

    fun loadForecast(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            refreshForecast(forceRefresh)
        }
    }

    suspend fun refreshForecast(forceRefresh: Boolean = false) {
        if (_isLoading.value) return

        val showLoading = _forecast.value.isEmpty()
        if (showLoading) {
            _isLoading.value = true
        }
        _error.value = null
        try {
            val result = withContext(Dispatchers.IO) {
                withTimeout(45_000) {
                    repository.getExtendedForecast(forceRefresh)
                }
            }
            when (result) {
                is Result.Success -> {
                    _forecast.value = result.data
                    _isStale.value = result.isStale
                }
                is Result.Error -> {
                    if (_forecast.value.isEmpty()) {
                        _error.value = application.getString(R.string.extended_forecast_error_load)
                    }
                    Log.e(TAG, "Error loading forecast", result.exception)
                }
                is Result.Loading -> Unit
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (_forecast.value.isEmpty()) {
                _error.value = application.getString(R.string.extended_forecast_error_load)
            }
            Log.e(TAG, "Error loading forecast", e)
        } finally {
            _isLoading.value = false
        }
    }

    companion object {
        private const val TAG = "ExtendedForecast"
    }
}
