package com.nnoidea.fitnez2.core.localization

import kotlin.reflect.full.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Internal manager to switch languages.
 * Acts as the registry for available languages.
 */
object LocalizationManager {
    // Registry of supported languages - Auto-discovered!
    val supportedLanguages: List<EnStrings> = EnStrings::class.sealedSubclasses
        .mapNotNull { it.objectInstance }
        .sortedBy { it.languageName }

    // User preference: null = System Default, non-null = Specific Language
    var selectedLanguage: EnStrings? by mutableStateOf(null)

    // Resolved language (what the UI actually uses)
    val currentLanguage: EnStrings
        get() = selectedLanguage ?: detectSystemLanguage()

    val strings: EnStrings
        get() = currentLanguage

    fun setLanguage(language: EnStrings?) {
        selectedLanguage = language
    }

    fun getLanguageByCode(code: String): EnStrings? {
        return supportedLanguages.find { it.appLocale.language == code }
    }

    private fun detectSystemLanguage(): EnStrings {
        val systemCode = Locale.getDefault().language
        return supportedLanguages.find { it.appLocale.language == systemCode }
            ?: EnglishStrings // Fallback to explicitly defined English object
    }
}

/**
 * Unified global accessor for strings.
 * Use this anywhere (Composables, Enums, ViewModels) to access localized text.
 * It is reactive; any Composable reading this will automatically update when the language changes.
 */
val globalLocalization: EnStrings
    get() = LocalizationManager.strings
