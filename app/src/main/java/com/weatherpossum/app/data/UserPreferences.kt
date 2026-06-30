package com.weatherpossum.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.weatherpossum.app.data.cache.ExtendedForecastCache
import com.weatherpossum.app.data.cache.HurricaneCache
import com.weatherpossum.app.data.cache.MoonCache
import com.weatherpossum.app.data.cache.WeatherCardCache
import com.weatherpossum.app.data.model.ForecastDay
import com.weatherpossum.app.data.model.HurricaneData
import com.weatherpossum.app.data.model.MoonData
import com.weatherpossum.app.data.model.WeatherCard
import com.weatherpossum.app.widget.WidgetMetricsParser
import com.weatherpossum.app.widget.WidgetSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    companion object {
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val LAST_MOON_FETCH_TIME_KEY = longPreferencesKey("last_moon_fetch_time")
        private val CACHED_SYNOPSIS_KEY = stringPreferencesKey("cached_synopsis")
        private val CACHED_WEATHER_CARDS_JSON_KEY = stringPreferencesKey("cached_weather_cards_json")
        private val LAST_WEATHER_FETCH_TIME_KEY = longPreferencesKey("last_weather_fetch_time")
        private val CACHED_EXTENDED_FORECAST_JSON_KEY = stringPreferencesKey("cached_extended_forecast_json")
        private val LAST_EXTENDED_FORECAST_FETCH_TIME_KEY = longPreferencesKey("last_extended_forecast_fetch_time")
        private val CACHED_HURRICANE_JSON_KEY = stringPreferencesKey("cached_hurricane_json")
        private val LAST_HURRICANE_FETCH_TIME_KEY = longPreferencesKey("last_hurricane_fetch_time")
        private val CACHED_MOON_DATA_JSON_KEY = stringPreferencesKey("cached_moon_data_json")

    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val lastMoonFetchTime: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[LAST_MOON_FETCH_TIME_KEY]
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    suspend fun updateLastMoonFetchTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_MOON_FETCH_TIME_KEY] = timestamp
        }
    }

    suspend fun saveCachedWeatherCards(cards: List<WeatherCard>, fetchedAt: Long) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_WEATHER_CARDS_JSON_KEY] = WeatherCardCache.encode(cards)
            preferences[LAST_WEATHER_FETCH_TIME_KEY] = fetchedAt
            cards.find { it.title.contains("Synopsis", ignoreCase = true) }
                ?.value
                ?.takeIf { it.isNotBlank() }
                ?.let { preferences[CACHED_SYNOPSIS_KEY] = it }
        }
    }

    suspend fun loadCachedWeatherCards(): Pair<List<WeatherCard>, Long>? {
        val preferences = context.dataStore.data.first()
        val json = preferences[CACHED_WEATHER_CARDS_JSON_KEY] ?: return null
        val fetchedAt = preferences[LAST_WEATHER_FETCH_TIME_KEY] ?: return null
        val cards = WeatherCardCache.decode(json) ?: return null
        return cards to fetchedAt
    }

    suspend fun saveCachedExtendedForecast(days: List<ForecastDay>, fetchedAt: Long) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_EXTENDED_FORECAST_JSON_KEY] = ExtendedForecastCache.encode(days)
            preferences[LAST_EXTENDED_FORECAST_FETCH_TIME_KEY] = fetchedAt
        }
    }

    suspend fun loadCachedExtendedForecast(): Pair<List<ForecastDay>, Long>? {
        val preferences = context.dataStore.data.first()
        val json = preferences[CACHED_EXTENDED_FORECAST_JSON_KEY] ?: return null
        val fetchedAt = preferences[LAST_EXTENDED_FORECAST_FETCH_TIME_KEY] ?: return null
        val days = ExtendedForecastCache.decode(json) ?: return null
        return days to fetchedAt
    }

    suspend fun saveCachedHurricaneData(data: HurricaneData, fetchedAt: Long) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_HURRICANE_JSON_KEY] = HurricaneCache.encode(data)
            preferences[LAST_HURRICANE_FETCH_TIME_KEY] = fetchedAt
        }
    }

    suspend fun loadCachedHurricaneData(): Pair<HurricaneData, Long>? {
        val preferences = context.dataStore.data.first()
        val json = preferences[CACHED_HURRICANE_JSON_KEY] ?: return null
        val fetchedAt = preferences[LAST_HURRICANE_FETCH_TIME_KEY] ?: return null
        val data = HurricaneCache.decode(json) ?: return null
        return data to fetchedAt
    }

    suspend fun saveCachedMoonData(data: MoonData) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_MOON_DATA_JSON_KEY] = MoonCache.encode(data)
        }
    }

    suspend fun loadCachedMoonData(): MoonData? {
        val preferences = context.dataStore.data.first()
        val json = preferences[CACHED_MOON_DATA_JSON_KEY] ?: return null
        return MoonCache.decode(json)
    }

    suspend fun readWidgetSnapshot(appWidgetId: Int): WidgetSnapshot {
        val preferences = context.dataStore.data.first()
        val cards = preferences[CACHED_WEATHER_CARDS_JSON_KEY]
            ?.let { json -> WeatherCardCache.decode(json) }
            .orEmpty()
        return WidgetSnapshot(
            userName = preferences[USER_NAME_KEY],
            synopsis = preferences[CACHED_SYNOPSIS_KEY],
            seaConditions = WidgetMetricsParser.seaConditionsFromCards(cards)
        )
    }
} 