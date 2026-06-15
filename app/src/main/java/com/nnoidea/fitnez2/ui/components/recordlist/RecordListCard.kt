package com.nnoidea.fitnez2.ui.components.recordlist

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.HistoryRepsField
import com.nnoidea.fitnez2.ui.components.HistorySetsField
import com.nnoidea.fitnez2.ui.components.HistoryWeightField
import com.nnoidea.fitnez2.ui.components.bottomsheet.ConnectedInputGroup
import com.nnoidea.fitnez2.core.localization.globalLocalization

internal val ColorRecordNeutralContainer @Composable get() = MaterialTheme.colorScheme.primary
internal val ColorRecordNeutralContent @Composable get() = MaterialTheme.colorScheme.onPrimary

internal val ColorRecordColoredContainer @Composable get() = MaterialTheme.colorScheme.secondaryContainer
internal val ColorRecordColoredContent @Composable get() = MaterialTheme.colorScheme.onSecondaryContainer


@Composable
fun RecordCard(
    exerciseName: String,
    sets: Int,
    reps: Int,
    weight: Double,
    timestamp: String?,
    showTimestamp: Boolean = false,
    isLight: Boolean,
    showTitle: Boolean,
    weightUnit: String,
    shape: androidx.compose.ui.graphics.Shape,
    prevIsSame: Boolean = false,
    nextIsSame: Boolean = false,
    showLabels: Boolean = false,
    onCardClick: (() -> Unit)? = null,
    onUpdate: (sets: Int, reps: Int, weight: Double) -> Unit
) {
    val containerColor = if (isLight) ColorRecordNeutralContainer else ColorRecordColoredContainer
    val contentColor = if (isLight) ColorRecordNeutralContent else ColorRecordColoredContent

    val view = LocalView.current

    val topPadding = if (prevIsSame) 1.5.dp else 2.dp
    val bottomPadding = if (nextIsSame) 1.5.dp else 2.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding)
            .clip(shape)
            .clickable(enabled = onCardClick != null) { 
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                onCardClick?.invoke()
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
                        visible = showTimestamp && timestamp != null,
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

@Composable
fun RecordCardCollapsed(
    isLight: Boolean,
    shape: androidx.compose.ui.graphics.Shape,
    prevIsSame: Boolean = false,
    nextIsSame: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val containerColor = if (isLight) ColorRecordNeutralContainer else ColorRecordColoredContainer
    val contentColor = if (isLight) ColorRecordNeutralContent else ColorRecordColoredContent
    val view = LocalView.current

    val topPadding = if (prevIsSame) 1.5.dp else 2.dp
    val bottomPadding = if (nextIsSame) 1.5.dp else 2.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding)
            .clip(shape)
            .height(10.dp)
            .clickable(enabled = onClick != null) {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                onClick?.invoke()
            },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { }
}
