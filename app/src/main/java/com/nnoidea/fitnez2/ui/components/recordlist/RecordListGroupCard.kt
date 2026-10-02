package com.nnoidea.fitnez2.ui.components.recordlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.components.SwipeToDeleteContainer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordListGroupCard(
    groupRecords: List<RecordWithExercise>,
    isLight: Boolean,
    isTopGroup: Boolean,
    showCollapse: Boolean,
    showSwipe: Boolean,
    showHeaders: Boolean,
    weightUnit: String,
    expandedRecordIds: SnapshotStateMap<String, Boolean>,
    timestampTokens: SnapshotStateMap<String, Long>,
    showTimestamp: (String) -> Unit,
    onUpdateRequest: ((Record) -> Unit)?,
    onDeleteRequest: ((Record) -> Unit)?,
    onDeleteGroupRequest: ((List<Record>) -> Unit)?,
    onSwipeRight: (() -> Unit)? = null,
    prevRenderItem: RecordDisplayItem?,
    modifier: Modifier = Modifier
) {
    val isGroupCollapsible = showCollapse && groupRecords.size > 1 && !isTopGroup
    val isGroupExpanded = !isGroupCollapsible || groupRecords.any {
        expandedRecordIds[it.record.id] == true
    }
    val topRecordId = groupRecords.first().record.id
    val showTopTimestamp = timestampTokens.containsKey(topRecordId)
    val showLabelsForTop = showHeaders && (
        (prevRenderItem is RecordDisplayItem.DateHeader) || (prevRenderItem == null)
    )

    val onGroupTapped: () -> Unit = {
        if (isGroupCollapsible) {
            if (isGroupExpanded) {
                groupRecords.forEach { expandedRecordIds.remove(it.record.id) }
            } else {
                groupRecords.forEach { expandedRecordIds[it.record.id] = true }
            }
        }
    }

    val groupPadding = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp)
    if (isGroupCollapsible && !isGroupExpanded) {
        if (showSwipe) {
            SwipeToDeleteContainer(
                onDelete = {
                    onDeleteGroupRequest?.invoke(groupRecords.map { it.record })
                },
                modifier = modifier.fillMaxWidth().then(groupPadding)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CollapsedGroupItems(
                        groupRecords, isLight, weightUnit,
                        showTopTimestamp, showLabelsForTop,
                        onGroupTapped, showTimestamp, onUpdateRequest
                    )
                }
            }
        } else {
            Column(modifier = modifier.fillMaxWidth().then(groupPadding)) {
                CollapsedGroupItems(
                    groupRecords, isLight, weightUnit,
                    showTopTimestamp, showLabelsForTop,
                    onGroupTapped, showTimestamp, onUpdateRequest
                )
            }
        }
    } else {
        Column(modifier = modifier) {
            groupRecords.forEachIndexed { groupIndex, recordItem ->
                val recordId = recordItem.record.id
                val prevIsSame = groupIndex > 0
                val nextIsSame = groupIndex < groupRecords.lastIndex
                val shape = recordCardShape(prevIsSame, nextIsSame)
                val topPadding = if (prevIsSame) 1.dp else 2.dp
                val bottomPadding = if (nextIsSame) 1.dp else 2.dp
                val rowPadding = Modifier.padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding)
                val timestamp = remember(recordItem.record.date) {
                    com.nnoidea.fitnez2.core.TimeUtils.formatTime(recordItem.record.date)
                }

                key(recordId) {
                    val cardContent: @Composable (Modifier) -> Unit = { cardModifier ->
                        RecordCard(
                            exerciseName = recordItem.exerciseName,
                            sets = recordItem.record.sets,
                            reps = recordItem.record.reps,
                            weight = recordItem.record.weight,
                            timestamp = timestamp,
                            showTimestamp = timestampTokens.containsKey(recordId),
                            isLight = isLight,
                            showTitle = groupIndex == 0,
                            weightUnit = weightUnit,
                            shape = shape,
                            modifier = cardModifier,
                            prevIsSame = prevIsSame,
                            nextIsSame = nextIsSame,
                            showLabels = showLabelsForTop && groupIndex == 0,
                            onCardClick = {
                                if (groupIndex == 0) onGroupTapped()
                                showTimestamp(recordId)
                            },
                            onUpdate = { sets, reps, weight ->
                                onUpdateRequest?.invoke(
                                    recordItem.record.copy(
                                        sets = sets, reps = reps, weight = weight
                                    )
                                )
                            }
                        )
                    }
                    if (showSwipe) {
                        SwipeToDeleteContainer(
                            onDelete = { onDeleteRequest?.invoke(recordItem.record) },
                            modifier = Modifier.fillMaxWidth().then(rowPadding)
                        ) {
                            cardContent(Modifier.fillMaxWidth())
                        }
                    } else {
                        cardContent(Modifier.fillMaxWidth().then(rowPadding))
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsedGroupItems(
    groupRecords: List<RecordWithExercise>,
    isLight: Boolean,
    weightUnit: String,
    showTopTimestamp: Boolean,
    showLabelsForTop: Boolean,
    onGroupTapped: () -> Unit,
    showTimestamp: (String) -> Unit,
    onUpdateRequest: ((Record) -> Unit)?
) {
    groupRecords.forEachIndexed { groupIndex, recordItem ->
        val prevIsSame = groupIndex > 0
        val nextIsSame = groupIndex < groupRecords.lastIndex
        val shape = when {
            groupIndex == 0 -> recordCardShape(prevIsSame, nextIsSame)
            groupIndex == groupRecords.lastIndex -> RoundedCornerShape(
                topStart = 4.dp, topEnd = 4.dp, bottomStart = 56.dp, bottomEnd = 56.dp
            )
            else -> RoundedCornerShape(2.dp)
        }
        val recordId = recordItem.record.id
        val timestamp = remember(recordItem.record.date) {
            com.nnoidea.fitnez2.core.TimeUtils.formatTime(recordItem.record.date)
        }

        key(recordId) {
            val lastIndex = groupRecords.lastIndex
            val showCollapsedCard = groupIndex >= kotlin.math.max(1, lastIndex - 1)
            val topPadding = if (prevIsSame) 1.dp else 0.dp
            val bottomPadding = if (nextIsSame) 1.dp else 0.dp
            val itemModifier = Modifier.fillMaxWidth().padding(top = topPadding, bottom = bottomPadding)

            if (groupIndex == 0) {
                RecordCard(
                    exerciseName = recordItem.exerciseName,
                    sets = recordItem.record.sets,
                    reps = recordItem.record.reps,
                    weight = recordItem.record.weight,
                    timestamp = timestamp,
                    showTimestamp = showTopTimestamp,
                    isLight = isLight,
                    showTitle = true,
                    weightUnit = weightUnit,
                    shape = shape,
                    modifier = itemModifier,
                    prevIsSame = prevIsSame,
                    nextIsSame = nextIsSame,
                    showLabels = showLabelsForTop,
                    onCardClick = {
                        onGroupTapped()
                        showTimestamp(recordId)
                    },
                    onUpdate = { sets, reps, weight ->
                        onUpdateRequest?.invoke(
                            recordItem.record.copy(
                                sets = sets, reps = reps, weight = weight
                            )
                        )
                    }
                )
            } else if (showCollapsedCard) {
                RecordCardCollapsed(
                    isLight = isLight,
                    shape = shape,
                    modifier = itemModifier,
                    prevIsSame = prevIsSame,
                    nextIsSame = nextIsSame,
                    onClick = {
                        onGroupTapped()
                        showTimestamp(recordId)
                    }
                )
            }
        }
    }
}
