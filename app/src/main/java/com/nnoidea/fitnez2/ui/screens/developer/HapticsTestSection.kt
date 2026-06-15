package com.nnoidea.fitnez2.ui.screens.developer

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import kotlin.math.roundToInt

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
            text = "${globalLocalization.devHapticsTest}: ${currentType.first}",
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
            text = globalLocalization.devMoveSlider,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
