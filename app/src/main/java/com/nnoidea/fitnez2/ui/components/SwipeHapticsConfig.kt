package com.nnoidea.fitnez2.ui.components

import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nnoidea.fitnez2.ui.components.haptics.HapticDefaults
import com.nnoidea.fitnez2.ui.components.haptics.HapticEngine
import com.nnoidea.fitnez2.ui.components.haptics.PopHapticMode
import com.nnoidea.fitnez2.ui.components.haptics.SliderTickMode

// Re-export for 100% backward compatibility
typealias PopHapticMode = com.nnoidea.fitnez2.ui.components.haptics.PopHapticMode
typealias SliderTickMode = com.nnoidea.fitnez2.ui.components.haptics.SliderTickMode

/**
 * Central configuration for Swipe-to-Delete gesture dynamics, thresholds, and haptic feedback.
 *
 * Backed by Compose mutable states so changing any option or slider immediately updates
 * the live swipe components across the app and in the Developer Options Haptics Studio.
 */
object SwipeHapticsConfig {

    // =========================================================================
    // 1. GESTURE & THRESHOLD DYNAMICS
    // =========================================================================

    /** Fraction of card width required to trigger deletion commit on release (default 0.35f = 35%) */
    var thresholdFraction by mutableFloatStateOf(HapticDefaults.SwipeDeleteThresholdFraction)

    /** Movement in DP between continuous slider ticks while dragging (default 2.dp for high-frequency micro-vibrations) */
    var tickIntervalDp by mutableStateOf(HapticDefaults.TickIntervalDp)

    // =========================================================================
    // 2. HAPTIC FEEDBACK SETTINGS
    // =========================================================================

    /** Master toggle for all swipe haptics */
    var hapticsEnabled by mutableStateOf(true)

    /** Toggle for continuous light vibration ticks while dragging */
    var sliderTicksEnabled by mutableStateOf(true)

    /** Toggle for subtle haptic feedback when pulling back under threshold ("clocking back") */
    var clockBackHapticsEnabled by mutableStateOf(true)

    /**
     * Feedback style for the Threshold POP when crossing the deletion threshold.
     * Default: SPRING_SNAP (tactile mechanical tension release)
     */
    var popMode by mutableStateOf(HapticDefaults.DefaultPopMode)

    /**
     * Feedback style for continuous slider ticks while dragging.
     * Default: PRIMITIVE_LOW_TICK (ultra-crisp active-braked hardware tick)
     */
    var sliderTickMode by mutableStateOf(HapticDefaults.DefaultSliderTickMode)

    /**
     * Actuator Power / Scale for the threshold POP [0.1f - 1.0f] (default 1.0 = 100% full punch).
     */
    var popScale by mutableFloatStateOf(HapticDefaults.PopScale)

    /**
     * Actuator Power / Scale for continuous swipe ticks [0.1f - 1.0f] (default 0.20f = 20% whisper-light detent).
     */
    var swipeTickScale by mutableFloatStateOf(HapticDefaults.SwipeTickScale)

    /**
     * Backwards-compatible alias for primitiveScale (maps to popScale).
     */
    var primitiveScale: Float
        get() = popScale
        set(value) {
            popScale = value
        }

    /**
     * Duration in milliseconds for STRONG_POP vibration (range: 10ms - 150ms).
     */
    var popVibrationDurationMs by mutableLongStateOf(50L)

    /**
     * Vibration amplitude for STRONG_POP (range: 10 to 255; 255 = maximum intensity).
     */
    var popVibrationAmplitude by mutableIntStateOf(255)

    /**
     * When true, bypasses view focus restrictions so haptics always fire.
     */
    var ignoreViewSetting by mutableStateOf(true)

    /**
     * Resets all parameters to recommended defaults.
     */
    fun resetToDefaults() {
        thresholdFraction = HapticDefaults.SwipeDeleteThresholdFraction
        tickIntervalDp = HapticDefaults.TickIntervalDp
        hapticsEnabled = true
        sliderTicksEnabled = true
        clockBackHapticsEnabled = true
        popMode = HapticDefaults.DefaultPopMode
        sliderTickMode = HapticDefaults.DefaultSliderTickMode
        popScale = HapticDefaults.PopScale
        swipeTickScale = HapticDefaults.SwipeTickScale
        popVibrationDurationMs = 50L
        popVibrationAmplitude = 255
        ignoreViewSetting = true
    }

    // =========================================================================
    // 3. EXECUTION HELPERS
    // =========================================================================

    /**
     * Triggers the configured threshold POP haptic feedback.
     */
    fun performPop(context: Context, view: View?) {
        if (!hapticsEnabled) return
        HapticEngine.executePopMode(
            context = context,
            view = view,
            mode = popMode,
            scale = popScale,
            durationMs = popVibrationDurationMs,
            amplitude = popVibrationAmplitude,
            ignoreViewSetting = ignoreViewSetting
        )
    }

    /**
     * Triggers continuous slider tick haptic feedback while dragging.
     */
    fun performSliderTick(context: Context, view: View?) {
        if (!hapticsEnabled || !sliderTicksEnabled) return
        HapticEngine.executeSliderTickMode(
            context = context,
            view = view,
            mode = sliderTickMode,
            scale = swipeTickScale,
            ignoreViewSetting = ignoreViewSetting
        )
    }

    /**
     * Triggers tactile feedback when dragging back under the threshold ("clocking back").
     */
    fun performClockBack(context: Context) {
        if (!hapticsEnabled || !clockBackHapticsEnabled) return
        HapticEngine.performClockBack(context, swipeTickScale)
    }
}
