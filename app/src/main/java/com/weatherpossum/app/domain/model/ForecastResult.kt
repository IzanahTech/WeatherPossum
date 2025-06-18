package com.weatherpossum.app.domain.model

import com.weatherpossum.app.presentation.ForecastDay

sealed class ForecastResult {
    data class Success(val days: List<ForecastDay>) : ForecastResult()
    data class Failure(val message: String, val cause: Throwable? = null) : ForecastResult()
} 