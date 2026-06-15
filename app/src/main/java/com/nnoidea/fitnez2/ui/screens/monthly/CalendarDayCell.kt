package com.nnoidea.fitnez2.ui.screens.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun CalendarDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    hasExercises: Boolean,
    exerciseDisplayNames: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cellShape: Shape = RoundedCornerShape(4.dp)
) {
    BoxWithConstraints(
        modifier = modifier
            .padding(1.5.dp)
            .clip(cellShape)
            .background(
                if (isCurrentMonth) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
            )
            .clickable(enabled = hasExercises) { onClick() },
        contentAlignment = Alignment.TopCenter
    ) {
        val availableHeight = maxHeight - 26.dp
        val computedMaxPills = (availableHeight.value / 13).toInt().coerceAtLeast(1)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = if (isToday) {
                    val pillBgColor = if (isCurrentMonth) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
                    }
                    Modifier
                        .width(28.dp)
                        .height(20.dp)
                        .background(
                            color = pillBgColor,
                            shape = RoundedCornerShape(10.dp)
                        )
                } else {
                    Modifier
                        .width(28.dp)
                        .height(20.dp)
                },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                            includeFontPadding = false
                        ),
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    color = if (isToday) {
                        if (isCurrentMonth) MaterialTheme.colorScheme.onTertiary
                        else lerp(MaterialTheme.colorScheme.onTertiary, Color.Black, 0.4f)
                    } else {
                        if (isCurrentMonth) MaterialTheme.colorScheme.onSecondaryContainer
                        else lerp(MaterialTheme.colorScheme.onSecondaryContainer, Color.Black, 0.4f)
                    }
                )
            }

            if (hasExercises) {
                Spacer(modifier = Modifier.height(2.dp))

                val pillContainerColor = if (isCurrentMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                val pillContentColor = if (isCurrentMonth) MaterialTheme.colorScheme.onPrimary else lerp(MaterialTheme.colorScheme.onPrimary, Color.Black, 0.4f)

                val displayNames = if (exerciseDisplayNames.size > computedMaxPills) {
                    exerciseDisplayNames.take(computedMaxPills - 1) + "..."
                } else {
                    exerciseDisplayNames
                }

                displayNames.forEach { name ->
                    ExercisePill(
                        name = name,
                        containerColor = pillContainerColor,
                        contentColor = pillContentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ExercisePill(
    name: String,
    containerColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(13.dp)
            .padding(vertical = 0.5.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.5.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center
        )
    }
}
