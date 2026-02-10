package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.LocalAppDatabase
import com.nnoidea.fitnez2.data.LocalSettingsRepository
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.core.TimeUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.animation.animateContentSize

import androidx.compose.material3.Surface

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class HistoryUiModel {
    data class RecordItem(val record: RecordWithExercise, val isLight: Boolean) : HistoryUiModel()
    data class Header(val date: Long) : HistoryUiModel()
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
    // Load all exercises into a map for fast lookup (Removing JOIN from DB query)
    val exercisesList by exerciseDao.getAllExercisesFlow().collectAsState(initial = emptyList())
    val exerciseMap = remember(exercisesList) {
        exercisesList.associate { it.id to it.name }
    }

    // In-memory record buffer: loaded once from DB, then mutated in-place
    var records by remember { mutableStateOf<List<Record>>(emptyList()) }
    var initialLoadDone by remember { mutableStateOf(false) }

    // One-shot load on first composition (or when filter changes)
    LaunchedEffect(selectedExerciseId) {
        records = if (selectedExerciseId == null) {
            dao.getLatestRecords()
        } else {
            dao.getRecordsByExerciseId(selectedExerciseId)
        }
        initialLoadDone = true
    }

    // When an exercise is deleted (CASCADE), remove its orphaned records from the buffer
    LaunchedEffect(exerciseMap) {
        if (initialLoadDone && exerciseMap.isNotEmpty()) {
            records = records.filter { it.exerciseId in exerciseMap }
        }
    }

    // Build UI model list: compute isLight in-memory and insert date headers
    val uiItems = remember(records, exerciseMap, useAlternatingColors) {
        buildUiItems(records, exerciseMap, useAlternatingColors)
    }

    // Content Display
    val globalUiState = LocalGlobalUiState.current
    val listState = rememberLazyListState()

    // Scroll trigger: Receive signal
    LaunchedEffect(Unit) {
        globalUiState.signalFlow.collect { signal ->
            if (signal is com.nnoidea.fitnez2.ui.common.UiSignal.ScrollToTop) {
                // A new record was added — fetch it from DB and prepend to our buffer
                val recordId = signal.recordId
                if (recordId != null) {
                    val newRecord = dao.getRecordById(recordId)
                    if (newRecord != null && records.none { it.id == newRecord.id }) {
                        records = listOf(newRecord) + records
                    }
                }
                listState.animateScrollToItem(0)
            }
        }
    }


    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(28.dp)
    ) {
        if (!initialLoadDone) return@Surface

        ExerciseHistoryListContent(
            modifier = Modifier.fillMaxSize(),
            listState = listState,
            uiItems = uiItems,
            weightUnit = weightUnit,
            extraBottomPadding = extraBottomPadding,
            onUpdateRequest = { updatedRecord ->
                scope.launch {
                     try {
                         dao.update(updatedRecord)
                         // Update in-memory buffer too
                         records = records.map { if (it.id == updatedRecord.id) updatedRecord else it }
                     } catch (e: Exception) {
                         // Ideally show snackbar error
                     }
                }
            },
            onDeleteRequest = { record ->
                scope.launch {
                    // 1. Fetch latest state for Undo (SSOT)
                    val freshRecord = dao.getRecordById(record.id) ?: record

                    // 2. Delete from DB and remove from in-memory buffer
                    dao.delete(record.id)
                    records = records.filter { it.id != record.id }

                    // 3. Show Snackbar with Undo using fresh data
                    globalUiState.showSnackbar(
                        message = globalLocalization.labelRecordDeleted,
                        actionLabel = globalLocalization.labelUndo,
                        onActionPerformed = {
                            scope.launch {
                                val newId = dao.create(freshRecord)
                                // Re-insert into buffer at the correct chronological position
                                val restored = freshRecord.copy(id = newId.toInt())
                                val mutable = records.toMutableList()
                                val insertIdx = mutable.indexOfFirst { it.date < restored.date || (it.date == restored.date && it.id < restored.id) }
                                if (insertIdx == -1) mutable.add(restored) else mutable.add(insertIdx, restored)
                                records = mutable
                            }
                        }
                    )
                }
            }
        )
    }
}

/**
 * Builds the flat UI list from raw records (DESC order from DB).
 * Computes isLight by walking from the oldest record (bottom) upward,
 * toggling when exerciseId changes. This keeps colors stable when new
 * records are added at the top.
 * Inserts date headers between days.
 */
private fun buildUiItems(
    records: List<Record>,
    exerciseMap: Map<Int, String>,
    useAlternatingColors: Boolean
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
            result.add(HistoryUiModel.Header(record.date))
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
    listState: androidx.compose.foundation.lazy.LazyListState,
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
                         is HistoryUiModel.Header -> "header_${model.date}"
                         is HistoryUiModel.RecordItem -> "record_${model.record.record.id}"
                     }
                },
                contentType = { _, model ->
                    when(model) {
                        is HistoryUiModel.Header -> "header"
                        is HistoryUiModel.RecordItem -> "record"
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
                    is HistoryUiModel.RecordItem -> {
                        val isLight = item.isLight
                        
                        val prevItem = if (index > 0) uiItems[index - 1] else null
                        val nextItem = if (index < uiItems.lastIndex) uiItems[index + 1] else null
                        
                        val prevIsSame = prevItem is HistoryUiModel.RecordItem && prevItem.isLight == isLight
                        val nextIsSame = nextItem is HistoryUiModel.RecordItem && nextItem.isLight == isLight
                        
                        // Also show title if previous is NOT the same exercise (or is null/header)
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
    val dateObj = remember(date) { Date(date) }
    // User requested format: 4/2/2025 (d/M/yyyy)
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






