package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
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

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.LocalAppDatabase
import com.nnoidea.fitnez2.data.LocalSettingsRepository
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import androidx.compose.animation.animateContentSize

import androidx.compose.material3.Surface

import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems

import androidx.paging.PagingData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.paging.compose.itemKey
import androidx.paging.compose.itemContentType
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

private const val HistoryInputBackgroundAlpha = 0.1f

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

    // Paging Configuration
    val recordPager: Pager<Int, Record> = remember(selectedExerciseId) {
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                val source: androidx.paging.PagingSource<Int, Record> = if (selectedExerciseId == null) {
                    dao.getRecordsPagingSourceRaw()
                } else {
                    dao.getRecordsByExerciseIdPagingSourceRaw(selectedExerciseId)
                }
                source
            }
        )
    }

    val pagingItems = remember(recordPager, exerciseMap, useAlternatingColors) {
        val flow: kotlinx.coroutines.flow.Flow<PagingData<HistoryUiModel>> = recordPager.flow
            .map { pagingData ->
                val mapped = pagingData.map { record ->
                    val exerciseName = exerciseMap[record.exerciseId] ?: "Unknown Exercise"
                    val recordWithExercise = RecordWithExercise(record, exerciseName)
                    
                    val isLight = if (!useAlternatingColors) true else record.groupIndex % 2 == 0
                    HistoryUiModel.RecordItem(recordWithExercise, isLight)
                }
                
                mapped.insertSeparators<HistoryUiModel.RecordItem, HistoryUiModel> { before, after ->
                    if (after == null) return@insertSeparators null
                    if (before == null) return@insertSeparators HistoryUiModel.Header(after.record.record.date)
                    
                    if (!isSameDay(before.record.record.date, after.record.record.date)) {
                        HistoryUiModel.Header(after.record.record.date)
                    } else {
                        null
                    }
                }
            }
        flow
    }.collectAsLazyPagingItems()

    // Content Display
    val globalUiState = LocalGlobalUiState.current
    val listState = rememberLazyListState()

    // Scroll trigger: Receive signal
    var pendingScrollRecordId by remember { mutableStateOf<Int?>(null) }
    
    LaunchedEffect(Unit) {
        globalUiState.signalFlow.collect { signal ->
            if (signal is com.nnoidea.fitnez2.ui.common.UiSignal.ScrollToTop) {
                 // Immediate action: If we are very deep in the list, snap to top immediately.
                 // This ensures Paging starts loading the top pages if they were dropped.
                 if (listState.firstVisibleItemIndex > 20) {
                     listState.scrollToItem(0)
                 }
                 
                 // Set pending state to wait for the specific record to appear for fine-tuning
                 pendingScrollRecordId = signal.recordId
            }
        }
    }
    
    // Watch for the pending record to appear in the list items
    val itemCount = pagingItems.itemCount
    LaunchedEffect(pendingScrollRecordId, itemCount) {
        val targetId = pendingScrollRecordId
        if (targetId != null) {
            
            if (itemCount > 0) {
                // Check if the record is in the first few items (it should be at the top)
                // We check the first 5 items to be safe (accounting for headers)
                val searchRange = 0 until minOf(5, itemCount)
                var found = false
                for (i in searchRange) {
                    val item = pagingItems.peek(i)
                    if (item is HistoryUiModel.RecordItem && item.record.record.id == targetId) {
                        found = true
                        break
                    }
                }
                
                if (found) {
                    listState.animateScrollToItem(0)
                    pendingScrollRecordId = null
                }
            }
        }
    }

    
    // Auto-scroll logic when new item is added is handled by Paging/LazyColumn behavior usually,
    // or triggered via the signal.
    // The previous complex logic waiting for ID is not easily applicable to Paging without scanning.
    // Assuming adding a record triggers a refresh and scroll signal.



    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(28.dp)
    ) {
        ExerciseHistoryListContent(
            modifier = Modifier.fillMaxSize(),
            listState = listState,
            pagingItems = pagingItems,

            weightUnit = weightUnit,
            extraBottomPadding = extraBottomPadding,
            onUpdateRequest = { updatedRecord ->
                scope.launch {
                     try {
                         dao.update(updatedRecord)
                     } catch (e: Exception) {
                         // Ideally show snackbar error
                     }
                }
            },
            onDeleteRequest = { record ->
                scope.launch {
                    // 1. Fetch latest state for Undo (SSOT)
                    val freshRecord = dao.getRecordById(record.id) ?: record

                    // 2. Delete
                    dao.delete(record.id)

                    // 3. Show Snackbar with Undo using fresh data
                    globalUiState.showSnackbar(
                        message = globalLocalization.labelRecordDeleted,
                        actionLabel = globalLocalization.labelUndo,
                        onActionPerformed = {
                            scope.launch {
                                dao.create(freshRecord)
                            }
                        }
                    )
                }
            }
        )
    }
}


private fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = millis1
    val y1 = cal.get(java.util.Calendar.YEAR)
    val d1 = cal.get(java.util.Calendar.DAY_OF_YEAR)
    cal.timeInMillis = millis2
    return y1 == cal.get(java.util.Calendar.YEAR) && d1 == cal.get(java.util.Calendar.DAY_OF_YEAR)
}

