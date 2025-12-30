package com.nnoidea.fitnez2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.core.localization.LocalizationManager

@Dao
abstract class RecordDao {

    // ============================================================================================
    // INTERNAL & HELPER METHODS
    // ============================================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertInternal(record: Record): Long

    @Update
    protected abstract suspend fun updateInternal(record: Record)

    @Query("SELECT COUNT(*) FROM exercise WHERE id = :exerciseId")
    protected abstract suspend fun checkExerciseExists(exerciseId: Int): Int

    // ============================================================================================
    // PUBLIC CRUD OPERATIONS
    // ============================================================================================

    // --- CREATE ---

    @Transaction
    open suspend fun create(record: Record) {
        val count = checkExerciseExists(record.exerciseId)
        if (count == 0) {
            throw IllegalArgumentException(LocalizationManager.strings.errorExerciseNotFoundById(record.exerciseId))
        }
        insertInternal(record)
    }

    // --- READ ---

    @Query("""
        SELECT record.*, exercise.name as exerciseName 
        FROM record 
        JOIN exercise ON record.exerciseId = exercise.id 
        WHERE record.exerciseId = :exerciseId AND record.isDeleted = 0 
        ORDER BY record.date DESC, record.id DESC
    """)
    abstract suspend fun getSortedOne(exerciseId: Int): List<RecordWithExercise>

    @Query("""
        SELECT record.*, exercise.name as exerciseName 
        FROM record 
        JOIN exercise ON record.exerciseId = exercise.id 
        WHERE record.exerciseId = :exerciseId 
        ORDER BY record.date DESC, record.id DESC
    """)
    abstract suspend fun getSortedOneIncludeDeleted(exerciseId: Int): List<RecordWithExercise>

    @Query("""
        SELECT record.*, exercise.name as exerciseName 
        FROM record 
        JOIN exercise ON record.exerciseId = exercise.id 
        WHERE record.isDeleted = 0 
        ORDER BY record.date DESC, record.id DESC
    """)
    abstract suspend fun getSortedAll(): List<RecordWithExercise>

    // --- UPDATE ---

    @Transaction
    open suspend fun update(record: Record) {
        val count = checkExerciseExists(record.exerciseId)
        if (count == 0) {
           throw IllegalArgumentException(LocalizationManager.strings.errorExerciseNotFoundById(record.exerciseId))
        }
        updateInternal(record)
    }

    // --- DELETE ---

    /**
     * Soft deletes a record by setting isDeleted = 1.
     */
    @Query("UPDATE record SET isDeleted = 1 WHERE id = :recordId")
    abstract suspend fun softDelete(recordId: Int)

    /**
     * Permanently deletes a record from the database.
     */
    @Query("DELETE FROM record WHERE id = :recordId")
    abstract suspend fun hardDelete(recordId: Int)
}
