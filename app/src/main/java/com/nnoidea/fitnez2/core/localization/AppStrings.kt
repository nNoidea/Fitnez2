package com.nnoidea.fitnez2.core.localization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Internal manager to switch languages.
 * Acts as the registry for available languages.
 */
object LocalizationManager {
    // Registry of supported languages
    val supportedLanguages: List<EnStrings> = listOf(
        EnglishStrings, ArStrings, DaStrings, DeStrings, EsStrings, FrStrings,
        HiStrings, IdStrings, ItStrings, JaStrings, KoStrings, NlStrings,
        NoStrings, PlStrings, PtStrings, RuStrings, SvStrings, TrStrings, UkStrings, ZhStrings
    ).sortedBy { it.languageName }

    private val languagesByCode: Map<String, EnStrings> =
        supportedLanguages.associateBy { it.appLocale.language }

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
        return languagesByCode[code]
    }

    private fun detectSystemLanguage(): EnStrings {
        return languagesByCode[Locale.getDefault().language] ?: EnglishStrings
    }
}

/**
 * Unified global accessor for strings.
 * Use this anywhere (Composables, Enums, ViewModels) to access localized text.
 * It is reactive; any Composable reading this will automatically update when the language changes.
 */
val globalLocalization: EnStrings
    get() = LocalizationManager.strings
