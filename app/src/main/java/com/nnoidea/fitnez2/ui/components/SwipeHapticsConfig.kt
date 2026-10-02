package com.nnoidea.fitnez2.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * All available haptic feedback modes for the threshold POP when swiping crosses the deletion point.
 */
enum class PopHapticMode(val displayName: String) {
    SPRING_SNAP("Spring Snap (Quick Rise + Click)"),
    PRIMITIVE_THUD("Hardware Thud (Deep LRA Punch)"),
    LINEAGE_COMPOUND("Lineage SystemUI (Tick + Click)"),
    PRIMITIVE_CLICK("Hardware Crisp Click"),
    DOUBLE_PRIMITIVE_CLICK("Hardware Double Click"),
    EFFECT_HEAVY_CLICK("M3 Heavy Click (Prebaked 5)"),
    EFFECT_POP("M3 Prebaked Pop (Prebaked 4)"),
    STRONG_POP("Actuator Punch (Customizable ms)"),
    DOUBLE_PULSE("Double Motor Pulse"),
    GESTURE_THRESHOLD("Android 14+ Gesture Threshold"),
    CONFIRM("System Confirm"),
    REJECT("System Reject"),
    LONG_PRESS("System Long Press"),
    NONE("None (Silent)");

    override fun toString(): String = displayName
}

/**
 * All available haptic modes for continuous slider ticks felt while dragging horizontally.
 */
enum class SliderTickMode(val displayName: String) {
    PRIMITIVE_LOW_TICK("Hardware Low Tick (Ultra-Crisp LRA)"),
    PRIMITIVE_TICK("Hardware Tick (Crisp LRA Detent)"),
    LINEAGE_SLIDER_TICK("LineageOS Slider (System UI Code 23)"),
    PRIMITIVE_SPIN("Hardware Spin Detent"),
    SEGMENT_TICK("Android 14+ Segment Tick"),
    SEGMENT_FREQUENT_TICK("Android 14+ Frequent Tick"),
    EFFECT_TICK("M3 Prebaked Tick (EFFECT_TICK)"),
    MICRO_PULSE("Actuator Micro Pulse (10ms)"),
    CLOCK_TICK("System Clock Tick"),
    NONE("None (Silent)");

