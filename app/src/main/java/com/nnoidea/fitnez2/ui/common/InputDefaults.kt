package com.nnoidea.fitnez2.ui.common

import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nnoidea.fitnez2.ui.components.haptics.HapticEngine

/**
 * Single source of truth (SSOT) defaults and haptic tokens for input fields across Fitnez2.
 */
object InputDefaults {

    var hapticsEnabled by mutableStateOf(true)

    /**
     * Tactile feedback when an input field gains focus upon user tap.
     */
    fun performFocusHaptic(view: View? = null) {
        if (!hapticsEnabled) return
        view?.let { HapticEngine.performClick(it.context, scale = 0.35f) }
    }

    fun onFocusHaptic(context: Context? = null, view: View? = null) {
        if (!hapticsEnabled) return
        val ctx = view?.context ?: context
        ctx?.let { HapticEngine.performClick(it, scale = 0.35f) }
    }

    /**
     * Tactile feedback when an input field successfully commits a valid number.
     */
    fun performCommitHaptic(view: View? = null) {
        if (!hapticsEnabled) return
        HapticEngine.performConfirm(view)
    }

    fun onCommitHaptic(context: Context? = null, view: View? = null) {
        if (!hapticsEnabled) return
        if (view != null) {
            HapticEngine.performConfirm(view)
        } else if (context != null) {
            HapticEngine.performClick(context, scale = 0.7f)
        }
    }

    /**
     * Tactile feedback when an input value fails validation or is rejected.
     */
    fun performErrorHaptic(view: View? = null) {
        if (!hapticsEnabled) return
        HapticEngine.performReject(view)
    }

    fun onErrorHaptic(context: Context? = null, view: View? = null) {
        if (!hapticsEnabled) return
        if (view != null) {
            HapticEngine.performReject(view)
        } else if (context != null) {
            HapticEngine.reject(context)
        }
    }

    /**
     * Tactile feedback when clearing an input field.
     */
    fun performClearHaptic(view: View? = null) {
        if (!hapticsEnabled) return
        view?.let { HapticEngine.performClick(it.context, scale = 0.25f) }
    }
}
