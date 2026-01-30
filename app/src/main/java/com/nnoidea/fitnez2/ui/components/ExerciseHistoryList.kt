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
import androidx.compose.foundation.text.KeyboardOptions
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

    // Grouping Logic - derived state handles language changes gracefully
    val groupedHistory by remember(history) {
        derivedStateOf {
            history.groupBy { record ->
                globalLocalization.formatDate(record.record.date)
            }
        }
    }

    // Editing State (Simply holds reference to the record being edited)
    var editingRecord by remember { mutableStateOf<Record?>(null) }

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
            onEditRequest = { record ->
                editingRecord = record
            },
            onDeleteRequest = { record ->
                scope.launch {
                    // 1. Delete
                    dao.delete(record.id)

                    // 2. Show Snackbar with Undo
                    globalUiState.showSnackbar(
                        message = globalLocalization.labelRecordDeleted,
                        actionLabel = globalLocalization.labelUndo,
                        onActionPerformed = {
                            scope.launch {
                                dao.create(record)
                            }
                        }
                    )
                }
            }
        )
    }

    // Edit Dialog (Unified)
    editingRecord?.let { record ->
        EditRecordDialog(
            record = record,
            onDismiss = { editingRecord = null },
            onConfirm = { updatedRecord ->
                scope.launch {
                    dao.update(updatedRecord)
                }
                editingRecord = null
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
    onEditRequest: (Record) -> Unit,
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
                            onClick = { onEditRequest(recordItem.record) }
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
        Text(
            text = dateString,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun HistoryRecordCard(
    item: RecordWithExercise,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                .padding(horizontal = 16.dp, vertical = 20.dp), // Adjust padding if needed
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Exercise Name (Weight 1f to take available space)
            Text(
                text = item.exerciseName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1.5f)
            )

            // 2. Sets -> "5 Sets" or just "5"
            Text(
                text = item.formattedSets, 
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.8f) // Fixed width-ish via weight
            )

            // 3. Reps -> "10 Reps" or just "10"
            Text(
                text = item.formattedReps,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(0.8f) 
            )

            // 4. Weight -> "20.0 kg"
            Text(
                text = item.formattedWeight(LocalGlobalUiState.current.weightUnit), 
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(0.9f)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Unified Edit Dialog
// -----------------------------------------------------------------------------

@Composable
private fun EditRecordDialog(
    record: Record,
    onDismiss: () -> Unit,
    onConfirm: (Record) -> Unit
) {
    var sets by remember { mutableStateOf(record.sets.toString()) }
    var reps by remember { mutableStateOf(record.reps.toString()) }
    var weight by remember { mutableStateOf(record.weight.toString()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(globalLocalization.labelEditExercise)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                EditFieldRow(label = globalLocalization.labelSets, value = sets, onValueChange = { sets = it })
                EditFieldRow(label = globalLocalization.labelReps, value = reps, onValueChange = { reps = it })
                EditFieldRow(label = globalLocalization.labelWeightWithUnit(LocalGlobalUiState.current.weightUnit), value = weight, onValueChange = { weight = it }, isDecimal = true)
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val updatedRecord = record.copy(
                            sets = sets.toIntOrNull() ?: record.sets,
                            reps = reps.toIntOrNull() ?: record.reps,
                            weight = weight.toDoubleOrNull() ?: record.weight
                        )
                        updatedRecord.validate() // SSOT: Use Entity validation
                        onConfirm(updatedRecord)
                    } catch (e: IllegalArgumentException) {
                        errorMessage = e.message
                    }
                }
            ) {
                Text(globalLocalization.labelSave)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(globalLocalization.labelCancel)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun EditFieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isDecimal: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    )
}


