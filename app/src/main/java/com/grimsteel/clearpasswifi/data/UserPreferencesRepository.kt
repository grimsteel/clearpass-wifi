package com.grimsteel.clearpasswifi.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.handleCoroutineException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val ENABLE_DEBUG_LOGGING = booleanPreferencesKey("enable_debug_logging");
    }

    val prefsFlow: Flow<UserPreferences> = dataStore.data
        .catch { err ->
            // don't throw when there's an error reading the prefs file
            if (err is IOException) {
                emit(emptyPreferences())
            } else {
                throw err
            }
        }
        .map { prefs ->
            val enableDebugLogging = prefs[ENABLE_DEBUG_LOGGING] ?: false
            UserPreferences(enableDebugLogging)
        }

    suspend fun updateEnableDebugLogging(enableDebugLogging: Boolean) {
        dataStore.edit { prefs -> prefs[ENABLE_DEBUG_LOGGING] = enableDebugLogging }
    }
}

data class UserPreferences(val enableDebugLogging: Boolean)
