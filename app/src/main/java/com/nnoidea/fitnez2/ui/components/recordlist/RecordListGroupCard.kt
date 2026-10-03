package com.nnoidea.fitnez2.ui.components.recordlist

import androidx.compose.foundation.layout.Box
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
import com.nnoidea.fitnez2.core.TimeUtils
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.components.SwipeToDeleteContainer

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
    prevRenderItem: RecordDisplayItem?,
    modifier: Modifier = Modifier
) {
    val canCollapse = showCollapse && groupRecords.size > 1 && !isTopGroup
    val isCollapsed = canCollapse && groupRecords.none { expandedRecordIds[it.record.id] == true }
    val showLabelsForTop = showHeaders && (prevRenderItem == null || prevRenderItem is RecordDisplayItem.DateHeader)
    val lastIndex = groupRecords.lastIndex

    val onGroupTapped = {
        if (canCollapse) {
            val shouldExpand = isCollapsed
            groupRecords.forEach {
                if (shouldExpand) expandedRecordIds[it.record.id] = true else expandedRecordIds.remove(it.record.id)
            }
        }
    }

    if (isCollapsed) {
        val groupPadding = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp)
        MaybeSwipeContainer(
            enabled = showSwipe,
            onDelete = { onDeleteGroupRequest?.invoke(groupRecords.map { it.record }) },
            modifier = modifier.fillMaxWidth().then(groupPadding)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val collapsedIndices = listOf(0) + (maxOf(1, lastIndex - 1)..lastIndex)

                collapsedIndices.forEach { groupIndex ->
                    val recordItem = groupRecords[groupIndex]
                    val recordId = recordItem.record.id
                    val isFirst = groupIndex == 0

                    key(recordId) {
                        GroupRecordRow(
                            recordItem = recordItem,
                            groupIndex = groupIndex,
                            lastIndex = lastIndex,
                            isLight = isLight,
                            weightUnit = weightUnit,
                            showTimestamp = isFirst && timestampTokens.containsKey(recordId),
                            showLabels = isFirst && showLabelsForTop,
                            showTitle = isFirst,
                            isCollapsed = true,
                            modifier = Modifier.fillMaxWidth().padding(top = if (groupIndex > 0) 2.dp else 0.dp),
                            onCardClick = {
                                onGroupTapped()
                                showTimestamp(recordId)
                            },
                            onUpdateRequest = onUpdateRequest
                        )
                    }
                }
            }
        }
    } else {
        Column(modifier = modifier) {
            groupRecords.forEachIndexed { groupIndex, recordItem ->
                val recordId = recordItem.record.id
                val topPadding = if (groupIndex > 0) 1.dp else 2.dp
                val bottomPadding = if (groupIndex < lastIndex) 1.dp else 2.dp
                val rowPadding = Modifier.padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding)

                key(recordId) {
                    MaybeSwipeContainer(
                        enabled = showSwipe,
                        onDelete = { onDeleteRequest?.invoke(recordItem.record) },
                        modifier = Modifier.fillMaxWidth().then(rowPadding)
                    ) {
                        GroupRecordRow(
                            recordItem = recordItem,
                            groupIndex = groupIndex,
                            lastIndex = lastIndex,
                            isLight = isLight,
                            weightUnit = weightUnit,
                            showTimestamp = timestampTokens.containsKey(recordId),
                            showLabels = groupIndex == 0 && showLabelsForTop,
                            showTitle = groupIndex == 0,
                            isCollapsed = false,
                            modifier = Modifier.fillMaxWidth(),
                            onCardClick = {
                                if (groupIndex == 0) onGroupTapped()
                                showTimestamp(recordId)
                            },
                            onUpdateRequest = onUpdateRequest
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupRecordRow(
    recordItem: RecordWithExercise,
    groupIndex: Int,
    lastIndex: Int,
    isLight: Boolean,
    weightUnit: String,
    showTimestamp: Boolean,
    showLabels: Boolean,
    showTitle: Boolean,
    isCollapsed: Boolean,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit,
    onUpdateRequest: ((Record) -> Unit)?
) {
    val shape = when {
        isCollapsed && groupIndex == lastIndex && lastIndex > 0 ->
            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 56.dp, bottomEnd = 56.dp)
        isCollapsed && groupIndex > 0 ->
            RoundedCornerShape(2.dp)
        else ->
            recordCardShape(prevSame = groupIndex > 0, nextSame = groupIndex < lastIndex)
    }

    if (isCollapsed && groupIndex > 0) {
        RecordCardCollapsed(
            isLight = isLight,
            shape = shape,
            modifier = modifier,
            onClick = onCardClick
        )
    } else {
        val timestamp = remember(recordItem.record.date) {
            TimeUtils.formatTime(recordItem.record.date)
        }
        RecordCard(
            recordItem = recordItem,
            timestamp = timestamp,
            showTimestamp = showTimestamp,
            isLight = isLight,
            showTitle = showTitle,
            weightUnit = weightUnit,
            shape = shape,
            modifier = modifier,
            showLabels = showLabels,
            onCardClick = onCardClick,
            onUpdateRequest = onUpdateRequest
        )
    }
}

@Composable
private fun MaybeSwipeContainer(
    enabled: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (enabled) {
        SwipeToDeleteContainer(onDelete = onDelete, modifier = modifier, content = content)
    } else {
        Box(modifier = modifier) { content() }
    }
}
