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
import androidx.compose.runtime.getValue
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
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier: Modifier = Modifier,
    topStartRadius: androidx.compose.ui.unit.Dp = 24.dp,
    topEndRadius: androidx.compose.ui.unit.Dp = 24.dp,
    bottomStartRadius: androidx.compose.ui.unit.Dp = 24.dp,
    bottomEndRadius: androidx.compose.ui.unit.Dp = 24.dp
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
    topStartRadius: androidx.compose.ui.unit.Dp = 24.dp,
    topEndRadius: androidx.compose.ui.unit.Dp = 24.dp,
    bottomStartRadius: androidx.compose.ui.unit.Dp = 24.dp,
    bottomEndRadius: androidx.compose.ui.unit.Dp = 24.dp
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
    topStartRadius: androidx.compose.ui.unit.Dp = 24.dp,
    topEndRadius: androidx.compose.ui.unit.Dp = 24.dp,
    bottomStartRadius: androidx.compose.ui.unit.Dp = 24.dp,
    bottomEndRadius: androidx.compose.ui.unit.Dp = 24.dp
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
    modifier: Modifier = Modifier,
    topStartRadius: androidx.compose.ui.unit.Dp = 12.dp,
    topEndRadius: androidx.compose.ui.unit.Dp = 12.dp,
    bottomStartRadius: androidx.compose.ui.unit.Dp = 12.dp,
    bottomEndRadius: androidx.compose.ui.unit.Dp = 12.dp
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
    topStartRadius: androidx.compose.ui.unit.Dp = 12.dp,
    topEndRadius: androidx.compose.ui.unit.Dp = 12.dp,
    bottomStartRadius: androidx.compose.ui.unit.Dp = 12.dp,
    bottomEndRadius: androidx.compose.ui.unit.Dp = 12.dp
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
    topStartRadius: androidx.compose.ui.unit.Dp = 12.dp,
    topEndRadius: androidx.compose.ui.unit.Dp = 12.dp,
    bottomStartRadius: androidx.compose.ui.unit.Dp = 12.dp,
    bottomEndRadius: androidx.compose.ui.unit.Dp = 12.dp
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
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false,
    topStartRadius: androidx.compose.ui.unit.Dp = 24.dp,
    topEndRadius: androidx.compose.ui.unit.Dp = 24.dp,
    bottomStartRadius: androidx.compose.ui.unit.Dp = 24.dp,
    bottomEndRadius: androidx.compose.ui.unit.Dp = 24.dp,
    unfocusedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    unfocusedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    focusedContainerColor: Color = MaterialTheme.colorScheme.tertiary,
    focusedContentColor: Color = MaterialTheme.colorScheme.onTertiary
) {
    val keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
    val focusRequester = remember { FocusRequester() }

    val animatedTopStart by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isFocused) 24.dp else topStartRadius,
        label = "topStartRadius"
    )
    val animatedTopEnd by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isFocused) 24.dp else topEndRadius,
        label = "topEndRadius"
    )
    val animatedBottomStart by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isFocused) 24.dp else bottomStartRadius,
        label = "bottomStartRadius"
    )
    val animatedBottomEnd by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isFocused) 24.dp else bottomEndRadius,
        label = "bottomEndRadius"
    )

    val currentShape = RoundedCornerShape(
        topStart = animatedTopStart,
        topEnd = animatedTopEnd,
        bottomEnd = animatedBottomEnd,
        bottomStart = animatedBottomStart
    )

    val currentContainerColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isFocused) focusedContainerColor else unfocusedContainerColor,
        label = "containerColor"
    )
    val currentContentColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isFocused) focusedContentColor else unfocusedContentColor,
        label = "contentColor"
    )

    Box(
        modifier = modifier
            .background(currentContainerColor, currentShape)
            .clip(currentShape),
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
                color = currentContentColor,
                textAlign = TextAlign.Start
            ),
            singleLine = true,
            cursorBrush = SolidColor(currentContentColor),
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
                        color = currentContentColor
                    )
                    Text(
                        text = " | ",
                        style = MaterialTheme.typography.titleMedium,
                        color = currentContentColor
                    )
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (displayValue.isEmpty()) {
                            Text(
                                text = placeholder.ifEmpty { " " },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = currentContentColor.copy(alpha = 0.5f)
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
                    color = currentContentColor
                )
                Text(
                    text = " | ",
                    style = MaterialTheme.typography.titleMedium,
                    color = currentContentColor
                )
                val facadeText = displayValue.ifEmpty { placeholder.ifEmpty { " " } }
                Text(
                    text = if (facadeText.length > 6) facadeText.take(6) + "\u2026" else facadeText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = if (displayValue.isEmpty()) currentContentColor.copy(alpha = 0.5f) else currentContentColor
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
    isDecimal: Boolean = false,
    topStartRadius: androidx.compose.ui.unit.Dp = 12.dp,
    topEndRadius: androidx.compose.ui.unit.Dp = 12.dp,
    bottomStartRadius: androidx.compose.ui.unit.Dp = 12.dp,
    bottomEndRadius: androidx.compose.ui.unit.Dp = 12.dp
) {
    val focusRequester = remember { FocusRequester() }
    
    val animatedTopStart by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isFocused) 24.dp else topStartRadius,
        label = "topStartRadius"
    )
    val animatedTopEnd by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isFocused) 24.dp else topEndRadius,
        label = "topEndRadius"
    )
    val animatedBottomStart by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isFocused) 24.dp else bottomStartRadius,
        label = "bottomStartRadius"
    )
    val animatedBottomEnd by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isFocused) 24.dp else bottomEndRadius,
        label = "bottomEndRadius"
    )

    val currentShape = RoundedCornerShape(
        topStart = animatedTopStart,
        topEnd = animatedTopEnd,
        bottomEnd = animatedBottomEnd,
        bottomStart = animatedBottomStart
    )

    val unfocusedBgColor = contentColor.copy(alpha = HistoryInputBackgroundAlpha)
    val focusedBgColor = MaterialTheme.colorScheme.tertiary
    val unfocusedTextColor = contentColor
    val focusedTextColor = MaterialTheme.colorScheme.onTertiary

    val currentBgColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isFocused) focusedBgColor else unfocusedBgColor,
        label = "containerColor"
    )
    val currentTextColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isFocused) focusedTextColor else unfocusedTextColor,
        label = "textColor"
    )

    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = currentTextColor
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .background(currentBgColor, currentShape)
            .clip(currentShape),
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
                                color = currentTextColor.copy(alpha = 0.5f)
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
                    color = if (displayValue.isEmpty()) currentTextColor.copy(alpha = 0.5f) else currentTextColor
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
