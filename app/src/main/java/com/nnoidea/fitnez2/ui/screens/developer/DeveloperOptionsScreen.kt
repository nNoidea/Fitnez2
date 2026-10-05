package com.nnoidea.fitnez2.ui.screens.developer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.components.dialog.LoadingDialog
import com.nnoidea.fitnez2.ui.components.dialog.PredictiveConfirmationDialog

import com.nnoidea.fitnez2.ui.components.SettingsPageScaffold
import com.nnoidea.fitnez2.ui.components.SettingsGroup

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

    val colorScheme = MaterialTheme.colorScheme
    val primaryContainer = colorScheme.primaryContainer
    val onPrimaryContainer = colorScheme.onPrimaryContainer
    val errorContainer = colorScheme.errorContainer
    val onErrorContainer = colorScheme.onErrorContainer
    val tertiaryContainer = colorScheme.tertiaryContainer
    val onTertiaryContainer = colorScheme.onTertiaryContainer

    SettingsPageScaffold(
        title = globalLocalization.labelDeveloperOptions,
        onBack = onBack
    ) {
        // Theme & UI Group
        SettingsGroup {
            item(
                label = globalLocalization.devColorPalette,
                value = globalLocalization.devViewColors,
                icon = Icons.Default.Palette,
                iconContainerColor = primaryContainer,
                iconTint = onPrimaryContainer,
                onClick = { showColorPalette = true }
            )
        }

        // Swipe Haptics Studio Group
        SettingsGroup(title = "Swipe Haptics Studio") {
            radioItem(
                label = "Threshold POP Vibration",
                value = com.nnoidea.fitnez2.ui.components.SwipeHapticsConfig.popMode.displayName,
                icon = Icons.Default.Star,
                iconContainerColor = tertiaryContainer,
                iconTint = onTertiaryContainer,
                options = com.nnoidea.fitnez2.ui.components.PopHapticMode.entries,
                selected = com.nnoidea.fitnez2.ui.components.SwipeHapticsConfig.popMode,
                onSelected = { com.nnoidea.fitnez2.ui.components.SwipeHapticsConfig.popMode = it },
                labelProvider = { it.displayName },
                bodyText = "Select the sensation fired when swiping crosses the delete threshold."
            )

            radioItem(
                label = "Drag Slider Ticks",
                value = com.nnoidea.fitnez2.ui.components.SwipeHapticsConfig.sliderTickMode.displayName,
                icon = Icons.Default.Palette,
                iconContainerColor = primaryContainer,
                iconTint = onPrimaryContainer,
                options = com.nnoidea.fitnez2.ui.components.SliderTickMode.entries,
                selected = com.nnoidea.fitnez2.ui.components.SwipeHapticsConfig.sliderTickMode,
                onSelected = { com.nnoidea.fitnez2.ui.components.SwipeHapticsConfig.sliderTickMode = it },
                labelProvider = { it.displayName },
                bodyText = "Select the tactile vibration ticks felt continuously while dragging horizontally."
            )

            switchItem(
                label = "Continuous Drag Ticks",
                checked = com.nnoidea.fitnez2.ui.components.SwipeHapticsConfig.sliderTicksEnabled,
                onCheckedChange = { com.nnoidea.fitnez2.ui.components.SwipeHapticsConfig.sliderTicksEnabled = it }
            )

            switchItem(
                label = "Clock-Back Reset Haptic",
                checked = com.nnoidea.fitnez2.ui.components.SwipeHapticsConfig.clockBackHapticsEnabled,
                onCheckedChange = { com.nnoidea.fitnez2.ui.components.SwipeHapticsConfig.clockBackHapticsEnabled = it }
            )

            custom {
                SwipeHapticsLabControls()
            }
        }

        // Generic System Haptics Step Slider
        SettingsGroup(title = globalLocalization.devHapticsTest) {
            custom {
                SystemHapticsStepSlider()
            }
        }

        // Database & Diagnostics Group
        SettingsGroup(title = globalLocalization.devDatabase) {
            item(
                label = globalLocalization.devRunStressTest,
                value = globalLocalization.devStressTestDescription,
                icon = Icons.Default.Storage,
                iconContainerColor = errorContainer,
                iconTint = onErrorContainer,
                onClick = { showStressTestDialog = true }
            )
            item(
                label = globalLocalization.devLoadingIndicators,
                value = globalLocalization.devLoadingIndicatorsDescription,
                icon = Icons.Default.Star,
                iconContainerColor = tertiaryContainer,
                iconTint = onTertiaryContainer,
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


