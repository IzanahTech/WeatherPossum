package com.weatherpossum.app.data.model

data class WeatherCard(
    val title: String,
    val value: String
)

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
} 