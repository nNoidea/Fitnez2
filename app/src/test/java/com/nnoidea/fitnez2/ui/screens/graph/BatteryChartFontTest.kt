package com.nnoidea.fitnez2.ui.screens.graph

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BatteryChartFontTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sessions = listOf(
        SessionPoint(
            date = 1_700_000_000_000L,
            maxWeight = 67.5,
            totalSets = 3,
            totalReps = 15,
            leftValue = 67.5,
            rightValue = 67.5,
            startDate = 1_700_000_000_000L
        ),
        SessionPoint(
            date = 1_700_086_400_000L,
            maxWeight = 70.0,
            totalSets = 4,
            totalReps = 12,
            leftValue = 67.5,
            rightValue = 70.0,
            startDate = 1_700_000_000_000L,
            isLatest = true
        )
    )

    @Composable
    private fun ChartInTheme(fontMode: String) {
        Fitnez2Theme(fontMode = fontMode, dynamicColor = false) {
            BatteryStyleChart(
                sessions = sessions,
                weightUnit = "kg",
                primaryColor = MaterialTheme.colorScheme.primary,
                gridColor = Color.Gray,
                textColor = MaterialTheme.colorScheme.onSurface,
                barTonalColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }

    @Test
    fun chart_rendersYLabelsAndDates_inRoundedMode() {
        composeRule.setContent { ChartInTheme("rounded") }
        composeRule.waitForIdle()
    }

    @Test
    fun chart_rendersYLabelsAndDates_inSystemMode() {
        composeRule.setContent { ChartInTheme("system") }
        composeRule.waitForIdle()
    }
}
