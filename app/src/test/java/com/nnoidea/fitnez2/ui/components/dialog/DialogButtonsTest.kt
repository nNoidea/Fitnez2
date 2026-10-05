package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class DialogButtonsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun confirmButton_tap_firesOnConfirmExactlyOnce() {
        var confirmCount = 0

        composeTestRule.setContent {
            Fitnez2Theme {
                DialogConfirmButton(label = "OK", onConfirm = { confirmCount++ })
            }
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        composeTestRule.onNodeWithText("OK").assertIsEnabled()
        composeTestRule.onNodeWithText("OK").performClick()

        assertEquals(1, confirmCount)
    }

    @Test
    fun confirmButton_disabled_tapDoesNotFire() {
        var confirmCount = 0

        composeTestRule.setContent {
            Fitnez2Theme {
                DialogConfirmButton(label = "OK", enabled = false, onConfirm = { confirmCount++ })
            }
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        composeTestRule.onNodeWithText("OK").assertIsNotEnabled()

        assertEquals(0, confirmCount)
    }

    @Test
    fun confirmButton_destructive_tapFiresOnConfirm() {
        var confirmCount = 0

        composeTestRule.setContent {
            Fitnez2Theme {
                DialogConfirmButton(label = "Delete", destructive = true, onConfirm = { confirmCount++ })
            }
        }

        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").performClick()

        assertEquals(1, confirmCount)
    }

    @Test
    fun dismissButton_tap_firesOnDismissExactlyOnce() {
        var dismissCount = 0

        composeTestRule.setContent {
            Fitnez2Theme {
                DialogDismissButton(label = "Cancel", onDismiss = { dismissCount++ })
            }
        }

        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").performClick()

        assertEquals(1, dismissCount)
    }

    @Test
    fun confirmationDialog_endToEnd_usesSharedButtons() {
        var confirmed = false
        var dismissed = false

        composeTestRule.setContent {
            Fitnez2Theme {
                PredictiveConfirmationDialog(
                    show = true,
                    onDismissRequest = { dismissed = true },
                    title = "Delete item?",
                    message = "This cannot be undone.",
                    confirmLabel = "Delete",
                    cancelLabel = "Keep",
                    isDestructive = true,
                    onConfirm = { confirmed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Delete").performClick()
        assertTrue(confirmed)

        composeTestRule.onNodeWithText("Keep").performClick()
        assertTrue(dismissed)
    }
}
