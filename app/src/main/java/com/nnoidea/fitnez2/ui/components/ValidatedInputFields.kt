package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.common.SetsInput
import com.nnoidea.fitnez2.ui.common.RepsInput
import com.nnoidea.fitnez2.ui.common.WeightInput

// =============================================================================
// Drop-in composables for Bottom Sheet
// =============================================================================

/**
 * Drop-in Sets input for the bottom sheet.
 * Just place it — logic + visuals are fully wired.
 */
@Composable
fun BottomSheetSetsField(
    value: String,
    onValidChange: (String) -> Unit,
    onRawValueChange: ((String) -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    modifier: Modifier = Modifier
) {
    SetsInput(
        value = value,
        onValidChange = { onValidChange(it.toString()) },
        onRawValueChange = onRawValueChange
    ) { displayValue, placeholder, interactionSource, onValueChange, isFocused ->
        BottomSheetInputSkin(
            label = globalLocalization.labelSets,
            displayValue = displayValue,
            placeholder = placeholder,
            interactionSource = interactionSource,
            onValueChange = onValueChange,
            containerColor = containerColor,
            contentColor = contentColor,
            isFocused = isFocused,
            modifier = modifier
        )
    }
}

/**
 * Drop-in Reps input for the bottom sheet.
 * Just place it — logic + visuals are fully wired.
 */
@Composable
fun BottomSheetRepsField(
    value: String,
    onValidChange: (String) -> Unit,
    onRawValueChange: ((String) -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    modifier: Modifier = Modifier
) {
    RepsInput(
        value = value,
        onValidChange = { onValidChange(it.toString()) },
        onRawValueChange = onRawValueChange
    ) { displayValue, placeholder, interactionSource, onValueChange, isFocused ->
        BottomSheetInputSkin(
            label = globalLocalization.labelReps,
            displayValue = displayValue,
            placeholder = placeholder,
            interactionSource = interactionSource,
            onValueChange = onValueChange,
            containerColor = containerColor,
            contentColor = contentColor,
            isFocused = isFocused,
            modifier = modifier
        )
    }
}

/**
 * Drop-in Weight input for the bottom sheet.
 * Just place it — logic + visuals are fully wired.
 */
@Composable
fun BottomSheetWeightField(
    value: Double,
    label: String,
    onValidChange: (String) -> Unit,
    onRawValueChange: ((String) -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier: Modifier = Modifier
) {
    WeightInput(
        value = value,
        onValidChange = { onValidChange(it.toString()) },
        onRawValueChange = onRawValueChange
    ) { displayValue, placeholder, interactionSource, onValueChange, isFocused ->
        BottomSheetInputSkin(
            label = label,
            displayValue = displayValue,
            placeholder = placeholder,
            interactionSource = interactionSource,
            onValueChange = onValueChange,
            containerColor = containerColor,
            contentColor = contentColor,
            isFocused = isFocused,
            isDecimal = true,
            modifier = modifier
        )
    }
}

// =============================================================================
// Drop-in composables for Exercise History List
// =============================================================================

/**
 * Drop-in Sets input for history cards.
 * Just place it — logic + visuals are fully wired.
 */
@Composable
fun HistorySetsField(
    value: Int,
    contentColor: Color,
    onValidChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    SetsInput(
        value = value.toString(),
        onValidChange = onValidChange
    ) { displayValue, placeholder, interactionSource, onValueChange, isFocused ->
        HistoryInputSkin(
            displayValue = displayValue,
            placeholder = placeholder,
            interactionSource = interactionSource,
            onValueChange = onValueChange,
            contentColor = contentColor,
            isFocused = isFocused,
            modifier = modifier
        )
    }
}

/**
 * Drop-in Reps input for history cards.
 * Just place it — logic + visuals are fully wired.
 */
@Composable
fun HistoryRepsField(
    value: Int,
    contentColor: Color,
    onValidChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    RepsInput(
        value = value.toString(),
        onValidChange = onValidChange
    ) { displayValue, placeholder, interactionSource, onValueChange, isFocused ->
        HistoryInputSkin(
            displayValue = displayValue,
            placeholder = placeholder,
            interactionSource = interactionSource,
            onValueChange = onValueChange,
            contentColor = contentColor,
            isFocused = isFocused,
            modifier = modifier
        )
    }
}

/**
 * Drop-in Weight input for history cards.
 * Just place it — logic + visuals are fully wired.
 */
@Composable
fun HistoryWeightField(
    value: Double,
    contentColor: Color,
    onValidChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    WeightInput(
        value = value,
        onValidChange = onValidChange
    ) { displayValue, placeholder, interactionSource, onValueChange, isFocused ->
        HistoryInputSkin(
            displayValue = displayValue,
            placeholder = placeholder,
            interactionSource = interactionSource,
            onValueChange = onValueChange,
            contentColor = contentColor,
            isFocused = isFocused,
            isDecimal = true,
            modifier = modifier
        )
    }
}

// =============================================================================
// Visual Skins (private — only used by the drop-in composables above)
// =============================================================================

private const val HistoryInputBackgroundAlpha = 0.1f

/**
 * Visual skin for bottom sheet input fields.
 *
 * Two modes:
 * 1. Facade (not focused): plain Text with label, truncated to 6 chars, no scrolling, no gesture stealing.
 * 2. Edit (focused): BasicTextField for actual editing.
 */
@Composable
private fun BottomSheetInputSkin(
    label: String,
    displayValue: String,
    placeholder: String,
    interactionSource: MutableInteractionSource,
    onValueChange: (String) -> Unit,
    containerColor: Color,
    contentColor: Color,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false
) {
    val keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        // BasicTextField always exists so interactionSource can track focus.
        // When not focused it's fully transparent; the facade sits on top.
        BasicTextField(
            value = displayValue,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isFocused) 1f else 0f)
                .focusRequester(focusRequester),
            interactionSource = interactionSource,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = contentColor,
                textAlign = TextAlign.Start
            ),
            singleLine = true,
            cursorBrush = SolidColor(contentColor),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor
                    )
                    Text(
                        text = " | ",
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor
                    )
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (displayValue.isEmpty()) {
                            Text(
                                text = placeholder.ifEmpty { " " },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = contentColor.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )

        // Facade: visible only when NOT focused
        if (!isFocused) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusRequester.requestFocus()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
                Text(
                    text = " | ",
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
                val facadeText = displayValue.ifEmpty { placeholder.ifEmpty { " " } }
                Text(
                    text = if (facadeText.length > 6) facadeText.take(6) + "\u2026" else facadeText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = if (displayValue.isEmpty()) contentColor.copy(alpha = 0.5f) else contentColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

/**
 * Visual skin for history list input fields.
 *
 * Two modes:
 * 1. Facade (not focused): plain Text, truncated to 6 chars, no scrolling, no gesture stealing.
 * 2. Edit (focused): BasicTextField for actual editing.
 */
@Composable
private fun HistoryInputSkin(
    displayValue: String,
    placeholder: String,
    interactionSource: MutableInteractionSource,
    onValueChange: (String) -> Unit,
    contentColor: Color,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = contentColor
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                contentColor.copy(alpha = HistoryInputBackgroundAlpha),
                RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = displayValue,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .alpha(if (isFocused) 1f else 0f)
                .focusRequester(focusRequester),
            interactionSource = interactionSource,
            textStyle = textStyle,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
            ),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    if (isFocused && displayValue.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = textStyle.copy(
                                color = contentColor.copy(alpha = 0.5f)
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Facade: visible only when NOT focused
        if (!isFocused) {
            val facadeText = displayValue.ifEmpty { placeholder }
            Text(
                text = if (facadeText.length > 6) facadeText.take(6) + "\u2026" else facadeText,
                style = textStyle.copy(
                    color = if (displayValue.isEmpty()) contentColor.copy(alpha = 0.5f) else contentColor
                ),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusRequester.requestFocus()
                    }
            )
        }
    }
}
