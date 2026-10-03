package com.nnoidea.fitnez2.ui.components

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.ui.common.InputDefaults
import com.nnoidea.fitnez2.ui.components.bottomsheet.BottomSheetDefaults
import com.nnoidea.fitnez2.ui.components.dialog.DialogDefaults
import com.nnoidea.fitnez2.ui.components.haptics.HapticEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HapticsSsotTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun testHapticEngine_methodsExecuteSafely() {
        HapticEngine.tick(context, 0.20f)
        HapticEngine.click(context, 0.60f)
        HapticEngine.springSnap(context, 1.0f)
        HapticEngine.clockBack(context, 0.20f)
        HapticEngine.confirm(context)
        HapticEngine.reject(context)
    }

    @Test
    fun testBottomSheetDefaults_lockedInValuesAndReset() {
        assertEquals(2.dp, BottomSheetDefaults.tickIntervalDp)
        assertEquals(0.20f, BottomSheetDefaults.dragTickScale, 0.001f)
        assertEquals(1.0f, BottomSheetDefaults.popScale, 0.001f)
        assertTrue(BottomSheetDefaults.hapticsEnabled)
        org.junit.Assert.assertFalse(BottomSheetDefaults.dragTicksEnabled)
        org.junit.Assert.assertFalse(BottomSheetDefaults.midpointPopEnabled)
        org.junit.Assert.assertFalse(BottomSheetDefaults.clockBackEnabled)
        assertTrue(BottomSheetDefaults.settleHapticEnabled)

        // Modify and test reset
        BottomSheetDefaults.tickIntervalDp = 5.dp
        BottomSheetDefaults.dragTickScale = 0.5f
        BottomSheetDefaults.dragTicksEnabled = true
        BottomSheetDefaults.resetToDefaults()

        assertEquals(2.dp, BottomSheetDefaults.tickIntervalDp)
        assertEquals(0.20f, BottomSheetDefaults.dragTickScale, 0.001f)
        org.junit.Assert.assertFalse(BottomSheetDefaults.dragTicksEnabled)
    }

    @Test
    fun testBottomSheetDefaults_triggersExecuteSafely() {
        BottomSheetDefaults.performDragTick(context)
        BottomSheetDefaults.performMidpointPop(context)
        BottomSheetDefaults.performClockBack(context)
        BottomSheetDefaults.performSettle(context, null, expanded = true)
        BottomSheetDefaults.performSettle(context, null, expanded = false)
    }

    @Test
    fun testDialogDefaults_hapticTokensExecuteSafely() {
        DialogDefaults.onConfirmHaptic(context)
        DialogDefaults.onDismissHaptic(context)
        DialogDefaults.onItemSelectHaptic(context)
        DialogDefaults.onDestructiveHaptic(context)
    }

    @Test
    fun testInputDefaults_hapticTokensExecuteSafely() {
        assertTrue(InputDefaults.hapticsEnabled)
        InputDefaults.onFocusHaptic(context)
        InputDefaults.onCommitHaptic(context)
        InputDefaults.onErrorHaptic(context)
    }
}
