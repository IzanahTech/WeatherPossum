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
} 