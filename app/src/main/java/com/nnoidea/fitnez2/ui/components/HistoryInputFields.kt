package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.nnoidea.fitnez2.ui.common.RepsInput
import com.nnoidea.fitnez2.ui.common.SetsInput
import com.nnoidea.fitnez2.ui.common.WeightInput

// =============================================================================
// Drop-in composables for Exercise History List
// =============================================================================

private const val HistoryInputBackgroundAlpha = 0.1f

/**
 * Drop-in Sets input for history cards.
 * Just place it — logic + visuals are fully wired.
 */
@Composable
fun HistorySetsField(
    value: Int,
    contentColor: Color,
    onValidChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    topStartRadius: Dp = 12.dp,
    topEndRadius: Dp = 12.dp,
    bottomStartRadius: Dp = 12.dp,
    bottomEndRadius: Dp = 12.dp
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
            modifier = modifier,
            topStartRadius = topStartRadius,
            topEndRadius = topEndRadius,
            bottomStartRadius = bottomStartRadius,
            bottomEndRadius = bottomEndRadius
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
    modifier: Modifier = Modifier,
    topStartRadius: Dp = 12.dp,
    topEndRadius: Dp = 12.dp,
    bottomStartRadius: Dp = 12.dp,
    bottomEndRadius: Dp = 12.dp
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
            modifier = modifier,
            topStartRadius = topStartRadius,
            topEndRadius = topEndRadius,
            bottomStartRadius = bottomStartRadius,
            bottomEndRadius = bottomEndRadius
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
    modifier: Modifier = Modifier,
    topStartRadius: Dp = 12.dp,
    topEndRadius: Dp = 12.dp,
    bottomStartRadius: Dp = 12.dp,
    bottomEndRadius: Dp = 12.dp
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
            modifier = modifier,
            topStartRadius = topStartRadius,
            topEndRadius = topEndRadius,
            bottomStartRadius = bottomStartRadius,
            bottomEndRadius = bottomEndRadius
        )
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
    isDecimal: Boolean = false,
    topStartRadius: Dp = 12.dp,
    topEndRadius: Dp = 12.dp,
    bottomStartRadius: Dp = 12.dp,
    bottomEndRadius: Dp = 12.dp
) {
    val unfocusedBgColor = contentColor.copy(alpha = HistoryInputBackgroundAlpha)
    val focusedBgColor = MaterialTheme.colorScheme.tertiary
    val unfocusedTextColor = contentColor
    val focusedTextColor = MaterialTheme.colorScheme.onTertiary

    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )

    FacadeEditableField(
        value = displayValue,
        onValueChange = onValueChange,
        interactionSource = interactionSource,
        isFocused = isFocused,
        isDecimal = isDecimal,
        modifier = modifier,
        height = 44.dp,
        unfocusedContainerColor = unfocusedBgColor,
        focusedContainerColor = focusedBgColor,
        unfocusedContentColor = unfocusedTextColor,
        focusedContentColor = focusedTextColor,
        textStyle = textStyle,
        topStartRadius = topStartRadius,
        topEndRadius = topEndRadius,
        bottomStartRadius = bottomStartRadius,
        bottomEndRadius = bottomEndRadius,
        decorationBox = { innerTextField, contentColor, textStyle ->
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (displayValue.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(
                            color = contentColor.copy(alpha = 0.5f)
                        )
                    )
                }
                innerTextField()
            }
        },
        facade = { clickModifier, _ ->
            val facadeText = displayValue.ifEmpty { placeholder }
            Text(
                text = if (facadeText.length > 6) facadeText.take(6) + "\u2026" else facadeText,
                style = textStyle.copy(
                    color = if (displayValue.isEmpty()) unfocusedTextColor.copy(alpha = 0.5f)
                           else unfocusedTextColor
                ),
                maxLines = 1,
                modifier = clickModifier.padding(horizontal = 4.dp)
            )
        }
    )
}
