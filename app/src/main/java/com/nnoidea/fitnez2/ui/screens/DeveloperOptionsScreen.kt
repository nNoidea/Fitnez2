package com.nnoidea.fitnez2.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.PredictiveModal
import com.nnoidea.fitnez2.ui.components.TopHeader
import com.nnoidea.fitnez2.core.localization.globalLocalization
import kotlin.math.roundToInt

@Composable
fun DeveloperOptionsScreen(onBack: () -> Unit) {
    var showColorPalette by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Header with Back Button
            TopHeader {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = globalLocalization.labelBack
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Developer Options",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Developer Settings
                SettingsItem(
                    label = "Color Palette",
                    value = "View Colors",
                    onClick = { showColorPalette = true }
                )

                HorizontalDivider()

                HapticsTestSection()
            }
        }
    }

    if (showColorPalette) {
        ColorPaletteDialog(
            show = showColorPalette,
            onDismissRequest = { showColorPalette = false }
        )
    }
}

// --- Haptics Test Section ---

@Composable
fun HapticsTestSection() {
    val view = LocalView.current

    // Define the available haptic types (Name -> Constant/Action)
    val hapticTypes = remember {
        listOf(
            "Clock Tick" to HapticFeedbackConstants.CLOCK_TICK,
            "Context Click" to HapticFeedbackConstants.CONTEXT_CLICK,
            "Keyboard Tap" to HapticFeedbackConstants.KEYBOARD_TAP,
            "Long Press" to HapticFeedbackConstants.LONG_PRESS,
            "Virtual Key" to HapticFeedbackConstants.VIRTUAL_KEY,
            "Confirm" to HapticFeedbackConstants.CONFIRM,
            "Reject" to HapticFeedbackConstants.REJECT,
            "Gesture Start" to HapticFeedbackConstants.GESTURE_START,
            "Gesture End" to HapticFeedbackConstants.GESTURE_END
        )
    }

    var sliderPosition by remember { mutableFloatStateOf(0f) }

    // Logic to snap and trigger
    // We want the slider to feel "steps".
    // When value changes, we find the closest index.
    val index = sliderPosition.roundToInt().coerceIn(0, hapticTypes.size - 1)
    val currentType = hapticTypes[index]

    // Trigger haptic only when the discrete index changes
    var lastIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(index) {
        if (index != lastIndex) {
            view.performHapticFeedback(currentType.second)
            lastIndex = index
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Haptics Test: ${currentType.first}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                // We could also trigger continuous feedback here if we wanted "ticks" while dragging
                // But triggering on step change (via LaunchedEffect) is safer for distinct feel.
            },
            valueRange = 0f..(hapticTypes.size - 1).toFloat(),
            steps = hapticTypes.size - 2, // Steps are the ticks *between* min and max.
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Move slider to feel different vibrations",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun ColorPaletteDialog(
    show: Boolean,
    onDismissRequest: () -> Unit
) {
    if (show) {
        val colorScheme = MaterialTheme.colorScheme
        // List of all Material 3 colors
        val colors = listOf(
            "primary" to colorScheme.primary,
            "onPrimary" to colorScheme.onPrimary,
            "primaryContainer" to colorScheme.primaryContainer,
            "onPrimaryContainer" to colorScheme.onPrimaryContainer,
            "inversePrimary" to colorScheme.inversePrimary,
            "secondary" to colorScheme.secondary,
            "onSecondary" to colorScheme.onSecondary,
            "secondaryContainer" to colorScheme.secondaryContainer,
            "onSecondaryContainer" to colorScheme.onSecondaryContainer,
            "tertiary" to colorScheme.tertiary,
            "onTertiary" to colorScheme.onTertiary,
            "tertiaryContainer" to colorScheme.tertiaryContainer,
            "onTertiaryContainer" to colorScheme.onTertiaryContainer,
            "background" to colorScheme.background,
            "onBackground" to colorScheme.onBackground,
            "surface" to colorScheme.surface,
            "onSurface" to colorScheme.onSurface,
            "surfaceVariant" to colorScheme.surfaceVariant,
            "onSurfaceVariant" to colorScheme.onSurfaceVariant,
            "surfaceTint" to colorScheme.surfaceTint,
            "inverseSurface" to colorScheme.inverseSurface,
            "inverseOnSurface" to colorScheme.inverseOnSurface,
            "error" to colorScheme.error,
            "onError" to colorScheme.onError,
            "errorContainer" to colorScheme.errorContainer,
            "onErrorContainer" to colorScheme.onErrorContainer,
            "outline" to colorScheme.outline,
            "outlineVariant" to colorScheme.outlineVariant,
            "scrim" to colorScheme.scrim,
        )

        PredictiveModal(
            show = show,
            onDismissRequest = onDismissRequest
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Color Palette",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { (name, color) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color Swatch
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp)
                                    .background(color, shape = MaterialTheme.shapes.small)
                                    .padding(1.dp) // Optional border effect if needed
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                // Optional: Display Hex code
                                // Requires manual conversion or just show
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
