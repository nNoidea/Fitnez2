package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

@Composable
fun PredictiveConfirmationDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit
) {
    val view = LocalView.current
    PredictiveAlertDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        title = title,
        text = message,
        confirmButton = {
            Button(
                onClick = {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_START)
                    onConfirm()
                },
                colors = if (isDestructive) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(cancelLabel)
            }
        }
    )
}
