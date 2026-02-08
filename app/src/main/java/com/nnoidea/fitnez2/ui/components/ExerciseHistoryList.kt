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

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.data.SettingsRepository
import kotlinx.coroutines.launch

import androidx.compose.material3.Surface

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    selectedExerciseId: Int? = null
) {


    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Ideally, pass DAO or VM, but keeping DB access here for drop-in compatibility
    val database = remember { AppDatabase.getDatabase(context, scope) }
    val dao = database.recordDao()

    val history by remember(selectedExerciseId) {
        if (selectedExerciseId == null) {
            dao.getAllRecordsFlow()
        } else {
            dao.getRecordsByExerciseId(selectedExerciseId)
        }
    }.collectAsState(initial = emptyList())
    val settingsRepository = remember { SettingsRepository(context) }
    val weightUnit by settingsRepository.weightUnitFlow.collectAsState(initial = "kg")

    // Grouping Logic - derived state handles language changes gracefully
    val groupedHistory by remember(history) {
        derivedStateOf {
            var isLight = true
            var prevName: String? = null
            
            // Process from oldest to newest to ensure stability when new records are added at the top
            val historyWithColor = history.asReversed().map { item ->
                if (prevName != null && item.exerciseName != prevName) {
                    isLight = !isLight
                }
                prevName = item.exerciseName
                item to isLight
            }.reversed()

            historyWithColor.groupBy { (item, _) ->
                globalLocalization.formatDate(item.record.date)
            }
        }
    }

    // Content Display
    val globalUiState = LocalGlobalUiState.current
    val listState = rememberLazyListState()

    // Scroll to top when receiving the signal
    // State to track if we are waiting for a specific record to appear to scroll to it
    var pendingScrollRecordId by remember { mutableStateOf<Int?>(null) }

    // Scroll trigger: Receive signal
    LaunchedEffect(Unit) {
        globalUiState.signalFlow.collect { signal ->
            if (signal is com.nnoidea.fitnez2.ui.common.UiSignal.ScrollToTop) {
                // Check if the record is already in the list (fast path)
                val targetId = signal.recordId
                if (targetId != null) {
                    val alreadyExists = groupedHistory.values.flatten().any { it.first.record.id == targetId }
                    if (alreadyExists) {
                         listState.animateScrollToItem(0)
                         pendingScrollRecordId = null
                    } else {
                        // Wait for it to appear
                        pendingScrollRecordId = targetId
                    }
                } else {
                     // Fallback for legacy calls (if any) or forced scrolls without ID
                     listState.animateScrollToItem(0)
                }
            }
        }
    }

    // Scroll trigger: Data update
    LaunchedEffect(groupedHistory, pendingScrollRecordId) {
        val targetId = pendingScrollRecordId
        if (targetId != null) {
             val exists = groupedHistory.values.flatten().any { it.first.record.id == targetId }
             if (exists) {
                 listState.animateScrollToItem(0)
                 pendingScrollRecordId = null
             }
        }
    }



    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(28.dp)
    ) {
        ExerciseHistoryListContent(
            modifier = Modifier.fillMaxSize(),
            listState = listState,
            groupedHistory = groupedHistory,

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

// -----------------------------------------------------------------------------
// Stateless UI Components
// -----------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseHistoryListContent(
    modifier: Modifier,
    listState: androidx.compose.foundation.lazy.LazyListState,
    groupedHistory: Map<String, List<Pair<RecordWithExercise, Boolean>>>,
    weightUnit: String,
    extraBottomPadding: Dp,
    onUpdateRequest: (Record) -> Unit,
    onDeleteRequest: (Record) -> Unit
) {

    if (groupedHistory.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = globalLocalization.labelHistoryEmpty,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = listState,
            contentPadding = PaddingValues(bottom = 80.dp + extraBottomPadding)
        ) {

            groupedHistory.forEach { (dateString, records) ->
                item(key = "header_$dateString") {
                    val date = records.firstOrNull()?.first?.record?.date ?: 0L
                    HistoryDateHeader(
                        date = date,
                        weightUnit = weightUnit,
                        modifier = Modifier.animateItem()
                    )
                }
                itemsIndexed(records, key = { _, item -> item.first.record.id }) { index, (recordItem, isLight) ->
                    val prevIsSame = index > 0 && records[index - 1].second == isLight
                    val nextIsSame = index < records.lastIndex && records[index + 1].second == isLight

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
                        onDelete = { onDeleteRequest(recordItem.record) },
                        modifier = Modifier.animateItem()
                    ) {
                        HistoryRecordCard(
                            item = recordItem,
                            isLight = isLight,
                            weightUnit = weightUnit,
                            shape = shape,
                            onUpdate = onUpdateRequest
                        )
                    }
                }
                item(key = "spacer_$dateString") {
                    Spacer(modifier = Modifier.height(16.dp).animateItem())
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
    weightUnit: String,
    shape: androidx.compose.ui.graphics.Shape,
    onUpdate: (Record) -> Unit
) {
    val containerColor = if (isLight) ColorHistoryNeutralContainer else ColorHistoryColoredContainer
    val contentColor = if (isLight) ColorHistoryNeutralContent else ColorHistoryColoredContent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        HistoryGridRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            col1 = {
                Text(
                    text = item.exerciseName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Unspecified // Inherit from Card
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            col2 = {
                HistoryInput(
                    value = item.record.sets.toString(),
                    label = globalLocalization.labelSets,
                    onUpdate = { newVal ->
                        com.nnoidea.fitnez2.core.ValidateAndCorrect.sets(newVal)?.let {
                            onUpdate(item.record.copy(sets = it))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentColor = contentColor
                )
            },
            col3 = {
                HistoryInput(
                    value = item.record.reps.toString(),
                    label = globalLocalization.labelReps,
                    onUpdate = { newVal ->
                        com.nnoidea.fitnez2.core.ValidateAndCorrect.reps(newVal)?.let {
                            onUpdate(item.record.copy(reps = it))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentColor = contentColor
                )
            },
            col4 = {
                HistoryInput(
                    value = item.record.weight.toString().removeSuffix(".0"),
                    label = weightUnit,
                    onUpdate = { newVal ->
                        com.nnoidea.fitnez2.core.ValidateAndCorrect.weight(newVal)?.let {
                            onUpdate(item.record.copy(weight = it))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isDecimal = true,
                    contentColor = contentColor
                )
            }
        )
    }
}

@Composable
private fun HistoryInput(
    value: String,
    label: String,
    onUpdate: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false,
    contentColor: Color
) {
    com.nnoidea.fitnez2.ui.common.SmartInputLogic(
        value = value,
        onValueChange = { },
        onFocusLost = { finalVal ->
             if (finalVal != value) {
                 onUpdate(finalVal)
             }
        },
        validate = {
            if (it.isEmpty()) true
            else if (isDecimal) it.toDoubleOrNull() != null
            else it.toIntOrNull() != null
        }
    ) { displayValue, placeholder, interactionSource, onWrappedValueChange ->
        
        // Visuals similar to Fitnez 1 "Pill" but using Fitnez 2 token style?
        // User said: "need to look visually different" from bottom sheet.
        // Bottom sheet is Colored Container.
        // Here let's use a subtle outline or filled background differently.
        // Fitnez 1 uses: backgroundColor = MaterialTheme.colorScheme.errorContainer (etc) w/ RoundedCorner 20.dp
        
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
                onValueChange = onWrappedValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                interactionSource = interactionSource,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
}



