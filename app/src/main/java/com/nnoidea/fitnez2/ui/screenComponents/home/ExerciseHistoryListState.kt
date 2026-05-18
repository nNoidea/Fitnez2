package com.nnoidea.fitnez2.ui.screenComponents.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.LocalAppDatabase
import com.nnoidea.fitnez2.data.LocalSettingsRepository
import com.nnoidea.fitnez2.data.SettingsRepository
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.common.UiSignal
import com.nnoidea.fitnez2.ui.components.ScrollEngine
import com.nnoidea.fitnez2.ui.components.history.buildUiItems
import com.nnoidea.fitnez2.ui.components.history.HistoryUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.view.HapticFeedbackConstants
import kotlinx.coroutines.flow.first
import kotlin.collections.buildList

/**
 * Universal interface for any component that shows a list of exercise records.
 * This can be used for standard history, live workout sessions, or even fake data for previews.
 */
@Stable
interface ExerciseHistoryListState {
    val listState: LazyListState
    val uiItems: List<HistoryUiModel>
    val weightUnit: String
    val initialLoadDone: Boolean
    val isScrollingToTop: Boolean

    fun onUpdateRequest(updatedRecord: Record)
    fun onDeleteRequest(record: Record)
    suspend fun scrollToTop(recordId: Int? = null)
}

/**
 * Standard implementation that pulls data from the App Database using [ScrollEngine].
 */
@Stable
class DatabaseExerciseHistoryListState(
    private val scope: CoroutineScope,
    private val dao: com.nnoidea.fitnez2.data.dao.RecordDao,
    private val exerciseDao: com.nnoidea.fitnez2.data.dao.ExerciseDao,
    private val settingsRepository: SettingsRepository,
    private val globalUiState: GlobalUiState,
    private val onHapticFeedback: (Int) -> Unit,
    val filterExerciseIds: List<Int>? = null,
    private val useAlternatingColors: Boolean = true
) : ExerciseHistoryListState {
    
    override val listState = LazyListState()
    
    val engine = ScrollEngine(dao, filterExerciseIds)

    override var weightUnit by mutableStateOf("kg")
        private set

    override var uiItems by mutableStateOf<List<HistoryUiModel>>(emptyList())
        private set

    override var initialLoadDone by mutableStateOf(false)
        private set

    override var isScrollingToTop by mutableStateOf(false)
        private set

    suspend fun loadInitial() {
        engine.loadInitial()
        initialLoadDone = engine.initialLoadDone
    }

    override fun onUpdateRequest(updatedRecord: Record) {
        scope.launch {
            try {
                dao.update(updatedRecord)
                GlobalUiState.emitToAll(UiSignal.RecordUpdated(updatedRecord))
            } catch (_: Exception) { }
        }
    }

    override fun onDeleteRequest(record: Record) {
        scope.launch {
            val freshRecord = dao.getRecordById(record.id) ?: record
            dao.delete(record.id)
            GlobalUiState.emitToAll(UiSignal.RecordDeleted(record.id))

            globalUiState.showSnackbar(
                message = globalLocalization.labelRecordDeleted,
                actionLabel = globalLocalization.labelUndo,
                onActionPerformed = {
                    onHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                    scope.launch {
                        val newId = dao.create(freshRecord.copy(id = 0))
                        GlobalUiState.emitToAll(UiSignal.RecordInserted(newId.toInt()))
                    }
                }
            )
        }
    }

    override suspend fun scrollToTop(recordId: Int?) {
        isScrollingToTop = true
        try {
            val wasAtTop = listState.firstVisibleItemIndex <= 1

            if (recordId == null) {
                if (wasAtTop) {
                    listState.scrollToItem(0)
                } else {
                    if (listState.firstVisibleItemIndex > 10) {
                        listState.scrollToItem(10)
                    }
                    listState.animateScrollToItem(0)
                }
                return
            }

            // Wait for the record to appear in the UI (inserted by RecordInserted signal)
            kotlinx.coroutines.withTimeoutOrNull(5000) {
                androidx.compose.runtime.snapshotFlow { uiItems }
                    .first { items ->
                        items.any { item ->
                            item is HistoryUiModel.RecordItem && item.record.record.id == recordId
                        }
                    }
            }

            if (wasAtTop) {
                listState.scrollToItem(0)
            } else {
                if (listState.firstVisibleItemIndex > 10) {
                    listState.scrollToItem(10)
                }
                listState.animateScrollToItem(0)
            }
        } finally {
            isScrollingToTop = false
        }
    }

    fun updateUiItems(items: List<HistoryUiModel>) {
        this.uiItems = items
    }

    fun updateWeightUnit(unit: String) {
        this.weightUnit = unit
    }

    fun updateInitialLoadDone(done: Boolean) {
        this.initialLoadDone = done
    }
}

