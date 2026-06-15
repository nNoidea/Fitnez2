package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.common.RepsInput
import com.nnoidea.fitnez2.ui.common.SetsInput
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
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier: Modifier = Modifier,
    topStartRadius: Dp = 24.dp,
    topEndRadius: Dp = 24.dp,
    bottomStartRadius: Dp = 24.dp,
    bottomEndRadius: Dp = 24.dp
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
            isFocused = isFocused,
            modifier = modifier,
            topStartRadius = topStartRadius,
            topEndRadius = topEndRadius,
            bottomStartRadius = bottomStartRadius,
            bottomEndRadius = bottomEndRadius,
            unfocusedContainerColor = containerColor,
            unfocusedContentColor = contentColor
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
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier: Modifier = Modifier,
    topStartRadius: Dp = 24.dp,
    topEndRadius: Dp = 24.dp,
    bottomStartRadius: Dp = 24.dp,
    bottomEndRadius: Dp = 24.dp
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
            isFocused = isFocused,
            modifier = modifier,
            topStartRadius = topStartRadius,
            topEndRadius = topEndRadius,
            bottomStartRadius = bottomStartRadius,
            bottomEndRadius = bottomEndRadius,
            unfocusedContainerColor = containerColor,
            unfocusedContentColor = contentColor
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
    modifier: Modifier = Modifier,
    topStartRadius: Dp = 24.dp,
    topEndRadius: Dp = 24.dp,
    bottomStartRadius: Dp = 24.dp,
    bottomEndRadius: Dp = 24.dp
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
            isFocused = isFocused,
            unfocusedContainerColor = containerColor,
            unfocusedContentColor = contentColor,
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
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false,
    topStartRadius: Dp = 24.dp,
    topEndRadius: Dp = 24.dp,
    bottomStartRadius: Dp = 24.dp,
    bottomEndRadius: Dp = 24.dp,
    unfocusedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unfocusedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    focusedContainerColor: Color = MaterialTheme.colorScheme.tertiary,
    focusedContentColor: Color = MaterialTheme.colorScheme.onTertiary
) {
    FacadeEditableField(
        value = displayValue,
        onValueChange = onValueChange,
        interactionSource = interactionSource,
        isFocused = isFocused,
        isDecimal = isDecimal,
        modifier = modifier,
        unfocusedContainerColor = unfocusedContainerColor,
        unfocusedContentColor = unfocusedContentColor,
        focusedContainerColor = focusedContainerColor,
        focusedContentColor = focusedContentColor,
        textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Start),
        topStartRadius = topStartRadius,
        topEndRadius = topEndRadius,
        bottomStartRadius = bottomStartRadius,
        bottomEndRadius = bottomEndRadius,
        decorationBox = { innerTextField, contentColor, textStyle ->
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
        facade = { clickModifier, contentColor ->
            Row(
                modifier = clickModifier.padding(horizontal = 12.dp),
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
                        color = if (displayValue.isEmpty()) contentColor.copy(alpha = 0.5f)
                               else MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    )
}
