package com.weatherpossum.app.domain.resource

import android.content.Context
import com.weatherpossum.app.R

/**
 * Interface for providing string resources to the domain layer
 */
interface ResourceProvider {
    fun getString(resourceId: Int): String
    fun getString(resourceId: Int, vararg args: Any): String
    
    // Weather specific strings
    val synopsisTitle: String
    val windConditionsTitle: String
    val seaConditionsTitle: String
    val sunTimesTitle: String
    val weatherOutlookTitle: String
    val forecastTodayTitle: String
    val forecastTonightTitle: String
    val forecastTodayTonightTitle: String
    val warningAdvisoryTitle: String
    
    // Error messages
    val errorNoInfoInResponse: String
    val errorParsingData: String
    val errorFetchFailedShowingCache: String
    val errorSocketTimeout: String
    val errorIo: String
    val errorAllRetriesFailed: String
    val unknownError: String
}

/**
 * Android implementation of ResourceProvider using Context
 */
class AndroidResourceProvider(
    private val context: Context
) : ResourceProvider {
    override fun getString(resourceId: Int): String = context.getString(resourceId)
    
    override fun getString(resourceId: Int, vararg args: Any): String = 
        context.getString(resourceId, *args)
    
    override val synopsisTitle: String
        get() = getString(R.string.repository_title_synopsis)
    
    override val windConditionsTitle: String
        get() = getString(R.string.repository_title_wind_conditions)
    
    override val seaConditionsTitle: String
        get() = getString(R.string.repository_title_sea_conditions)
    
    override val sunTimesTitle: String
        get() = getString(R.string.repository_title_sun_times)
    
    override val weatherOutlookTitle: String
        get() = getString(R.string.repository_title_weather_outlook)
    
    override val forecastTodayTitle: String
        get() = getString(R.string.repository_title_forecast_today)
    
    override val forecastTonightTitle: String
        get() = getString(R.string.repository_title_forecast_tonight)
    
    override val forecastTodayTonightTitle: String
        get() = getString(R.string.repository_title_forecast_today_tonight)
    
    override val warningAdvisoryTitle: String
        get() = getString(R.string.repository_title_warning_advisory)
    
    override val errorNoInfoInResponse: String
        get() = getString(R.string.repository_error_no_info_in_response)
    
    override val errorParsingData: String
        get() = getString(R.string.repository_error_parsing_data)
    
    override val errorFetchFailedShowingCache: String
        get() = getString(R.string.repository_error_fetch_failed_showing_cache)
    
    override val errorSocketTimeout: String
        get() = getString(R.string.repository_error_socket_timeout)
    
    override val errorIo: String
        get() = getString(R.string.repository_error_io)
    
    override val errorAllRetriesFailed: String
        get() = getString(R.string.repository_error_all_retries_failed)
    
    override val unknownError: String
        get() = getString(R.string.unknown_error)
} 