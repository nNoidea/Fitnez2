package com.nnoidea.fitnez2.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants

/**
 * Core validated input logic. Handles:
 * 1. Clear on focus (save current value as placeholder)
 * 2. Restore on blur if empty
 * 3. Validate on blur - revert if invalid
 * 4. Clear focus when keyboard dismissed
 * 5. Canonicalize numeric values (e.g., "01" -> "1")
 */
@Composable
internal fun <T> SmartValidatedInput(
    value: String,
    validate: (String) -> T?,
    inputFilter: (String) -> Boolean,
    onValidChange: (T) -> Unit,
    onRawValueChange: ((String) -> Unit)? = null,
    content: @Composable (
        displayValue: String,
        placeholder: String,
        interactionSource: MutableInteractionSource,
        onValueChange: (String) -> Unit,
        isFocused: Boolean
    ) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    var internalValue by remember { mutableStateOf(value) }
    var savedValue by remember { mutableStateOf("") }
    var wasFocused by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val view = LocalView.current

    // Clear focus when keyboard dismissed
    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible && isFocused) {
            focusManager.clearFocus()
        }
    }

    // Sync with upstream value when not focused
    LaunchedEffect(value) {
        if (!isFocused) {
            internalValue = value
        }
    }

    // Focus/Unfocus behavior
    LaunchedEffect(isFocused) {
        if (isFocused) {
            // Focus gained - save and clear
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
            savedValue = internalValue
            internalValue = ""
            wasFocused = true
        } else if (wasFocused) {
            // Focus lost - validate and commit or revert
            wasFocused = false
            
            if (internalValue.isEmpty()) {
                // Empty -> restore saved value
                internalValue = savedValue
                return@LaunchedEffect
            }
            
            // Canonicalize (remove leading zeros, format decimals)
            var effectiveValue = internalValue
            effectiveValue.toDoubleOrNull()?.let { num ->
                effectiveValue = if (num % 1.0 == 0.0) {
                    num.toInt().toString()
                } else {
                    num.toString()
                }
            }
            
            // Validate
            val validated = validate(effectiveValue)
            if (validated != null) {
                // Valid -> commit
                internalValue = effectiveValue
                if (effectiveValue != value) {
                    onValidChange(validated)
                }
            } else {
                // Invalid -> revert (tooltip already shown by ValidateAndCorrect)
                internalValue = savedValue
            }
        }
    }

    // Filter input while typing
    val onValueChangeWrapper: (String) -> Unit = { newValue ->
        if (inputFilter(newValue)) {
            internalValue = newValue
            onRawValueChange?.invoke(newValue)
        }
    }

    // Report raw value on initial composition and when synced from upstream
    LaunchedEffect(internalValue) {
        onRawValueChange?.invoke(internalValue)
    }

    content(internalValue, savedValue, interactionSource, onValueChangeWrapper, isFocused)
}
