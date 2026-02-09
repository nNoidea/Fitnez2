package com.nnoidea.fitnez2.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.*

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants

import com.nnoidea.fitnez2.core.ValidateAndCorrect

/**
 * Headless input component for SETS.
 * Handles focus/unfocus behavior and applies ValidateAndCorrect.sets() on blur.
 * If validation fails, the value reverts to the previous valid value.
 *
 * @param value Current sets value as string
 * @param onValidChange Called with validated value when user commits a valid change
 * @param onRawValueChange Optional callback for current uncommitted text (for external validation)
 * @param content Slot for rendering - receives display state and handlers
 */
@Composable
fun SetsInput(
    value: String,
    onValidChange: (Int) -> Unit,
    onRawValueChange: ((String) -> Unit)? = null,
    content: @Composable (
        displayValue: String,
        placeholder: String,
        interactionSource: MutableInteractionSource,
        onValueChange: (String) -> Unit
    ) -> Unit
) {
    SmartValidatedInput(
        value = value,
        validate = { ValidateAndCorrect.sets(it) },
        inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } },
        onValidChange = { onValidChange(it as Int) },
        onRawValueChange = onRawValueChange,
        content = content
    )
}

/**
 * Headless input component for REPS.
 * Handles focus/unfocus behavior and applies ValidateAndCorrect.reps() on blur.
 * If validation fails, the value reverts to the previous valid value.
 *
 * @param value Current reps value as string
 * @param onValidChange Called with validated value when user commits a valid change
 * @param onRawValueChange Optional callback for current uncommitted text (for external validation)
 * @param content Slot for rendering - receives display state and handlers
 */
@Composable
fun RepsInput(
    value: String,
    onValidChange: (Int) -> Unit,
    onRawValueChange: ((String) -> Unit)? = null,
    content: @Composable (
        displayValue: String,
        placeholder: String,
        interactionSource: MutableInteractionSource,
        onValueChange: (String) -> Unit
    ) -> Unit
) {
    SmartValidatedInput(
        value = value,
        validate = { ValidateAndCorrect.reps(it) },
        inputFilter = { it.isEmpty() || it.all { c -> c.isDigit() } },
        onValidChange = { onValidChange(it as Int) },
        onRawValueChange = onRawValueChange,
        content = content
    )
}

/**
 * Headless input component for WEIGHT.
 * Handles focus/unfocus behavior and applies ValidateAndCorrect.weight() on blur.
 * If validation fails, the value reverts to the previous valid value.
 *
 * @param value Current weight value as Double (formatted internally, removes .0 for whole numbers)
 * @param onValidChange Called with validated value when user commits a valid change
 * @param onRawValueChange Optional callback for current uncommitted text (for external validation)
 * @param content Slot for rendering - receives display state and handlers
 */
@Composable
fun WeightInput(
    value: Double,
    onValidChange: (Double) -> Unit,
    onRawValueChange: ((String) -> Unit)? = null,
    content: @Composable (
        displayValue: String,
        placeholder: String,
        interactionSource: MutableInteractionSource,
        onValueChange: (String) -> Unit
    ) -> Unit
) {
    // Format: remove unnecessary .0 suffix for whole numbers (e.g., 50.0 -> "50")
    val formattedValue = value.toString().removeSuffix(".0")
    
    SmartValidatedInput(
        value = formattedValue,
        validate = { ValidateAndCorrect.weight(it) },
        inputFilter = { it.isEmpty() || it == "-" || it.toDoubleOrNull() != null },
        onValidChange = { onValidChange(it as Double) },
        onRawValueChange = onRawValueChange,
        content = content
    )
}

// ---------------------------------------------------------------------------
// Private Implementation
// ---------------------------------------------------------------------------

/**
 * Core validated input logic. Handles:
 * 1. Clear on focus (save current value as placeholder)
 * 2. Restore on blur if empty
 * 3. Validate on blur - revert if invalid
 * 4. Clear focus when keyboard dismissed
 * 5. Canonicalize numeric values (e.g., "01" -> "1")
 */
@Composable
private fun <T> SmartValidatedInput(
    value: String,
    validate: (String) -> T?,
    inputFilter: (String) -> Boolean,
    onValidChange: (T) -> Unit,
    onRawValueChange: ((String) -> Unit)? = null,
    content: @Composable (
        displayValue: String,
        placeholder: String,
        interactionSource: MutableInteractionSource,
        onValueChange: (String) -> Unit
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
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
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

    content(internalValue, savedValue, interactionSource, onValueChangeWrapper)
}
