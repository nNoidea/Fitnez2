package com.nnoidea.fitnez2.ui.common

import android.content.Context
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nnoidea.fitnez2.ui.components.haptics.HapticEngine

/**
 * Single Source of Truth (SSOT) tokens and haptics for text and number input fields.
 *
 * Controls tactile response when gaining focus, validating input, or rejecting invalid values.
 */
object InputDefaults {

    var hapticsEnabled by mutableStateOf(true)

    /**
     * Subtle tactile tick when an input field gains focus.
     */
    fun onFocusHaptic(context: Context, view: View? = null) {
        if (!hapticsEnabled) return
        HapticEngine.tick(context, scale = 0.25f)
    }

    /**
     * Subtle affirmative click when a valid value is successfully committed.
     */
    fun onCommitHaptic(context: Context, view: View? = null) {
        if (!hapticsEnabled) return
        HapticEngine.click(context, scale = 0.50f)
    }

    /**
     * Haptic feedback when entered input fails validation or gets reverted.
     */
    fun onErrorHaptic(context: Context, view: View? = null) {
        if (!hapticsEnabled) return
        HapticEngine.reject(context, view)
    }
}
