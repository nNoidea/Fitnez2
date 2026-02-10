package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Visual style for bottom sheet input fields.
 * This is just the "skin" - logic is handled by SetsInput/RepsInput/WeightInput.
 */
@Composable
fun BottomSheetInputStyle(
    label: String,
    displayValue: String,
    placeholder: String,
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource,
    onValueChange: (String) -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false
) {
    val keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
    
    BasicTextField(
        value = displayValue,
        onValueChange = onValueChange,
        modifier = modifier
            .background(containerColor, RoundedCornerShape(24.dp)),
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            color = contentColor,
            textAlign = TextAlign.Start
        ),
        singleLine = false,
        maxLines = 1,
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
}
