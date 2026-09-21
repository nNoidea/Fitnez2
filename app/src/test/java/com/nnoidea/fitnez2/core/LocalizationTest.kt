package com.nnoidea.fitnez2.core

import com.nnoidea.fitnez2.core.localization.EnglishStrings
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.core.localization.globalLocalization
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalizationTest {

    @Before
    fun setUp() {
        LocalizationManager.setLanguage(null)
    }

    @After
    fun tearDown() {
        LocalizationManager.setLanguage(null)
    }

    @Test
    fun supportedLanguages_isPopulated() {
        val languages = LocalizationManager.supportedLanguages
        assertTrue("Supported languages should not be empty", languages.isNotEmpty())
        assertTrue("Should include English", languages.any { it.appLocale.language == "en" })
    }

    @Test
    fun getLanguageByCode_knownCodes_returnsMatchingLanguage() {
        val en = LocalizationManager.getLanguageByCode("en")
        assertNotNull(en)
        assertEquals("en", en?.appLocale?.language)

        val de = LocalizationManager.getLanguageByCode("de")
        assertNotNull(de)
        assertEquals("de", de?.appLocale?.language)

        val fr = LocalizationManager.getLanguageByCode("fr")
        assertNotNull(fr)
        assertEquals("fr", fr?.appLocale?.language)
    }

    @Test
    fun getLanguageByCode_unknownCode_returnsNull() {
        val unknown = LocalizationManager.getLanguageByCode("xyz123")
        assertEquals(null, unknown)
    }

    @Test
    fun setLanguage_updatesCurrentLanguageAndGlobalAccessor() {
        val de = LocalizationManager.getLanguageByCode("de")
        assertNotNull(de)

        LocalizationManager.setLanguage(de)
        assertEquals(de, LocalizationManager.currentLanguage)
        assertEquals(de, globalLocalization)

        // Reset to system / default
        LocalizationManager.setLanguage(null)
        assertNotNull(globalLocalization)
    }

    @Test
    fun stringFormatters_produceNonEmptyMessages() {
        val strings = EnglishStrings
        assertTrue(strings.errorExerciseAlreadyExists("Squat").contains("Squat"))
        assertTrue(strings.errorExerciseRenameConflict("Bench").contains("Bench"))
        assertTrue(strings.errorExerciseNotFoundById("123").contains("123"))
        assertTrue(strings.errorRecordNotFoundById("456").contains("456"))
        assertTrue(strings.errorWorkoutAlreadyExists("Push Day").contains("Push Day"))
    }
}
