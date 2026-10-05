package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    PredictiveAlertDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        title = title,
        text = message,
        modifier = modifier,
        dismissOnClickOutside = dismissOnClickOutside,
        dismissOnBackPress = dismissOnBackPress,
        confirmButton = {
            DialogConfirmButton(label = confirmLabel, destructive = isDestructive, onConfirm = onConfirm)
        },
        dismissButton = {
            DialogDismissButton(label = cancelLabel, onDismiss = onDismissRequest)
        }
    )
}
