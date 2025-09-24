package com.weatherpossum.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherpossum.app.data.repository.HurricaneRepository
import com.weatherpossum.app.data.model.HurricaneData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log

private const val TAG = "HurricaneViewModel"

sealed class HurricaneUiState {
    data object Loading : HurricaneUiState()
    data class Success(val hurricaneData: HurricaneData) : HurricaneUiState()
    data class Error(val message: String) : HurricaneUiState()
}

class HurricaneViewModel(
    private val repository: HurricaneRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HurricaneUiState>(HurricaneUiState.Loading)
    val uiState: StateFlow<HurricaneUiState> = _uiState.asStateFlow()
    
    init {
        Log.d(TAG, "HurricaneViewModel init: starting coroutine to fetch hurricane data")
        viewModelScope.launch {
            repository.hurricaneData.collect { hurricaneData ->
                if (hurricaneData != null) {
                    _uiState.value = HurricaneUiState.Success(hurricaneData)
                } else {
                    // Fetch initial data
                    refreshHurricaneData()
                }
            }
        }
    }
    
    fun refreshHurricaneData() {
        viewModelScope.launch {
            try {
                _uiState.value = HurricaneUiState.Loading
                val result = repository.refreshHurricaneData()
                result.fold(
                    onSuccess = { hurricaneData ->
                        _uiState.value = HurricaneUiState.Success(hurricaneData)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error refreshing hurricane data", exception)
                        val userMessage = when {
                            exception.message?.contains("UnknownHostException") == true -> 
                                "Unable to connect to hurricane data service. Please check your internet connection."
                            exception.message?.contains("timeout") == true -> 
                                "Connection timeout. Please try again."
                            else -> "Failed to fetch hurricane data. Please try again."
                        }
                        _uiState.value = HurricaneUiState.Error(userMessage)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing hurricane data", e)
                val userMessage = when {
                    e.message?.contains("UnknownHostException") == true -> 
                        "Unable to connect to hurricane data service. Please check your internet connection."
                    e.message?.contains("timeout") == true -> 
                        "Connection timeout. Please try again."
                    else -> "Failed to fetch hurricane data. Please try again."
                }
                _uiState.value = HurricaneUiState.Error(userMessage)
            }
        }
    }
    
}
