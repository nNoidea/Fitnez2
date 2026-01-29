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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.launch

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

    var history by remember { mutableStateOf(emptyList<RecordWithExercise>()) }

    // Load Data
    LaunchedEffect(Unit) {
        history = dao.getSortedAll()
    }

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
    ExerciseHistoryListContent(
        modifier = modifier,
        groupedHistory = groupedHistory,
        onEditRequest = { record ->
            editingRecord = record
        }
    )

    // Edit Dialog (Unified)
    editingRecord?.let { record ->
        EditRecordDialog(
            record = record,
            onDismiss = { editingRecord = null },
            onConfirm = { updatedRecord ->
                scope.launch {
                    dao.update(updatedRecord)
                    history = dao.getSortedAll()
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
    onEditRequest: (Record) -> Unit
) {
    if (groupedHistory.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No history yet.",
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
                stickyHeader {
                    HistoryDateHeader(dateString)
                }
                items(records, key = { it.record.id }) { recordItem ->
                    HistoryRecordCard(
                        item = recordItem,
                        onClick = { onEditRequest(recordItem.record) }
                    )
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
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Title
            Text(
                text = item.exerciseName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium), // Bigger, bolder
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // "Expressive" Single Line Stats
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.record.sets} ${globalLocalization.labelSets}  •  ${item.record.reps} ${globalLocalization.labelReps}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Weight Highlight
                Text(
                    text = "${item.record.weight} kg",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(globalLocalization.labelEdit(globalLocalization.labelExerciseName)) // Just generic "Edit" would be better but reusing existing loc function
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                EditFieldRow(label = globalLocalization.labelSets, value = sets, onValueChange = { sets = it })
                EditFieldRow(label = globalLocalization.labelReps, value = reps, onValueChange = { reps = it })
                EditFieldRow(label = globalLocalization.labelWeight, value = weight, onValueChange = { weight = it }, isDecimal = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedRecord = record.copy(
                        sets = sets.toIntOrNull() ?: record.sets,
                        reps = reps.toIntOrNull() ?: record.reps,
                        weight = weight.toDoubleOrNull() ?: record.weight
                    )
                    onConfirm(updatedRecord)
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


