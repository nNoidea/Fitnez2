package com.nnoidea.fitnez2.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsServiceTest {

    private lateinit var context: Context
    private lateinit var settingsService: SettingsService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        settingsService = SettingsService(context)
    }

    @Test
    fun setAndGetLanguageCode() = runBlocking {
        settingsService.setLanguageCode("de")
        assertEquals("de", settingsService.languageCodeFlow.first())

        settingsService.setLanguageCode(null)
        assertEquals(null, settingsService.languageCodeFlow.first())
    }

    @Test
    fun setAndGetWeightUnit() = runBlocking {
        settingsService.setWeightUnit("lbs")
        assertEquals("lbs", settingsService.weightUnitFlow.first())

        settingsService.setWeightUnit("kg")
        assertEquals("kg", settingsService.weightUnitFlow.first())
    }

    @Test
    fun setAndGetDefaultSetsRepsWeight() = runBlocking {
        settingsService.setDefaultSets("4")
        assertEquals("4", settingsService.defaultSetsFlow.first())

        settingsService.setDefaultReps("12")
        assertEquals("12", settingsService.defaultRepsFlow.first())

        settingsService.setDefaultWeight("85.5")
        assertEquals("85.5", settingsService.defaultWeightFlow.first())
    }

    @Test
    fun setAndGetRotationAndFontMode() = runBlocking {
        settingsService.setRotationMode("ON")
        assertEquals("ON", settingsService.rotationModeFlow.first())

        settingsService.setFontMode("LARGE")
        assertEquals("LARGE", settingsService.fontModeFlow.first())
    }
}
