package com.nnoidea.fitnez2.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nnoidea.fitnez2.data.dao.RecordDao
import com.nnoidea.fitnez2.data.entities.Record

// =============================================================================
//  ScrollEngine — Encapsulates all infinite-scroll, batching & eviction logic.
//
//  ┌─────────────────────────────────────────────┐
//  │  This is "fragile engine territory".        │
//  │  Every mutation here is buffer-aware:        │
//  │   • recent vs older batch ownership          │
//  │   • color stability across sections          │
//  │   • height-preserving eviction               │
//  │                                              │
//  │  Think twice before modifying.               │
//  └─────────────────────────────────────────────┘
//
//  Architecture:
//
//  Tier 1 — recentRecords (latest RECENT_LIMIT, always in memory)
//  Tier 2 — olderBatches  (List<List<Record>?>)
//             loaded batch = List<Record>
//             evicted batch = null  → Spacer placeholder using saved height
//
//  Sliding window: at most (2 × BATCH_WINDOW_RADIUS + 1) batches loaded.
//  Everything else is evicted to free memory but keeps its scroll height.
// =============================================================================

class ScrollEngine(
    private val dao: RecordDao,
    private val selectedExerciseId: Int? = null
) {

    // -------------------------------------------------------------------------
    //  Constants
    // -------------------------------------------------------------------------

    companion object {
        const val RECENT_LIMIT = 100
        const val OLDER_BATCH_SIZE = 50

        /** How many batches to keep loaded around the current viewport. */
        const val BATCH_WINDOW_RADIUS = 2

        // Height estimates for eviction placeholders
        private const val ESTIMATED_RECORD_HEIGHT_DP = 60
        private const val ESTIMATED_HEADER_HEIGHT_DP = 80
        private const val ESTIMATED_SEPARATOR_HEIGHT_DP = 72

        fun estimateBatchHeightDp(recordCount: Int): Int {
            return (recordCount * ESTIMATED_RECORD_HEIGHT_DP) +
                    ESTIMATED_HEADER_HEIGHT_DP +
                    ESTIMATED_SEPARATOR_HEIGHT_DP
        }
    }

    // -------------------------------------------------------------------------
    //  Observable state (read by Compose)
    // -------------------------------------------------------------------------

    /** Latest N records — always in memory, never evicted. */
    var recentRecords by mutableStateOf<List<Record>>(emptyList())
        private set

    /** Older batches beyond RECENT_LIMIT. null = evicted. */
    var olderBatches by mutableStateOf<List<List<Record>?>>(emptyList())
        private set

    /** Remembered heights (dp) for evicted batches. */
    var batchHeights by mutableStateOf<Map<Int, Int>>(emptyMap())
        private set

    /** Original record count per batch (for DB offset calculations). */
    var batchSizes by mutableStateOf<List<Int>>(emptyList())
        private set

    var hasMoreOlderRecords by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var initialLoadDone by mutableStateOf(false)
        private set

    /** Total records loaded across all older batches (for offset calculation). */
    val totalOlderLoaded: Int get() = batchSizes.sum()

    // -------------------------------------------------------------------------
    //  Initial load
    // -------------------------------------------------------------------------

    suspend fun loadInitial() {
        recentRecords = if (selectedExerciseId == null) {
            dao.getLatestRecords()
        } else {
            dao.getRecordsByExerciseId(selectedExerciseId)
        }
        val totalCount = if (selectedExerciseId == null) {
            dao.getTotalRecordCount()
        } else {
            dao.getRecordCountByExerciseId(selectedExerciseId)
        }
        hasMoreOlderRecords = totalCount > RECENT_LIMIT
        olderBatches = emptyList()
        batchHeights = emptyMap()
        batchSizes = emptyList()
        initialLoadDone = true
    }

    // -------------------------------------------------------------------------
    //  Orphan cleanup (when exercises are deleted via CASCADE)
    // -------------------------------------------------------------------------

    fun removeOrphanedRecords(validExerciseIds: Set<Int>) {
        recentRecords = recentRecords.filter { it.exerciseId in validExerciseIds }
        olderBatches = olderBatches.map { batch ->
            batch?.filter { it.exerciseId in validExerciseIds }?.ifEmpty { null }
        }
    }

    // -------------------------------------------------------------------------
    //  Prepend a newly created record (scroll-to-top signal)
    // -------------------------------------------------------------------------

    suspend fun prependNewRecord(recordId: Int) {
        val newRecord = dao.getRecordById(recordId)
        if (newRecord != null && recentRecords.none { it.id == newRecord.id }) {
            recentRecords = listOf(newRecord) + recentRecords
        }
    }

    // -------------------------------------------------------------------------
    //  Load next batch (triggered when near bottom of list)
    // -------------------------------------------------------------------------

    suspend fun loadNextBatchIfNeeded(lastVisible: Int, totalItems: Int) {
        if (!hasMoreOlderRecords || isLoadingMore || totalItems == 0) return
        if (lastVisible < totalItems - 5) return

        isLoadingMore = true
        val offset = RECENT_LIMIT + totalOlderLoaded
        val batch = if (selectedExerciseId == null) {
            dao.getOlderRecords(offset = offset, limit = OLDER_BATCH_SIZE)
        } else {
            dao.getOlderRecordsByExerciseId(selectedExerciseId, offset = offset, limit = OLDER_BATCH_SIZE)
        }
        if (batch.isNotEmpty()) {
            olderBatches = olderBatches + listOf(batch)
            batchSizes = batchSizes + batch.size
        }
        hasMoreOlderRecords = batch.size == OLDER_BATCH_SIZE
        isLoadingMore = false
    }

    // -------------------------------------------------------------------------
    //  Eviction & reload (sliding window around viewport)
    // -------------------------------------------------------------------------

    /**
     * Given the batch index currently in the viewport, evicts far-away batches
     * and reloads nearby ones that were previously evicted.
     */
    suspend fun evictAndReload(focusBatch: Int) {
        if (olderBatches.isEmpty()) return

        var changed = false
        val newBatches = olderBatches.toMutableList()

        for (i in newBatches.indices) {
            val distance = kotlin.math.abs(i - focusBatch)

            if (distance > BATCH_WINDOW_RADIUS && newBatches[i] != null) {
                // Evict: save height estimate, free data
                if (i !in batchHeights) {
                    batchHeights = batchHeights + (i to estimateBatchHeightDp(
                        batchSizes.getOrElse(i) { OLDER_BATCH_SIZE }
                    ))
                }
                newBatches[i] = null
                changed = true
            } else if (distance <= BATCH_WINDOW_RADIUS && newBatches[i] == null) {
                // Reload: re-fetch from DB
                val offset = RECENT_LIMIT + batchSizes.take(i).sum()
                val reloaded = if (selectedExerciseId == null) {
                    dao.getOlderRecords(offset = offset, limit = batchSizes.getOrElse(i) { OLDER_BATCH_SIZE })
                } else {
                    dao.getOlderRecordsByExerciseId(
                        selectedExerciseId, offset = offset,
                        limit = batchSizes.getOrElse(i) { OLDER_BATCH_SIZE }
                    )
                }
                newBatches[i] = reloaded.ifEmpty { null }
                changed = true
            }
        }

        if (changed) {
            olderBatches = newBatches
        }
    }

    // -------------------------------------------------------------------------
    //  Buffer-aware update
    // -------------------------------------------------------------------------

    suspend fun updateRecord(updatedRecord: Record) {
        dao.update(updatedRecord)

        if (recentRecords.any { it.id == updatedRecord.id }) {
            recentRecords = recentRecords.map {
                if (it.id == updatedRecord.id) updatedRecord else it
            }
        } else {
            olderBatches = olderBatches.map { batch ->
                batch?.let {
                    if (it.any { r -> r.id == updatedRecord.id }) {
                        it.map { r -> if (r.id == updatedRecord.id) updatedRecord else r }
                    } else it
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Buffer-aware delete (returns info needed for undo)
    // -------------------------------------------------------------------------

    /** Snapshot of where a deleted record lived, so undo can restore it correctly. */
    data class DeleteContext(
        val freshRecord: Record,
        val wasInRecent: Boolean,
        val ownerBatchIndex: Int
    )

    /**
     * Deletes a record from DB and the correct in-memory buffer.
     * Returns a [DeleteContext] that [undoDelete] needs to restore it.
     */
    suspend fun deleteRecord(record: Record): DeleteContext {
        val freshRecord = dao.getRecordById(record.id) ?: record

        val wasInRecent = recentRecords.any { it.id == record.id }
        val ownerBatchIndex = if (!wasInRecent) {
            olderBatches.indexOfFirst { batch -> batch?.any { it.id == record.id } == true }
        } else -1

        dao.delete(record.id)

        if (wasInRecent) {
            recentRecords = recentRecords.filter { it.id != record.id }
        } else if (ownerBatchIndex >= 0) {
            olderBatches = olderBatches.mapIndexed { i, batch ->
                if (i == ownerBatchIndex) {
                    batch?.filter { it.id != record.id }?.ifEmpty { null }
                } else batch
            }
        }

        return DeleteContext(freshRecord, wasInRecent, ownerBatchIndex)
    }

    /**
     * Restores a previously deleted record into the correct buffer.
     * If the target batch was evicted, the record is already in DB and will
     * appear when the batch is reloaded on scroll-back.
     */
    suspend fun undoDelete(ctx: DeleteContext) {
        val newId = dao.create(ctx.freshRecord)
        val restored = ctx.freshRecord.copy(id = newId.toInt())

        if (ctx.wasInRecent) {
            val mutable = recentRecords.toMutableList()
            val insertIdx = mutable.indexOfFirst {
                it.date < restored.date || (it.date == restored.date && it.id < restored.id)
            }
            if (insertIdx == -1) mutable.add(restored) else mutable.add(insertIdx, restored)
            recentRecords = mutable
        } else if (ctx.ownerBatchIndex >= 0) {
            val targetIdx = ctx.ownerBatchIndex.coerceAtMost(olderBatches.lastIndex)
            if (targetIdx >= 0 && olderBatches[targetIdx] != null) {
                olderBatches = olderBatches.mapIndexed { i, batch ->
                    if (i == targetIdx) {
                        val mutable = (batch ?: emptyList()).toMutableList()
                        val insertIdx = mutable.indexOfFirst {
                            it.date < restored.date || (it.date == restored.date && it.id < restored.id)
                        }
                        if (insertIdx == -1) mutable.add(restored) else mutable.add(insertIdx, restored)
                        mutable
                    } else batch
                }
            }
            // If evicted → record is already in DB, will appear on reload.
        }
    }
}