    override fun toString(): String = displayName
}

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
    var thresholdFraction by mutableFloatStateOf(0.35f)

    /** Movement in DP between continuous slider ticks while dragging (default 2.dp for high-frequency micro-vibrations) */
    var tickIntervalDp by mutableStateOf(2.dp)

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
    var popMode by mutableStateOf(PopHapticMode.SPRING_SNAP)

    /**
     * Feedback style for continuous slider ticks while dragging.
     * Default: PRIMITIVE_LOW_TICK (ultra-crisp active-braked hardware tick)
     */
    var sliderTickMode by mutableStateOf(SliderTickMode.PRIMITIVE_LOW_TICK)

    /**
     * Actuator Power / Scale for the threshold POP [0.1f - 1.0f] (default 1.0 = 100% full punch).
     */
    var popScale by mutableFloatStateOf(1.0f)

    /**
     * Actuator Power / Scale for continuous swipe ticks [0.1f - 1.0f] (default 0.20f = 20% whisper-light detent).
     */
    var swipeTickScale by mutableFloatStateOf(0.20f)

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
        thresholdFraction = 0.35f
        tickIntervalDp = 2.dp
        hapticsEnabled = true
        sliderTicksEnabled = true
        clockBackHapticsEnabled = true
        popMode = PopHapticMode.SPRING_SNAP
        sliderTickMode = SliderTickMode.PRIMITIVE_LOW_TICK
        popScale = 1.0f
        swipeTickScale = 0.20f
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

        val flags = if (ignoreViewSetting) {
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        } else 0

        val scale = popScale.coerceIn(0.1f, 1.0f)

        when (popMode) {
            PopHapticMode.SPRING_SNAP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, (0.7f * scale).coerceIn(0.1f, 1.0f))
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, scale, 16)
                        .compose()
                    vibrateEffect(context, effect)
                } else {
                    vibratePredefined(context, VibrationEffect.EFFECT_HEAVY_CLICK)
                }
            }
            PopHapticMode.PRIMITIVE_THUD -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, scale)
                        .compose()
                    vibrateEffect(context, effect)
                } else {
                    vibratePredefined(context, VibrationEffect.EFFECT_HEAVY_CLICK)
                }
            }
            PopHapticMode.LINEAGE_COMPOUND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, scale)
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, scale, 52)
                        .compose()
                    vibrateEffect(context, effect)
                } else {
                    vibratePredefined(context, VibrationEffect.EFFECT_HEAVY_CLICK)
                }
            }
            PopHapticMode.PRIMITIVE_CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, scale)
                        .compose()
                    vibrateEffect(context, effect)
                } else {
                    vibratePredefined(context, VibrationEffect.EFFECT_CLICK)
                }
            }
            PopHapticMode.DOUBLE_PRIMITIVE_CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, scale)
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, scale, 40)
                        .compose()
                    vibrateEffect(context, effect)
                } else {
                    vibratePredefined(context, VibrationEffect.EFFECT_DOUBLE_CLICK)
                }
            }
            PopHapticMode.EFFECT_HEAVY_CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibratePredefined(context, VibrationEffect.EFFECT_HEAVY_CLICK)
                } else if (view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.LONG_PRESS, flags)
                }
            }
            PopHapticMode.EFFECT_POP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibratePredefined(context, 4 /* EFFECT_POP */)
                } else if (view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.KEYBOARD_TAP, flags)
                }
            }
            PopHapticMode.STRONG_POP -> {
                val amp = (popVibrationAmplitude * scale).toInt().coerceIn(1, 255)
                vibrateActuator(context, popVibrationDurationMs, amp)
            }
            PopHapticMode.DOUBLE_PULSE -> {
                val amp = (popVibrationAmplitude * scale).toInt().coerceIn(1, 255)
                val timings = longArrayOf(0, 20, 25, 25)
                val amplitudes = intArrayOf(0, amp, 0, amp)
                vibrateWaveform(context, timings, amplitudes)
            }
            PopHapticMode.GESTURE_THRESHOLD -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE, flags)
                } else if (view != null) {
                    performViewHaptic(view, 23, flags)
                }
            }
            PopHapticMode.CONFIRM -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.CONFIRM, flags)
                } else if (view != null) {
                    performViewHaptic(view, 12, flags)
                }
            }
            PopHapticMode.REJECT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.REJECT, flags)
                } else if (view != null) {
                    performViewHaptic(view, 13, flags)
                }
            }
            PopHapticMode.LONG_PRESS -> {
                if (view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.LONG_PRESS, flags)
                } else {
                    vibrateActuator(context, 60L, 220)
                }
            }
            PopHapticMode.NONE -> { /* Silent */ }
        }
    }

    /**
     * Triggers continuous slider tick haptic feedback while dragging.
     */
    fun performSliderTick(context: Context, view: View?) {
        if (!hapticsEnabled || !sliderTicksEnabled) return

        val flags = if (ignoreViewSetting) {
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        } else 0

        val scale = swipeTickScale.coerceIn(0.1f, 1.0f)

        when (sliderTickMode) {
            SliderTickMode.PRIMITIVE_LOW_TICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, scale)
                        .compose()
                    vibrateEffect(context, effect)
                } else if (view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.CLOCK_TICK, flags)
                }
            }
            SliderTickMode.PRIMITIVE_TICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, scale)
                        .compose()
                    vibrateEffect(context, effect)
                } else if (view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.CLOCK_TICK, flags)
                }
            }
            SliderTickMode.LINEAGE_SLIDER_TICK -> {
                if (view != null) {
                    // Constant 23 is GESTURE_THRESHOLD_ACTIVATE in Android 14+ (the exact call LineageOS SystemUI uses for slider ticks)
                    performViewHaptic(view, 23, flags)
                }
            }
            SliderTickMode.PRIMITIVE_SPIN -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, scale)
                        .compose()
                    vibrateEffect(context, effect)
                } else if (view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.CLOCK_TICK, flags)
                }
            }
            SliderTickMode.SEGMENT_TICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.SEGMENT_TICK, flags)
                } else if (view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.CLOCK_TICK, flags)
                }
            }
            SliderTickMode.SEGMENT_FREQUENT_TICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.SEGMENT_FREQUENT_TICK, flags)
                } else if (view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.CLOCK_TICK, flags)
                }
            }
            SliderTickMode.EFFECT_TICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibratePredefined(context, VibrationEffect.EFFECT_TICK)
                } else if (view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.CLOCK_TICK, flags)
                }
            }
            SliderTickMode.MICRO_PULSE -> {
                vibrateActuator(context, 10L, (120 * scale).toInt().coerceIn(10, 255))
            }
            SliderTickMode.CLOCK_TICK -> {
                if (view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.CLOCK_TICK, flags)
                }
            }
            SliderTickMode.NONE -> { /* Silent */ }
        }
    }

    /**
     * Triggers tactile feedback when dragging back under the threshold ("clocking back").
     */
    fun performClockBack(context: Context, view: View?) {
        if (!hapticsEnabled || !clockBackHapticsEnabled) return

        val flags = if (ignoreViewSetting) {
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        } else 0

        val scale = swipeTickScale.coerceIn(0.1f, 1.0f)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, (0.7f * scale).coerceIn(0.1f, 1.0f))
                .compose()
            vibrateEffect(context, effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && view != null) {
            performViewHaptic(view, HapticFeedbackConstants.GESTURE_THRESHOLD_DEACTIVATE, flags)
        } else if (view != null) {
            performViewHaptic(view, HapticFeedbackConstants.CLOCK_TICK, flags)
        } else {
            vibrateActuator(context, 12L, 100)
        }
    }

    private fun performViewHaptic(view: View, feedbackConstant: Int, flags: Int) {
        try {
            view.performHapticFeedback(feedbackConstant, flags)
        } catch (_: Throwable) {
            try {
                view.performHapticFeedback(feedbackConstant)
            } catch (_: Throwable) {}
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun vibrateEffect(context: Context, effect: VibrationEffect): Boolean {
        return try {
            val vibrator = getVibrator(context) ?: return false
            if (!vibrator.hasVibrator()) return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val attributes = VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_HARDWARE_FEEDBACK)
                    .build()
                vibrator.vibrate(effect, attributes)
            } else {
                vibrator.vibrate(effect)
            }
            true
        } catch (_: Throwable) {
            try {
                getVibrator(context)?.vibrate(effect)
                true
            } catch (_: Throwable) {
                false
            }
        }
    }

    private fun vibrateActuator(context: Context, durationMs: Long, amplitude: Int): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmplitude = amplitude.coerceIn(1, 255)
                val effect = VibrationEffect.createOneShot(durationMs, clampedAmplitude)
                vibrateEffect(context, effect)
            } else {
                @Suppress("DEPRECATION")
                getVibrator(context)?.vibrate(durationMs)
                true
            }
        } catch (_: Throwable) {
            false
        }
    }

    private fun vibrateWaveform(context: Context, timings: LongArray, amplitudes: IntArray): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrateEffect(context, effect)
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }

    private fun vibratePredefined(context: Context, effectId: Int): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(effectId)
                vibrateEffect(context, effect)
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }
}
