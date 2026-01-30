package com.nnoidea.fitnez2.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val LANGUAGE_KEY = stringPreferencesKey("language_code")

    val languageCodeFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY]
        }

    private val WEIGHT_UNIT_KEY = stringPreferencesKey("weight_unit")

    val weightUnitFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[WEIGHT_UNIT_KEY] ?: "kg"
        }

    suspend fun setWeightUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[WEIGHT_UNIT_KEY] = unit
        }
    }

    suspend fun setLanguageCode(code: String?) {
        context.dataStore.edit { preferences ->
            if (code == null) {
                preferences.remove(LANGUAGE_KEY)
            } else {
                preferences[LANGUAGE_KEY] = code
            }
        }
    }
}
