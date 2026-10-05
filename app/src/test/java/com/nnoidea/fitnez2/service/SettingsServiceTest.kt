package com.nnoidea.fitnez2.service

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.data.dataStore
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
    fun setUp() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        context.dataStore.edit { it.clear() }
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

    @Test
    fun setAndGetGraphRangeAndMetric() = runBlocking {
        assertEquals("SEVEN", settingsService.graphRangeFlow.first())
        assertEquals("MAX_WEIGHT", settingsService.graphMetricFlow.first())

        settingsService.setGraphRange("THIRTY")
        assertEquals("THIRTY", settingsService.graphRangeFlow.first())

        settingsService.setGraphMetric("VOLUME")
        assertEquals("VOLUME", settingsService.graphMetricFlow.first())
    }

    @Test
    fun setAndGetNightModeHour() = runBlocking {
        assertEquals(0, settingsService.nightModeHourFlow.first())

        settingsService.setNightModeHour(4)
        assertEquals(4, settingsService.nightModeHourFlow.first())

        settingsService.setNightModeHour(12)
        assertEquals(12, settingsService.nightModeHourFlow.first())
    }
}
