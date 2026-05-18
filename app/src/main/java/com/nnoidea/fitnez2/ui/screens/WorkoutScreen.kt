package com.nnoidea.fitnez2.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.ui.components.TopHeader
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.mutableIntStateOf
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.LocalAppDatabase
import com.nnoidea.fitnez2.data.entities.Workout
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise
import kotlinx.coroutines.launch
import com.nnoidea.fitnez2.ui.components.bottomsheet.PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP
import com.nnoidea.fitnez2.ui.components.dialog.PredictiveAlertDialog
import com.nnoidea.fitnez2.ui.screenComponents.workout.WorkoutExerciseList
import com.nnoidea.fitnez2.ui.screenComponents.workout.rememberWorkoutBottomSheetState
import com.nnoidea.fitnez2.ui.screenComponents.workout.WorkoutBottomSheet

@Composable
fun WorkoutScreen(
    workoutId: Int? = null,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val database = LocalAppDatabase.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var workoutName by remember { mutableStateOf("") }
    val workoutItems = remember { mutableStateListOf<WorkoutRecordWithExercise>() }
    val listState = rememberLazyListState()

    // Track initial state to detect unsaved changes
    var initialWorkoutName by remember { mutableStateOf("") }
    var initialItemCount by remember { mutableIntStateOf(0) }

    // Dialog state: null = hidden, false = no-name variant, true = unsaved-with-save variant
    var dialogVariant by remember { mutableStateOf<Boolean?>(null) }

    val hasName = workoutName.isNotBlank()
    val hasRecords = workoutItems.isNotEmpty()
    val hasUnsavedChanges =
        (workoutName != initialWorkoutName) || (workoutItems.size != initialItemCount)

    // Shared back-press handler for both the arrow icon and system back gesture
    fun handleBack() {
        when {
            // No changes at all → just go back
            !hasUnsavedChanges -> onBack()
            // Name only, no records → silently discard
            hasName && !hasRecords -> onBack()
            // Records but no name → show "No Name" dialog
            !hasName && hasRecords -> {
                dialogVariant = false
            }
            // Both present → show "Unsaved Work" dialog
            hasName && hasRecords -> {
                dialogVariant = true
            }
        }
    }

    // Intercept system back gesture
    BackHandler(enabled = hasUnsavedChanges && !(hasName && !hasRecords)) {
        handleBack()
    }

    androidx.compose.runtime.LaunchedEffect(workoutId) {
        if (workoutId != null) {
            val workout = database.workoutDao().getWorkoutById(workoutId)
            if (workout != null) {
                workoutName = workout.name
                initialWorkoutName = workout.name
            }
            val records = database.workoutDao().getRecordsForWorkout(workoutId)
            workoutItems.clear()
            workoutItems.addAll(records)
            initialItemCount = records.size
        }
    }

    val exercises by database.exerciseDao().getAllExercisesFlow().collectAsState(initial = null)
    androidx.compose.runtime.LaunchedEffect(exercises) {
        val currentExercises = exercises
        if (currentExercises != null) {
            val validIds = currentExercises.asSequence().map { it.id }.toSet()
            workoutItems.removeAll { !validIds.contains(it.workoutRecord.exerciseId) }
        }
    }

    val bottomSheetState = rememberWorkoutBottomSheetState(workoutId = workoutId) { newRecord ->
        val wasAtTop = listState.firstVisibleItemIndex <= 1
        workoutItems.add(0, newRecord)
        scope.launch {
            if (wasAtTop) {
                listState.scrollToItem(0)
            } else {
                listState.animateScrollToItem(0)
            }
        }
    }

    // --- "No Name" dialog (records exist but name is blank) ---
    PredictiveAlertDialog(
        show = dialogVariant == false,
        onDismissRequest = { dialogVariant = null },
        title = globalLocalization.titleNoName,
        text = globalLocalization.msgNoName,
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { dialogVariant = null }
            ) {
                Text(globalLocalization.labelEditAction)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = { dialogVariant = null; onBack() },
            ) {
                Text(globalLocalization.labelDiscard, color = Color(0xFFEF5350))
            }
        }
    )

    // --- "Unsaved Work" dialog (name + records → offer discard / keep editing / save) ---
    PredictiveAlertDialog(
        show = dialogVariant == true,
        onDismissRequest = { dialogVariant = null },
        title = globalLocalization.titleUnsavedWork,
        text = globalLocalization.msgUnsavedWork,
        confirmButton = { } // buttons handled in content slot below
    ) {
        // Three separate buttons, right-aligned: Discard | Edit | Save
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                8.dp,
                androidx.compose.ui.Alignment.End
            )
        ) {
            androidx.compose.material3.TextButton(
                onClick = { dialogVariant = null; onBack() },
            ) {
                Text(globalLocalization.labelDiscard, color = Color(0xFFEF5350))
            }

            androidx.compose.material3.TextButton(
                onClick = { dialogVariant = null }
            ) {
                Text(globalLocalization.labelEditAction)
            }

            Button(
                onClick = {
                    scope.launch {
                        val existing = database.workoutDao().getWorkoutByName(workoutName.trim())
                        val isSelf =
                            (existing != null) && (workoutId != null) && (existing.id == workoutId)
                        if ((existing != null) && (!isSelf)) {
                            Toast.makeText(
                                context,
                                globalLocalization.errorWorkoutAlreadyExists(workoutName.trim()),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        dialogVariant = null
                        val targetWorkoutId = if (workoutId != null) {
                            database.workoutDao()
                                .updateWorkout(Workout(id = workoutId, name = workoutName.trim()))
                            database.workoutDao().deleteRecordsByWorkoutId(workoutId)
                            workoutId
                        } else {
                            database.workoutDao().insertWorkout(Workout(name = workoutName.trim()))
                                .toInt()
                        }
                        workoutItems.forEach { item ->
                            val newRecord =
                                item.workoutRecord.copy(id = 0, workoutId = targetWorkoutId)
                            database.workoutDao().insertWorkoutRecord(newRecord)
                        }
                        onBack()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                )
            ) {
                Text(globalLocalization.labelSave)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            TopHeader {
                IconButton(onClick = { handleBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = globalLocalization.labelBack
                    )
                }

                TextField(
                    value = workoutName,
                    onValueChange = { workoutName = it },
                    placeholder = {
                        Text(
                            globalLocalization.labelWorkoutName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Button(
                    onClick = {
                        if (workoutName.isBlank()) {
                            Toast.makeText(
                                context,
                                globalLocalization.errorWorkoutNameBlank,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (workoutItems.isEmpty()) {
                            Toast.makeText(
                                context,
                                globalLocalization.errorWorkoutNoExercises,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            scope.launch {
                                val existing =
                                    database.workoutDao().getWorkoutByName(workoutName.trim())
                                val isSelf =
                                    (existing != null) && (workoutId != null) && (existing.id == workoutId)
                                if ((existing != null) && (!isSelf)) {
                                    Toast.makeText(
                                        context,
                                        globalLocalization.errorWorkoutAlreadyExists(workoutName.trim()),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    return@launch
                                }

                                val targetWorkoutId = if (workoutId != null) {
                                    database.workoutDao().updateWorkout(
                                        Workout(
                                            id = workoutId,
                                            name = workoutName.trim()
                                        )
                                    )
                                    database.workoutDao().deleteRecordsByWorkoutId(workoutId)
                                    workoutId
                                } else {
                                    database.workoutDao()
                                        .insertWorkout(Workout(name = workoutName.trim())).toInt()
                                }

                                workoutItems.forEach { item ->
                                    val newRecord =
                                        item.workoutRecord.copy(id = 0, workoutId = targetWorkoutId)
                                    database.workoutDao().insertWorkoutRecord(newRecord)
                                }
                                onBack()
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = globalLocalization.labelSave,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            WorkoutExerciseList(
                items = workoutItems,
                listState = listState,
                weightUnit = bottomSheetState.weightUnit,
                modifier = Modifier.weight(1f),
                extraBottomPadding = PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp,
                enableAutoHide = true,
                onDeleteRequest = { recordToDelete ->
                    workoutItems.removeAll { it.workoutRecord.id == recordToDelete.id }
                }
            ) { updatedRecord ->
                val index =
                    workoutItems.indexOfFirst { it.workoutRecord.id == updatedRecord.id }
                if (index != -1) {
                    workoutItems[index] =
                        workoutItems[index].copy(workoutRecord = updatedRecord)
                }
            }
        }

        WorkoutBottomSheet(
            state = bottomSheetState,
            modifier = Modifier.fillMaxSize()
        )
    }
}
