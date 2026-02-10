package com.nnoidea.fitnez2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.core.ValidateAndCorrect
import androidx.paging.PagingSource

@Dao
abstract class RecordDao {

    // ============================================================================================
    // INTERNAL & HELPER METHODS
    // ============================================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertInternal(record: Record): Long

    @Update
    protected abstract suspend fun updateInternal(record: Record)

    @Query("DELETE FROM record WHERE id = :id")
    protected abstract suspend fun deleteInternal(id: Int)

    @Query("SELECT COUNT(*) FROM record WHERE groupIndex = :groupIndex")
    protected abstract suspend fun countRecordsInGroup(groupIndex: Int): Int

    @Query("SELECT * FROM record WHERE groupIndex = :groupIndex LIMIT 1")
    protected abstract suspend fun getOneRecordByGroupIndex(groupIndex: Int): Record?

    @Query("UPDATE record SET groupIndex = :newIndex WHERE groupIndex = :oldIndex")
    protected abstract suspend fun updateGroupIndex(oldIndex: Int, newIndex: Int)

    @Query("UPDATE record SET groupIndex = groupIndex - :amount WHERE groupIndex > :threshold")
    protected abstract suspend fun shiftGroupIndicesDown(threshold: Int, amount: Int)



    // ============================================================================================
    // PUBLIC CRUD OPERATIONS
    // ============================================================================================

    // --- CREATE ---

    @Transaction
    open suspend fun create(record: Record): Long {

        validateRecord(record)

        // Calculate groupIndex using lightweight query
        val lastRecord = getLastRecordInternal()
        val newGroupIndex = if (lastRecord == null) {
            0
        } else {
            if (lastRecord.exerciseId == record.exerciseId) {
                lastRecord.groupIndex
            } else {
                lastRecord.groupIndex + 1
            }
        }

        return insertInternal(record.copy(groupIndex = newGroupIndex))
    }

    // --- READ ---


    
    @Query("SELECT * FROM record WHERE id = :recordId")
    abstract suspend fun getRecordById(recordId: Int): Record?

    @Query("SELECT * FROM record ORDER BY date DESC, id DESC")
    abstract fun getRecordsPagingSourceRaw(): PagingSource<Int, Record>

    @Query("SELECT * FROM record WHERE exerciseId = :exerciseId ORDER BY date DESC, id DESC")
    abstract fun getRecordsByExerciseIdPagingSourceRaw(exerciseId: Int): PagingSource<Int, Record>

    // Optimized for "create" logic - avoids JOIN and complex sort
    @Query("SELECT * FROM record ORDER BY date DESC, id DESC LIMIT 1")
    abstract suspend fun getLastRecordInternal(): Record?

    @Query("""
        SELECT record.*, exercise.name as exerciseName 
        FROM record 
        JOIN exercise ON record.exerciseId = exercise.id 
        ORDER BY record.date DESC, record.id DESC
        LIMIT 1
    """)
    abstract suspend fun getLatestRecord(): RecordWithExercise?


    @Query("""
        SELECT record.*, exercise.name as exerciseName 
        FROM record 
        JOIN exercise ON record.exerciseId = exercise.id 
        WHERE record.exerciseId = :exerciseId 
        ORDER BY record.date DESC, record.id DESC
        LIMIT 1
    """)
    abstract suspend fun getLatestRecordByExerciseId(exerciseId: Int): RecordWithExercise?

    // --- UPDATE ---

    @Transaction
    open suspend fun update(record: Record) {
        validateRecord(record)
        updateInternal(record)
    }

    private fun validateRecord(record: Record) {
        require(ValidateAndCorrect.validateSets(record.sets)) { "Invalid sets: ${record.sets}" }
        require(ValidateAndCorrect.validateReps(record.reps)) { "Invalid reps: ${record.reps}" }
        require(ValidateAndCorrect.validateWeight(record.weight)) { "Invalid weight: ${record.weight}" }
    }

    // --- DELETE ---

    @Transaction
    open suspend fun delete(recordId: Int) {
        val record = getRecordById(recordId) ?: return
        val groupIndex = record.groupIndex
        
        // 1. Check if this is the last record in the group
        val count = countRecordsInGroup(groupIndex)
        
        // 2. Delete the record
        deleteInternal(recordId)
        
        if (count <= 1) {
            // Group is gone. Check for merge or shift.
            val prevGroupIndex = groupIndex - 1
            val nextGroupIndex = groupIndex + 1
            
            // We need to check if we can merge prev and next
            // Only if both exist.
            // Since we deleted 'groupIndex', we look for 'groupIndex-1' and 'groupIndex+1'
            // But wait, 'groupIndex+1' in the DB is currently labeled 'groupIndex+1'.
            
            val prevRecord = if (prevGroupIndex >= 0) getOneRecordByGroupIndex(prevGroupIndex) else null
            val nextRecord = getOneRecordByGroupIndex(nextGroupIndex)
            
            if (prevRecord != null && nextRecord != null && prevRecord.exerciseId == nextRecord.exerciseId) {
                // MERGE CASE
                // 1. Move next group to prev group
                updateGroupIndex(oldIndex = nextGroupIndex, newIndex = prevGroupIndex)
                // 2. Shift everything else down by 2 (because we removed one group AND merged one)
                shiftGroupIndicesDown(threshold = nextGroupIndex, amount = 2)
            } else {
                // STANDARD SHIFT CASE
                // Just shift everything above down by 1
                shiftGroupIndicesDown(threshold = groupIndex, amount = 1)
            }
        }
    }

    @Query("SELECT * FROM record")
    abstract suspend fun getAllRecords(): List<Record>

    @Query("DELETE FROM record")
    abstract suspend fun deleteAllRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(records: List<Record>)
}
