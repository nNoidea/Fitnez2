package com.nnoidea.fitnez2.ui.components.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.ui.common.RepsInput
import com.nnoidea.fitnez2.ui.common.SetsInput
import com.nnoidea.fitnez2.ui.common.WeightInput

/**
 * Single Source of Truth (SSOT) component for editing Sets, Reps, and Weight.
 *
 * Replaces the fragmented input field hierarchy with a clean, connected 3-chip group.
 * Supports both Bottom Sheet mode (with labels, e.g. "Sets | 3") and Timeline Card mode
 * (compact centered numbers).
 */
@Composable
fun SetsRepsWeightGroup(
    sets: String,
    reps: String,
    weight: String,
    weightUnit: String,
    modifier: Modifier = Modifier,
    showLabels: Boolean = false,
    height: Dp = if (showLabels) Dp.Unspecified else 44.dp,
    unfocusedContainerColor: Color? = null,
    unfocusedContentColor: Color? = null,
    focusedContainerColor: Color? = null,
    focusedContentColor: Color? = null,
    spacing: Dp = if (showLabels) 4.dp else 2.dp,
    outerCornerRadius: Dp = 24.dp,
    innerCornerRadius: Dp = 8.dp,
    onSetsChange: (Int) -> Unit,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Double) -> Unit,
    onPendingSetsChange: ((String) -> Unit)? = null,
    onPendingRepsChange: ((String) -> Unit)? = null,
    onPendingWeightChange: ((String) -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Sets (Index 0: Leading)
        ExerciseChipField(
            type = ChipType.SETS,
            value = sets,
            label = globalLocalization.labelSets,
            showLabels = showLabels,
            height = height,
            unfocusedContainerColor = unfocusedContainerColor,
            unfocusedContentColor = unfocusedContentColor,
            focusedContainerColor = focusedContainerColor,
            focusedContentColor = focusedContentColor,
            topStartRadius = outerCornerRadius,
            bottomStartRadius = outerCornerRadius,
            topEndRadius = innerCornerRadius,
            bottomEndRadius = innerCornerRadius,
            modifier = Modifier.weight(1f),
            onValidChangeInt = onSetsChange,
            onRawValueChange = onPendingSetsChange
        )

        // Reps (Index 1: Middle)
        ExerciseChipField(
            type = ChipType.REPS,
            value = reps,
            label = globalLocalization.labelReps,
            showLabels = showLabels,
            height = height,
            unfocusedContainerColor = unfocusedContainerColor,
            unfocusedContentColor = unfocusedContentColor,
            focusedContainerColor = focusedContainerColor,
            focusedContentColor = focusedContentColor,
            topStartRadius = innerCornerRadius,
            bottomStartRadius = innerCornerRadius,
            topEndRadius = innerCornerRadius,
            bottomEndRadius = innerCornerRadius,
            modifier = Modifier.weight(1f),
            onValidChangeInt = onRepsChange,
            onRawValueChange = onPendingRepsChange
        )

        // Weight (Index 2: Trailing)
        ExerciseChipField(
            type = ChipType.WEIGHT,
            value = weight,
            label = weightUnit,
            showLabels = showLabels,
            height = height,
            unfocusedContainerColor = unfocusedContainerColor,
            unfocusedContentColor = unfocusedContentColor,
            focusedContainerColor = focusedContainerColor,
            focusedContentColor = focusedContentColor,
            topStartRadius = innerCornerRadius,
            bottomStartRadius = innerCornerRadius,
            topEndRadius = outerCornerRadius,
            bottomEndRadius = outerCornerRadius,
            modifier = Modifier.weight(1f),
            onValidChangeDouble = onWeightChange,
            onRawValueChange = onPendingWeightChange
        )
    }
}

private enum class ChipType { SETS, REPS, WEIGHT }

@Composable
private fun ExerciseChipField(
    type: ChipType,
    value: String,
    label: String,
    showLabels: Boolean,
    height: Dp,
    unfocusedContainerColor: Color?,
    unfocusedContentColor: Color?,
    focusedContainerColor: Color?,
    focusedContentColor: Color?,
    topStartRadius: Dp,
    topEndRadius: Dp,
    bottomStartRadius: Dp,
    bottomEndRadius: Dp,
    modifier: Modifier = Modifier,
    onValidChangeInt: ((Int) -> Unit)? = null,
    onValidChangeDouble: ((Double) -> Unit)? = null,
    onRawValueChange: ((String) -> Unit)? = null
) {
    val renderContent: @Composable (String, String, MutableInteractionSource, (String) -> Unit, Boolean) -> Unit =
        { displayValue, placeholder, interactionSource, onValueChange, isFocused ->
            ChipFieldSkin(
                label = label,
                displayValue = displayValue,
                placeholder = placeholder,
                showLabels = showLabels,
                isFocused = isFocused,
                isDecimal = type == ChipType.WEIGHT,
                height = height,
                unfocusedContainerColor = unfocusedContainerColor,
                unfocusedContentColor = unfocusedContentColor,
                focusedContainerColor = focusedContainerColor,
                focusedContentColor = focusedContentColor,
                topStartRadius = topStartRadius,
                topEndRadius = topEndRadius,
                bottomStartRadius = bottomStartRadius,
                bottomEndRadius = bottomEndRadius,
                modifier = modifier,
                interactionSource = interactionSource,
                onValueChange = onValueChange
            )
        }

    when (type) {
        ChipType.SETS -> SetsInput(
            value = value,
            onValidChange = { onValidChangeInt?.invoke(it) },
            onRawValueChange = onRawValueChange,
            content = renderContent
        )
        ChipType.REPS -> RepsInput(
            value = value,
            onValidChange = { onValidChangeInt?.invoke(it) },
            onRawValueChange = onRawValueChange,
            content = renderContent
        )
        ChipType.WEIGHT -> WeightInput(
            value = value.toDoubleOrNull() ?: 0.0,
            onValidChange = { onValidChangeDouble?.invoke(it) },
            onRawValueChange = onRawValueChange,
            content = renderContent
        )
    }
}

