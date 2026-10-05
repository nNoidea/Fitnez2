package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PredictiveDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDialogDefaults_tokensAreConsistent() {
        assertEquals(560.dp, DialogDefaults.MaxWidth)
        assertEquals(24.dp, DialogDefaults.ScreenMarginHorizontal)
        assertEquals(28.dp, DialogDefaults.CornerRadius)
        assertEquals(24.dp, DialogDefaults.ContentPadding)
        assertEquals(16.dp, DialogDefaults.ContentSpacing)
        assertEquals(8.dp, DialogDefaults.ButtonSpacing)
        assertEquals(6.dp, DialogDefaults.Elevation)
        assertEquals(0.70f, DialogDefaults.MaxContentHeightRatio, 0.001f)
    }

    @Test
    fun testPredictiveAlertDialog_renders2ButtonsAndClicks() {
        var confirmed = false
        var dismissed = false

        composeTestRule.setContent {
            Fitnez2Theme {
                PredictiveAlertDialog(
                    show = true,
                    onDismissRequest = { dismissed = true },
                    title = "Test Alert",
                    text = "Alert body message",
                    confirmButton = {
                        Button(onClick = { confirmed = true }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { dismissed = true }) {
                            Text("Dismiss")
                        }
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Test Alert").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alert body message").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dismiss").assertIsDisplayed()

        composeTestRule.onNodeWithText("Confirm").performClick()
        assertTrue(confirmed)

        composeTestRule.onNodeWithText("Dismiss").performClick()
        assertTrue(dismissed)
    }

    @Test
    fun testPredictiveAlertDialog_supports3ButtonsNatively() {
        var neutralClicked = false
        var dismissed = false
        var confirmed = false

        composeTestRule.setContent {
            Fitnez2Theme {
                PredictiveAlertDialog(
                    show = true,
                    onDismissRequest = { dismissed = true },
                    title = "3-Button Dialog",
                    text = "Supports discard, cancel, save",
                    neutralButton = {
                        TextButton(onClick = { neutralClicked = true }) {
                            Text("Discard")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { dismissed = true }) {
                            Text("Cancel")
                        }
                    },
                    confirmButton = {
                        Button(onClick = { confirmed = true }) {
                            Text("Save")
                        }
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("3-Button Dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Discard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()

        composeTestRule.onNodeWithText("Discard").performClick()
        assertTrue(neutralClicked)

        composeTestRule.onNodeWithText("Save").performClick()
        assertTrue(confirmed)
    }

    @Test
    fun testPredictiveConfirmationDialog_rendersAndConfirms() {
        var confirmed = false

        composeTestRule.setContent {
            Fitnez2Theme {
                PredictiveConfirmationDialog(
                    show = true,
                    onDismissRequest = {},
                    title = "Confirm Delete",
                    message = "Are you sure?",
                    confirmLabel = "Delete",
                    isDestructive = true,
                    onConfirm = { confirmed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Confirm Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()

        composeTestRule.onNodeWithText("Delete").performClick()
        assertTrue(confirmed)
    }

    @Test
    fun testRadioSelectionDialog_selectsOption() {
        var selectedValue = "Option A"

        composeTestRule.setContent {
            Fitnez2Theme {
                RadioSelectionDialog(
                    show = true,
                    title = "Select Option",
                    options = listOf("Option A", "Option B", "Option C"),
                    selectedValue = selectedValue,
                    onValueSelected = { selectedValue = it },
                    onDismissRequest = {},
                    labelProvider = { it }
                )
            }
        }

        composeTestRule.onNodeWithText("Select Option").assertIsDisplayed()
        composeTestRule.onNodeWithText("Option B").assertIsDisplayed()

        composeTestRule.onNodeWithText("Option B").performClick()
        assertEquals("Option B", selectedValue)
    }

    @Test
    fun testLoadingDialog_rendersWithoutCrash() {
        composeTestRule.setContent {
            Fitnez2Theme {
                LoadingDialog(
                    show = true,
                    title = "Processing Data",
                    message = "Please wait..."
                )
            }
        }

        composeTestRule.onNodeWithText("Processing Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please wait...").assertIsDisplayed()
    }
}
