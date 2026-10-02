package com.nnoidea.fitnez2.ui.components.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Modern Android 12+ (API 31+) hardware actuator engine.
 *
 * Directly interfaces with the device's [VibratorManager] and LRA motor using
 * rich composition primitives:
 * - [VibrationEffect.Composition.PRIMITIVE_LOW_TICK] (ultra-crisp slider detent)
 * - [VibrationEffect.Composition.PRIMITIVE_CLICK] (discrete mechanical click)
 * - [VibrationEffect.Composition.PRIMITIVE_QUICK_RISE] (spring-tension swell)
 * - [VibrationEffect.Composition.PRIMITIVE_THUD] (deep physical thump)
 * - [VibrationEffect.Composition.PRIMITIVE_SPIN] (rotational detent)
 *
 * Targets API 31+ directly with zero legacy Android 8–10 fallback ladders.
 */
object HapticEngine {

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Sends a rich [VibrationEffect] directly to the LRA actuator using hardware feedback usage.
     */
    fun vibrateEffect(context: Context, effect: VibrationEffect): Boolean {
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
            false
        }
    }

    /**
     * Plays a discrete LRA primitive at a scaled intensity.
     */
    fun playPrimitive(context: Context, primitiveId: Int, scale: Float): Boolean {
        val clampedScale = scale.coerceIn(0.1f, 1.0f)
        val effect = VibrationEffect.startComposition()
            .addPrimitive(primitiveId, clampedScale)
            .compose()
        return vibrateEffect(context, effect)
    }

    /**
     * Plays a crisp, light hardware click for button/card taps.
     */
    fun performClick(context: Context, scale: Float = 0.5f) {
        playPrimitive(context, VibrationEffect.Composition.PRIMITIVE_CLICK, scale)
    }

    fun click(context: Context, scale: Float = 0.5f) = performClick(context, scale)

    /**
     * Plays a subtle hardware low-tick for slider / drag notch movements.
     */
    fun performSliderTick(context: Context, scale: Float = HapticDefaults.SwipeTickScale) {
        playPrimitive(context, VibrationEffect.Composition.PRIMITIVE_LOW_TICK, scale)
    }

    fun tick(context: Context, scale: Float = HapticDefaults.SwipeTickScale) = performSliderTick(context, scale)

    /**
     * Plays a two-stage Spring Snap (quick rise swell + tactile click) for threshold crossings.
     */
    fun performSpringSnap(context: Context, scale: Float = HapticDefaults.PopScale) {
        val clampedScale = scale.coerceIn(0.1f, 1.0f)
        val effect = VibrationEffect.startComposition()
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, (0.7f * clampedScale).coerceIn(0.1f, 1.0f))
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, clampedScale, 16)
            .compose()
        vibrateEffect(context, effect)
    }

    fun springSnap(context: Context, scale: Float = HapticDefaults.PopScale) = performSpringSnap(context, scale)

    /**
     * Plays a subtle detent release when pulling back under a threshold.
     */
    fun performClockBack(context: Context, scale: Float = HapticDefaults.SwipeTickScale) {
        val clampedScale = scale.coerceIn(0.1f, 1.0f)
        val effect = VibrationEffect.startComposition()
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, (0.7f * clampedScale).coerceIn(0.1f, 1.0f))
            .compose()
        vibrateEffect(context, effect)
    }

    fun clockBack(context: Context, scale: Float = HapticDefaults.SwipeTickScale) = performClockBack(context, scale)

    /**
     * Standard view-based affirmative confirmation (e.g. dialog confirm, item commit).
     */
    fun performConfirm(view: View?) {
        view?.let {
            performViewHaptic(it, HapticFeedbackConstants.CONFIRM, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
        }
    }

    fun confirm(context: Context? = null, view: View? = null) {
        if (view != null) {
            performConfirm(view)
        } else if (context != null) {
            performClick(context, 0.7f)
        }
    }

    /**
     * Standard view-based error or reject feedback (e.g. validation error, illegal action).
     */
    fun performReject(view: View?) {
        view?.let {
            performViewHaptic(it, HapticFeedbackConstants.REJECT, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
        }
    }

    fun reject(context: Context? = null, view: View? = null) {
        if (view != null) {
            performReject(view)
        } else if (context != null) {
            playPrimitive(context, VibrationEffect.Composition.PRIMITIVE_THUD, 0.8f)
        }
    }

    /**
     * Standard view-based gesture start / touch down feedback.
     */
    fun performGestureStart(view: View?) {
        view?.let {
            performViewHaptic(it, HapticFeedbackConstants.GESTURE_START, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
        }
    }

    /**
     * Full execution dispatcher for configured [PopHapticMode] threshold POPs.
     */
    fun executePopMode(
        context: Context,
        view: View?,
        mode: PopHapticMode,
        scale: Float,
        durationMs: Long = 50L,
        amplitude: Int = 255,
        ignoreViewSetting: Boolean = true
    ) {
        val flags = if (ignoreViewSetting) HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING else 0
        val clampedScale = scale.coerceIn(0.1f, 1.0f)

        when (mode) {
            PopHapticMode.SPRING_SNAP -> performSpringSnap(context, clampedScale)
            PopHapticMode.PRIMITIVE_THUD -> playPrimitive(context, VibrationEffect.Composition.PRIMITIVE_THUD, clampedScale)
            PopHapticMode.LINEAGE_COMPOUND -> {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, clampedScale)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, clampedScale, 52)
                    .compose()
                vibrateEffect(context, effect)
            }
            PopHapticMode.PRIMITIVE_CLICK -> playPrimitive(context, VibrationEffect.Composition.PRIMITIVE_CLICK, clampedScale)
            PopHapticMode.DOUBLE_PRIMITIVE_CLICK -> {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, clampedScale)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, clampedScale, 40)
                    .compose()
                vibrateEffect(context, effect)
            }
            PopHapticMode.EFFECT_HEAVY_CLICK -> {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                vibrateEffect(context, effect)
            }
            PopHapticMode.EFFECT_POP -> {
                val effect = VibrationEffect.createPredefined(4 /* EFFECT_POP */)
                vibrateEffect(context, effect)
            }
            PopHapticMode.STRONG_POP -> {
                val amp = (amplitude * clampedScale).toInt().coerceIn(1, 255)
                val effect = VibrationEffect.createOneShot(durationMs, amp)
                vibrateEffect(context, effect)
            }
            PopHapticMode.DOUBLE_PULSE -> {
                val amp = (amplitude * clampedScale).toInt().coerceIn(1, 255)
                val timings = longArrayOf(0, 20, 25, 25)
                val amplitudes = intArrayOf(0, amp, 0, amp)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrateEffect(context, effect)
            }
            PopHapticMode.GESTURE_THRESHOLD -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && view != null) {
                    performViewHaptic(view, HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE, flags)
                } else if (view != null) {
                    performViewHaptic(view, 23, flags)
                }
            }
            PopHapticMode.CONFIRM -> {
                if (view != null) performViewHaptic(view, HapticFeedbackConstants.CONFIRM, flags)
            }
            PopHapticMode.REJECT -> {
                if (view != null) performViewHaptic(view, HapticFeedbackConstants.REJECT, flags)
            }
            PopHapticMode.LONG_PRESS -> {
                if (view != null) performViewHaptic(view, HapticFeedbackConstants.LONG_PRESS, flags)
            }
            PopHapticMode.NONE -> { /* Silent */ }
        }
    }

    /**
     * Full execution dispatcher for configured [SliderTickMode] continuous ticks.
     */
    fun executeSliderTickMode(
        context: Context,
        view: View?,
        mode: SliderTickMode,
        scale: Float,
        ignoreViewSetting: Boolean = true
    ) {
        val flags = if (ignoreViewSetting) HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING else 0
        val clampedScale = scale.coerceIn(0.1f, 1.0f)

        when (mode) {
            SliderTickMode.PRIMITIVE_LOW_TICK -> playPrimitive(context, VibrationEffect.Composition.PRIMITIVE_LOW_TICK, clampedScale)
            SliderTickMode.PRIMITIVE_TICK -> playPrimitive(context, VibrationEffect.Composition.PRIMITIVE_TICK, clampedScale)
            SliderTickMode.LINEAGE_SLIDER_TICK -> {
                if (view != null) performViewHaptic(view, 23, flags)
            }
            SliderTickMode.PRIMITIVE_SPIN -> playPrimitive(context, VibrationEffect.Composition.PRIMITIVE_SPIN, clampedScale)
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
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                vibrateEffect(context, effect)
            }
            SliderTickMode.MICRO_PULSE -> {
                val amp = (120 * clampedScale).toInt().coerceIn(10, 255)
                val effect = VibrationEffect.createOneShot(10L, amp)
                vibrateEffect(context, effect)
            }
            SliderTickMode.CLOCK_TICK -> {
                if (view != null) performViewHaptic(view, HapticFeedbackConstants.CLOCK_TICK, flags)
            }
            SliderTickMode.NONE -> { /* Silent */ }
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
}
