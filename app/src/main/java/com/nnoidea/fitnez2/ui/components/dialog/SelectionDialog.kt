package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization

/**
 * Standard selection dialog with vertical scroll containment (Level 3C).
 */
@Composable
fun SelectionDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    bodyText: String? = null,
    buttons: @Composable (BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!show) return

    CorePredictiveDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = if (bodyText != null) 8.dp else DialogDefaults.ContentSpacing)
                )
            }

            if (bodyText != null) {
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = DialogDefaults.ContentSpacing),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    content = content
                )
            }

            if (buttons != null) {
                Spacer(modifier = Modifier.height(DialogDefaults.ContentSpacing))
                Box(modifier = Modifier.fillMaxWidth()) {
                    buttons()
                }
            }
        }
    }
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
                onClick = onDismissRequest,
                modifier = Modifier.align(Alignment.CenterEnd)
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
