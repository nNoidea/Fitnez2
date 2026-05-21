package com.nnoidea.fitnez2.ui.components.history

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.components.HistoryRepsField
import com.nnoidea.fitnez2.ui.components.HistorySetsField
import com.nnoidea.fitnez2.ui.components.HistoryWeightField
import com.nnoidea.fitnez2.ui.components.bottomsheet.ConnectedInputGroup
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.nnoidea.fitnez2.core.localization.globalLocalization

internal val ColorHistoryNeutralContainer @Composable get() = MaterialTheme.colorScheme.primary
internal val ColorHistoryNeutralContent @Composable get() = MaterialTheme.colorScheme.onPrimary

internal val ColorHistoryColoredContainer @Composable get() = MaterialTheme.colorScheme.secondaryContainer
internal val ColorHistoryColoredContent @Composable get() = MaterialTheme.colorScheme.onSecondaryContainer

sealed class HistoryUiModel {
    data class RecordItem(val record: RecordWithExercise, val isLight: Boolean) : HistoryUiModel()
    data class Header(val date: Long, val section: Int = 0) : HistoryUiModel()
    data class BatchSeparator(val index: Int) : HistoryUiModel()
    /** Placeholder for an evicted batch — preserves scroll height. */
    data class EvictedBatch(val index: Int, val heightDp: Int) : HistoryUiModel()
    data object LoadingMore : HistoryUiModel()
}

@Composable
fun HistoryGridRow(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    col1: @Composable BoxScope.() -> Unit,
    col2: @Composable BoxScope.() -> Unit,
    col3: @Composable BoxScope.() -> Unit,
    col4: @Composable BoxScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.weight(1.5f)) {
            col1()
        }
        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
            col2()
        }
        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
            col3()
        }
        Box(modifier = Modifier.width(65.dp), contentAlignment = Alignment.Center) {
            col4()
        }
    }
}

@Composable
fun HeaderLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun HistoryRecordCard(
    exerciseName: String,
    sets: Int,
    reps: Int,
    weight: Double,
    timestamp: String?,
    isLight: Boolean,
    showTitle: Boolean,
    weightUnit: String,
    shape: androidx.compose.ui.graphics.Shape,
    prevIsSame: Boolean = false,
    nextIsSame: Boolean = false,
    showLabels: Boolean = false,
    onUpdate: (sets: Int, reps: Int, weight: Double) -> Unit
) {
    val containerColor = if (isLight) ColorHistoryNeutralContainer else ColorHistoryColoredContainer
    val contentColor = if (isLight) ColorHistoryNeutralContent else ColorHistoryColoredContent

    var isExpanded by remember { mutableStateOf(false) }

    if (isExpanded) {
        LaunchedEffect(Unit) {
            delay(5000)
            isExpanded = false
        }
    }

    val view = LocalView.current

    val topPadding = if (prevIsSame) 1.5.dp else 2.dp
    val bottomPadding = if (nextIsSame) 1.5.dp else 2.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding)
            .clip(shape)
            .clickable(enabled = timestamp != null) { 
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                isExpanded = !isExpanded 
            },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            if (showTitle) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Unspecified
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp)
                )
            }

            if (showLabels) {
                Row(
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = if (showTitle) 2.dp else 6.dp,
                            bottom = 0.dp
                        )
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(
                        modifier = Modifier.weight(3f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = globalLocalization.labelSets,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = contentColor.copy(alpha = 0.85f)
                            )
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = globalLocalization.labelReps,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = contentColor.copy(alpha = 0.85f)
                            )
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = weightUnit,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = contentColor.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = if (showTitle) 4.dp else 6.dp
                    )
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isExpanded && timestamp != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = timestamp ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }
                
                ConnectedInputGroup(
                    modifier = Modifier.weight(3f),
                    spacing = 2.dp,
                    outerCornerRadius = 24.dp,
                    innerCornerRadius = 8.dp,
                    items = listOf(
                        { ts, te, bs, be ->
                            HistorySetsField(
                                value = sets,
                                contentColor = contentColor,
                                onValidChange = { onUpdate(it, reps, weight) },
                                modifier = Modifier.weight(1f),
                                topStartRadius = ts,
                                topEndRadius = te,
                                bottomStartRadius = bs,
                                bottomEndRadius = be
                            )
                        },
                        { ts, te, bs, be ->
                            HistoryRepsField(
                                value = reps,
                                contentColor = contentColor,
                                onValidChange = { onUpdate(sets, it, weight) },
                                modifier = Modifier.weight(1f),
                                topStartRadius = ts,
                                topEndRadius = te,
                                bottomStartRadius = bs,
                                bottomEndRadius = be
                            )
                        },
                        { ts, te, bs, be ->
                            HistoryWeightField(
                                value = weight,
                                contentColor = contentColor,
                                onValidChange = { onUpdate(sets, reps, it) },
                                modifier = Modifier.weight(1f),
                                topStartRadius = ts,
                                topEndRadius = te,
                                bottomStartRadius = bs,
                                bottomEndRadius = be
                            )
                        }
                    )
                )
            }
            
        }
    }
}
