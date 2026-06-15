package com.nnoidea.fitnez2.ui.screens.timeline

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.nnoidea.fitnez2.core.TimeUtils
import com.nnoidea.fitnez2.service.LocalExerciseService
import com.nnoidea.fitnez2.service.LocalRecordService
import com.nnoidea.fitnez2.service.LocalSettingsService
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.common.UiSignal
import com.nnoidea.fitnez2.ui.components.recordlist.RecordDisplayItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun rememberRecordListState(
    filterExerciseIds: List<String>? = null,
    useAlternatingColors: Boolean = true
): RecordListState {
    val scope = rememberCoroutineScope()
    val recordService = LocalRecordService.current
    val exerciseService = LocalExerciseService.current
    val settingsService = LocalSettingsService.current
    val globalUiState = LocalGlobalUiState.current
    val view = LocalView.current

    val weightUnit by settingsService.weightUnitFlow.collectAsState(initial = "kg")
    val exercisesList by exerciseService.getAllExercisesFlow().collectAsState(initial = emptyList())
    val exerciseMap = remember(exercisesList) {
        exercisesList.associate { it.id to it.name }
    }

    val state = remember(filterExerciseIds, useAlternatingColors) {
        RecordListStateImpl(
            scope = scope,
            recordService = recordService,
            exerciseService = exerciseService,
            settingsService = settingsService,
            globalUiState = globalUiState,
            onHapticFeedback = { view.performHapticFeedback(it) },
            filterExerciseIds = filterExerciseIds,
            useAlternatingColors = useAlternatingColors
        )
    }

    LaunchedEffect(exerciseMap) {
        state.updateExerciseMap(exerciseMap)
        if (exerciseMap.isNotEmpty()) state.loadInitial()
    }

    LaunchedEffect(weightUnit) { (state as RecordListStateImpl).updateWeightUnit(weightUnit) }

    LaunchedEffect(state.listState) {
        snapshotFlow {
            val layout = state.listState.layoutInfo
            Triple(
                layout.visibleItemsInfo.firstOrNull()?.index ?: 0,
                layout.visibleItemsInfo.lastOrNull()?.index ?: 0,
                layout.totalItemsCount
            )
        }.collect { (firstVisible, lastVisible, total) ->
            if (total > 0 && state.uiItems.isNotEmpty() && state.listState.isScrollInProgress) {
                val impl = state as RecordListStateImpl
                if (firstVisible <= 3) impl.loadNewer()
                if (lastVisible >= total - 5) impl.loadMore()
            }
        }
    }

    val context = LocalContext.current
    val activity = context as? Activity
    var intentHandled by remember { mutableStateOf(false) }
    LaunchedEffect(state.initialLoadDone) {
        if (!state.initialLoadDone || intentHandled) return@LaunchedEffect
        val intent = activity?.intent
        val targetDate = intent?.getLongExtra("extra_target_date", -1L)?.takeIf { it != -1L }
        if (targetDate != null) {
            state.loadUntilDate(targetDate)
            withTimeoutOrNull(5000) {
                snapshotFlow { state.uiItems }
                    .first { items ->
                        items.any { it is RecordDisplayItem.DateHeader &&
                            TimeUtils.isSameDay(it.date, targetDate) }
                    }
            }
            val items = state.uiItems
            val targetIndex = items.indexOfFirst {
                it is RecordDisplayItem.DateHeader && TimeUtils.isSameDay(it.date, targetDate)
            }
            if (targetIndex >= 0) state.listState.scrollToItem(targetIndex)
            intent.removeExtra("extra_target_date")
            intentHandled = true
        }
    }

    LaunchedEffect(state, globalUiState) {
        globalUiState.signalFlow.collect { signal ->
            when (signal) {
                is UiSignal.ScrollToTop -> state.scrollToTop(null)
                is UiSignal.ScrollToRecord -> state.scrollToTop(signal.recordId)
                is UiSignal.RecordInserted -> (state as RecordListStateImpl).handleSignalInsert(signal.recordId)
                is UiSignal.RecordUpdated -> (state as RecordListStateImpl).handleSignalUpdate(signal.record)
                is UiSignal.RecordDeleted -> (state as RecordListStateImpl).handleSignalDelete(signal.recordId)
                is UiSignal.DatabaseSeeded -> state.loadInitial()
            }
        }
    }

    return state
}
