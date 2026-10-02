package com.nnoidea.fitnez2.ui.screens.developer

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.components.PopHapticMode
import com.nnoidea.fitnez2.ui.components.SwipeHapticsConfig
import com.nnoidea.fitnez2.ui.components.SwipeToDeleteContainer
import kotlin.math.roundToInt

// --- Swipe Haptics Lab Controls & Live Interactive Card ---

@Composable
fun SwipeHapticsLabControls() {
    val context = LocalContext.current
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 1. Threshold percentage slider
        Text(
            text = "Delete Threshold: ${(SwipeHapticsConfig.thresholdFraction * 100).roundToInt()}% of card width",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = SwipeHapticsConfig.thresholdFraction,
            onValueChange = { SwipeHapticsConfig.thresholdFraction = it },
            valueRange = 0.20f..0.60f,
            steps = 7,
            modifier = Modifier.fillMaxWidth()
        )

        // 2. Drag micro-tick frequency / interval
        val tickDpInt = SwipeHapticsConfig.tickIntervalDp.value.roundToInt().coerceIn(2, 24)
        val freqLabel = when {
            tickDpInt <= 2 -> "Ultra-Dense (~60 ticks/swipe)"
            tickDpInt <= 4 -> "Dense Micro (~30 ticks/swipe)"
            tickDpInt <= 6 -> "Crisp Ratchet (~20 ticks/swipe - Default)"
            tickDpInt <= 10 -> "Medium Steps (~12 ticks/swipe)"
            else -> "Spaced Notches (~7 ticks/swipe)"
        }
        Text(
            text = "Micro-Tick Interval: $tickDpInt dp ($freqLabel)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = tickDpInt.toFloat(),
            onValueChange = { SwipeHapticsConfig.tickIntervalDp = it.roundToInt().dp },
            valueRange = 2f..20f,
            steps = 8,
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Swipe Tick Power (Dragging Detent Intensity)
        Text(
            text = "Swipe Tick Power: ${(SwipeHapticsConfig.swipeTickScale * 100).roundToInt()}% (subtle detent)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = SwipeHapticsConfig.swipeTickScale,
            onValueChange = { SwipeHapticsConfig.swipeTickScale = it },
            valueRange = 0.10f..1.0f,
            steps = 8,
            modifier = Modifier.fillMaxWidth()
        )

        // 4. Threshold POP Power (Punch Contrast)
        Text(
            text = "Threshold POP Power: ${(SwipeHapticsConfig.popScale * 100).roundToInt()}% (high-contrast punch)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = SwipeHapticsConfig.popScale,
            onValueChange = { SwipeHapticsConfig.popScale = it },
            valueRange = 0.20f..1.0f,
            steps = 7,
            modifier = Modifier.fillMaxWidth()
        )

        // 5. Custom Actuator Punch Controls (if STRONG_POP or DOUBLE_PULSE selected)
        if (SwipeHapticsConfig.popMode == PopHapticMode.STRONG_POP || SwipeHapticsConfig.popMode == PopHapticMode.DOUBLE_PULSE) {
            Spacer(modifier = Modifier.height(4.dp))
            if (SwipeHapticsConfig.popMode == PopHapticMode.STRONG_POP) {
                Text(
                    text = "POP Vibration Duration: ${SwipeHapticsConfig.popVibrationDurationMs} ms",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = SwipeHapticsConfig.popVibrationDurationMs.toFloat(),
                    onValueChange = { SwipeHapticsConfig.popVibrationDurationMs = it.toLong() },
                    valueRange = 10f..150f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                text = "POP Actuator Amplitude: ${SwipeHapticsConfig.popVibrationAmplitude} / 255",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = SwipeHapticsConfig.popVibrationAmplitude.toFloat(),
                onValueChange = { SwipeHapticsConfig.popVibrationAmplitude = it.roundToInt() },
                valueRange = 20f..255f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick test buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { SwipeHapticsConfig.performPop(context, view) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Test POP")
            }

            FilledTonalButton(
                onClick = { SwipeHapticsConfig.performSliderTick(context, view) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Test Tick")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Live Interactive Test Card",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Swipe left to feel ticks and the POP. Drag back to clock it back!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        var isTestCardDeleted by remember { mutableStateOf(false) }

        if (!isTestCardDeleted) {
            SwipeToDeleteContainer(
                onDelete = { isTestCardDeleted = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Swipe this card left to test!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Interval: ${SwipeHapticsConfig.tickIntervalDp.value.roundToInt()}dp  •  Tick: ${(SwipeHapticsConfig.swipeTickScale * 100).roundToInt()}%  •  POP: ${(SwipeHapticsConfig.popScale * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Card committed delete past threshold!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { isTestCardDeleted = false }
                    ) {
                        Text("Reset Test Card")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { SwipeHapticsConfig.resetToDefaults() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset to Recommended Defaults")
        }
    }
}

// --- Generic Android System Haptics Step Slider ---

@Composable
fun SystemHapticsStepSlider() {
    val view = LocalView.current

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

    val index = sliderPosition.roundToInt().coerceIn(0, hapticTypes.size - 1)
    val currentType = hapticTypes[index]

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
            text = "${globalLocalization.devHapticsTest}: ${currentType.first}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
            },
            valueRange = 0f..(hapticTypes.size - 1).toFloat(),
            steps = hapticTypes.size - 2,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = globalLocalization.devMoveSlider,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
