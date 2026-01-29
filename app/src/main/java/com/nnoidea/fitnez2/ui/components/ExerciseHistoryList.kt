package com.nnoidea.fitnez2.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseHistoryList(
    history: List<RecordWithExercise>,
    modifier: Modifier = Modifier
) {
    val groupedHistory = remember(history) {
        history.groupBy {
            formatDateHeader(it.record.date)
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        groupedHistory.forEach { (dateString, dayRecords) ->
            item {
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }

            itemsIndexed(dayRecords) { index, item ->
                val isFirst = index == 0
                val isLast = index == dayRecords.lastIndex

                ExerciseHistoryItem(
                    record = item,
                    isFirst = isFirst,
                    isLast = isLast
                )

                if (!isLast) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ExerciseHistoryItem(
    record: RecordWithExercise,
    isFirst: Boolean,
    isLast: Boolean
) {
    val cornerRadius = 24.dp 
    val smallRadius = 4.dp
    
    val shape = RoundedCornerShape(
        topStart = if (isFirst) cornerRadius else smallRadius,
        topEnd = if (isFirst) cornerRadius else smallRadius,
        bottomStart = if (isLast) cornerRadius else smallRadius,
        bottomEnd = if (isLast) cornerRadius else smallRadius
    )

    // Using basic Material 3 surfaceContainer color for a standard card look
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                Text(
                    text = "${record.record.sets} sets",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(
                    text = "${record.record.reps} reps",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(
                    text = "${record.record.weight} kg",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun formatDateHeader(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy - EEEE", Locale.getDefault())
    return sdf.format(Date(timestamp)).lowercase()
}
