package com.nnoidea.fitnez2.ui.screenComponents.home

import com.nnoidea.fitnez2.ui.components.history.HistoryGridRow
import com.nnoidea.fitnez2.ui.components.history.HeaderLabel
import com.nnoidea.fitnez2.ui.components.history.HistoryRecordCard
import com.nnoidea.fitnez2.ui.components.history.computeColorParityByName
import com.nnoidea.fitnez2.ui.components.history.computeColorParity
import com.nnoidea.fitnez2.ui.components.history.recordCardShape
import com.nnoidea.fitnez2.ui.components.history.HistoryUiModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.nnoidea.fitnez2.ui.components.bottomsheet.autoHideBottomSheet
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nnoidea.fitnez2.core.TimeUtils
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.LocalAppDatabase
import com.nnoidea.fitnez2.data.LocalSettingsRepository
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Stable
import com.nnoidea.fitnez2.data.SettingsRepository
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.components.SwipeToDeleteContainer
import com.nnoidea.fitnez2.ui.components.HistorySetsField
import com.nnoidea.fitnez2.ui.components.HistoryRepsField
import com.nnoidea.fitnez2.ui.components.HistoryWeightField
import kotlinx.coroutines.CoroutineScope

// HistoryUiModel moved to SharedHistoryComponents.kt

// -----------------------------------------------------------------------------
// UI Style Constants - Change these to tweak the list's look
// -----------------------------------------------------------------------------

// -----------------------------------------------------------------------------

// -----------------------------------------------------------------------------
// Public Smart Component
// -----------------------------------------------------------------------------

@Composable
fun ExerciseHistoryList(
    modifier: Modifier = Modifier,
    extraBottomPadding: Dp = 0.dp,
    filterExerciseIds: List<Int>? = null,
    useAlternatingColors: Boolean = true,
    enableAutoHide: Boolean = false,
) {
    val state = rememberExerciseHistoryListState(filterExerciseIds, useAlternatingColors)
    ExerciseHistoryList(state = state, modifier = modifier, extraBottomPadding = extraBottomPadding, enableAutoHide = enableAutoHide)
}

@Composable
fun ExerciseHistoryList(
    state: ExerciseHistoryListState,
    modifier: Modifier = Modifier,
    extraBottomPadding: Dp = 0.dp,
    enableAutoHide: Boolean = false,
) {
    val scope = rememberCoroutineScope()

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(28.dp)
        ) {
            if (!state.initialLoadDone) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return@Surface
            }

            ExerciseHistoryListContent(
                modifier = Modifier.fillMaxSize(),
                listState = state.listState,
                uiItems = state.uiItems,
                weightUnit = state.weightUnit,
                extraBottomPadding = extraBottomPadding,
                enableAutoHide = enableAutoHide,
                onUpdateRequest = { state.onUpdateRequest(it) }
            ) { state.onDeleteRequest(it) }
        }

        ScrollToTopButton(
            listState = state.listState,
            onClick = {
                scope.launch {
                    state.scrollToTop(null)
                }
            },
            extraBottomPadding = extraBottomPadding,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
        )
    }
}

// -----------------------------------------------------------------------------
// Scroll-to-Top FAB
// -----------------------------------------------------------------------------

@Composable
private fun ScrollToTopButton(
    listState: LazyListState,
    onClick: () -> Unit,
    extraBottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    val globalUiState = com.nnoidea.fitnez2.ui.common.LocalGlobalUiState.current
    val view = LocalView.current
    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    LaunchedEffect(showButton) {
        globalUiState.isScrollToTopButtonVisible = showButton
    }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val animatedBottomPadding by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (globalUiState.isBottomSheetHidden) navBarPadding else extraBottomPadding,
        animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow),
        label = "fabBottomPadding"
    )

    AnimatedVisibility(
        visible = showButton,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier.padding(bottom = animatedBottomPadding)
    ) {
        FloatingActionButton(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                onClick()
            },
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}




