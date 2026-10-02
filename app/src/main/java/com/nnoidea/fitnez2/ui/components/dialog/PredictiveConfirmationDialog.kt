package com.nnoidea.fitnez2.ui.components.dialog

import android.view.HapticFeedbackConstants
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import com.nnoidea.fitnez2.core.localization.globalLocalization

/**
 * Standard confirmation dialog (Level 3A).
 *
 * Implements standard confirm/cancel flows with localized defaults and haptic feedback.
 */
@Composable
fun PredictiveConfirmationDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmLabel: String,
    modifier: Modifier = Modifier,
    cancelLabel: String = globalLocalization.labelCancel,
    isDestructive: Boolean = false,
    dismissOnClickOutside: Boolean = true,
    dismissOnBackPress: Boolean = true,
    onConfirm: () -> Unit
) {
    val view = LocalView.current
    PredictiveAlertDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        title = title,
        text = message,
        modifier = modifier,
        dismissOnClickOutside = dismissOnClickOutside,
        dismissOnBackPress = dismissOnBackPress,
        confirmButton = {
            Button(
                onClick = {
                    DialogDefaults.performConfirmHaptic(view)
                    onConfirm()
                },
                colors = if (isDestructive) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    DialogDefaults.performDismissHaptic(view)
                    onDismissRequest()
                }
            ) {
                Text(cancelLabel)
            }
        }
    )
}
