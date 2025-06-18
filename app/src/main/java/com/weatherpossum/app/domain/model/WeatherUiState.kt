package com.weatherpossum.app.domain.model

/**
 * Represents the current state of the weather UI
 */
sealed class WeatherUiState {
    /**
     * Initial state when no data has been loaded
     */
    object Initial : WeatherUiState()
    
    /**
     * Loading state indicating data is being fetched
     */
    data class Loading(
        val isRefreshing: Boolean
    ) : WeatherUiState()
    
    /**
     * Success state containing the weather report
     */
    data class Success(
        val report: WeatherReport
    ) : WeatherUiState()
    
    /**
     * Error state with detailed error information
     */
    sealed class Error : WeatherUiState() {
        /**
         * Network-related errors (timeouts, connection issues, etc.)
         * @param message User-friendly error message
         * @param cause The underlying exception that caused the error
         * @param isRetryable Whether the operation can be retried
         */
        data class Network(
            val message: String,
            val cause: Throwable,
            val isRetryable: Boolean
        ) : Error()
        
        /**
         * Data parsing or validation errors
         * @param message User-friendly error message
         * @param cause The underlying exception that caused the error
         */
        data class Data(
            val message: String,
            val cause: Throwable
        ) : Error()
        
        /**
         * Unknown or unexpected errors
         * @param message User-friendly error message
         * @param cause The underlying exception that caused the error
         */
        data class Unknown(
            val message: String,
            val cause: Throwable
        ) : Error()
        
        /**
         * Error with cached data available
         * @param underlying The original error that occurred
         * @param cachedReport The cached weather report that can be shown
         */
        data class WithCache(
            val underlying: Error,
            val cachedReport: WeatherReport
        ) : Error()
    }
} 