package com.nnoidea.fitnez2.ui.screens

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nnoidea.fitnez2.MainActivity
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.LocalAppDatabase
import com.nnoidea.fitnez2.ui.components.ScreenScaffold
import com.nnoidea.fitnez2.ui.navigation.AppPage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun MonthlyScreen(onOpenDrawer: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val database = LocalAppDatabase.current

    val exerciseDao = remember(database) { database.exerciseDao() }
    val exercises by exerciseDao.getAllExercisesFlow().collectAsState(initial = emptyList())
    val exerciseMap = remember(exercises) { exercises.associate { it.id to it.name } }

    // Setup Horizontal Pager with an initial center index for infinite-like swiping
    val initialPage = 10000
    val pagerState = rememberPagerState(initialPage = initialPage) { 20000 }

    // State to dynamically control pager scrolling when swiping open the navigation drawer from the left edge
    var pagerScrollEnabled by remember { mutableStateOf(true) }

    // Derive active month start date from current page position
    val currentMonthStart = remember(pagerState.currentPage) {
        LocalDate.now().withDayOfMonth(1).plusMonths((pagerState.currentPage - initialPage).toLong())
    }

    // Optimize database loading by fetching records for a 5-month swiping window (2 previous, current, 2 next)
    val windowStartMillis = remember(currentMonthStart) {
        currentMonthStart.minusMonths(2).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    val windowEndMillis = remember(currentMonthStart) {
        currentMonthStart.plusMonths(3).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1L
    }

    val recordDao = remember(database) { database.recordDao() }
    val records by remember(windowStartMillis, windowEndMillis) {
        recordDao.getRecordsByDateRangeFlow(windowStartMillis, windowEndMillis)
    }.collectAsState(initial = emptyList())

    // Pre-compute and group records by day ONCE to prevent massive O(N) date conversions inside every cell!
    val recordsByDay = remember(records) {
        records.groupBy { record ->
            java.time.Instant.ofEpochMilli(record.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    // Dynamic header Month + Year string, automatically localized
    val monthYearLabel = remember(currentMonthStart, globalLocalization.appLocale) {
        currentMonthStart.format(DateTimeFormatter.ofPattern("MMMM yyyy", globalLocalization.appLocale))
    }

    // Dynamic weekday abbreviations (Mon, Tue, etc.)
    val weekdayHeaders = remember(globalLocalization.appLocale) {
        (1..7).map {
            DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, globalLocalization.appLocale).uppercase()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val isNotCurrentMonth = pagerState.currentPage != initialPage

    ScreenScaffold(
        title = monthYearLabel,
        onOpenDrawer = onOpenDrawer,
        actions = {
            if (isNotCurrentMonth) {
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                        coroutineScope.launch {
                            val diff = kotlin.math.abs(pagerState.currentPage - initialPage)
                            if (diff > 1) {
                                // Optimization: Snap to the adjacent page instantly to avoid measuring 
                                // and rendering all intermediate months during a long-distance scroll animation.
                                val adjacentPage = if (pagerState.currentPage > initialPage) {
                                    initialPage + 1
                                } else {
                                    initialPage - 1
                                }
                                pagerState.scrollToPage(adjacentPage)
                            }
                            // Smoothly animate the final 1-month transition
                            pagerState.animateScrollToPage(initialPage)
                        }
                    },
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = globalLocalization.labelGoToCurrentMonth,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, bottom = 24.dp) // Increase bottom padding to 24.dp to make day tiles shorter
                ) {
                    // Weekday Headers Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp), // Added horizontal alignment padding
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        weekdayHeaders.forEach { header ->
                            Text(
                                text = header,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer // Match standard day number color!
                            )
                        }
                    }

                    // Swipeable Calendar Grid
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val firstChange = event.changes.firstOrNull()
                                        if (firstChange != null) {
                                            if (firstChange.pressed) {
                                                // If gesture starts in leftmost 30dp (the navigation drawer's drag zone),
                                                // temporarily disable the pager to let the parent drawer handle the swipe!
                                                val startX = firstChange.position.x
                                                val edgeThreshold = 30.dp.toPx()
                                                if (startX < edgeThreshold) {
                                                    pagerScrollEnabled = false
                                                }
                                            } else {
                                                pagerScrollEnabled = true
                                            }
                                        }
                                        // Reset to true once all fingers are lifted
                                        if (event.changes.none { it.pressed }) {
                                            pagerScrollEnabled = true
                                        }
                                    }
                                }
                            },
                        pageSpacing = 16.dp, // Beautiful spacer gap between pages while swiping
                        userScrollEnabled = pagerScrollEnabled
                    ) { page ->
                        // Math for current page's target month
                        val pageMonthStart = remember(page) {
                            LocalDate.now().withDayOfMonth(1).plusMonths((page - initialPage).toLong())
                        }

                        val pageGridDays = remember(pageMonthStart) {
                            val firstDayOfMonth = pageMonthStart.withDayOfMonth(1)
                            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Monday, 7 = Sunday
                            val offset = firstDayOfWeek - 1
                            val gridStartDate = firstDayOfMonth.minusDays(offset.toLong())
                            (0 until 42).map { gridStartDate.plusDays(it.toLong()) }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp), // Pages are inset horizontally, but swipe edge-to-edge!
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            val rows = pageGridDays.chunked(7)
                            rows.forEachIndexed { rowIndex, week ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f), // Equally distribute remaining vertical height to each row
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    week.forEachIndexed { colIndex, day ->
                                        val gridIndex = rowIndex * 7 + colIndex
                                        val isCurrentMonth = day.month == pageMonthStart.month && day.year == pageMonthStart.year
                                        val isToday = day.isEqual(LocalDate.now())
                                        val dayRecords = recordsByDay[day] ?: emptyList()
                                        val hasExercises = dayRecords.isNotEmpty()

                                        // We only display up to 6 + "..." so we can safely short-circuit processing after finding 7 unique names!
                                        val dayExerciseNames = remember(dayRecords, exerciseMap) {
                                            dayRecords.asSequence()
                                                .mapNotNull { record -> exerciseMap[record.exerciseId] }
                                                .distinct()
                                                .take(7)
                                                .toList()
                                        }

                                        // Apply large 18.dp corners to the 4 outermost layout grid corners, tiny soft 4.dp inside
                                        val cellShape = remember(gridIndex) {
                                            when (gridIndex) {
                                                0 -> RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomEnd = 4.dp, bottomStart = 4.dp)
                                                6 -> RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 4.dp, bottomStart = 4.dp)
                                                35 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomEnd = 4.dp, bottomStart = 18.dp)
                                                41 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomEnd = 18.dp, bottomStart = 4.dp)
                                                else -> RoundedCornerShape(4.dp)
                                            }
                                        }

                                        
                                            


                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .padding(1.5.dp) // Symmetrical 1.5.dp padding (giving a perfect 3.dp gap between days)
                                                .clip(cellShape)
                                                
                                                .background(
                                                    if (isCurrentMonth) MaterialTheme.colorScheme.secondaryContainer
                                                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                                                )
                                                .clickable(enabled = hasExercises) {
                                                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                                                    // Jump directly to the Timeline position for that day
                                                    val epochMillis = day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                                    val intent = Intent(context, MainActivity::class.java).apply {
                                                        putExtra(MainActivity.EXTRA_PAGE_ROUTE, AppPage.Timeline.route)
                                                        putExtra("extra_target_date", epochMillis)
                                                        putExtra("extra_source_route", AppPage.Monthly.route)
                                                    }
                                                    context.startActivity(intent)
                                                },
                                            contentAlignment = Alignment.TopCenter // Day numbers start at the top
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 0.dp, vertical = 2.dp), // Zeroed horizontal padding so pills stretch edge-to-edge
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Top
                                            ) {
                                                 Box(
                                                     modifier = if (isToday) {
                                                          val pillBgColor = if (isCurrentMonth) {
                                                              MaterialTheme.colorScheme.onPrimaryContainer
                                                          } else {
                                                              MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.35f)
                                                          }
                                                          Modifier
                                                              .width(28.dp)
                                                              .height(20.dp)
                                                              .background(
                                                                  color = pillBgColor,
                                                                  // Logical pill shape: since height is 20.dp, half of height (10.dp) makes left and right fully rounded
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
                                                         text = day.dayOfMonth.toString(),
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
                                                             if (isCurrentMonth) MaterialTheme.colorScheme.primaryContainer
                                                             else androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.primaryContainer, androidx.compose.ui.graphics.Color.Black, 0.4f)
                                                         } else {
                                                             if (isCurrentMonth) MaterialTheme.colorScheme.onSecondaryContainer
                                                             else androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.onSecondaryContainer, androidx.compose.ui.graphics.Color.Black, 0.4f)
                                                         }
                                                     )
                                                 }

                                                if (hasExercises) {
                                                    Spacer(modifier = Modifier.height(2.dp)) // Lowered gap

                                                    val pillContainerColor = if (isCurrentMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                                    val pillContentColor = if (isCurrentMonth) MaterialTheme.colorScheme.onPrimary else androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.onPrimary, androidx.compose.ui.graphics.Color.Black, 0.4f)

                                                    // Safely display up to 6 items max. If larger, show 5 items + overflow pill ellipsis "..."
                                                    val displayNames = if (dayExerciseNames.size > 6) {
                                                        dayExerciseNames.take(5) + "..."
                                                    } else {
                                                        dayExerciseNames
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
                                }
                            }
                        }
                    }
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
            .padding(vertical = 0.5.dp) // Tight vertical spacing
            .clip(RoundedCornerShape(4.dp))
            .background(containerColor)
            .padding(horizontal = 2.dp, vertical = 1.dp), // Minimal inner padding
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.5.sp, // Extremely compact font size
                lineHeight = 9.sp,
                fontWeight = FontWeight.Bold // Made bold
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Clip, // Clip directly with zero ... ellipsis!
            textAlign = TextAlign.Center
        )
    }
}
