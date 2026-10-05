package com.nnoidea.fitnez2.ui.screens.timeline

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.nnoidea.fitnez2.service.LocalExerciseService
import com.nnoidea.fitnez2.service.LocalRecordService
import com.nnoidea.fitnez2.service.LocalSettingsService
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.common.UiSignal

@Composable
fun rememberRecordListState(
    filterExerciseIds: List<String>? = null,
    useAlternatingColors: Boolean = true,
    targetDate: Long? = null
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
            globalUiState = globalUiState,
            onHapticFeedback = { view.performHapticFeedback(it) },
            filterExerciseIds = filterExerciseIds,
            useAlternatingColors = useAlternatingColors
        )
    }

    val context = LocalContext.current
    val activity = context as? Activity
    val effectiveTargetDate = targetDate ?: activity?.intent?.getLongExtra("extra_target_date", -1L)?.takeIf { it != -1L }

    LaunchedEffect(state, exerciseMap, effectiveTargetDate) {
        state.updateExerciseMap(exerciseMap)
        if (exerciseMap.isNotEmpty()) {
            if (effectiveTargetDate != null) {
                state.loadUntilDate(effectiveTargetDate)
                activity?.intent?.removeExtra("extra_target_date")
            } else if (!state.initialLoadDone) {
                state.loadInitial()
            }
        }
    }

    LaunchedEffect(state, weightUnit) { (state as RecordListStateImpl).updateWeightUnit(weightUnit) }

    LaunchedEffect(state, globalUiState.nightModeHour) {
        (state as? RecordListStateImpl)?.rebuildItems()
    }

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

    LaunchedEffect(state, globalUiState) {
        globalUiState.signalFlow.collect { signal ->
            when (signal) {
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
