package com.nnoidea.fitnez2.ui.screenComponents.workout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.WorkoutRecord
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise
import com.nnoidea.fitnez2.ui.components.SwipeToDeleteContainer
import com.nnoidea.fitnez2.ui.components.history.HistoryGridRow
import com.nnoidea.fitnez2.ui.components.history.HeaderLabel
import com.nnoidea.fitnez2.ui.components.history.HistoryRecordCard
import com.nnoidea.fitnez2.ui.components.history.computeColorParityByName
import com.nnoidea.fitnez2.ui.components.history.recordCardShape
import com.nnoidea.fitnez2.ui.components.bottomsheet.autoHideBottomSheet

@Composable
fun WorkoutExerciseList(
    items: List<WorkoutRecordWithExercise>,
    weightUnit: String,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    extraBottomPadding: Dp = 0.dp,
    enableAutoHide: Boolean = false,
    onDeleteRequest: (WorkoutRecord) -> Unit,
    onUpdateRequest: (WorkoutRecord) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(28.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().autoHideBottomSheet(enableAutoHide),
            contentPadding = PaddingValues(bottom = 80.dp + extraBottomPadding)
        ) {
            item(key = "top_spacer_anchor") {
                Spacer(modifier = Modifier.height(1.dp))
            }

            item {
                HistoryGridRow(
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 8.dp),
                    col1 = { Spacer(modifier = Modifier) }, // Empty col 1 in place of Name/Date
                    col2 = { HeaderLabel(globalLocalization.labelSets) },
                    col3 = { HeaderLabel(globalLocalization.labelReps) },
                    col4 = { HeaderLabel(weightUnit) }
                )
            }

            // Precompute color parity once per list change — O(n) instead of O(n²)
            val colorParity = computeColorParityByName(items)

            itemsIndexed(
                items = items,
                key = { _, item -> item.workoutRecord.id }
            ) { index, item ->
                val prevItem = if (index > 0) items[index - 1] else null
                val nextItem = if (index < items.lastIndex) items[index + 1] else null

                val prevIsSame = prevItem != null && prevItem.exerciseName == item.exerciseName
                val nextIsSame = nextItem != null && nextItem.exerciseName == item.exerciseName

                val showTitle = !prevIsSame
                val shape = recordCardShape(prevIsSame, nextIsSame)

                SwipeToDeleteContainer(
                    onDelete = { onDeleteRequest(item.workoutRecord) },
                    modifier = Modifier.fillMaxWidth().animateItem()
                ) {
                    HistoryRecordCard(
                        exerciseName = item.exerciseName,
                        sets = item.workoutRecord.sets,
                        reps = item.workoutRecord.reps,
                        weight = item.workoutRecord.weight,
                        timestamp = null,
                        isLight = colorParity.getOrElse(index) { true },
                        showTitle = showTitle,
                        weightUnit = weightUnit,
                        shape = shape,
                        prevIsSame = prevIsSame,
                        nextIsSame = nextIsSame,
                        onUpdate = { sets, reps, weight ->
                            onUpdateRequest(item.workoutRecord.copy(sets = sets, reps = reps, weight = weight))
                        }
                    )
                }
            }
        }
    }
}
