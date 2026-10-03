package com.nnoidea.fitnez2.ui.components.bottomsheet

import android.content.Context
import android.view.View
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single Source of Truth (SSOT) tokens and haptics for the Bottom Sheet subsystem.
 *
 * All bottom sheet layout geometry, gesture physics, tick frequencies, detent pops, and settle
 * haptics are configured and tuned directly from this root object.
 */
object BottomSheetDefaults {

    // ── Layout Tokens ────────────────────────────────────────────────────────
    val CornerRadius: Dp = 28.dp
    val Shape: Shape = RoundedCornerShape(topStart = CornerRadius, topEnd = CornerRadius)
    val DragHandleWidth: Dp = 32.dp
    val DragHandleHeight: Dp = 4.dp
    val DragHandleVerticalPadding: Dp = 10.dp
    val OvershootBuffer: Dp = 150.dp

    // ── Haptic Configuration & Tuned Defaults (Delegated to BottomSheetHapticsConfig) ──
    var hapticsEnabled: Boolean
        get() = BottomSheetHapticsConfig.hapticsEnabled
        set(value) { BottomSheetHapticsConfig.hapticsEnabled = value }

    var dragTicksEnabled: Boolean
        get() = BottomSheetHapticsConfig.dragTicksEnabled
        set(value) { BottomSheetHapticsConfig.dragTicksEnabled = value }

    var midpointPopEnabled: Boolean
        get() = BottomSheetHapticsConfig.midpointPopEnabled
        set(value) { BottomSheetHapticsConfig.midpointPopEnabled = value }

    var clockBackEnabled: Boolean
        get() = BottomSheetHapticsConfig.clockBackEnabled
        set(value) { BottomSheetHapticsConfig.clockBackEnabled = value }

    var settleHapticEnabled: Boolean
        get() = BottomSheetHapticsConfig.settleHapticEnabled
        set(value) { BottomSheetHapticsConfig.settleHapticEnabled = value }

    var tickIntervalDp: Dp
        get() = BottomSheetHapticsConfig.tickIntervalDp
        set(value) { BottomSheetHapticsConfig.tickIntervalDp = value }

    var dragTickScale: Float
        get() = BottomSheetHapticsConfig.dragTickScale
        set(value) { BottomSheetHapticsConfig.dragTickScale = value }

    var popScale: Float
        get() = BottomSheetHapticsConfig.popScale
        set(value) { BottomSheetHapticsConfig.popScale = value }

    // ── Haptic Triggers ──────────────────────────────────────────────────────

    fun performDragTick(context: Context, view: View? = null) {
        BottomSheetHapticsConfig.performDragTick(context, view)
    }

    fun performMidpointPop(context: Context, view: View? = null) {
        BottomSheetHapticsConfig.performMidpointPop(context, view)
    }

    fun performClockBack(context: Context, view: View? = null) {
        BottomSheetHapticsConfig.performClockBack(context, view)
    }

    fun performSettle(context: Context, view: View? = null, expanded: Boolean = false) {
        BottomSheetHapticsConfig.performSettle(context, view)
    }

    fun resetToDefaults() {
        BottomSheetHapticsConfig.resetToDefaults()
    }
}
