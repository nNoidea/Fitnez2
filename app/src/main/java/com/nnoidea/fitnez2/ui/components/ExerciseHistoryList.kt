package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.width
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

// -----------------------------------------------------------------------------
// Public Smart Component
// -----------------------------------------------------------------------------

@Composable
fun ExerciseHistoryList(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Ideally, pass DAO or VM, but keeping DB access here for drop-in compatibility
    val database = remember { AppDatabase.getDatabase(context, scope) }
    val dao = database.recordDao()

    val history by dao.getSortedAll().collectAsState(initial = emptyList())
    val settingsRepository = remember { SettingsRepository(context) }
    val weightUnit by settingsRepository.weightUnitFlow.collectAsState(initial = "kg")

    // Grouping Logic - derived state handles language changes gracefully
    val groupedHistory by remember(history) {
        derivedStateOf {
            history.groupBy { record ->
                globalLocalization.formatDate(record.record.date)
            }
        }
    }

    // Content Display
    val globalUiState = LocalGlobalUiState.current

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(28.dp)
    ) {
        ExerciseHistoryListContent(
            modifier = Modifier.fillMaxSize(),
            groupedHistory = groupedHistory,
            weightUnit = weightUnit,
            onUpdateRequest = { updatedRecord ->
                scope.launch {
                     try {
                         updatedRecord.validate()
                         dao.update(updatedRecord)
                     } catch (e: Exception) {
                         // Ideally show snackbar error
                     }
                }
            },
            onDeleteRequest = { record ->
                scope.launch {
                    // 1. Fetch latest state for Undo (SSOT)
                    val freshRecord = dao.getById(record.id) ?: record

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

    groupedHistory: Map<String, List<RecordWithExercise>>,
    weightUnit: String,
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
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            groupedHistory.forEach { (dateString, records) ->
                item {
                    HistoryDateHeader(dateString)
                }
                items(records, key = { it.record.id }) { recordItem ->
                    SwipeToDeleteContainer(
                        onDelete = { onDeleteRequest(recordItem.record) }
                    ) {
                        HistoryRecordCard(
                            item = recordItem,
                            weightUnit = weightUnit,
                            onUpdate = onUpdateRequest
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryDateHeader(dateString: String) {
    // "Sticky" header needs a background to cover items scrolling under it
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = dateString,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            // Optional: Legend could go here if we wanted column headers like Fitnez 1
        }
    }
}

@Composable
private fun HistoryRecordCard(
    item: RecordWithExercise,
    weightUnit: String,
    onUpdate: (Record) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(26.dp), // Expressive Extra Large Corner
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Single Row for everything: Exercise Name | Sets | Reps | Weight
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Exercise Name (Weight 1f to take available space)
            Text(
                text = item.exerciseName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1.5f)
            )

            // 2. Sets Input
            HistoryInput(
                value = item.record.sets.toString(),
                label = "Sets",
                onUpdate = { newVal ->
                    newVal.toIntOrNull()?.let { 
                        onUpdate(item.record.copy(sets = it)) 
                    }
                },
                modifier = Modifier.width(60.dp)
            )

            // 3. Reps Input
            HistoryInput(
                value = item.record.reps.toString(),
                label = "Reps", // Fitnez 1 style label
                onUpdate = { newVal ->
                     newVal.toIntOrNull()?.let {
                         onUpdate(item.record.copy(reps = it))
                     }
                },
                modifier = Modifier.width(60.dp)
            )

            // 4. Weight Input
            HistoryInput(
                value = item.record.weight.toString().removeSuffix(".0"),
                label = weightUnit,
                onUpdate = { newVal ->
                    newVal.toDoubleOrNull()?.let {
                        onUpdate(item.record.copy(weight = it))
                    }
                },
                modifier = Modifier.width(70.dp),
                isDecimal = true
            )
        }
    }
}

@Composable
private fun HistoryInput(
    value: String,
    label: String,
    onUpdate: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false
) {
    // Local state to hold the edits before commit
    var currentValue by remember(value) { mutableStateOf(value) }

    com.nnoidea.fitnez2.ui.common.SmartInputLogic(
        value = currentValue,
        onValueChange = { currentValue = it },
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
        
        // Let's use SurfaceContainerHigh for a subtle input look
        Box(
            modifier = modifier
                .height(44.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh, 
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
                    color = MaterialTheme.colorScheme.onSurface
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                         )
                     }
                     innerTextField()
                }
            )
        }
    }
}



