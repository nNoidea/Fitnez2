package com.nnoidea.fitnez2.ui.components.bottomsheet

import android.content.Context
import android.view.View
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.haptics.HapticEngine

/**
 * Single Source of Truth (SSOT) tokens and haptics for the Bottom Sheet subsystem.
 *
 * All bottom sheet gesture physics, tick frequencies, detent pops, and settle
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

    // ── Haptic Configuration & Tuned Defaults ────────────────────────────────
    var hapticsEnabled by mutableStateOf(true)
    var dragTicksEnabled by mutableStateOf(true)
    var midpointPopEnabled by mutableStateOf(true)
    var clockBackEnabled by mutableStateOf(true)

    /** Movement in DP between continuous micro-ticks while dragging vertically (2.dp) */
    var tickIntervalDp: Dp by mutableStateOf(2.dp)

    /** Power / intensity for continuous vertical drag ticks (20% whisper-light detent) */
    var dragTickScale: Float by mutableFloatStateOf(0.20f)

    /** Power / intensity for the midpoint expansion POP (100% full mechanical punch) */
    var popScale: Float by mutableFloatStateOf(1.0f)

    // ── Haptic Triggers ──────────────────────────────────────────────────────

    /**
     * Continuous micro-tick felt while dragging the sheet up or down.
     */
    fun performDragTick(context: Context) {
        if (!hapticsEnabled || !dragTicksEnabled) return
        HapticEngine.tick(context, dragTickScale)
    }

    /**
     * High-contrast mechanical POP triggered when the sheet crosses the midpoint
     * between collapsed and expanded.
     */
    fun performMidpointPop(context: Context) {
        if (!hapticsEnabled || !midpointPopEnabled) return
        HapticEngine.springSnap(context, popScale)
    }

    /**
     * Subtle detent click when pulling back below the midpoint.
     */
    fun performClockBack(context: Context) {
        if (!hapticsEnabled || !clockBackEnabled) return
        HapticEngine.clockBack(context, dragTickScale)
    }

    /**
     * Settle confirmation feedback when the sheet springs to a rest state.
     */
    fun performSettle(context: Context, view: View? = null, expanded: Boolean) {
        if (!hapticsEnabled) return
        if (expanded) {
            HapticEngine.confirm(context, view)
        } else {
            HapticEngine.click(context, 0.40f)
        }
    }

    /**
     * Resets bottom sheet haptic parameters to the locked-in defaults.
     */
    fun resetToDefaults() {
        hapticsEnabled = true
        dragTicksEnabled = true
        midpointPopEnabled = true
        clockBackEnabled = true
        tickIntervalDp = 2.dp
        dragTickScale = 0.20f
        popScale = 1.0f
    }
}
