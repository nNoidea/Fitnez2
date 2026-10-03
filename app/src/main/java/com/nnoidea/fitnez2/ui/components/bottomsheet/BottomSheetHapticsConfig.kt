package com.nnoidea.fitnez2.ui.components.bottomsheet

import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import com.nnoidea.fitnez2.ui.components.haptics.HapticDefaults
import com.nnoidea.fitnez2.ui.components.haptics.HapticEngine
import com.nnoidea.fitnez2.ui.components.haptics.PopHapticMode
import com.nnoidea.fitnez2.ui.components.haptics.SliderTickMode

/**
 * Single Source of Truth (SSOT) configuration for BottomSheet vertical gestures and tactile feedback.
 *
 * Backed by Compose mutable states so adjustments in Developer Options immediately update
 * sheet interactions across the app in real time.
 */
object BottomSheetHapticsConfig {

    /** Master toggle for all bottom sheet haptic feedback */
    var hapticsEnabled by mutableStateOf(true)

    /** Toggle for continuous micro-ticks while dragging the sheet up and down */
    var dragTicksEnabled by mutableStateOf(false)

    /** Toggle for tactile POP detent when crossing the expand/collapse midpoint */
    var midpointPopEnabled by mutableStateOf(false)

    /** Toggle for subtle haptic click when dragging back across the midpoint */
    var clockBackEnabled by mutableStateOf(false)

    /** Toggle for gentle landing click when sheet settles into expanded/collapsed position */
    var settleHapticEnabled by mutableStateOf(true)

    /** Distance in DP between continuous vertical drag notches (default 2dp) */
    var tickIntervalDp: Dp by mutableStateOf(HapticDefaults.TickIntervalDp)

    /** Actuator power scale for continuous vertical drag ticks [0.1f - 1.0f] (default 20%) */
    var dragTickScale by mutableFloatStateOf(HapticDefaults.SwipeTickScale)

    /** Actuator power scale for the midpoint POP detent [0.1f - 1.0f] (default 100%) */
    var popScale by mutableFloatStateOf(HapticDefaults.PopScale)

    /** Haptic mode for the midpoint threshold POP */
    var popMode by mutableStateOf(HapticDefaults.DefaultPopMode)

    /** Haptic mode for continuous vertical drag ticks */
    var sliderTickMode by mutableStateOf(HapticDefaults.DefaultSliderTickMode)

    /**
     * Resets bottom sheet haptic settings to the locked-in defaults.
     */
    fun resetToDefaults() {
        hapticsEnabled = true
        dragTicksEnabled = false
        midpointPopEnabled = false
        clockBackEnabled = false
        settleHapticEnabled = true
        tickIntervalDp = HapticDefaults.TickIntervalDp
        dragTickScale = HapticDefaults.SwipeTickScale
        popScale = HapticDefaults.PopScale
        popMode = HapticDefaults.DefaultPopMode
        sliderTickMode = HapticDefaults.DefaultSliderTickMode
    }

    /**
     * Emits a high-frequency drag notch tick as the sheet slides.
     */
    fun performDragTick(context: Context, view: View?) {
        if (!hapticsEnabled || !dragTicksEnabled) return
        HapticEngine.executeSliderTickMode(context, view, sliderTickMode, dragTickScale)
    }

    /**
     * Emits a tactile POP detent when crossing the midpoint threshold.
     */
    fun performMidpointPop(context: Context, view: View?) {
        if (!hapticsEnabled || !midpointPopEnabled) return
        HapticEngine.executePopMode(context, view, popMode, popScale)
    }

    /**
     * Emits a subtle reset tick when pulling back across the midpoint.
     */
    fun performClockBack(context: Context, view: View?) {
        if (!hapticsEnabled || !clockBackEnabled) return
        HapticEngine.performClockBack(context, dragTickScale)
    }

    /**
     * Emits a light physical landing click when sheet springs to rest.
     */
    fun performSettle(context: Context, view: View? = null) {
        if (!hapticsEnabled || !settleHapticEnabled) return
        HapticEngine.performClick(context, 0.5f)
    }
}
