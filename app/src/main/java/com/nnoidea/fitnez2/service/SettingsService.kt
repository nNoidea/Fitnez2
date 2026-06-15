package com.nnoidea.fitnez2.service

import android.content.Context
import com.nnoidea.fitnez2.data.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsService(context: Context) {

    private val repository = SettingsRepository(context)

    val languageCodeFlow: Flow<String?> = repository.languageCodeFlow

    val weightUnitFlow: Flow<String> = repository.weightUnitFlow

    val defaultSetsFlow: Flow<String> = repository.defaultSetsFlow

    val defaultRepsFlow: Flow<String> = repository.defaultRepsFlow

    val defaultWeightFlow: Flow<String> = repository.defaultWeightFlow

    val rotationModeFlow: Flow<String> = repository.rotationModeFlow

    val fontModeFlow: Flow<String> = repository.fontModeFlow

    suspend fun setLanguageCode(code: String?) {
        repository.setLanguageCode(code)
    }

    suspend fun setWeightUnit(unit: String) {
        repository.setWeightUnit(unit)
    }

    suspend fun setDefaultSets(value: String) {
        repository.setDefaultSets(value)
    }

    suspend fun setDefaultReps(value: String) {
        repository.setDefaultReps(value)
    }

    suspend fun setDefaultWeight(value: String) {
        repository.setDefaultWeight(value)
    }

    suspend fun setRotationMode(mode: String) {
        repository.setRotationMode(mode)
    }

    suspend fun setFontMode(mode: String) {
        repository.setFontMode(mode)
    }
}
