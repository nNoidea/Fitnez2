package com.nnoidea.fitnez2.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
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

sealed class HistoryUiModel {
    data class RecordItem(val record: RecordWithExercise, val isLight: Boolean) : HistoryUiModel()
    data class Header(val date: Long, val section: Int = 0) : HistoryUiModel()
    data class BatchSeparator(val index: Int) : HistoryUiModel()
    /** Placeholder for an evicted batch — preserves scroll height. */
    data class EvictedBatch(val index: Int, val heightDp: Int) : HistoryUiModel()
    data object LoadingMore : HistoryUiModel()
}

// -----------------------------------------------------------------------------
// UI Style Constants - Change these to tweak the list's look
// -----------------------------------------------------------------------------

private val ColorHistoryNeutralContainer @Composable get() = MaterialTheme.colorScheme.primary
private val ColorHistoryNeutralContent @Composable get() = MaterialTheme.colorScheme.onPrimary

private val ColorHistoryColoredContainer @Composable get() = MaterialTheme.colorScheme.secondaryContainer
private val ColorHistoryColoredContent @Composable get() = MaterialTheme.colorScheme.onSecondaryContainer

// -----------------------------------------------------------------------------
// Public Smart Component
// -----------------------------------------------------------------------------

@Composable
fun ExerciseHistoryList(
    modifier: Modifier = Modifier,
    extraBottomPadding: Dp = 0.dp,
    selectedExerciseId: Int? = null,
    useAlternatingColors: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val database = LocalAppDatabase.current
    val dao = database.recordDao()

    val settingsRepository = LocalSettingsRepository.current
    val weightUnit by settingsRepository.weightUnitFlow.collectAsState(initial = "kg")

    val exerciseDao = database.exerciseDao()
    val exercisesList by exerciseDao.getAllExercisesFlow().collectAsState(initial = emptyList())
    val exerciseMap = remember(exercisesList) {
        exercisesList.associate { it.id to it.name }
    }

    // =====================================================================
    //  ScrollEngine — all batch/eviction/buffer logic lives here.
    //  See ScrollEngine.kt for internals. Think twice before modifying.
    // =====================================================================
    val engine = remember(selectedExerciseId) {
        ScrollEngine(dao, selectedExerciseId)
    }

    // One-shot load (or when filter changes — engine is re-created via key)
    LaunchedEffect(engine) {
        engine.loadInitial()
    }

    // Orphan cleanup when exercises are deleted (CASCADE)
    LaunchedEffect(exerciseMap) {
        if (engine.initialLoadDone && exerciseMap.isNotEmpty()) {
            engine.removeOrphanedRecords(exerciseMap.keys)
        }
    }

    // Build UI model for recent records (section 0)
    val recentUiItems = remember(engine.recentRecords, exerciseMap, useAlternatingColors) {
        buildUiItems(engine.recentRecords, exerciseMap, useAlternatingColors, section = 0)
    }

    // Build UI models for each loaded batch independently.
    // Evicted (null) batches produce a single EvictedBatch placeholder.
    val olderBatchUiItems = remember(engine.olderBatches, engine.batchHeights, engine.batchSizes, exerciseMap, useAlternatingColors) {
        engine.olderBatches.mapIndexed { i, batch ->
            if (batch != null) {
                buildUiItems(batch, exerciseMap, useAlternatingColors, section = i + 1)
            } else {
                val height = engine.batchHeights[i]
                    ?: ScrollEngine.estimateBatchHeightDp(engine.batchSizes.getOrElse(i) { ScrollEngine.OLDER_BATCH_SIZE })
                listOf(HistoryUiModel.EvictedBatch(i, height))
            }
        }
    }

    // Combine into a single flat list for the LazyColumn
    val allUiItems = remember(recentUiItems, olderBatchUiItems, engine.hasMoreOlderRecords, engine.isLoadingMore) {
        buildList {
            addAll(recentUiItems)
            for ((batchIndex, batchItems) in olderBatchUiItems.withIndex()) {
                if (batchItems.isEmpty()) continue

                val isEvicted = batchItems.firstOrNull() is HistoryUiModel.EvictedBatch
                if (!isEvicted) {
                    val lastHeaderDate = filterIsInstance<HistoryUiModel.Header>().lastOrNull()?.date
                    val batchStart = if (
                        lastHeaderDate != null &&
                        batchItems.firstOrNull() is HistoryUiModel.Header &&
                        (batchItems.first() as HistoryUiModel.Header).date == lastHeaderDate
                    ) batchItems.drop(1) else batchItems

                    add(HistoryUiModel.BatchSeparator(batchIndex))
                    addAll(batchStart)
                } else {
                    add(HistoryUiModel.BatchSeparator(batchIndex))
                    addAll(batchItems)
                }
            }
            if (olderBatchUiItems.isEmpty() && engine.hasMoreOlderRecords) {
                add(HistoryUiModel.BatchSeparator(0))
            }
            if (engine.isLoadingMore) {
                add(HistoryUiModel.LoadingMore)
            }
        }
    }

    val globalUiState = LocalGlobalUiState.current
    val listState = rememberLazyListState()

    // Always-fresh reference for the coroutine — avoids stale closure after eviction
    val latestUiItems by rememberUpdatedState(allUiItems)

    // ---- ScrollEngine: batch loading, eviction & reloading ----
    LaunchedEffect(listState, engine) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = layoutInfo.totalItemsCount
            Triple(firstVisible, lastVisible, total)
        }.collect { (firstVisible, lastVisible, total) ->
            // 1. Load next batch when near the bottom
            engine.loadNextBatchIfNeeded(lastVisible, total)

            // 2. Determine which batch the viewport is in
            if (engine.olderBatches.isEmpty()) return@collect

            val visibleBatches = mutableSetOf<Int>()
            val currentItems = latestUiItems
            for (idx in firstVisible..lastVisible) {
                when (val item = currentItems.getOrNull(idx)) {
                    is HistoryUiModel.BatchSeparator -> visibleBatches.add(item.index)
                    is HistoryUiModel.EvictedBatch -> visibleBatches.add(item.index)
                    is HistoryUiModel.RecordItem -> {
                        for (j in idx downTo 0) {
                            val prev = currentItems.getOrNull(j)
                            if (prev is HistoryUiModel.BatchSeparator) {
                                visibleBatches.add(prev.index)
                                break
                            }
                        }
                    }
                    else -> {}
                }
            }

            // 3. Evict far batches, reload near ones
            engine.evictAndReload(visibleBatches.maxOrNull() ?: 0)
        }
    }

    // Scroll trigger: Receive signal
    LaunchedEffect(Unit) {
        globalUiState.signalFlow.collect { signal ->
            if (signal is com.nnoidea.fitnez2.ui.common.UiSignal.ScrollToTop) {
                signal.recordId?.let { engine.prependNewRecord(it) }
                listState.animateScrollToItem(0)
            }
        }
    }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(28.dp)
        ) {
            if (!engine.initialLoadDone) return@Surface

            ExerciseHistoryListContent(
                modifier = Modifier.fillMaxSize(),
                listState = listState,
                uiItems = allUiItems,
                weightUnit = weightUnit,
                extraBottomPadding = extraBottomPadding,
                onUpdateRequest = { updatedRecord ->
                    scope.launch {
                        try {
                            engine.updateRecord(updatedRecord)
                        } catch (_: Exception) { }
                    }
                },
                onDeleteRequest = { record ->
                    scope.launch {
                        val ctx = engine.deleteRecord(record)
                        globalUiState.showSnackbar(
                            message = globalLocalization.labelRecordDeleted,
                            actionLabel = globalLocalization.labelUndo,
                            onActionPerformed = {
                                scope.launch { engine.undoDelete(ctx) }
                            }
                        )
                    }
                }
            )
        }

        // Scroll-to-top FAB — outside Surface so it's not clipped by rounded corners
        ScrollToTopButton(
            listState = listState,
            onClick = { scope.launch { listState.animateScrollToItem(0) } },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp + extraBottomPadding)
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
    modifier: Modifier = Modifier
) {
    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    AnimatedVisibility(
        visible = showButton,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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

/**
 * Builds the flat UI list from raw records (DESC order from DB).
 * Computes isLight by walking from the oldest record (bottom) upward,
 * toggling when exerciseId changes. This keeps colors stable when new
 * records are added at the top.
 * Inserts date headers between days.
 *
 * @param section Used to generate unique header keys across batches.
 *                Section 0 = recent, 1+ = older batches.
 */
private fun buildUiItems(
    records: List<Record>,
    exerciseMap: Map<Int, String>,
    useAlternatingColors: Boolean,
    section: Int = 0
): List<HistoryUiModel> {
    if (records.isEmpty()) return emptyList()

    // 1. Compute isLight for each record (walk from oldest = last index)
    val isLightArray = BooleanArray(records.size)
    var currentIsLight = true
    var lastExerciseId = records.last().exerciseId
    isLightArray[records.lastIndex] = currentIsLight

    for (i in records.lastIndex - 1 downTo 0) {
        if (records[i].exerciseId != lastExerciseId) {
            currentIsLight = !currentIsLight
            lastExerciseId = records[i].exerciseId
        }
        isLightArray[i] = currentIsLight
    }

    // 2. Build flat list with date headers inserted
    val result = mutableListOf<HistoryUiModel>()
    for (i in records.indices) {
        val record = records[i]
        val exerciseName = exerciseMap[record.exerciseId] ?: globalLocalization.labelUnknownExercise
        val recordWithExercise = RecordWithExercise(record, exerciseName)
        val isLight = if (!useAlternatingColors) true else isLightArray[i]

        // Insert header if this is the first record or a new day
        if (i == 0 || !TimeUtils.isSameDay(records[i - 1].date, record.date)) {
            result.add(HistoryUiModel.Header(record.date, section))
        }

        result.add(HistoryUiModel.RecordItem(recordWithExercise, isLight))
    }

    return result
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
    onUpdateRequest: (Record) -> Unit,
    onDeleteRequest: (Record) -> Unit
) {

    // Hide keyboard when scrolling
    val focusManager = LocalFocusManager.current
    val keyboardScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: NestedScrollSource): androidx.compose.ui.geometry.Offset {
                focusManager.clearFocus()
                return androidx.compose.ui.geometry.Offset.Zero
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
        LazyColumn(
            modifier = modifier.nestedScroll(keyboardScrollConnection),
            state = listState,
            contentPadding = PaddingValues(bottom = 80.dp + extraBottomPadding)
        ) {
            itemsIndexed(
                items = uiItems,
                key = { _, model ->
                     when(model) {
                         is HistoryUiModel.Header -> "header_${model.section}_${model.date}"
                         is HistoryUiModel.RecordItem -> "record_${model.record.record.id}"
                         is HistoryUiModel.BatchSeparator -> "separator_${model.index}"
                         is HistoryUiModel.EvictedBatch -> "evicted_${model.index}"
                         is HistoryUiModel.LoadingMore -> "loading_more"
                     }
                },
                contentType = { _, model ->
                    when(model) {
                        is HistoryUiModel.Header -> "header"
                        is HistoryUiModel.RecordItem -> "record"
                        is HistoryUiModel.BatchSeparator -> "separator"
                        is HistoryUiModel.EvictedBatch -> "evicted"
                        is HistoryUiModel.LoadingMore -> "loading"
                    }
                }
            ) { index, item ->
                when (item) {
                    is HistoryUiModel.Header -> {
                        HistoryDateHeader(
                            date = item.date,
                            weightUnit = weightUnit,
                            modifier = Modifier.animateItem()
                        )
                    }
                    is HistoryUiModel.BatchSeparator -> {
                        OlderRecordsSeparator(modifier = Modifier.animateItem())
                    }
                    is HistoryUiModel.EvictedBatch -> {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(item.heightDp.dp)
                        )
                    }
                    is HistoryUiModel.LoadingMore -> {
                        LoadingMoreIndicator(modifier = Modifier.animateItem())
                    }
                    is HistoryUiModel.RecordItem -> {
                        val isLight = item.isLight
                        
                        val prevItem = if (index > 0) uiItems[index - 1] else null
                        val nextItem = if (index < uiItems.lastIndex) uiItems[index + 1] else null
                        
                        val prevIsSame = prevItem is HistoryUiModel.RecordItem && prevItem.isLight == isLight
                        val nextIsSame = nextItem is HistoryUiModel.RecordItem && nextItem.isLight == isLight
                        
                        // Also show title if previous is NOT the same exercise (or is null/header/separator)
                        val showTitle = if (prevItem is HistoryUiModel.RecordItem) {
                             prevItem.record.exerciseName != item.record.exerciseName
                        } else {
                             true
                        }

                        val shape = when {
                            !prevIsSame && !nextIsSame -> RoundedCornerShape(28.dp)
                            !prevIsSame && nextIsSame -> RoundedCornerShape(
                                topStart = 28.dp,
                                topEnd = 28.dp,
                                bottomStart = 4.dp,
                                bottomEnd = 4.dp
                            )
                            prevIsSame && !nextIsSame -> RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 4.dp,
                                bottomStart = 28.dp,
                                bottomEnd = 28.dp
                            )
                            else -> RoundedCornerShape(4.dp)
                        }

                        SwipeToDeleteContainer(
                            onDelete = { onDeleteRequest(item.record.record) },
                            modifier = Modifier.animateItem()
                        ) {
                            HistoryRecordCard(
                                item = item.record,
                                isLight = isLight,
                                showTitle = showTitle,
                                weightUnit = weightUnit,
                                shape = shape,
                                onUpdate = onUpdateRequest
                            )
                        }
                    }
                }
            }
        }
    }
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
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun HistoryGridRow(
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
        // Col 1: Date/Name (Flexible)
        // If we want to change the width ratio simply change this weight!
        Box(modifier = Modifier.weight(1.5f)) {
            col1()
        }

        // Col 2: Sets (Fixed)
        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
            col2()
        }

        // Col 3: Reps (Fixed)
        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
            col3()
        }

        // Col 4: Weight (Fixed)
        Box(modifier = Modifier.width(70.dp), contentAlignment = Alignment.Center) {
            col4()
        }
    }
}

