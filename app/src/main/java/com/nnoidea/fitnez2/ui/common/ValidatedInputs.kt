package com.nnoidea.fitnez2.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
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
        onValueChange: (String) -> Unit,
        isFocused: Boolean
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
        onValueChange: (String) -> Unit,
        isFocused: Boolean
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
        onValueChange: (String) -> Unit,
        isFocused: Boolean
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

// Core logic extracted to SmartValidatedInput.kt
