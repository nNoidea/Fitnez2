package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization

/**
 * Shared scaffold for all screens. Provides:
 * - Status bar padding
 * - TopHeader with hamburger menu OR back button
 * - Flexible title content via slot API
 *
 * Usage:
 * ```
 * ScreenScaffold(title = "Settings", onOpenDrawer = { ... }) {
 *     // Screen content
 * }
 * ```
 *
 * Or with custom header content:
 * ```
 * ScreenScaffold(onBack = { ... }, headerContent = { Text("Custom") }) {
 *     // Screen content
 * }
 * ```
 */
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    onOpenDrawer: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    headerContent: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Header
        TopHeader {
            when {
                // Custom header content takes full priority
                headerContent != null -> headerContent()

                // Back button mode
                onBack != null -> {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = globalLocalization.labelBack
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                // Hamburger menu mode
                onOpenDrawer != null -> {
                    HamburgerMenu(onClick = onOpenDrawer)
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }

        content()
    }
}
