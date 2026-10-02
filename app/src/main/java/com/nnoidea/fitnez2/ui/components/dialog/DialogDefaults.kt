package com.nnoidea.fitnez2.ui.components.dialog

import android.content.Context
import android.os.VibrationEffect
import android.view.View
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.haptics.HapticEngine

/**
 * Single source of truth (SSOT) tokens for the Fitnez2 dialog subsystem.
 *
 * Implements Material 3 Expressive dialog specifications.
 */
object DialogDefaults {
    /** Maximum width of any dialog on tablets, landscape, or large windows. */
    val MaxWidth: Dp = 560.dp

    /** Horizontal margin between the dialog edges and the screen boundaries on mobile displays. */
    val ScreenMarginHorizontal: Dp = 24.dp

    /** Vertical margin between the dialog edges and system bars (status bar / nav bar). */
    val ScreenMarginVertical: Dp = 24.dp

    /** Maximum height percentage of the window that any dialog may occupy (70%). */
    const val MaxHeightRatio: Float = 0.70f

    /** Backward-compatible alias for [MaxHeightRatio]. */
    const val MaxContentHeightRatio: Float = MaxHeightRatio

    /** Outer corner radius for standard dialog containers (28dp). */
    val CornerRadius: Dp = 28.dp

    /** Shape applied to the dialog container surface. */
    val Shape: Shape = RoundedCornerShape(CornerRadius)

    /** Standard inner content padding applied to dialogs. */
    val ContentPadding: Dp = 24.dp

    /** Spacing between vertical content elements (e.g. title to body, body to input). */
    val ContentSpacing: Dp = 16.dp

    /** Spacing between horizontal action buttons in the dialog footer. */
    val ButtonSpacing: Dp = 8.dp

    /** Default tonal elevation of the dialog container surface. */
    val Elevation: Dp = 6.dp

    /** Default background container color for dialogs. */
    val ContainerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surfaceContainerHigh

    /** Default scrim color and opacity (32% dim) overlaying the background content. */
    val ScrimColor: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)

    /** Scale reduction factor during predictive back gesture. */
    const val PredictiveScaleFactor: Float = 0.20f

    // ── Haptic Feedback Tokens ──────────────────────────────────────────

    /** Haptic feedback on dialog affirmative/confirm action. */
    fun performConfirmHaptic(view: View? = null) {
        HapticEngine.performConfirm(view)
    }

    fun onConfirmHaptic(context: Context? = null, view: View? = null) {
        if (view != null) {
            HapticEngine.performConfirm(view)
        } else if (context != null) {
            HapticEngine.performClick(context, scale = 0.7f)
        }
    }

    /** Haptic feedback on dialog cancel/dismiss action. */
    fun performDismissHaptic(view: View? = null) {
        view?.let { HapticEngine.performClick(it.context, scale = 0.35f) }
    }

    fun onDismissHaptic(context: Context? = null, view: View? = null) {
        val ctx = view?.context ?: context
        ctx?.let { HapticEngine.performClick(it, scale = 0.35f) }
    }

    /** Haptic feedback on dialog item/chip selection. */
    fun performItemClickHaptic(view: View? = null) {
        view?.let { HapticEngine.performClick(it.context, scale = 0.5f) }
    }

    fun onItemSelectHaptic(context: Context? = null, view: View? = null) {
        val ctx = view?.context ?: context
        ctx?.let { HapticEngine.performClick(it, scale = 0.5f) }
    }

    /** Haptic feedback on destructive dialog action. */
    fun onDestructiveHaptic(context: Context? = null, view: View? = null) {
        val ctx = view?.context ?: context
        ctx?.let { HapticEngine.playPrimitive(it, VibrationEffect.Composition.PRIMITIVE_THUD, 0.8f) }
    }
}
