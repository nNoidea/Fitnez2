package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Standard Material 3 Alert Dialog (Level 2A).
 *
 * Provides a structured layout:
 * - Title ([MaterialTheme.typography.headlineSmall])
 * - Supporting text ([MaterialTheme.typography.bodyLarge])
 * - Custom content body slot
 * - Flexible action buttons:
 *   - Standard 2-button: [confirmButton] + optional [dismissButton]
 *   - 3-button: [neutralButton] (e.g. Discard) aligned to start, [dismissButton] + [confirmButton] aligned to end
 *   - Custom row: [buttons] slot for complete button row control
 */
@Composable
fun PredictiveAlertDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    neutralButton: (@Composable () -> Unit)? = null,
    buttons: (@Composable RowScope.() -> Unit)? = null,
    dismissOnClickOutside: Boolean = true,
    dismissOnBackPress: Boolean = true,
    content: (@Composable () -> Unit)? = null
) {
    CorePredictiveDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        dismissOnClickOutside = dismissOnClickOutside,
        dismissOnBackPress = dismissOnBackPress
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DialogDefaults.ContentSpacing)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )

            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            content?.invoke()

            // Footer action buttons
            if (buttons != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DialogDefaults.ButtonSpacing, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    content = buttons
                )
            } else if (confirmButton != null || dismissButton != null || neutralButton != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (neutralButton != null) {
                        neutralButton()
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (dismissButton != null) {
                        dismissButton()
                        Spacer(modifier = Modifier.width(DialogDefaults.ButtonSpacing))
                    }

                    confirmButton?.invoke()
                }
            }
        }
    }
}
