package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView

@Composable
fun PredictiveInputDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    label: String,
    initialValue: String = "",
    placeholder: String? = null,
    confirmLabel: String,
    cancelLabel: String = "Cancel",
    onConfirm: (String) -> Unit
) {
    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var text by remember { mutableStateOf(initialValue) }

    LaunchedEffect(show, initialValue) {
        if (show) {
            text = initialValue
        }
    }

    PredictiveAlertDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        title = title,
        confirmButton = {
            Button(
                onClick = {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_START)
                    keyboardController?.hide()
                    if (text.isNotBlank()) {
                        onConfirm(text)
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(cancelLabel)
            }
        }
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = if (placeholder != null) {
                { Text(placeholder) }
            } else null
        )
    }
}