// -----------------------------------------------------------------------------
// Stateless UI Components
// -----------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExerciseHistoryListContent(
    modifier: Modifier,
    listState: LazyListState,
    uiItems: List<HistoryUiModel>,
    weightUnit: String,
    extraBottomPadding: Dp,
    enableAutoHide: Boolean,
    onUpdateRequest: (Record) -> Unit,
    onDeleteRequest: (Record) -> Unit
) {

    if (uiItems.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = globalLocalization.labelHistoryEmpty,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.autoHideBottomSheet(enableAutoHide),
            state = listState,
            contentPadding = PaddingValues(bottom = 80.dp + extraBottomPadding)
        ) {
            item(key = "top_spacer_anchor") {
                Spacer(modifier = Modifier.height(1.dp))
            }

            itemsIndexed(
                items = uiItems,
                key = { _, model ->
                     when(model) {
                         is HistoryUiModel.Header -> "header_${model.section}_${model.date}"
                         is HistoryUiModel.RecordItem -> "record_${model.record.record.id}"
                         is HistoryUiModel.BatchSeparator -> "separator_${model.index}"
                         is HistoryUiModel.EvictedBatch -> "evicted_${model.index}"
                         is HistoryUiModel.LoadingMore -> "loading_more"
                     }
                },
                contentType = { _, model ->
                    when(model) {
                        is HistoryUiModel.Header -> "header"
                        is HistoryUiModel.RecordItem -> "record"
                        is HistoryUiModel.BatchSeparator -> "separator"
                        is HistoryUiModel.EvictedBatch -> "evicted"
                        is HistoryUiModel.LoadingMore -> "loading"
                    }
                }
            ) { index, item ->
                when (item) {
                    is HistoryUiModel.Header -> {
                        HistoryDateHeader(
                            date = item.date,
                            modifier = Modifier.animateItem()
                        )
                    }
                    is HistoryUiModel.BatchSeparator -> {
                        OlderRecordsSeparator(modifier = Modifier.animateItem())
                    }
                    is HistoryUiModel.EvictedBatch -> {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(item.heightDp.dp)
                        )
                    }
                    is HistoryUiModel.LoadingMore -> {
                        LoadingMoreIndicator(modifier = Modifier.animateItem())
                    }
                    is HistoryUiModel.RecordItem -> {
                        val isLight = item.isLight
                        
                        val prevItem = if (index > 0) uiItems[index - 1] else null
                        val nextItem = if (index < uiItems.lastIndex) uiItems[index + 1] else null
                        
                        val prevIsSame = (prevItem is HistoryUiModel.RecordItem) && (prevItem.isLight == isLight)
                        val nextIsSame = (nextItem is HistoryUiModel.RecordItem) && (nextItem.isLight == isLight)
                        
                        // Also show title if previous is NOT the same exercise (or is null/header/separator)
                        val showTitle = if (prevItem is HistoryUiModel.RecordItem) {
                             prevItem.record.exerciseName != item.record.exerciseName
                        } else {
                             true
                        }

                        val shape = recordCardShape(prevIsSame, nextIsSame)
                        
                        val showLabels = (prevItem is HistoryUiModel.Header) || (prevItem == null)

                        SwipeToDeleteContainer(
                            onDelete = { onDeleteRequest(item.record.record) },
                            modifier = Modifier.animateItem()
                        ) {
                            val timestamp = remember(item.record.record.date) {
                                java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(item.record.record.date))
                            }

                            HistoryRecordCard(
                                exerciseName = item.record.exerciseName,
                                sets = item.record.record.sets,
                                reps = item.record.record.reps,
                                weight = item.record.record.weight,
                                timestamp = timestamp,
                                isLight = isLight,
                                showTitle = showTitle,
                                weightUnit = weightUnit,
                                shape = shape,
                                prevIsSame = prevIsSame,
                                nextIsSame = nextIsSame,
                                showLabels = showLabels,
                                onUpdate = { sets, reps, weight ->
                                    onUpdateRequest(item.record.record.copy(sets = sets, reps = reps, weight = weight))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Separator & Loading Indicator
// -----------------------------------------------------------------------------

@Composable
private fun OlderRecordsSeparator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = globalLocalization.labelOlderRecords,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun LoadingMoreIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Extracted to SharedHistoryComponents.kt

@Composable
private fun HistoryDateHeader(
    date: Long,
    modifier: Modifier = Modifier
) {
    val globalUiState = LocalGlobalUiState.current
    val currentLocale = globalLocalization.appLocale
    val isToday = remember(date, globalUiState.currentDayKey) { android.text.format.DateUtils.isToday(date) }
    val isYesterday = remember(date, globalUiState.currentDayKey) {
        android.text.format.DateUtils.isToday(date + android.text.format.DateUtils.DAY_IN_MILLIS)
    }

    val dateString = remember(date, currentLocale) { 
        globalLocalization.formatDateShort(date) 
    }
    
    val dayName = remember(date, currentLocale, isToday, isYesterday) {
        if (isToday) globalLocalization.labelToday
        else if (isYesterday) globalLocalization.labelYesterday
        else globalLocalization.formatDayName(date)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = dateString,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

// Extracted to SharedHistoryComponents.kt
