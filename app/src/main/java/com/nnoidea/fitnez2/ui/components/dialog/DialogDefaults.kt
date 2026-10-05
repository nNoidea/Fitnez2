package com.nnoidea.fitnez2.ui.components.dialog

import android.view.View
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
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

    /** Haptic feedback on dialog cancel/dismiss action. */
    fun performDismissHaptic(view: View? = null) {
        view?.let { HapticEngine.performClick(it.context, scale = 0.35f) }
    }

    /** Haptic feedback on dialog item/chip selection. */
    fun performItemClickHaptic(view: View? = null) {
        view?.let { HapticEngine.performClick(it.context, scale = 0.5f) }
    }
}

/**
 * The confirm/cancel button pair shared by [PredictiveConfirmationDialog] and
 * [PredictiveInputDialog]. Lives in this file so the haptic on an affirmative
 * tap is defined once instead of in every dialog that has an OK button.
 */
@Composable
internal fun DialogConfirmButton(
    label: String,
    enabled: Boolean = true,
    destructive: Boolean = false,
    onConfirm: () -> Unit
) {
    val view = LocalView.current
    Button(
        onClick = {
            DialogDefaults.performConfirmHaptic(view)
            onConfirm()
        },
        enabled = enabled,
        colors = if (destructive) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        } else {
            ButtonDefaults.buttonColors()
        }
    ) {
        Text(label)
    }
}

@Composable
internal fun DialogDismissButton(label: String, onDismiss: () -> Unit) {
    val view = LocalView.current
    TextButton(
        onClick = {
            DialogDefaults.performDismissHaptic(view)
            onDismiss()
        }
    ) {
        Text(label)
    }
}
