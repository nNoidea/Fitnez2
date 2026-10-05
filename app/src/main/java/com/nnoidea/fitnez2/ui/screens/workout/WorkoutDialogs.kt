package com.nnoidea.fitnez2.ui.screens.workout

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.components.dialog.PredictiveAlertDialog

@Composable
fun NoNameDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onDiscard: () -> Unit
) {
    PredictiveAlertDialog(
        show = show,
        onDismissRequest = onDismiss,
        title = globalLocalization.titleNoName,
        text = globalLocalization.msgNoName,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(globalLocalization.labelEditAction)
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text(globalLocalization.labelDiscard, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
fun UnsavedWorkDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit
) {
    PredictiveAlertDialog(
        show = show,
        onDismissRequest = onDismiss,
        title = globalLocalization.titleUnsavedWork,
        text = globalLocalization.msgUnsavedWork,
        neutralButton = {
            TextButton(onClick = onDiscard) {
                Text(globalLocalization.labelDiscard, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(globalLocalization.labelEditAction)
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                )
            ) {
                Text(globalLocalization.labelSave)
            }
        }
    )
}
