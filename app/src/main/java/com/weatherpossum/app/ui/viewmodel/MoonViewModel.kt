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
                repository.moonData
            ) { lastFetchTime: Long?, moonData: MoonData? ->
                Log.d(TAG, "combine: lastFetchTime=$lastFetchTime, moonData=$moonData")
                if (moonData == null || FetchSchedule.shouldFetchMoonData(lastFetchTime)) {
                    Log.d(TAG, "combine: moonData is null or shouldFetchMoonData is TRUE, calling refreshMoonData()")
                    try {
                        repository.refreshMoonData()
                        userPreferences.updateLastMoonFetchTime(System.currentTimeMillis())
                        MoonUiState.Success(moonData ?: throw IllegalStateException("Moon data is null after refresh"))
                    } catch (e: Exception) {
                        Log.e(TAG, "combine: Error fetching moon data", e)
                        MoonUiState.Error(e.message ?: "Failed to fetch moon data")
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

    fun getNextFetchTime(): String {
        val nextFetch = FetchSchedule.getNextMoonFetchTime()
        return nextFetch.toString()
    }
} 