@Composable
private fun ChipFieldSkin(
    label: String,
    displayValue: String,
    placeholder: String,
    showLabels: Boolean,
    isFocused: Boolean,
    isDecimal: Boolean,
    height: Dp,
    unfocusedContainerColor: Color?,
    unfocusedContentColor: Color?,
    focusedContainerColor: Color?,
    focusedContentColor: Color?,
    topStartRadius: Dp,
    topEndRadius: Dp,
    bottomStartRadius: Dp,
    bottomEndRadius: Dp,
    modifier: Modifier,
    interactionSource: MutableInteractionSource,
    onValueChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    val defaultUnfocusedBg = if (showLabels) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        (unfocusedContentColor ?: MaterialTheme.colorScheme.onSurface).copy(alpha = 0.12f)
    }
    val defaultFocusedBg = if (showLabels) {
        MaterialTheme.colorScheme.tertiary
    } else {
        unfocusedContentColor ?: MaterialTheme.colorScheme.onSurface
    }
    val defaultUnfocusedText = if (showLabels) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        unfocusedContentColor ?: MaterialTheme.colorScheme.onSurface
    }
    val defaultFocusedText = if (showLabels) {
        MaterialTheme.colorScheme.onTertiary
    } else {
        unfocusedContainerColor ?: MaterialTheme.colorScheme.surface
    }

    val targetContainerColor = if (isFocused) (focusedContainerColor ?: defaultFocusedBg) else (unfocusedContainerColor ?: defaultUnfocusedBg)
    val targetContentColor = if (isFocused) (focusedContentColor ?: defaultFocusedText) else (unfocusedContentColor ?: defaultUnfocusedText)

    val currentContainerColor by animateColorAsState(targetValue = targetContainerColor, label = "chipContainerColor")
    val currentContentColor by animateColorAsState(targetValue = targetContentColor, label = "chipContentColor")

    val animatedTopStart by animateDpAsState(targetValue = if (isFocused) 24.dp else topStartRadius, label = "topStart")
    val animatedTopEnd by animateDpAsState(targetValue = if (isFocused) 24.dp else topEndRadius, label = "topEnd")
    val animatedBottomStart by animateDpAsState(targetValue = if (isFocused) 24.dp else bottomStartRadius, label = "bottomStart")
    val animatedBottomEnd by animateDpAsState(targetValue = if (isFocused) 24.dp else bottomEndRadius, label = "bottomEnd")

    val currentShape = RoundedCornerShape(
        topStart = animatedTopStart,
        topEnd = animatedTopEnd,
        bottomEnd = animatedBottomEnd,
        bottomStart = animatedBottomStart
    )

    val textStyle = if (showLabels) {
        MaterialTheme.typography.titleLarge.copy(
            color = currentContentColor,
            textAlign = TextAlign.Start
        )
    } else {
        MaterialTheme.typography.bodyLarge.copy(
            color = currentContentColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }

    Box(
        modifier = modifier
            .then(if (height != Dp.Unspecified) Modifier.height(height) else Modifier)
            .background(currentContainerColor, currentShape)
            .clip(currentShape),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = displayValue,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            interactionSource = interactionSource,
            textStyle = textStyle,
            singleLine = true,
            cursorBrush = SolidColor(currentContentColor),
            decorationBox = { innerTextField ->
                if (showLabels) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = label, style = MaterialTheme.typography.titleMedium, color = currentContentColor)
                        Text(text = " | ", style = MaterialTheme.typography.titleMedium, color = currentContentColor)
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (displayValue.isEmpty()) {
                                Text(
                                    text = placeholder.ifEmpty { " " },
                                    style = MaterialTheme.typography.titleLarge.copy(color = currentContentColor.copy(alpha = 0.5f))
                                )
                            }
                            innerTextField()
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (displayValue.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = textStyle.copy(color = currentContentColor.copy(alpha = 0.5f))
                            )
                        }
                        innerTextField()
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
            )
        )
    }
}
