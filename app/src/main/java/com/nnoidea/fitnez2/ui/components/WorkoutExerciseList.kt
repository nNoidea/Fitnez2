package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise

@Composable
fun WorkoutExerciseList(
    items: List<RecordWithExercise>,
    weightUnit: String,
    modifier: Modifier = Modifier,
    extraBottomPadding: Dp = 0.dp,
    onDeleteRequest: (Record) -> Unit,
    onUpdateRequest: (Record) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(28.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp + extraBottomPadding)
        ) {
            item {
                HistoryGridRow(
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 8.dp),
                    col1 = { Spacer(modifier = Modifier) }, // Empty col 1 in place of Name/Date
                    col2 = { HeaderLabel(globalLocalization.labelSets) },
                    col3 = { HeaderLabel(globalLocalization.labelReps) },
                    col4 = { HeaderLabel(weightUnit) }
                )
            }

            itemsIndexed(
                items = items,
                key = { _, item -> item.record.id }
            ) { index, item ->
                // Compute isLight manually based on previous item
                // Alternately, just use true/false parity by index or grouped by exercise.
                val prevItem = if (index > 0) items[index - 1] else null
                val nextItem = if (index < items.lastIndex) items[index + 1] else null
                
                // Keep the color parity stable per exercise
                // We'll mimic the toggled color per exercise name change
                var currentIsLight = true
                if (index > 0) {
                    var isLightAcc = true
                    var lastExe = items[0].exerciseName
                    for(i in 1..index) {
                        if (items[i].exerciseName != lastExe) {
                            isLightAcc = !isLightAcc
                            lastExe = items[i].exerciseName
                        }
                    }
                    currentIsLight = isLightAcc
                }

                val prevIsSame = prevItem != null && prevItem.exerciseName == item.exerciseName
                val nextIsSame = nextItem != null && nextItem.exerciseName == item.exerciseName

                val showTitle = !prevIsSame

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
                    onDelete = { onDeleteRequest(item.record) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HistoryRecordCard(
                        item = item,
                        isLight = currentIsLight,
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
