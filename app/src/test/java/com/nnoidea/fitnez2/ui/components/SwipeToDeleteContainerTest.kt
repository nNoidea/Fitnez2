package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import org.junit.Assert.assertFalse
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
class SwipeToDeleteContainerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSwipeToDelete_rendersContent() {
        composeTestRule.setContent {
            Fitnez2Theme {
                SwipeToDeleteContainer(
                    onDelete = {}
                ) {
                    Text("Swipable Item Content")
                }
            }
        }

        composeTestRule.onNodeWithText("Swipable Item Content").assertIsDisplayed()
    }

    @Test
    fun testSwipeToDelete_hasDeleteIconDescription() {
        composeTestRule.setContent {
            Fitnez2Theme {
                SwipeToDeleteContainer(
                    onDelete = {}
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                        Text("Record Item")
                    }
                }
            }
        }

        composeTestRule.onNodeWithContentDescription(globalLocalization.labelDelete)
            .assertExists()
    }

    @Test
    fun testSwipeToDelete_swipeLeftTriggersOnDelete() {
        var deleted = false

        composeTestRule.setContent {
            Fitnez2Theme {
                SwipeToDeleteContainer(
                    onDelete = { deleted = true },
                    modifier = Modifier.testTag("swipe_container")
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                        Text("Item to Delete")
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        assertFalse(deleted)

        // Perform full swipe left to trigger delete
        composeTestRule.onNodeWithTag("swipe_container").performTouchInput {
            swipeLeft()
        }

        composeTestRule.waitForIdle()
        assertTrue("onDelete should be called after full swipe left", deleted)
    }

    @Test
    fun testSwipeToDelete_afterDismissAndUndo_contentIsDisplayedAndNotDismissed() {
        var isItemPresent by androidx.compose.runtime.mutableStateOf(true)

        composeTestRule.setContent {
            Fitnez2Theme {
                val saveableStateHolder = androidx.compose.runtime.saveable.rememberSaveableStateHolder()
                if (isItemPresent) {
                    saveableStateHolder.SaveableStateProvider("item_1") {
                        SwipeToDeleteContainer(
                            onDelete = { isItemPresent = false },
                            modifier = Modifier.testTag("swipe_container")
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                                Text("Card Title: Squat")
                            }
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Card Title: Squat").assertIsDisplayed()

        // Swipe left to delete
        composeTestRule.onNodeWithTag("swipe_container").performTouchInput {
            swipeLeft()
        }
        composeTestRule.waitForIdle()

        // Item was deleted
        assertFalse(isItemPresent)
        composeTestRule.onNodeWithText("Card Title: Squat").assertDoesNotExist()

        // Now simulate Undo
        isItemPresent = true
        composeTestRule.waitForIdle()

        // The card content MUST be displayed, NOT stuck off-screen or in red!
        composeTestRule.onNodeWithText("Card Title: Squat").assertIsDisplayed()
    }

    @Test
    fun testSwipeToDelete_afterDismissAndUndo_canDeleteAgain() {
        var isItemPresent by androidx.compose.runtime.mutableStateOf(true)
        var deleteCount = 0

        composeTestRule.setContent {
            Fitnez2Theme {
                val saveableStateHolder = androidx.compose.runtime.saveable.rememberSaveableStateHolder()
                if (isItemPresent) {
                    saveableStateHolder.SaveableStateProvider("item_1") {
                        SwipeToDeleteContainer(
                            onDelete = {
                                deleteCount++
                                isItemPresent = false
                            },
                            modifier = Modifier.testTag("swipe_container")
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                                Text("Card Title: Squat")
                            }
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        // 1. Swipe left to delete first time
        composeTestRule.onNodeWithTag("swipe_container").performTouchInput {
            swipeLeft()
        }
        composeTestRule.waitForIdle()
        org.junit.Assert.assertEquals(1, deleteCount)
        assertFalse(isItemPresent)

        // 2. Undo
        isItemPresent = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Card Title: Squat").assertIsDisplayed()

        // 3. Swipe left to delete AGAIN
        composeTestRule.onNodeWithTag("swipe_container").performTouchInput {
            swipeLeft()
        }
        composeTestRule.waitForIdle()

        // Verify it was deleted a second time!
        org.junit.Assert.assertEquals(2, deleteCount)
        assertFalse(isItemPresent)
    }

    @Test
    fun testSwipeToDelete_dragPastThresholdAndDragBack_doesNotDelete() {
        var deleted = false

        composeTestRule.setContent {
            Fitnez2Theme {
                SwipeToDeleteContainer(
                    onDelete = { deleted = true },
                    modifier = Modifier.testTag("swipe_container")
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                        Text("Clock-Back Card")
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        assertFalse(deleted)

        // Drag left past threshold, then drag back right and lift finger
        composeTestRule.onNodeWithTag("swipe_container").performTouchInput {
            down(androidx.compose.ui.geometry.Offset(right - 10f, centerY))
            moveTo(androidx.compose.ui.geometry.Offset(left + 50f, centerY))
            moveTo(androidx.compose.ui.geometry.Offset(right - 10f, centerY))
            up()
        }

        composeTestRule.waitForIdle()
        assertFalse("Card should NOT be deleted if clocked back before releasing", deleted)
        composeTestRule.onNodeWithText("Clock-Back Card").assertIsDisplayed()
    }

    @Test
    fun testSwipeHapticsConfig_allPopModesExecuteSafely() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val dummyView = android.view.View(context)

        for (mode in PopHapticMode.entries) {
            SwipeHapticsConfig.popMode = mode
            // Ensure calling performPop doesn't crash in any mode
            SwipeHapticsConfig.performPop(context, dummyView)
            SwipeHapticsConfig.performPop(context, null)
        }

        // Reset to default
        SwipeHapticsConfig.popMode = PopHapticMode.SPRING_SNAP
    }

    @Test
    fun testSwipeHapticsConfig_allSliderTickModesExecuteSafely() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val dummyView = android.view.View(context)

        for (mode in SliderTickMode.entries) {
            SwipeHapticsConfig.sliderTickMode = mode
            // Ensure calling performSliderTick doesn't crash in any mode
            SwipeHapticsConfig.performSliderTick(context, dummyView)
            SwipeHapticsConfig.performSliderTick(context, null)
        }

        // Reset to default
        SwipeHapticsConfig.sliderTickMode = SliderTickMode.PRIMITIVE_LOW_TICK
    }

    @Test
    fun testSwipeHapticsConfig_configurableValues() {
        SwipeHapticsConfig.thresholdFraction = 0.40f
        org.junit.Assert.assertEquals(0.40f, SwipeHapticsConfig.thresholdFraction, 0.001f)

        SwipeHapticsConfig.tickIntervalDp = 4.dp
        org.junit.Assert.assertEquals(4.dp, SwipeHapticsConfig.tickIntervalDp)

        SwipeHapticsConfig.popScale = 0.85f
        org.junit.Assert.assertEquals(0.85f, SwipeHapticsConfig.popScale, 0.001f)

        SwipeHapticsConfig.swipeTickScale = 0.35f
        org.junit.Assert.assertEquals(0.35f, SwipeHapticsConfig.swipeTickScale, 0.001f)

        SwipeHapticsConfig.popVibrationDurationMs = 65L
        org.junit.Assert.assertEquals(65L, SwipeHapticsConfig.popVibrationDurationMs)

        SwipeHapticsConfig.popVibrationAmplitude = 200
        org.junit.Assert.assertEquals(200, SwipeHapticsConfig.popVibrationAmplitude)

        // Reset to defaults
        SwipeHapticsConfig.resetToDefaults()
        org.junit.Assert.assertEquals(0.35f, SwipeHapticsConfig.thresholdFraction, 0.001f)
        org.junit.Assert.assertEquals(2.dp, SwipeHapticsConfig.tickIntervalDp)
        org.junit.Assert.assertEquals(1.0f, SwipeHapticsConfig.popScale, 0.001f)
        org.junit.Assert.assertEquals(0.20f, SwipeHapticsConfig.swipeTickScale, 0.001f)
        org.junit.Assert.assertEquals(50L, SwipeHapticsConfig.popVibrationDurationMs)
        org.junit.Assert.assertEquals(255, SwipeHapticsConfig.popVibrationAmplitude)
    }

    @Test
    fun testSwipeHapticsConfig_clockBackExecutesSafely() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val dummyView = android.view.View(context)

        SwipeHapticsConfig.clockBackHapticsEnabled = true
        SwipeHapticsConfig.performClockBack(context)
        SwipeHapticsConfig.performClockBack(context)

        SwipeHapticsConfig.clockBackHapticsEnabled = false
        SwipeHapticsConfig.performClockBack(context)
        SwipeHapticsConfig.clockBackHapticsEnabled = true
    }

    @Test
    fun testSwipeToDelete_defaultRoundedCornerShape() {
        composeTestRule.setContent {
            Fitnez2Theme {
                SwipeToDeleteContainer(
                    onDelete = {}
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                        Text("Default Shape Item")
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Default Shape Item").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(globalLocalization.labelDelete).assertExists()
    }

    @Test
    fun testHeightDependentCornerShape_preventsFullyCircledSides() {
        val density = androidx.compose.ui.unit.Density(1f)
        val shape = heightDependentCornerShape(maxRadius = 28.dp) as androidx.compose.foundation.shape.RoundedCornerShape

        // 1. Tall card (e.g. 84dp): uses full 28dp radius
        val tallSize = androidx.compose.ui.geometry.Size(width = 300f, height = 84f)
        val tallRadius = shape.topStart.toPx(tallSize, density)
        org.junit.Assert.assertEquals(28f, tallRadius, 0.01f)

        // 2. Extra tall card (e.g. 120dp): capped at 28dp maxRadius
        val extraTallSize = androidx.compose.ui.geometry.Size(width = 300f, height = 120f)
        val extraTallRadius = shape.topStart.toPx(extraTallSize, density)
        org.junit.Assert.assertEquals(28f, extraTallRadius, 0.01f)

        // 3. Compact card (e.g. 48dp): scales down to 16dp (1/3 of height)
        // With 16dp radius on 48dp height: 48 - (2 * 16) = 16dp straight vertical side.
        // It is strictly LESS than 24dp (half height), so sides are NEVER fully circled!
        val compactSize = androidx.compose.ui.geometry.Size(width = 300f, height = 48f)
        val compactRadius = shape.topStart.toPx(compactSize, density)
        org.junit.Assert.assertEquals(16f, compactRadius, 0.01f)
        org.junit.Assert.assertTrue("Radius must be strictly less than half height to avoid capsule pill", compactRadius < compactSize.height / 2f)

        // 4. Short card (e.g. 30dp): scales down to 10dp
        val shortSize = androidx.compose.ui.geometry.Size(width = 300f, height = 30f)
        val shortRadius = shape.topStart.toPx(shortSize, density)
        org.junit.Assert.assertEquals(10f, shortRadius, 0.01f)

        // 5. Zero height: returns 0
        val zeroSize = androidx.compose.ui.geometry.Size(width = 300f, height = 0f)
        val zeroRadius = shape.topStart.toPx(zeroSize, density)
        org.junit.Assert.assertEquals(0f, zeroRadius, 0.01f)
    }
}

