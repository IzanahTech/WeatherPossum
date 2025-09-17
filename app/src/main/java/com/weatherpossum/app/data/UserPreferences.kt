package com.weatherpossum.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    companion object {
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val LAST_MOON_FETCH_TIME_KEY = longPreferencesKey("last_moon_fetch_time")
        private val LAST_RATE_LIMIT_ERROR_TIME_KEY = longPreferencesKey("last_rate_limit_error_time")
        private val WEATHER_CACHE_KEY = stringPreferencesKey("weather_cache")
        private val WEATHER_CACHE_TIME_KEY = longPreferencesKey("weather_cache_time")
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val lastMoonFetchTime: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[LAST_MOON_FETCH_TIME_KEY]
    }

    val lastRateLimitErrorTime: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[LAST_RATE_LIMIT_ERROR_TIME_KEY]
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

    suspend fun updateLastRateLimitErrorTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_RATE_LIMIT_ERROR_TIME_KEY] = timestamp
        }
    }

    suspend fun clearLastRateLimitErrorTime() {
        context.dataStore.edit { preferences ->
            preferences.remove(LAST_RATE_LIMIT_ERROR_TIME_KEY)
        }
    }
    
    suspend fun saveWeatherCache(cacheData: String, timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[WEATHER_CACHE_KEY] = cacheData
            preferences[WEATHER_CACHE_TIME_KEY] = timestamp
        }
    }
} 