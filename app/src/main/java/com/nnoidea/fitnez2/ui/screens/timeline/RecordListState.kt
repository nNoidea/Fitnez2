package com.nnoidea.fitnez2.ui.screens.timeline

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.service.ExerciseService
import com.nnoidea.fitnez2.service.RecordService
import com.nnoidea.fitnez2.service.SettingsService
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.common.UiSignal
import com.nnoidea.fitnez2.ui.components.recordlist.RecordDisplayItem
import com.nnoidea.fitnez2.ui.components.recordlist.prepareRecordDisplayItems
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Stable
    interface RecordListState {
    val listState: LazyListState
    val uiItems: List<RecordDisplayItem>
    val weightUnit: String
    val initialLoadDone: Boolean
    val expandedRecordIds: SnapshotStateMap<String, Boolean>
    val timestampTokens: SnapshotStateMap<String, Long>
    fun onUpdateRequest(record: Record)
    fun onDeleteRequest(record: Record)
    fun onDeleteGroupRequest(records: List<Record>)
    fun showTimestampFor(recordId: String)
    suspend fun scrollToTop(recordId: String?)
    fun loadInitial()
    suspend fun loadUntilDate(targetDate: Long)
    fun updateExerciseMap(map: Map<String, String>)
}

@Stable
class RecordListStateImpl(
    private val scope: CoroutineScope,
    private val recordService: RecordService,
    private val exerciseService: ExerciseService,
    private val settingsService: SettingsService,
    private val globalUiState: GlobalUiState,
    private val onHapticFeedback: (Int) -> Unit,
    val filterExerciseIds: List<String>? = null,
    private val useAlternatingColors: Boolean = true
) : RecordListState {

    override val listState = LazyListState()
    override var weightUnit by mutableStateOf("kg")
    override var uiItems by mutableStateOf<List<RecordDisplayItem>>(emptyList())
    override var initialLoadDone by mutableStateOf(false)
    override val expandedRecordIds = mutableStateMapOf<String, Boolean>()
    override val timestampTokens = mutableStateMapOf<String, Long>()

    private var loadedRecords by mutableStateOf<List<Record>>(emptyList())
    private var hasMore by mutableStateOf(true)
    private var hasNewer by mutableStateOf(false)
    private var isLoading by mutableStateOf(false)

    var exerciseMap by mutableStateOf<Map<String, String>>(emptyMap())

    companion object {
        private const val INITIAL_LOAD = 100
        private const val PAGE_SIZE = 50
        private const val WINDOW_SIZE = 300
    }

    private val olderCursor get(): Triple<Long, Int, String>? {
        val last = loadedRecords.lastOrNull() ?: return null
        return Triple(last.date, last.orderNumber, last.id)
    }

    private val newerCursor get(): Triple<Long, Int, String>? {
        val first = loadedRecords.firstOrNull() ?: return null
        return Triple(first.date, first.orderNumber, first.id)
    }

    override fun loadInitial() {
        scope.launch {
            try {
                loadedRecords = if (filterExerciseIds.isNullOrEmpty()) {
                    recordService.getLatestRecords(INITIAL_LOAD)
                } else {
                    recordService.getRecordsByExerciseIds(filterExerciseIds, INITIAL_LOAD)
                }
                hasMore = loadedRecords.size >= INITIAL_LOAD
                hasNewer = false
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                loadedRecords = emptyList()
                hasMore = false
            }
            rebuildItems()
            initialLoadDone = true
        }
    }

    fun loadMore() {
        if (!hasMore || isLoading) return
        scope.launch {
            isLoading = true
            try {
                val cursor = olderCursor
                val more = if (cursor != null && filterExerciseIds.isNullOrEmpty()) {
                    recordService.getOlderRecordsAfter(cursor.first, cursor.second, cursor.third, PAGE_SIZE)
                } else if (cursor != null) {
                    recordService.getOlderRecordsByExerciseIds(filterExerciseIds!!, loadedRecords.size, PAGE_SIZE)
                } else {
                    recordService.getLatestRecords(PAGE_SIZE)
                }
                if (more.isEmpty()) {
                    hasMore = false
                } else {
                    var updated = loadedRecords + more
                    if (updated.size > WINDOW_SIZE) {
                        updated = updated.drop(updated.size - WINDOW_SIZE)
                        hasNewer = true
                    }
                    loadedRecords = updated
                    hasMore = more.size >= PAGE_SIZE
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                hasMore = false
            } finally {
                isLoading = false
                rebuildItems()
            }
        }
    }

    fun loadNewer() {
        if (!hasNewer || isLoading) return
        scope.launch {
            isLoading = true
            try {
                val cursor = newerCursor ?: return@launch
                val more = recordService.getNewerRecordsBefore(cursor.first, cursor.second, cursor.third, PAGE_SIZE)
                if (more.isEmpty()) {
                    hasNewer = false
                } else {
                    var updated = more.reversed() + loadedRecords
                    if (updated.size > WINDOW_SIZE) {
                        updated = updated.take(WINDOW_SIZE)
                        hasMore = true
                    }
                    loadedRecords = updated
                    hasNewer = more.size >= PAGE_SIZE
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                hasNewer = false
            } finally {
                isLoading = false
                rebuildItems()
            }
        }
    }

    override suspend fun loadUntilDate(targetDate: Long) {
        try {
            val dayRecords = recordService.getRecordsAroundDate(
                targetDate - 86400000L, targetDate + 86400000L
            ).filter { com.nnoidea.fitnez2.core.TimeUtils.isSameDay(it.date, targetDate) }

            if (dayRecords.isEmpty()) {
                val nearby = recordService.getRecordsAroundDate(targetDate - 86400000L, targetDate + 86400000L)
                if (nearby.isEmpty()) return
                loadedRecords = nearby.take(WINDOW_SIZE)
            } else {
                val latest = dayRecords.first()
                val earliest = dayRecords.last()
                var result = dayRecords.toMutableList()
                // 125 newer records above the target day
                try {
                    val newer = recordService.getNewerRecordsBefore(latest.date, latest.orderNumber, latest.id, 125)
                    if (newer.isNotEmpty()) result = (newer.reversed() + result).toMutableList()
                } catch (_: Exception) { }
                // 125 older records below the target day
                try {
                    val older = recordService.getOlderRecordsAfter(earliest.date, earliest.orderNumber, earliest.id, 125)
                    if (older.isNotEmpty()) result = (result + older).toMutableList()
                } catch (_: Exception) { }
                // Center the window with the target day ~50 records from the top
                val targetIdx = result.indexOfFirst { it.id == latest.id }
                val start = (targetIdx - 50).coerceAtLeast(0)
                val end = (start + WINDOW_SIZE).coerceAtMost(result.size)
                loadedRecords = result.subList(start, end).toList()
            }
            hasMore = true
            hasNewer = true
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) { }
        rebuildItems()
    }

    private fun rebuildItems() {
        val eligibleRecords = loadedRecords.filter {
            exerciseMap.containsKey(it.exerciseId) &&
            (filterExerciseIds == null || it.exerciseId in filterExerciseIds)
        }
        val items = prepareRecordDisplayItems(eligibleRecords, exerciseMap, useAlternatingColors, section = 0)
        uiItems = if (isLoading) {
            items + RecordDisplayItem.LoadingMore
        } else {
            items
        }
    }

    override fun onUpdateRequest(record: Record) {
        scope.launch {
            try {
                recordService.updateRecord(record.id, record.sets, record.reps, record.weight)
                updateRecordInList(record)
                GlobalUiState.emitToAll(UiSignal.RecordUpdated(record))
            } catch (e: CancellationException) { throw e } catch (_: Exception) {}
        }
    }

    override fun onDeleteRequest(record: Record) {
        scope.launch {
            val recordSnapshot = recordService.getRecordById(record.id) ?: record
            recordService.deleteRecord(record.id)
            removeRecordFromList(record.id)
            GlobalUiState.emitToAll(UiSignal.RecordDeleted(record.id))
            globalUiState.showSnackbar(
                message = globalLocalization.labelRecordDeleted,
                actionLabel = globalLocalization.labelUndo,
                onActionPerformed = {
                    onHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                    scope.launch {
                        val newRecord = recordService.createRecord(
                            exerciseId = recordSnapshot.exerciseId, sets = recordSnapshot.sets,
                            reps = recordSnapshot.reps, weight = recordSnapshot.weight, date = recordSnapshot.date
                        )
                        insertRecordIntoList(newRecord.id)
                        GlobalUiState.emitToAll(UiSignal.RecordInserted(newRecord.id))
                    }
                }
            )
        }
    }

    override fun onDeleteGroupRequest(records: List<Record>) {
        if (records.isEmpty()) return
        scope.launch {
            val snapshotRecords = records.toList()
            snapshotRecords.forEach { recordService.deleteRecord(it.id); removeRecordFromList(it.id) }
            GlobalUiState.emitToAll(UiSignal.RecordDeleted(snapshotRecords.last().id))
            globalUiState.showSnackbar(
                message = globalLocalization.labelRecordsDeleted,
                actionLabel = globalLocalization.labelUndo,
                onActionPerformed = {
                    onHapticFeedback(HapticFeedbackConstants.GESTURE_START)
                    scope.launch {
                        snapshotRecords.forEach {
                            val r = recordService.createRecord(
                                exerciseId = it.exerciseId, sets = it.sets,
                                reps = it.reps, weight = it.weight, date = it.date
                            )
                            insertRecordIntoList(r.id)
                            GlobalUiState.emitToAll(UiSignal.RecordInserted(r.id))
                        }
                    }
                }
            )
        }
    }

    private fun updateRecordInList(record: Record) {
        val idx = loadedRecords.indexOfFirst { it.id == record.id }
        if (idx >= 0) {
            val updated = loadedRecords.toMutableList()
            updated[idx] = record
            loadedRecords = updated
            rebuildItems()
        }
    }

    private fun removeRecordFromList(recordId: String) {
        loadedRecords = loadedRecords.filter { it.id != recordId }
        rebuildItems()
    }

    private fun insertRecordIntoList(recordId: String) {
        scope.launch {
            val record = recordService.getRecordById(recordId) ?: return@launch
            if (filterExerciseIds != null && record.exerciseId !in filterExerciseIds) return@launch
            val insertAt = loadedRecords.indexOfFirst { it.date <= record.date }
            val updated = loadedRecords.toMutableList()
            if (insertAt < 0) updated.add(record) else updated.add(insertAt, record)
            loadedRecords = updated
            rebuildItems()
        }
    }

    fun handleSignalInsert(recordId: String) {
        if (loadedRecords.any { it.id == recordId }) return
        insertRecordIntoList(recordId)
    }

    fun handleSignalUpdate(record: Record) {
        if (filterExerciseIds != null && record.exerciseId !in filterExerciseIds) return
        val idx = loadedRecords.indexOfFirst { it.id == record.id }
        if (idx >= 0) {
            val existing = loadedRecords[idx]
            if (existing.sets == record.sets && existing.reps == record.reps && existing.weight == record.weight) return
        }
        updateRecordInList(record)
    }

    fun handleSignalDelete(recordId: String) {
        if (loadedRecords.none { it.id == recordId }) return
        removeRecordFromList(recordId)
    }

    override fun showTimestampFor(recordId: String) {
        val token = System.currentTimeMillis()
        timestampTokens[recordId] = token
        scope.launch {
            delay(5000)
            if (timestampTokens[recordId] == token) timestampTokens.remove(recordId)
        }
    }

    override suspend fun scrollToTop(recordId: String?) {
        if (recordId == null) { listState.scrollToItem(0); return }
        withTimeoutOrNull(5000) {
            snapshotFlow { uiItems }.first { items ->
                items.any { it is RecordDisplayItem.RecordGroup && it.records.any { r -> r.record.id == recordId } }
            }
        }
        listState.scrollToItem(0)
    }

    override fun updateExerciseMap(map: Map<String, String>) { this.exerciseMap = map }

    fun updateWeightUnit(unit: String) { this.weightUnit = unit }
}