@Composable
private fun HistoryDateHeader(
    date: Long,
    weightUnit: String,
    modifier: Modifier = Modifier
) {
    val currentLocale = globalLocalization.appLocale
    val isToday = remember(date) { android.text.format.DateUtils.isToday(date) }
    val isYesterday = remember(date) {
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

    // Aligned with the content inside the cards
    // Card Padding (16) + Card Internal Padding (16) = 32dp start offset
    HistoryGridRow(
        modifier = modifier.padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Bottom,
        col1 = {
            Column {
                // First: Numerical Date (smaller)
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                // Second: Day Name (bigger)
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        col2 = {
            HeaderLabel(globalLocalization.labelSets)
        },
        col3 = {
            HeaderLabel(globalLocalization.labelReps)
        },
        col4 = {
            HeaderLabel(weightUnit)
        }
    )
}

@Composable
private fun HeaderLabel(text: String) {
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
private fun HistoryRecordCard(
    item: RecordWithExercise,
    isLight: Boolean,
    showTitle: Boolean,
    weightUnit: String,
    shape: androidx.compose.ui.graphics.Shape,
    onUpdate: (Record) -> Unit
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

    val timestamp = remember(item.record.date) {
        // Format: 14:05:30
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(item.record.date))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(shape)
            .clickable { isExpanded = !isExpanded }, // Toggle expansion
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            HistoryGridRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                col1 = {
                    if (showTitle) {
                        Text(
                            text = item.exerciseName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Unspecified // Inherit from Card
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Maintain spacing if needed or just empty
                        Spacer(modifier = Modifier.height(0.dp))
                    }
                },
                col2 = {
                    HistorySetsField(
                        value = item.record.sets,
                        contentColor = contentColor,
                        onValidChange = { onUpdate(item.record.copy(sets = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                col3 = {
                    HistoryRepsField(
                        value = item.record.reps,
                        contentColor = contentColor,
                        onValidChange = { onUpdate(item.record.copy(reps = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                col4 = {
                    HistoryWeightField(
                        value = item.record.weight,
                        contentColor = contentColor,
                        onValidChange = { onUpdate(item.record.copy(weight = it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
            
            if (isExpanded) {
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                )
            }
        }
    }
}