@Composable
fun rememberExerciseHistoryListState(
    filterExerciseIds: List<Int>? = null,
    useAlternatingColors: Boolean = true
): ExerciseHistoryListState {
    val scope = rememberCoroutineScope()
    val database = LocalAppDatabase.current
    val dao = database.recordDao()
    val exerciseDao = database.exerciseDao()
    val settingsRepository = LocalSettingsRepository.current
    val globalUiState = LocalGlobalUiState.current
    val view = androidx.compose.ui.platform.LocalView.current

    val state = remember(filterExerciseIds, useAlternatingColors) {
        DatabaseExerciseHistoryListState(
            scope = scope,
            dao = dao,
            exerciseDao = exerciseDao,
            settingsRepository = settingsRepository,
            globalUiState = globalUiState,
            onHapticFeedback = { view.performHapticFeedback(it) },
            filterExerciseIds = filterExerciseIds,
            useAlternatingColors = useAlternatingColors
        )
    }

    // Connect dependencies and UI items reactivity
    val weightUnit by settingsRepository.weightUnitFlow.collectAsState(initial = "kg")
    
    val exercisesList by exerciseDao.getAllExercisesFlow().collectAsState(initial = emptyList())
    val exerciseMap = remember(exercisesList) {
        exercisesList.associate { it.id to it.name }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(state, lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    state.loadInitial()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Ensures initial data loading is triggered immediately upon composition.
    // This resolves potential lifecycle gaps when navigating via internal Compose routes.
    LaunchedEffect(state) {
        if (!state.engine.initialLoadDone) {
            state.loadInitial()
        }
    }

    // Build UI model for recent records (section 0)
    val recentUiItems = remember(state.engine.recentRecords, exerciseMap, useAlternatingColors) {
        val validRecords = state.engine.recentRecords.filter { exerciseMap.containsKey(it.exerciseId) }
        buildUiItems(validRecords, exerciseMap, useAlternatingColors, section = 0)
    }

    // Build UI models for each loaded batch independently.
    val olderBatchUiItems = remember(state.engine.olderBatches, state.engine.batchHeights, state.engine.batchSizes, exerciseMap, useAlternatingColors) {
        state.engine.olderBatches.mapIndexed { i, batch ->
            if (batch != null) {
                val validRecords = batch.filter { exerciseMap.containsKey(it.exerciseId) }
                buildUiItems(validRecords, exerciseMap, useAlternatingColors, section = i + 1)
            } else {
                val height = state.engine.batchHeights[i]
                    ?: ScrollEngine.estimateBatchHeightDp(state.engine.batchSizes.getOrElse(i) { ScrollEngine.OLDER_BATCH_SIZE })
                listOf(HistoryUiModel.EvictedBatch(i, height))
            }
        }
    }

    // Combine into a single flat list
    val allUiItems = remember(recentUiItems, olderBatchUiItems, state.engine.hasMoreOlderRecords, state.engine.isLoadingMore) {
        buildList {
            addAll(recentUiItems)
            for ((batchIndex, batchItems) in olderBatchUiItems.withIndex()) {
                if (batchItems.isEmpty()) continue

                val isEvicted = batchItems.firstOrNull() is HistoryUiModel.EvictedBatch
                if (!isEvicted) {
                    val lastHeaderDate = (this as MutableList<HistoryUiModel>).filterIsInstance<HistoryUiModel.Header>().lastOrNull()?.date
                    val batchStart = if (
                        lastHeaderDate != null &&
                        batchItems.firstOrNull() is HistoryUiModel.Header &&
                        (batchItems.first() as HistoryUiModel.Header).date == lastHeaderDate
                    ) batchItems.drop(1) else batchItems

                    add(HistoryUiModel.BatchSeparator(batchIndex))
                    addAll(batchStart)
                } else {
                    add(HistoryUiModel.BatchSeparator(batchIndex))
                    addAll(batchItems)
                }
            }
            if (olderBatchUiItems.isEmpty() && state.engine.hasMoreOlderRecords) {
                add(HistoryUiModel.BatchSeparator(0))
            }
            if (state.engine.isLoadingMore) {
                add(HistoryUiModel.LoadingMore)
            }
        }
    }

    // Sync state properties
    LaunchedEffect(allUiItems) { state.updateUiItems(allUiItems) }
    LaunchedEffect(weightUnit) { state.updateWeightUnit(weightUnit) }
    LaunchedEffect(state.engine.initialLoadDone) { state.updateInitialLoadDone(state.engine.initialLoadDone) }

    // Handle intent target date scrolling
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    LaunchedEffect(allUiItems, activity) {
        val intent = activity?.intent
        val targetDate = intent?.getLongExtra("extra_target_date", -1L)?.takeIf { it != -1L }
        if (targetDate != null && allUiItems.isNotEmpty()) {
            val targetIndex = allUiItems.indexOfFirst {
                it is HistoryUiModel.Header && com.nnoidea.fitnez2.core.TimeUtils.isSameDay(it.date, targetDate)
            }
            if (targetIndex >= 0) {
                state.listState.scrollToItem(targetIndex)
                intent.removeExtra("extra_target_date")
            }
        }
    }

    // Handle signals
    LaunchedEffect(state, globalUiState) {
        globalUiState.signalFlow.collect { signal ->
            when (signal) {
                is UiSignal.ScrollToTop -> state.scrollToTop(signal.recordId)
                is UiSignal.RecordInserted -> state.engine.insertRecordIntoBuffer(signal.recordId)
                is UiSignal.RecordUpdated -> state.engine.updateRecordInBuffer(signal.record)
                is UiSignal.RecordDeleted -> state.engine.removeRecordFromBuffer(signal.recordId)
                is UiSignal.DatabaseSeeded -> state.loadInitial()
            }
        }
    }

    // ScrollEngine integration
    val currentAllUiItems by androidx.compose.runtime.rememberUpdatedState(allUiItems)
    LaunchedEffect(state.listState, state.engine) {
        snapshotFlow {
            val layoutInfo = state.listState.layoutInfo
            val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = layoutInfo.totalItemsCount
            Triple(firstVisible, lastVisible, total)
        }.collect { (firstVisible, lastVisible, total) ->
            state.engine.loadNextBatchIfNeeded(lastVisible, total)
            if (state.engine.olderBatches.isNotEmpty()) {
                val visibleBatches = mutableSetOf<Int>()
                for (idx in firstVisible..lastVisible) {
                    when (val item = currentAllUiItems.getOrNull(idx)) {
                        is HistoryUiModel.BatchSeparator -> visibleBatches.add(item.index)
                        is HistoryUiModel.EvictedBatch -> visibleBatches.add(item.index)
                        is HistoryUiModel.RecordItem -> {
                            for (j in idx downTo 0) {
                                val prev = currentAllUiItems.getOrNull(j)
                                if (prev is HistoryUiModel.BatchSeparator) {
                                    visibleBatches.add(prev.index)
                                    break
                                }
                            }
                        }
                        else -> {}
                    }
                }
                if (!state.isScrollingToTop) {
                    state.engine.evictAndReload(visibleBatches.maxOrNull() ?: 0)
                }
            }
        }
    }

    return state
}


