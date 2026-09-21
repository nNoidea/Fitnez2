package com.nnoidea.fitnez2.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ValidatedInputsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun setsInput_validNumber_triggersOnValidChange() {
        var committedSets by mutableStateOf(3)
        var dummyText by mutableStateOf("")

        composeRule.setContent {
            Column {
                SetsInput(
                    value = committedSets.toString(),
                    onValidChange = { committedSets = it }
                ) { displayValue, _, interactionSource, onValueChange, _ ->
                    BasicTextField(
                        value = displayValue,
                        onValueChange = onValueChange,
                        interactionSource = interactionSource,
                        modifier = Modifier.testTag("sets_field")
                    )
                }
                BasicTextField(
                    value = dummyText,
                    onValueChange = { dummyText = it },
                    modifier = Modifier.testTag("other_field")
                )
            }
        }

        // Tap field, type new valid number
        composeRule.onNodeWithTag("sets_field").performClick()
        composeRule.onNodeWithTag("sets_field").performTextReplacement("5")
        composeRule.waitForIdle()

        // Transfer focus to other_field to trigger blur
        composeRule.onNodeWithTag("other_field").performClick()
        composeRule.waitForIdle()

        assertEquals(5, committedSets)
    }

    @Test
    fun repsInput_validNumber_triggersOnValidChange() {
        var committedReps by mutableStateOf(10)
        var dummyText by mutableStateOf("")

        composeRule.setContent {
            Column {
                RepsInput(
                    value = committedReps.toString(),
                    onValidChange = { committedReps = it }
                ) { displayValue, _, interactionSource, onValueChange, _ ->
                    BasicTextField(
                        value = displayValue,
                        onValueChange = onValueChange,
                        interactionSource = interactionSource,
                        modifier = Modifier.testTag("reps_field")
                    )
                }
                BasicTextField(
                    value = dummyText,
                    onValueChange = { dummyText = it },
                    modifier = Modifier.testTag("other_field")
                )
            }
        }

        composeRule.onNodeWithTag("reps_field").performClick()
        composeRule.onNodeWithTag("reps_field").performTextReplacement("12")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("other_field").performClick()
        composeRule.waitForIdle()

        assertEquals(12, committedReps)
    }

    @Test
    fun weightInput_validDecimal_triggersOnValidChange() {
        var committedWeight by mutableStateOf(75.0)
        var dummyText by mutableStateOf("")

        composeRule.setContent {
            Column {
                WeightInput(
                    value = committedWeight,
                    onValidChange = { committedWeight = it }
                ) { displayValue, _, interactionSource, onValueChange, _ ->
                    BasicTextField(
                        value = displayValue,
                        onValueChange = onValueChange,
                        interactionSource = interactionSource,
                        modifier = Modifier.testTag("weight_field")
                    )
                }
                BasicTextField(
                    value = dummyText,
                    onValueChange = { dummyText = it },
                    modifier = Modifier.testTag("other_field")
                )
            }
        }

        composeRule.onNodeWithTag("weight_field").performClick()
        composeRule.onNodeWithTag("weight_field").performTextReplacement("82.5")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("other_field").performClick()
        composeRule.waitForIdle()

        assertEquals(82.5, committedWeight, 0.01)
    }
}
