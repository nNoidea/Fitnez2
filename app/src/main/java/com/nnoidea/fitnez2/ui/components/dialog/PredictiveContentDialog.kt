package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/**
 * Foundation dialog container for rich, scrollable, or list content (Level 2B).
 *
 * Enforces:
 * - Dynamic vertical height clamping bounded to [DialogDefaults.MaxContentHeightRatio] of the screen.
 * - Consistent header layout (Title + Optional supporting text).
 * - Scrollable viewport containment with single-padding ownership.
 * - Optional bottom action button row.
 */
@Composable
fun PredictiveContentDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    bodyText: String? = null,
    dismissOnClickOutside: Boolean = true,
    dismissOnBackPress: Boolean = true,
    scrollable: Boolean = true,
    buttons: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!show) return

    CorePredictiveDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        dismissOnClickOutside = dismissOnClickOutside,
        dismissOnBackPress = dismissOnBackPress,
        maxHeightRatio = DialogDefaults.MaxHeightRatio
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!title.isNullOrEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = if (bodyText != null) 8.dp else DialogDefaults.ContentSpacing)
                )
            }

            if (!bodyText.isNullOrEmpty()) {
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = DialogDefaults.ContentSpacing)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                if (scrollable) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        content = content
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        content = content
                    )
                }
            }

            if (buttons != null) {
                Spacer(modifier = Modifier.height(DialogDefaults.ContentSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DialogDefaults.ButtonSpacing, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    content = buttons
                )
            }
        }
    }
}
