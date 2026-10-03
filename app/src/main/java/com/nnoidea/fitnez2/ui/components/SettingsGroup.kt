package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.dialog.RadioSelectionDialog

/**
 * DSL scope for building items inside a [SettingsGroup].
 */
interface SettingsGroupScope {
    /**
     * Adds a standard settings item (clickable row with label, value, and optional icon).
     */
    fun item(
        label: String,
        value: String = "",
        icon: ImageVector? = null,
        iconTint: Color? = null,
        iconContainerColor: Color? = null,
        showChevron: Boolean = false,
        trailingContent: (@Composable () -> Unit)? = null,
        onClick: () -> Unit
    )

    /**
     * Adds a settings toggle switch item.
     */
    fun switchItem(
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        value: String = "",
        icon: ImageVector? = null,
        iconTint: Color? = null,
        iconContainerColor: Color? = null,
        enabled: Boolean = true
    )

    /**
     * Adds a settings item that opens a radio selection dialog when clicked,
     * managing dialog presentation, selection, and dismiss state internally.
     */
    fun <T> radioItem(
        label: String,
        value: String = "",
        icon: ImageVector? = null,
        iconTint: Color? = null,
        iconContainerColor: Color? = null,
        dialogTitle: String = label,
        options: List<T>,
        selected: T,
        bodyText: String? = null,
        labelProvider: (T) -> String = { it?.toString() ?: "" },
        onSelected: (T) -> Unit
    )

    /**
     * Adds custom composable content inside the group card.
     */
    fun custom(content: @Composable () -> Unit)
}

internal class SettingsGroupScopeImpl(
    private val containerColor: Color
) : SettingsGroupScope {
    val items = mutableListOf<@Composable (shape: Shape) -> Unit>()

    override fun item(
        label: String,
        value: String,
        icon: ImageVector?,
        iconTint: Color?,
        iconContainerColor: Color?,
        showChevron: Boolean,
        trailingContent: (@Composable () -> Unit)?,
        onClick: () -> Unit
    ) {
        items.add { shape ->
            SettingsItem(
                label = label,
                value = value,
                icon = icon,
                iconTint = iconTint,
                iconContainerColor = iconContainerColor,
                shape = shape,
                containerColor = containerColor,
                showChevron = showChevron,
                trailingContent = trailingContent,
                onClick = onClick
            )
        }
    }

    override fun switchItem(
        label: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        value: String,
        icon: ImageVector?,
        iconTint: Color?,
        iconContainerColor: Color?,
        enabled: Boolean
    ) {
        items.add { shape ->
            SettingsItem(
                label = label,
                value = value,
                icon = icon,
                iconTint = iconTint,
                iconContainerColor = iconContainerColor,
                shape = shape,
                containerColor = containerColor,
                showChevron = false,
                trailingContent = {
                    Switch(
                        checked = checked,
                        onCheckedChange = onCheckedChange,
                        enabled = enabled
                    )
                },
                onClick = {
                    if (enabled) {
                        onCheckedChange(!checked)
                    }
                }
            )
        }
    }

    override fun <T> radioItem(
        label: String,
        value: String,
        icon: ImageVector?,
        iconTint: Color?,
        iconContainerColor: Color?,
        dialogTitle: String,
        options: List<T>,
        selected: T,
        bodyText: String?,
        labelProvider: (T) -> String,
        onSelected: (T) -> Unit
    ) {
        items.add { shape ->
            var showDialog by remember { mutableStateOf(false) }

            SettingsItem(
                label = label,
                value = value,
                icon = icon,
                iconTint = iconTint,
                iconContainerColor = iconContainerColor,
                shape = shape,
                containerColor = containerColor,
                onClick = { showDialog = true }
            )

            RadioSelectionDialog(
                show = showDialog,
                title = dialogTitle,
                options = options,
                selectedValue = selected,
                onValueSelected = {
                    onSelected(it)
                    showDialog = false
                },
                onDismissRequest = { showDialog = false },
                labelProvider = labelProvider,
                bodyText = bodyText
            )
        }
    }

    override fun custom(content: @Composable () -> Unit) {
        items.add { shape ->
            Surface(
                shape = shape,
                color = containerColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

/**
 * Single source of truth (SSOT) default tokens for Settings UI components.
 */
object SettingsDefaults {
    /** Spacing between individual options within a segmented settings group. */
    val ItemSpacing: Dp = 2.dp

    /** Spacing between distinct settings groups on a page. */
    val GroupSpacing: Dp = 16.dp

    /** Outer corner radius for top/bottom items and standalone items. */
    val CornerRadius: Dp = 24.dp

    /** Subtle inner corner radius for middle items. */
    val InnerCornerRadius: Dp = 4.dp

    /** Size of the circular badge container behind item icons. */
    val IconContainerSize: Dp = 40.dp

    /** Size of the icon inside the circular badge container. */
    val IconSize: Dp = 22.dp

    /** Standard horizontal padding inside each settings item row. */
    val ItemPaddingHorizontal: Dp = 16.dp

    /** Standard vertical padding inside each settings item row. */
    val ItemPaddingVertical: Dp = 14.dp
}

/**
 * Material 3 Expressive segmented options container.
 *
 * Implements native connected / segmented item styling:
 * - Each item is an individual surface shaped according to its position in the group:
 *   - Single item: fully rounded (outer corner radius)
 *   - Top item: rounded top corners, slightly rounded bottom corners
 *   - Middle items: slightly rounded corners (inner corner radius)
 *   - Bottom item: slightly rounded top corners, rounded bottom corners
 * - Items are separated by [SettingsDefaults.ItemSpacing] rather than flat hairline dividers.
 */
@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    cornerRadius: Dp = SettingsDefaults.CornerRadius,
    innerCornerRadius: Dp = SettingsDefaults.InnerCornerRadius,
    itemSpacing: Dp = SettingsDefaults.ItemSpacing,
    content: SettingsGroupScope.() -> Unit
) {
    val scope = SettingsGroupScopeImpl(containerColor).apply(content)

    Column(modifier = modifier.fillMaxWidth()) {
        if (!title.isNullOrEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 4.dp)
            )
        }

        val totalItems = scope.items.size
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            scope.items.forEachIndexed { index, itemComposable ->
                val shape = when {
                    totalItems == 1 -> RoundedCornerShape(cornerRadius)
                    index == 0 -> RoundedCornerShape(
                        topStart = cornerRadius,
                        topEnd = cornerRadius,
                        bottomStart = innerCornerRadius,
                        bottomEnd = innerCornerRadius
                    )
                    index == totalItems - 1 -> RoundedCornerShape(
                        topStart = innerCornerRadius,
                        topEnd = innerCornerRadius,
                        bottomStart = cornerRadius,
                        bottomEnd = cornerRadius
                    )
                    else -> RoundedCornerShape(innerCornerRadius)
                }
                itemComposable(shape)
            }
        }
    }
}
