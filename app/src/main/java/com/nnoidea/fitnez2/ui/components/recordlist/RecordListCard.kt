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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.input.SetsRepsWeightGroup
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.theme.ColorRecordColoredContainer
import com.nnoidea.fitnez2.ui.theme.ColorRecordColoredContent
import com.nnoidea.fitnez2.ui.theme.ColorRecordNeutralContainer
import com.nnoidea.fitnez2.ui.theme.ColorRecordNeutralContent

@Composable
fun RecordCard(
    recordItem: RecordWithExercise,
    timestamp: String?,
    showTimestamp: Boolean = false,
    isLight: Boolean,
    showTitle: Boolean,
    weightUnit: String,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    showLabels: Boolean = false,
    onCardClick: (() -> Unit)? = null,
    onUpdateRequest: ((Record) -> Unit)? = null
) {
    val record = recordItem.record
    val exerciseName = recordItem.exerciseName
    val containerColor = if (isLight) ColorRecordNeutralContainer else ColorRecordColoredContainer
    val contentColor = if (isLight) ColorRecordNeutralContent else ColorRecordColoredContent

    val view = LocalView.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("record_card_$exerciseName")
            .clip(shape)
            .clickable(enabled = onCardClick != null) { 
                com.nnoidea.fitnez2.ui.components.haptics.HapticEngine.performClick(view.context, scale = 0.4f)
                onCardClick?.invoke()
            },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .testTag("record_card")
        ) {
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
                
                SetsRepsWeightGroup(
                    sets = record.sets.toString(),
                    reps = record.reps.toString(),
                    weight = record.weight.toString(),
                    weightUnit = weightUnit,
                    showLabels = false,
                    unfocusedContainerColor = contentColor.copy(alpha = 0.12f),
                    unfocusedContentColor = contentColor,
                    focusedContainerColor = contentColor,
                    focusedContentColor = containerColor,
                    modifier = Modifier.weight(3f),
                    onSetsChange = { onUpdateRequest?.invoke(record.copy(sets = it)) },
                    onRepsChange = { onUpdateRequest?.invoke(record.copy(reps = it)) },
                    onWeightChange = { onUpdateRequest?.invoke(record.copy(weight = it)) }
                )
            }
            
        }
    }
}

@Composable
fun RecordCardCollapsed(
    isLight: Boolean,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val containerColor = if (isLight) ColorRecordNeutralContainer else ColorRecordColoredContainer
    val contentColor = if (isLight) ColorRecordNeutralContent else ColorRecordColoredContent
    val view = LocalView.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .height(10.dp)
            .clickable(enabled = onClick != null) {
                com.nnoidea.fitnez2.ui.components.haptics.HapticEngine.performClick(view.context, scale = 0.3f)
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
