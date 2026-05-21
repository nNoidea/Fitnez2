package com.nnoidea.fitnez2.ui.screenComponents.home

import com.nnoidea.fitnez2.ui.components.history.HistoryGridRow
import com.nnoidea.fitnez2.ui.components.history.HeaderLabel
import com.nnoidea.fitnez2.ui.components.history.HistoryRecordCard
import com.nnoidea.fitnez2.ui.components.history.HistoryCollapsedRecordCard
import com.nnoidea.fitnez2.ui.components.history.computeColorParityByName
import com.nnoidea.fitnez2.ui.components.history.computeColorParity
import com.nnoidea.fitnez2.ui.components.history.recordCardShape
import com.nnoidea.fitnez2.ui.components.history.HistoryUiModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.nnoidea.fitnez2.ui.components.bottomsheet.autoHideBottomSheet
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.TimeUtils
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.LocalAppDatabase
import com.nnoidea.fitnez2.data.LocalSettingsRepository
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Stable
import com.nnoidea.fitnez2.data.SettingsRepository
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.components.SwipeToDeleteContainer
import com.nnoidea.fitnez2.ui.components.HistorySetsField
import com.nnoidea.fitnez2.ui.components.HistoryRepsField
import com.nnoidea.fitnez2.ui.components.HistoryWeightField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

// HistoryUiModel moved to SharedHistoryComponents.kt

// -----------------------------------------------------------------------------
// UI Style Constants - Change these to tweak the list's look
// -----------------------------------------------------------------------------

// -----------------------------------------------------------------------------

// -----------------------------------------------------------------------------
// Public Smart Component
// -----------------------------------------------------------------------------

@Composable
fun ExerciseHistoryList(
    modifier: Modifier = Modifier,
    extraBottomPadding: Dp = 0.dp,
    filterExerciseIds: List<Int>? = null,
    useAlternatingColors: Boolean = true,
    enableAutoHide: Boolean = false,
) {
    val state = rememberExerciseHistoryListState(filterExerciseIds, useAlternatingColors)
    ExerciseHistoryList(state = state, modifier = modifier, extraBottomPadding = extraBottomPadding, enableAutoHide = enableAutoHide)
}

@Composable
fun ExerciseHistoryList(
    state: ExerciseHistoryListState,
    modifier: Modifier = Modifier,
    extraBottomPadding: Dp = 0.dp,
    enableAutoHide: Boolean = false,
) {
    val scope = rememberCoroutineScope()

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(28.dp)
        ) {
            if (!state.initialLoadDone) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return@Surface
            }

            ExerciseHistoryListContent(
                modifier = Modifier.fillMaxSize(),
                listState = state.listState,
                uiItems = state.uiItems,
                weightUnit = state.weightUnit,
                extraBottomPadding = extraBottomPadding,
                enableAutoHide = enableAutoHide,
                onUpdateRequest = { state.onUpdateRequest(it) },
                onDeleteRequest = { state.onDeleteRequest(it) },
                onDeleteGroupRequest = { state.onDeleteGroupRequest(it) }
            )
        }

        ScrollToTopButton(
            listState = state.listState,
            onClick = {
                scope.launch {
                    state.scrollToTop(null)
                }
            },
            extraBottomPadding = extraBottomPadding,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
        )
    }
}

// -----------------------------------------------------------------------------
// Scroll-to-Top FAB
// -----------------------------------------------------------------------------

@Composable
private fun ScrollToTopButton(
    listState: LazyListState,
    onClick: () -> Unit,
    extraBottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val globalUiState = com.nnoidea.fitnez2.ui.common.LocalGlobalUiState.current
    val view = LocalView.current
    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    LaunchedEffect(showButton) {
        globalUiState.isScrollToTopButtonVisible = showButton
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val animatedBottomPadding by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (globalUiState.isBottomSheetHidden) navBarPadding else extraBottomPadding,
        animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow),
        label = "fabBottomPadding"
    )

    AnimatedVisibility(
        visible = showButton,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier.padding(bottom = animatedBottomPadding)
    ) {
        FloatingActionButton(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                onClick()
            },
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}




