package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standardized Material 3 Expressive scaffold for settings and options screens.
 *
 * Enforces standard edge padding (16dp), consistent spacing between groups (14dp),
 * and vertical scrolling, while integrating with [ScreenScaffold] for top bar navigation.
 */
@Composable
fun SettingsPageScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    groupSpacing: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    ScreenScaffold(
        title = title,
        onBack = onBack,
        onOpenDrawer = onOpenDrawer,
        actions = actions,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(groupSpacing),
            content = content
        )
    }
}
