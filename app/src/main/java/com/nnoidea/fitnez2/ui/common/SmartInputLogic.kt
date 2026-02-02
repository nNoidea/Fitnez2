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
    areEquivalent: (String, String) -> Boolean = { a, b -> 
        a == b || (a.toDoubleOrNull() != null && a.toDoubleOrNull() == b.toDoubleOrNull()) 
    },
    content: @Composable (
        displayValue: String,
        placeholder: String,
        interactionSource: MutableInteractionSource,
        onValueChangeWrapper: (String) -> Unit
    ) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Internal state to hold the text value (preserves formatting like "01" or ".00")
    var internalValue by remember { mutableStateOf(value) }

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

    // -------------------------------------------------------------------------
    // Behavior Logic
    // -------------------------------------------------------------------------

    // 1. Upstream Sync
    // Sync internal state when the external value changes.
    // We treat the external 'value' as the source of truth, but we must be careful
    // not to overwrite the user's ongoing input with an "old" value during async updates.
    LaunchedEffect(value) {
        // If we are focused, we assume the user is typing. We only sync if the value
        // is drastically different (not just an formatting diff) or if it seems to be a new external value.
        // For simple cases (Bottom Sheet), 'value' echoes 'internalValue', so this is a no-op.
        // For async cases (History), 'value' assumes the committed state. 
        if (!isFocused) {
            internalValue = value
        } else {
             // While focused, if the upstream value changes to something that isn't equivalent 
             // to what we are typing, it's likely an external event (e.g. data refresh).
             if (!areEquivalent(internalValue, value)) {
                 internalValue = value
             }
        }
    }

    // 2. Focus Management (Clear-on-focus, Restore-on-blur, Canonicalize)
    LaunchedEffect(isFocused) {
        if (isFocused) {
            // ---> Focus Gained
            // Save current state to allow restoration if aborted (left empty)
            savedValue = internalValue
            
            // Clear the field for fresh input (Design Requirement)
            internalValue = ""
            onValueChange("")
            wasFocused = true
        } else {
            if (wasFocused) {
                // <--- Focus Lost
                var effectiveValue = if (internalValue.isEmpty()) savedValue else internalValue
                
                // Feature: Canonicalize / Format (Omit leading zeros)
                // We parse the string and re-format it to remove artifacts like "01" -> "1"
                effectiveValue.toDoubleOrNull()?.let { num ->
                    val canonical = if (num % 1.0 == 0.0) {
                        num.toInt().toString()
                    } else {
                        num.toString()
                    }
                    if (canonical != effectiveValue) {
                        effectiveValue = canonical
                    }
                }

                // Logic:
                // 1. If user left it empty -> Restore saved value.
                // 2. If user typed something -> Commit effective (canonical) value.
                
                if (internalValue.isEmpty()) {
                    // Restore case
                    internalValue = savedValue
                    if (savedValue != value) {
                        onValueChange(savedValue)
                    }
                } else {
                    // Commit case
                    // Update internal view immediately (Prevent snap-back for async parents)
                    internalValue = effectiveValue
                    
                    // Propagate changes
                    onValueChange(effectiveValue)
                }
                
                // Final "Commit" event for Logic that relies on Blur (History List)
                onFocusLost(effectiveValue)
                
                wasFocused = false
            }
        }
    }

    // Wrapper for value change involving validation or formatting hooks
    val onValueChangeWrapper: (String) -> Unit = { newValue ->
        if (validate(newValue)) {
             internalValue = newValue
             onValueChange(newValue)
        }
    }

    content(
        internalValue,
        savedValue,
        interactionSource,
        onValueChangeWrapper
    )
}
