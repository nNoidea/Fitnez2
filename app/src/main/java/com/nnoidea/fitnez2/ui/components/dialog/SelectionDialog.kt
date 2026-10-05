package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization

/**
 * Standard selection dialog with vertical scroll containment (Level 3C).
 *
 * Thin wrapper over [PredictiveContentDialog] — kept as a name because two call
 * sites want "a scrollable dialog with a Cancel button" and neither should care
 * which container implements it.
 */
@Composable
fun SelectionDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    bodyText: String? = null,
    buttons: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    PredictiveContentDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = title,
        bodyText = bodyText,
        buttons = buttons,
        content = content
    )
}

/**
 * A dialog displaying a vertically scrollable list of single-choice radio options.
 */
@Composable
fun <T> RadioSelectionDialog(
    show: Boolean,
    title: String,
    options: List<T>,
    selectedValue: T,
    onValueSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    labelProvider: (T) -> String,
    bodyText: String? = null
) {
    SelectionDialog(
        show = show,
        title = title,
        onDismissRequest = onDismissRequest,
        bodyText = bodyText,
        buttons = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(globalLocalization.labelCancel)
            }
        }
    ) {
        options.forEach { option ->
            RadioOption(
                text = labelProvider(option),
                selected = option == selectedValue,
                onClick = { onValueSelected(option) }
            )
        }
    }
}

@Composable
private fun RadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
