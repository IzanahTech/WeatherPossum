package com.weatherpossum.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherpossum.app.data.repository.MoonRepository
import com.weatherpossum.app.data.MoonData
import com.weatherpossum.app.data.UserPreferences
import com.weatherpossum.app.util.FetchSchedule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

private const val TAG = "MoonViewModel"

sealed class MoonUiState {
    data object Loading : MoonUiState()
    data class Success(val moonData: MoonData) : MoonUiState()
    data class Error(val message: String) : MoonUiState()
}

class MoonViewModel(
    private val repository: MoonRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow<MoonUiState>(MoonUiState.Loading)
    val uiState: StateFlow<MoonUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "MoonViewModel init: starting coroutine to fetch moon data")
        viewModelScope.launch {
            // Combine the last fetch time with the moon data
            combine(
                userPreferences.lastMoonFetchTime,
                userPreferences.lastRateLimitErrorTime,
                repository.moonData
            ) { lastFetchTime: Long?, lastRateLimitErrorTime: Long?, moonData: MoonData? ->
                Log.d(TAG, "combine: lastFetchTime=$lastFetchTime, lastRateLimitErrorTime=$lastRateLimitErrorTime, moonData=$moonData")
                
                // Check if we should skip due to recent rate limit error
                if (FetchSchedule.shouldSkipDueToRateLimit(lastRateLimitErrorTime)) {
                    Log.d(TAG, "combine: Skipping fetch due to recent rate limit error")
                    if (moonData != null) {
                        MoonUiState.Success(moonData)
                    } else {
                        MoonUiState.Error("API rate limit exceeded. Please try again later.")
                    }
                } else if (moonData == null || FetchSchedule.shouldFetchMoonData(lastFetchTime)) {
                    Log.d(TAG, "combine: moonData is null or shouldFetchMoonData is TRUE, calling refreshMoonData()")
                    try {
                        val result = repository.refreshMoonData()
                        result.fold(
                            onSuccess = { newMoonData ->
                                userPreferences.updateLastMoonFetchTime(System.currentTimeMillis())
                                MoonUiState.Success(newMoonData)
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "combine: Error fetching moon data", exception)
                                // If we have cached data, use it even if it's old
                                if (moonData != null) {
                                    Log.d(TAG, "combine: Using cached moon data due to fetch failure")
                                    MoonUiState.Success(moonData)
                                } else {
                                    MoonUiState.Error(exception.message ?: "Failed to fetch moon data")
                                }
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "combine: Error fetching moon data", e)
                        // If we have cached data, use it even if it's old
                        if (moonData != null) {
                            Log.d(TAG, "combine: Using cached moon data due to exception")
                            MoonUiState.Success(moonData)
                        } else {
                            MoonUiState.Error(e.message ?: "Failed to fetch moon data")
                        }
                    }
                } else {
                    Log.d(TAG, "combine: using cached moonData")
                    MoonUiState.Success(moonData)
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
} 