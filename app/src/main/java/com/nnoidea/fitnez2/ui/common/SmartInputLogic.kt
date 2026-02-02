package com.nnoidea.fitnez2.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.*

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager

/**
 * A shared logic component for input fields that need "clear on focus" behavior.
 * This component acts as the Single Source of Truth for the input behavior rules.
 *
 * Rules:
 * 1. When focused, the current value is cleared and saved as a placeholder.
 * 2. If the user leaves the field empty (blur), the saved value is restored.
 * 3. Focus is automatically cleared if the keyboard is dismissed.
 * 4. Provides a clean interface for UI components to implement their specific look.
 *
 * @param value The current text value.
 * @param onValueChange Callback when valid text input changes.
 * @param onFocusLost Callback when focus is lost. Passes the final committed value (handles the restoration case).
 * @param validate Input validation predicate (default allows all).
 * @param content Slot for rendering the actual visual component.
 */
@Composable
fun SmartInputLogic(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusLost: (String) -> Unit = {},
    validate: (String) -> Boolean = { true },
    content: @Composable (
        displayValue: String,
        placeholder: String,
        interactionSource: MutableInteractionSource,
        onValueChangeWrapper: (String) -> Unit
    ) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    
    // Check if keyboard (IME) is visible
    // Note: accessing WindowInsets.ime directly in composition ensures we recompose when it changes
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0

    // State to hold the value for restoration
    var savedValue by remember { mutableStateOf("") }
    
    // We track the previous focus state to detect the *event* of losing focus
    var wasFocused by remember { mutableStateOf(false) }

    // Logic: Clear focus if keyboard is dismissed while focused
    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible && isFocused) {
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            // Event: Focus Gained
            savedValue = value
            onValueChange("")
            wasFocused = true
        } else {
            if (wasFocused) {
                // Event: Focus Lost
                val finalValue = if (value.isEmpty()) savedValue else value
                
                // Restore if empty
                if (value.isEmpty()) {
                    onValueChange(savedValue)
                }
                
                // Notify parent of commit with the final effective value
                onFocusLost(finalValue)
                wasFocused = false
            }
        }
    }

    // Wrapper for value change involving validation or formatting hooks
    val onValueChangeWrapper: (String) -> Unit = { newValue ->
        if (validate(newValue)) {
             onValueChange(newValue)
        }
    }

    content(
        value,
        savedValue,
        interactionSource,
        onValueChangeWrapper
    )
}
