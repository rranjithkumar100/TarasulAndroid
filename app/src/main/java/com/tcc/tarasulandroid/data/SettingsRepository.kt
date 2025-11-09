package com.tcc.tarasulandroid.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private val isDarkThemeKey = booleanPreferencesKey("is_dark_theme")

    val isDarkTheme: Flow<Boolean> = dataStore.data.map {
        it[isDarkThemeKey] ?: false
    }

    suspend fun setDarkTheme(isDarkTheme: Boolean) {
        dataStore.edit {
            it[isDarkThemeKey] = isDarkTheme
        }
    }
}