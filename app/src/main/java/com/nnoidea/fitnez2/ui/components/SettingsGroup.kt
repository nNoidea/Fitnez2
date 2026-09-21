package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
        enabled: Boolean = true
    )

    /**
     * Adds custom composable content inside the group card.
     */
    fun custom(content: @Composable () -> Unit)
}

internal class SettingsGroupScopeImpl : SettingsGroupScope {
    val items = mutableListOf<@Composable () -> Unit>()

    override fun item(
        label: String,
        value: String,
        icon: ImageVector?,
        iconTint: Color?,
        showChevron: Boolean,
        trailingContent: (@Composable () -> Unit)?,
        onClick: () -> Unit
    ) {
        items.add {
            SettingsItem(
                label = label,
                value = value,
                icon = icon,
                iconTint = iconTint ?: MaterialTheme.colorScheme.onSurface,
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
        enabled: Boolean
    ) {
        items.add {
            SettingsItem(
                label = label,
                value = value,
                icon = icon,
                iconTint = iconTint ?: MaterialTheme.colorScheme.onSurface,
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

    override fun custom(content: @Composable () -> Unit) {
        items.add(content)
    }
}

/**
 * Material 3 Expressive grouped options container.
 *
 * Renders an optional section title followed by a [SettingsCardGroup],
 * automatically inserting [SettingsDivider] between adjacent items.
 */
@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    cornerRadius: Dp = 24.dp,
    content: SettingsGroupScope.() -> Unit
) {
    val scope = SettingsGroupScopeImpl().apply(content)

    Column(modifier = modifier.fillMaxWidth()) {
        if (!title.isNullOrEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp, top = 4.dp)
            )
        }

        SettingsCardGroup(
            containerColor = containerColor,
            cornerRadius = cornerRadius
        ) {
            scope.items.forEachIndexed { index, itemComposable ->
                if (index > 0) {
                    SettingsDivider()
                }
                itemComposable()
            }
        }
    }
}
