package com.nnoidea.fitnez2.ui.components.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SetsRepsShiftTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPrintSemanticsTree() {
        composeTestRule.setContent {
            Fitnez2Theme {
                Box(modifier = Modifier.width(360.dp)) {
                    SetsRepsWeightGroup(
                        sets = "1",
                        reps = "6",
                        weight = "8.75",
                        weightUnit = "kg",
                        showLabels = true,
                        onSetsChange = {},
                        onRepsChange = {},
                        onWeightChange = {}
                    )
                }
            }
        }
        val repsBefore = composeTestRule.onNodeWithText("Reps").fetchSemanticsNode().boundsInRoot
        println("Reps bounds before focus: $repsBefore")

        composeTestRule.onNodeWithText("Reps").performClick()
        composeTestRule.waitForIdle()

        val repsAfter = composeTestRule.onNodeWithText("Reps").fetchSemanticsNode().boundsInRoot
        println("Reps bounds after focus: $repsAfter")

        org.junit.Assert.assertEquals("Reps X must not shift when focused", repsBefore.left, repsAfter.left, 0.01f)
    }
}