// -----------------------------------------------------------------------------
// Stateless UI Components
// -----------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseHistoryListContent(
    modifier: Modifier,
    listState: LazyListState,
    uiItems: List<HistoryUiModel>,
    weightUnit: String,
    extraBottomPadding: Dp,
    enableAutoHide: Boolean,
    onUpdateRequest: (Record) -> Unit,
    onDeleteRequest: (Record) -> Unit,
    onDeleteGroupRequest: (List<Record>) -> Unit
) {

    val renderItems = remember(uiItems) { buildRenderItems(uiItems) }
    val expandedRecordIds = remember { mutableStateMapOf<Int, Boolean>() }
    val timestampTokens = remember { mutableStateMapOf<Int, Long>() }
    val scope = rememberCoroutineScope()

    fun showTimestampFor(recordId: Int) {
        val token = System.currentTimeMillis()
        timestampTokens[recordId] = token
        scope.launch {
            delay(5000)
            if (timestampTokens[recordId] == token) {
                timestampTokens.remove(recordId)
            }
        }
    }

    if (uiItems.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = globalLocalization.labelHistoryEmpty,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val firstGroupIndex = remember(renderItems) {
            renderItems.indexOfFirst { it is HistoryRenderItem.RecordGroup }
        }

        LazyColumn(
            modifier = modifier.autoHideBottomSheet(enableAutoHide),
            state = listState,
            contentPadding = PaddingValues(bottom = 80.dp + extraBottomPadding)
        ) {
            item(key = "top_spacer_anchor") {
                Spacer(modifier = Modifier.height(1.dp))
            }

            itemsIndexed(
                items = renderItems,
                key = { _, model ->
                     when(model) {
                         is HistoryRenderItem.Header -> "header_${model.item.section}_${model.item.date}"
                         is HistoryRenderItem.RecordGroup -> "record_group_${model.records.first().record.record.id}"
                         is HistoryRenderItem.BatchSeparator -> "separator_${model.item.index}"
                         is HistoryRenderItem.EvictedBatch -> "evicted_${model.item.index}"
                         is HistoryRenderItem.LoadingMore -> "loading_more"
                     }
                },
                contentType = { _, model ->
                    when(model) {
                        is HistoryRenderItem.Header -> "header"
                        is HistoryRenderItem.RecordGroup -> "record_group"
                        is HistoryRenderItem.BatchSeparator -> "separator"
                        is HistoryRenderItem.EvictedBatch -> "evicted"
                        is HistoryRenderItem.LoadingMore -> "loading"
                    }
                }
            ) { index, item ->
                when (item) {
                    is HistoryRenderItem.Header -> {
                        HistoryDateHeader(
                            date = item.item.date,
                            modifier = Modifier.animateItem()
                        )
                    }
                    is HistoryRenderItem.BatchSeparator -> {
                        OlderRecordsSeparator(modifier = Modifier.animateItem())
                    }
                    is HistoryRenderItem.EvictedBatch -> {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(item.item.heightDp.dp)
                        )
                    }
                    is HistoryRenderItem.LoadingMore -> {
                        LoadingMoreIndicator(modifier = Modifier.animateItem())
                    }
                    is HistoryRenderItem.RecordGroup -> {
                        val groupRecords = item.records
                        val isTopGroup = index == firstGroupIndex
                        val isGroupCollapsible = groupRecords.size > 1 && !isTopGroup
                        val isGroupExpanded = !isGroupCollapsible || groupRecords.any {
                            expandedRecordIds[it.record.record.id] == true
                        }
                        val topRecordId = groupRecords.first().record.record.id
                        val showTopTimestamp = timestampTokens.containsKey(topRecordId)
                        val prevRenderItem = if (index > 0) renderItems[index - 1] else null
                        val showLabelsForTop = (prevRenderItem is HistoryRenderItem.Header) || (prevRenderItem == null)

                        val onGroupTapped = {
                            if (isGroupCollapsible) {
                                if (isGroupExpanded) {
                                    groupRecords.forEach { expandedRecordIds.remove(it.record.record.id) }
                                } else {
                                    groupRecords.forEach { expandedRecordIds[it.record.record.id] = true }
                                }
                            }
                        }

                        if (isGroupCollapsible && !isGroupExpanded) {
                            SwipeToDeleteContainer(
                                onDelete = { onDeleteGroupRequest(groupRecords.map { it.record.record }) },
                                modifier = Modifier.animateItem()
                            ) {
                                Column {
                                    groupRecords.forEachIndexed { groupIndex, recordItem ->
                                        val prevIsSame = groupIndex > 0
                                        val nextIsSame = groupIndex < groupRecords.lastIndex
                                        val shape = when {
                                            groupIndex == 0 -> recordCardShape(prevIsSame, nextIsSame)
                                            groupIndex == groupRecords.lastIndex -> RoundedCornerShape(
                                                topStart = 4.dp,
                                                topEnd = 4.dp,
                                                bottomStart = 56.dp,
                                                bottomEnd = 56.dp
                                            )
                                            else -> RoundedCornerShape(2.dp)
                                        }
                                        val recordId = recordItem.record.record.id
                                        val timestamp = remember(recordItem.record.record.date) {
                                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                                .format(java.util.Date(recordItem.record.record.date))
                                        }

                                        key(recordId) {
                                            val lastIndex = groupRecords.lastIndex
                                            val showCollapsed = groupIndex >= kotlin.math.max(1, lastIndex - 1)
                                            if (groupIndex == 0) {
                                                HistoryRecordCard(
                                                    exerciseName = recordItem.record.exerciseName,
                                                    sets = recordItem.record.record.sets,
                                                    reps = recordItem.record.record.reps,
                                                    weight = recordItem.record.record.weight,
                                                    timestamp = timestamp,
                                                    showTimestamp = showTopTimestamp,
                                                    isLight = recordItem.isLight,
                                                    showTitle = true,
                                                    weightUnit = weightUnit,
                                                    shape = shape,
                                                    prevIsSame = prevIsSame,
                                                    nextIsSame = nextIsSame,
                                                    showLabels = showLabelsForTop,
                                                    onCardClick = {
                                                        onGroupTapped()
                                                        showTimestampFor(recordId)
                                                    },
                                                    onUpdate = { sets, reps, weight ->
                                                        onUpdateRequest(
                                                            recordItem.record.record.copy(
                                                                sets = sets,
                                                                reps = reps,
                                                                weight = weight
                                                            )
                                                        )
                                                    }
                                                )
                                            } else if (showCollapsed) {
                                                HistoryCollapsedRecordCard(
                                                    isLight = recordItem.isLight,
                                                    shape = shape,
                                                    prevIsSame = prevIsSame,
                                                    nextIsSame = nextIsSame,
                                                    onClick = {
                                                        onGroupTapped()
                                                        showTimestampFor(recordId)
                                                    }
                                                )
                                            } else Unit
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(modifier = Modifier.animateItem()) {
                                groupRecords.forEachIndexed { groupIndex, recordItem ->
                                    val recordId = recordItem.record.record.id
                                    val prevIsSame = groupIndex > 0
                                    val nextIsSame = groupIndex < groupRecords.lastIndex
                                    val shape = recordCardShape(prevIsSame, nextIsSame)
                                    val timestamp = remember(recordItem.record.record.date) {
                                        java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                            .format(java.util.Date(recordItem.record.record.date))
                                    }

                                    key(recordId) {
                                        SwipeToDeleteContainer(
                                            onDelete = { onDeleteRequest(recordItem.record.record) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            HistoryRecordCard(
                                                exerciseName = recordItem.record.exerciseName,
                                                sets = recordItem.record.record.sets,
                                                reps = recordItem.record.record.reps,
                                                weight = recordItem.record.record.weight,
                                                timestamp = timestamp,
                                                showTimestamp = timestampTokens.containsKey(recordId),
                                                isLight = recordItem.isLight,
                                                showTitle = groupIndex == 0,
                                                weightUnit = weightUnit,
                                                shape = shape,
                                                prevIsSame = prevIsSame,
                                                nextIsSame = nextIsSame,
                                                showLabels = showLabelsForTop && groupIndex == 0,
                                                onCardClick = {
                                                    if (groupIndex == 0) {
                                                        onGroupTapped()
                                                    }
                                                    showTimestampFor(recordId)
                                                },
                                                onUpdate = { sets, reps, weight ->
                                                    onUpdateRequest(
                                                        recordItem.record.record.copy(
                                                            sets = sets,
                                                            reps = reps,
                                                            weight = weight
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed class HistoryRenderItem {
    data class Header(val item: HistoryUiModel.Header) : HistoryRenderItem()
    data class BatchSeparator(val item: HistoryUiModel.BatchSeparator) : HistoryRenderItem()
    data class EvictedBatch(val item: HistoryUiModel.EvictedBatch) : HistoryRenderItem()
    data object LoadingMore : HistoryRenderItem()
    data class RecordGroup(val records: List<HistoryUiModel.RecordItem>) : HistoryRenderItem()
}

private fun buildRenderItems(uiItems: List<HistoryUiModel>): List<HistoryRenderItem> {
    if (uiItems.isEmpty()) return emptyList()

    val result = mutableListOf<HistoryRenderItem>()
    var index = 0
    while (index < uiItems.size) {
        when (val item = uiItems[index]) {
            is HistoryUiModel.Header -> {
                result.add(HistoryRenderItem.Header(item))
                index += 1
            }
            is HistoryUiModel.BatchSeparator -> {
                result.add(HistoryRenderItem.BatchSeparator(item))
                index += 1
            }
            is HistoryUiModel.EvictedBatch -> {
                result.add(HistoryRenderItem.EvictedBatch(item))
                index += 1
            }
            is HistoryUiModel.LoadingMore -> {
                result.add(HistoryRenderItem.LoadingMore)
                index += 1
            }
            is HistoryUiModel.RecordItem -> {
                val group = mutableListOf(item)
                var nextIndex = index + 1
                while (nextIndex < uiItems.size) {
                    val next = uiItems[nextIndex]
                    if (next is HistoryUiModel.RecordItem && next.record.exerciseName == item.record.exerciseName) {
                        group.add(next)
                        nextIndex += 1
                    } else {
                        break
                    }
                }
                result.add(HistoryRenderItem.RecordGroup(group))
                index = nextIndex
            }
        }
    }

    return result
}

// -----------------------------------------------------------------------------
// Separator & Loading Indicator
// -----------------------------------------------------------------------------

@Composable
private fun OlderRecordsSeparator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = globalLocalization.labelOlderRecords,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun LoadingMoreIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Extracted to SharedHistoryComponents.kt

@Composable
private fun HistoryDateHeader(
    date: Long,
    modifier: Modifier = Modifier
) {
    val globalUiState = LocalGlobalUiState.current
    val currentLocale = globalLocalization.appLocale
    val isToday = remember(date, globalUiState.currentDayKey) { android.text.format.DateUtils.isToday(date) }
    val isYesterday = remember(date, globalUiState.currentDayKey) {
        android.text.format.DateUtils.isToday(date + android.text.format.DateUtils.DAY_IN_MILLIS)
    }

    val dateString = remember(date, currentLocale) { 
        globalLocalization.formatDateShort(date) 
    }
    
    val dayName = remember(date, currentLocale, isToday, isYesterday) {
        if (isToday) globalLocalization.labelToday
        else if (isYesterday) globalLocalization.labelYesterday
        else globalLocalization.formatDayName(date)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = dateString,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

// Extracted to SharedHistoryComponents.kt
