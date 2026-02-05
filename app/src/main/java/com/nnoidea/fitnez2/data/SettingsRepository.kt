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

    private val DEFAULT_SETS_KEY = stringPreferencesKey("default_sets")
    private val DEFAULT_REPS_KEY = stringPreferencesKey("default_reps")
    private val DEFAULT_WEIGHT_KEY = stringPreferencesKey("default_weight")

    val defaultSetsFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[DEFAULT_SETS_KEY] ?: "3" }

    val defaultRepsFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[DEFAULT_REPS_KEY] ?: "10" }

    val defaultWeightFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[DEFAULT_WEIGHT_KEY] ?: "20" }

    suspend fun setDefaultSets(value: String) {
        context.dataStore.edit { it[DEFAULT_SETS_KEY] = value }
    }

    suspend fun setDefaultReps(value: String) {
        context.dataStore.edit { it[DEFAULT_REPS_KEY] = value }
    }

    suspend fun setDefaultWeight(value: String) {
        context.dataStore.edit { it[DEFAULT_WEIGHT_KEY] = value }
    }
}
