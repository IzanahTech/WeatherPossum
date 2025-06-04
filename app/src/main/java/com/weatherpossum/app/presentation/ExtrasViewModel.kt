package com.weatherpossum.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.util.Log

private const val TAG = "ExtrasViewModel"

class ExtrasViewModel : ViewModel() {
    // Moon phase related code has been moved to MoonViewModel
} 