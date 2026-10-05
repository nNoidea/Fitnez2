package com.nnoidea.fitnez2.ui.common

import android.view.View
import com.nnoidea.fitnez2.ui.components.haptics.HapticEngine

/**
 * Single source of truth (SSOT) defaults and haptic tokens for input fields across Fitnez2.
 */
object InputDefaults {

    /**
     * Tactile feedback when an input field gains focus upon user tap.
     */
    fun performFocusHaptic(view: View? = null) {
        view?.let { HapticEngine.performClick(it.context, scale = 0.35f) }
    }

    /**
     * Tactile feedback when an input field successfully commits a valid number.
     */
    fun performCommitHaptic(view: View? = null) {
        HapticEngine.performConfirm(view)
    }

    /**
     * Tactile feedback when an input value fails validation or is rejected.
     */
    fun performErrorHaptic(view: View? = null) {
        HapticEngine.performReject(view)
    }
}
