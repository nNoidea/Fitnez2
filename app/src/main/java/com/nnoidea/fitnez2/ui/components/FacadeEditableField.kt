package com.nnoidea.fitnez2.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared shell for input fields that use a transparent [BasicTextField] overlaid
 * with a clickable [Text]-based facade when not focused.
 *
 * Handles: animated colors, animated corner radii, focus tracking,
 * alpha fade between facade/edit mode, and click-to-focus.
 */
@Composable
internal fun FacadeEditableField(
    value: String,
    onValueChange: (String) -> Unit,
    interactionSource: MutableInteractionSource,
    isFocused: Boolean,
    isDecimal: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = Dp.Unspecified,
    unfocusedContainerColor: Color,
    focusedContainerColor: Color,
    unfocusedContentColor: Color,
    focusedContentColor: Color,
    textStyle: TextStyle,
    topStartRadius: Dp = 24.dp,
    topEndRadius: Dp = 24.dp,
    bottomStartRadius: Dp = 24.dp,
    bottomEndRadius: Dp = 24.dp,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit, contentColor: Color, textStyle: TextStyle) -> Unit,
    facade: @Composable (clickModifier: Modifier, contentColor: Color) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    val animatedTopStart by animateDpAsState(
        targetValue = if (isFocused) 24.dp else topStartRadius,
        label = "topStartRadius"
    )
    val animatedTopEnd by animateDpAsState(
        targetValue = if (isFocused) 24.dp else topEndRadius,
        label = "topEndRadius"
    )
    val animatedBottomStart by animateDpAsState(
        targetValue = if (isFocused) 24.dp else bottomStartRadius,
        label = "bottomStartRadius"
    )
    val animatedBottomEnd by animateDpAsState(
        targetValue = if (isFocused) 24.dp else bottomEndRadius,
        label = "bottomEndRadius"
    )

    val currentShape = RoundedCornerShape(
        topStart = animatedTopStart,
        topEnd = animatedTopEnd,
        bottomEnd = animatedBottomEnd,
        bottomStart = animatedBottomStart
    )

    val currentContainerColor by animateColorAsState(
        targetValue = if (isFocused) focusedContainerColor else unfocusedContainerColor,
        label = "containerColor"
    )
    val currentContentColor by animateColorAsState(
        targetValue = if (isFocused) focusedContentColor else unfocusedContentColor,
        label = "contentColor"
    )

    val effectiveTextStyle = textStyle.copy(color = currentContentColor)

    val facadeClickModifier = Modifier
        .fillMaxSize()
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            focusRequester.requestFocus()
        }

    Box(
        modifier = modifier
            .then(if (height != Dp.Unspecified) Modifier.height(height) else Modifier)
            .background(currentContainerColor, currentShape)
            .clip(currentShape),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isFocused) 1f else 0f)
                .focusRequester(focusRequester),
            interactionSource = interactionSource,
            textStyle = effectiveTextStyle,
            singleLine = true,
            cursorBrush = SolidColor(currentContentColor),
            decorationBox = { innerTextField ->
                decorationBox(innerTextField, currentContentColor, effectiveTextStyle)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
            )
        )

        if (!isFocused) {
            facade(facadeClickModifier, currentContentColor)
        }
    }
}
