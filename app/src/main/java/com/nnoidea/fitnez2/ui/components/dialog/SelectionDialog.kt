package com.nnoidea.fitnez2.ui.components.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import kotlin.math.roundToInt

@Composable
fun SelectionDialog(
    show: Boolean,
    title: String = "",
    onDismissRequest: () -> Unit,
    bodyText: String? = null,
    buttons: @Composable (androidx.compose.foundation.layout.BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (show) {
        PredictiveModal(
            show = show,
            onDismissRequest = onDismissRequest
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = if (bodyText != null) 8.dp else 16.dp)
                    )
                }

                if (bodyText != null) {
                    Text(
                        text = bodyText,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Custom Scrollbar Logic
                val scrollState = rememberScrollState()
                val windowInfo = androidx.compose.ui.platform.LocalWindowInfo.current
                val density = LocalDensity.current
                val screenHeight = remember(windowInfo, density) {
                    with(density) { windowInfo.containerSize.height.toDp() }
                }
                var columnHeightPx by remember { mutableFloatStateOf(0f) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .heightIn(max = screenHeight * 0.6f)
                            .padding(end = 4.dp) // Space for scrollbar
                            .onGloballyPositioned { coordinates ->
                                columnHeightPx = coordinates.size.height.toFloat()
                            }
                            .verticalScroll(scrollState)
                    ) {
                        content()
                    }

                    // Vertical Scrollbar Overlay
                    val scrollbarVisible = scrollState.maxValue > 0

                    if (scrollbarVisible && columnHeightPx > 0f) {
                        val scrollbarHeight by remember {
                            derivedStateOf {
                                val viewportHeight = columnHeightPx
                                val contentHeight = viewportHeight + scrollState.maxValue
                                if (contentHeight > 0) {
                                    (viewportHeight * (viewportHeight / contentHeight)).coerceAtLeast(40f)
                                } else 0f
                            }
                        }

                        val scrollbarOffset by remember {
                            derivedStateOf {
                                val maxScroll = scrollState.maxValue.toFloat()
                                if (maxScroll <= 0f) return@derivedStateOf 0f
                                
                                val availableTrack = columnHeightPx - scrollbarHeight
                                val scrollProgress = scrollState.value.toFloat() / maxScroll
                                
                                availableTrack * scrollProgress
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(4.dp)
                                .height(with(LocalDensity.current) { columnHeightPx.toDp() }) 
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(with(LocalDensity.current) { scrollbarHeight.toDp() })
                                    .offset { IntOffset(0, scrollbarOffset.roundToInt()) }
                                    .background(
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }

                if (buttons != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        buttons()
                    }
                }
            }
        }
    }
}

@Composable
fun <T> RadioSelectionDialog(
    show: Boolean,
    title: String,
    options: List<T>,
    selectedValue: T,
    onValueSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    labelProvider: (T) -> String,
    bodyText: String? = null
) {
    SelectionDialog(
        show = show,
        title = title,
        onDismissRequest = onDismissRequest,
        bodyText = bodyText,
        buttons = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(globalLocalization.labelCancel)
            }
        }
    ) {
        options.forEach { option ->
            RadioOption(
                text = labelProvider(option),
                selected = option == selectedValue,
                onClick = { onValueSelected(option) }
            )
        }
    }
}

@Composable
private fun RadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // Handled by Row
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}