// -----------------------------------------------------------------------------
// Stateless UI Components
// -----------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseHistoryListContent(
    modifier: Modifier,
    listState: androidx.compose.foundation.lazy.LazyListState,
    pagingItems: androidx.paging.compose.LazyPagingItems<HistoryUiModel>,
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

    if (pagingItems.itemCount == 0) {
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
            items(
                count = pagingItems.itemCount,
                key = pagingItems.itemKey { model ->
                     when(model) {
                         is HistoryUiModel.Header -> "header_${model.date}"
                         is HistoryUiModel.RecordItem -> "record_${model.record.record.id}"
                     }
                },
                contentType = pagingItems.itemContentType { model ->
                    when(model) {
                        is HistoryUiModel.Header -> "header"
                        is HistoryUiModel.RecordItem -> "record"
                    }
                }
            ) { index ->
                val item = pagingItems[index]
                
                when (item) {
                    is HistoryUiModel.Header -> {
                        HistoryDateHeader(
                            date = item.date,
                            weightUnit = weightUnit,
                            modifier = Modifier.animateItem()
                        )
                    }
                    is HistoryUiModel.RecordItem -> {
                        // Calculate shape based on neighbors
                        // This is tricky with Paging because neighbors might be headers or bounds.
                        // We can look at index-1 and index+1
                        
                        val prevItem = if (index > 0) pagingItems.peek(index - 1) else null
                        val nextItem = if (index < pagingItems.itemCount - 1) pagingItems.peek(index + 1) else null
                        
                        val isLight = item.isLight
                        
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
                        
                        // Spacer logic? Previously spacer was explicit.
                        // We can add bottom margin to the card if it's the last in a group?
                        // Or insert separate Spacer items? Separators are cleaner.
                        // Let's add top padding to the Header instead to simulate spacer, 
                        // or add marginBottom to the last item of a group.
                        // The original code had: item(key = "spacer_...") { Spacer(...) } after each day group.
                        // With separators, we can just make the Header have top padding.
                        // Warning: The first header shouldn't have huge top padding.
                    }
                    null -> {
                        // Placeholder (disabled)
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

    val dateFormat = remember(currentLocale) { SimpleDateFormat("d/M/yyyy", currentLocale) }
    val dayFormat = remember(currentLocale) { SimpleDateFormat("EEEE", currentLocale) }

    val dateString = remember(dateObj, currentLocale) { 
        dateFormat.format(dateObj) 
    }
    
    val dayName = remember(dateObj, currentLocale, isToday, isYesterday) {
        if (isToday) globalLocalization.labelToday
        else if (isYesterday) globalLocalization.labelYesterday
        else dayFormat.format(dateObj)
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
                    com.nnoidea.fitnez2.ui.common.SetsInput(
                        value = item.record.sets.toString(),
                        onValidChange = { validSets ->
                            onUpdate(item.record.copy(sets = validSets))
                        }
                    ) { displayValue, placeholder, interactionSource, onValueChange ->
                        HistoryInputStyle(
                            displayValue = displayValue,
                            placeholder = placeholder,
                            interactionSource = interactionSource,
                            onValueChange = onValueChange,
                            contentColor = contentColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                col3 = {
                    com.nnoidea.fitnez2.ui.common.RepsInput(
                        value = item.record.reps.toString(),
                        onValidChange = { validReps ->
                            onUpdate(item.record.copy(reps = validReps))
                        }
                    ) { displayValue, placeholder, interactionSource, onValueChange ->
                        HistoryInputStyle(
                            displayValue = displayValue,
                            placeholder = placeholder,
                            interactionSource = interactionSource,
                            onValueChange = onValueChange,
                            contentColor = contentColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                col4 = {
                    com.nnoidea.fitnez2.ui.common.WeightInput(
                        value = item.record.weight,
                        onValidChange = { validWeight ->
                            onUpdate(item.record.copy(weight = validWeight))
                        }
                    ) { displayValue, placeholder, interactionSource, onValueChange ->
                        HistoryInputStyle(
                            displayValue = displayValue,
                            placeholder = placeholder,
                            interactionSource = interactionSource,
                            onValueChange = onValueChange,
                            contentColor = contentColor,
                            isDecimal = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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

/**
 * Visual style for history input fields.
 * This is just the "skin" - logic is handled by SetsInput/RepsInput/WeightInput.
 */
@Composable
private fun HistoryInputStyle(
    displayValue: String,
    placeholder: String,
    interactionSource: MutableInteractionSource,
    onValueChange: (String) -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                contentColor.copy(alpha = HistoryInputBackgroundAlpha), 
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = displayValue,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            interactionSource = interactionSource,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = contentColor
            ),
            singleLine = false,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
            ),
            decorationBox = { innerTextField ->
                 if (displayValue.isEmpty()) {
                      Text(
                        text = placeholder,
                         style = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = contentColor.copy(alpha = 0.5f)
                        )
                      )
                 }
                 innerTextField()
            }
        )
    }
}





