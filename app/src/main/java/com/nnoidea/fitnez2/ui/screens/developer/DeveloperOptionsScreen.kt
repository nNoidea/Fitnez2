package com.nnoidea.fitnez2.ui.screens.developer

import com.nnoidea.fitnez2.ui.components.SettingsItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.nnoidea.fitnez2.ui.components.ScreenScaffold
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.components.dialog.LoadingDialog
import com.nnoidea.fitnez2.ui.components.dialog.PredictiveConfirmationDialog

@Composable
fun DeveloperOptionsScreen(onBack: () -> Unit) {
    var showColorPalette by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var showStressTestDialog by remember { mutableStateOf(false) }
    var isStressTestRunning by remember { mutableStateOf(false) }
    var stressTestProgressMessage by remember { mutableStateOf("") }
    var stressTestProgressValue by remember { mutableFloatStateOf(0f) }
    var showLoadingShowcase by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = globalLocalization.labelDeveloperOptions,
        onBack = onBack
    ) {
            HorizontalDivider()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Developer Settings
                SettingsItem(
                    label = globalLocalization.devColorPalette,
                    value = globalLocalization.devViewColors,
                    icon = Icons.Default.Palette,
                    onClick = { showColorPalette = true }
                )

                HorizontalDivider()

                HapticsTestSection()

                HorizontalDivider()

                // --- Database Section ---
                Text(
                    text = globalLocalization.devDatabase,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                SettingsItem(
                    label = globalLocalization.devRunStressTest,
                    value = globalLocalization.devStressTestDescription,
                    icon = Icons.Default.Storage,
                    onClick = { showStressTestDialog = true }
                )

                HorizontalDivider()

                SettingsItem(
                    label = "Loading Indicators",
                    value = "View all M3 loading styles",
                    icon = Icons.Default.Star,
                    onClick = { showLoadingShowcase = true }
                )
            }
    }

    if (showColorPalette) {
        ColorPaletteDialog(
            show = showColorPalette,
            onDismissRequest = { showColorPalette = false }
        )
    }

    if (showStressTestDialog) {
        PredictiveConfirmationDialog(
            show = showStressTestDialog,
            onDismissRequest = { showStressTestDialog = false },
            title = globalLocalization.devStressTestConfirmTitle,
            message = globalLocalization.devStressTestConfirmMessage,
            confirmLabel = globalLocalization.devWipeAndGenerate,
            isDestructive = true,
            onConfirm = {
                showStressTestDialog = false
                isStressTestRunning = true
                scope.launch {
                    try {
                        val db = com.nnoidea.fitnez2.data.AppDatabase.getDatabase(context, this)
                        com.nnoidea.fitnez2.data.StressTestManager.performStressTest(db) { progress, message ->
                            stressTestProgressValue = progress
                            stressTestProgressMessage = message
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isStressTestRunning = false
                        stressTestProgressMessage = ""
                        stressTestProgressValue = 0f
                    }
                }
            }
        )
    }

    LoadingDialog(
        show = isStressTestRunning,
        title = globalLocalization.devGeneratingData,
        progress = stressTestProgressValue.takeIf { it > 0f },
        message = stressTestProgressMessage.ifEmpty { null }
    )

    if (showLoadingShowcase) {
        LoadingShowcaseDialog(
            show = showLoadingShowcase,
            onDismiss = { showLoadingShowcase = false }
        )
    }
}


