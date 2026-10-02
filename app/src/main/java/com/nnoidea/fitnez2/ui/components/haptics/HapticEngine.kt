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
 * Modern Android Single Source of Truth (SSOT) Hardware Haptic Engine.
 *
 * Targets Android 12+ (minSdk 31) directly without legacy fallback bloat.
 * Uses native [VibratorManager], LRA [VibrationEffect.Composition] primitives,
 * and active DSP hardware braking.
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
     * Vibrates a composition effect using [VibrationAttributes.USAGE_HARDWARE_FEEDBACK].
     */
    fun playEffect(context: Context, effect: VibrationEffect): Boolean {
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
     * Plays a single modern hardware composition primitive (e.g. LOW_TICK, CLICK, SPIN, THUD).
     */
    fun playPrimitive(context: Context, primitiveId: Int, scale: Float = 1.0f, delayMs: Int = 0): Boolean {
        val clampedScale = scale.coerceIn(0.01f, 1.0f)
        val effect = VibrationEffect.startComposition()
            .addPrimitive(primitiveId, clampedScale, delayMs)
            .compose()
        return playEffect(context, effect)
    }

    /**
     * High-frequency micro-tick for continuous slider/sheet drags.
     * Uses active-braked [VibrationEffect.Composition.PRIMITIVE_LOW_TICK].
     */
    fun tick(context: Context, scale: Float = 0.20f) {
        playPrimitive(context, VibrationEffect.Composition.PRIMITIVE_LOW_TICK, scale)
    }

    /**
     * Crisp, distinct hardware click for card / button / item selections.
     * Uses [VibrationEffect.Composition.PRIMITIVE_CLICK].
     */
    fun click(context: Context, scale: Float = 0.60f) {
        playPrimitive(context, VibrationEffect.Composition.PRIMITIVE_CLICK, scale)
    }

    /**
     * High-contrast mechanical threshold POP.
     * Uses Quick Rise buildup followed immediately by a sharp Click.
     */
    fun springSnap(context: Context, scale: Float = 1.0f) {
        val clampedScale = scale.coerceIn(0.1f, 1.0f)
        val effect = VibrationEffect.startComposition()
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, (0.7f * clampedScale).coerceIn(0.1f, 1.0f))
            .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, clampedScale, 16)
            .compose()
        playEffect(context, effect)
    }

    /**
     * Subtle deactivation detent when dragging back under a threshold.
     */
    fun clockBack(context: Context, scale: Float = 0.20f) {
        val clampedScale = (scale * 0.7f).coerceIn(0.05f, 1.0f)
        playPrimitive(context, VibrationEffect.Composition.PRIMITIVE_LOW_TICK, clampedScale)
    }

    /**
     * Affirmative confirmation feedback (e.g. dialog confirm, item added).
     */
    fun confirm(context: Context, view: View? = null) {
        if (view != null) {
            performViewHaptic(view, HapticFeedbackConstants.CONFIRM)
        } else {
            click(context, 0.85f)
        }
    }

    /**
     * Error or rejection feedback (e.g. invalid input, rejected action).
     */
    fun reject(context: Context, view: View? = null) {
        if (view != null) {
            performViewHaptic(view, HapticFeedbackConstants.REJECT)
        } else {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.5f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.7f, 40)
                .compose()
            playEffect(context, effect)
        }
    }

    /**
     * Standard view-based haptic with FLAG_IGNORE_VIEW_SETTING for guaranteed delivery.
     */
    fun performViewHaptic(view: View, feedbackConstant: Int) {
        try {
            view.performHapticFeedback(feedbackConstant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
        } catch (_: Throwable) {
            try {
                view.performHapticFeedback(feedbackConstant)
            } catch (_: Throwable) {}
        }
    }
}
