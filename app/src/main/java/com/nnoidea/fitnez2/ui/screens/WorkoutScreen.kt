package com.nnoidea.fitnez2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
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
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.LocalAppDatabase
import com.nnoidea.fitnez2.data.entities.Workout
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise
import kotlinx.coroutines.launch
import com.nnoidea.fitnez2.ui.components.bottomsheet.PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP
import com.nnoidea.fitnez2.ui.screenComponents.workout.WorkoutExerciseList
import com.nnoidea.fitnez2.ui.screenComponents.workout.rememberWorkoutBottomSheetState
import com.nnoidea.fitnez2.ui.screenComponents.workout.WorkoutBottomSheet

@Composable
fun WorkoutScreen(
    workoutId: Int? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val database = LocalAppDatabase.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var workoutName by remember { mutableStateOf("") }
    val workoutItems = remember { mutableStateListOf<WorkoutRecordWithExercise>() }
    val listState = rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(workoutId) {
        if (workoutId != null) {
            val workout = database.workoutDao().getWorkoutById(workoutId)
            if (workout != null) {
                workoutName = workout.name
            }
            val records = database.workoutDao().getRecordsForWorkout(workoutId)
            workoutItems.clear()
            workoutItems.addAll(records)
        }
    }

    val exercises by database.exerciseDao().getAllExercisesFlow().collectAsState(initial = null)
    androidx.compose.runtime.LaunchedEffect(exercises) {
        val currentExercises = exercises
        if (currentExercises != null) {
            val validIds = currentExercises.map { it.id }.toSet()
            workoutItems.removeAll { !validIds.contains(it.workoutRecord.exerciseId) }
        }
    }

    val bottomSheetState = rememberWorkoutBottomSheetState { newRecord ->
        workoutItems.add(0, newRecord)
        scope.launch {
            listState.animateScrollToItem(0)
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
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = globalLocalization.labelBack
                    )
                }

                TextField(
                    value = workoutName,
                    onValueChange = { workoutName = it },
                    placeholder = { 
                        Text(globalLocalization.labelWorkoutName, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) 
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
                            Toast.makeText(context, "please fill in a name", Toast.LENGTH_SHORT).show()
                        } else if (workoutItems.isEmpty()) {
                            Toast.makeText(context, "please add at least one exercise", Toast.LENGTH_SHORT).show()
                        } else {
                            scope.launch {
                                val targetWorkoutId = if (workoutId != null) {
                                    database.workoutDao().updateWorkout(Workout(id = workoutId, name = workoutName))
                                    database.workoutDao().deleteRecordsByWorkoutId(workoutId)
                                    workoutId
                                } else {
                                    database.workoutDao().insertWorkout(Workout(name = workoutName)).toInt()
                                }
                                
                                workoutItems.forEach { item ->
                                    val newRecord = item.workoutRecord.copy(id = 0, workoutId = targetWorkoutId)
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
                onDeleteRequest = { recordToDelete ->
                    workoutItems.removeAll { it.workoutRecord.id == recordToDelete.id }
                },
                onUpdateRequest = { updatedRecord ->
                    val index = workoutItems.indexOfFirst { it.workoutRecord.id == updatedRecord.id }
                    if (index != -1) {
                        workoutItems[index] = workoutItems[index].copy(workoutRecord = updatedRecord)
                    }
                }
            )
        }
        
        WorkoutBottomSheet(
            state = bottomSheetState,
            modifier = Modifier.fillMaxSize()
        )
    }
}
