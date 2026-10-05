package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
            DialogConfirmButton(
                label = confirmLabel,
                enabled = text.isNotBlank(),
                onConfirm = {
                    keyboardController?.hide()
                    if (text.isNotBlank()) {
                        onConfirm(text)
                    }
                }
            )
        },
        dismissButton = {
            DialogDismissButton(label = cancelLabel, onDismiss = onDismissRequest)
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
