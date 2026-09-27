package com.nnoidea.fitnez2.ui.components.dialog

import android.view.HapticFeedbackConstants
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
import com.nnoidea.fitnez2.core.localization.globalLocalization

/**
 * Standard single-input dialog (Level 3B).
 *
 * Implements a single text field input dialog with IME handling, validation, and localized defaults.
 */
@Composable
fun PredictiveInputDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    label: String,
    modifier: Modifier = Modifier,
    initialValue: String = "",
    placeholder: String? = null,
    confirmLabel: String = globalLocalization.labelSave,
    cancelLabel: String = globalLocalization.labelCancel,
    dismissOnClickOutside: Boolean = true,
    dismissOnBackPress: Boolean = true,
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
        modifier = modifier,
        dismissOnClickOutside = dismissOnClickOutside,
        dismissOnBackPress = dismissOnBackPress,
        confirmButton = {
            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
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
