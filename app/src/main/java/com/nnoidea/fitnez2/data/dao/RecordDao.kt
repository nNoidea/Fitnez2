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
    open suspend fun create(record: Record): Long {
        val count = checkExerciseExists(record.exerciseId)
        if (count == 0) {
            throw IllegalArgumentException(LocalizationManager.strings.errorExerciseNotFoundById(record.exerciseId))
        }

        // Validation
        if (!com.nnoidea.fitnez2.core.ValidateAndCorrect.validateSets(record.sets)) {
            throw IllegalArgumentException("Invalid sets: ${record.sets}")
        }
        if (!com.nnoidea.fitnez2.core.ValidateAndCorrect.validateReps(record.reps)) {
            throw IllegalArgumentException("Invalid reps: ${record.reps}")
        }
        if (!com.nnoidea.fitnez2.core.ValidateAndCorrect.validateWeight(record.weight)) {
            throw IllegalArgumentException("Invalid weight: ${record.weight}")
        }

        return insertInternal(record)
    }

    // --- READ ---

    @Query("""
        SELECT record.*, exercise.name as exerciseName 
        FROM record 
        JOIN exercise ON record.exerciseId = exercise.id 
        WHERE record.exerciseId = :exerciseId 
        ORDER BY record.date DESC, record.id DESC
    """)
    abstract fun getRecordsByExerciseId(exerciseId: Int): Flow<List<RecordWithExercise>>

    @Query("""
        SELECT record.*, exercise.name as exerciseName 
        FROM record 
        JOIN exercise ON record.exerciseId = exercise.id 
        ORDER BY record.date DESC, record.id DESC
    """)
    abstract fun getAllRecordsFlow(): Flow<List<RecordWithExercise>>
    
    @Query("SELECT * FROM record WHERE id = :recordId")
    abstract suspend fun getRecordById(recordId: Int): Record?

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
        // Validation
         if (!com.nnoidea.fitnez2.core.ValidateAndCorrect.validateSets(record.sets)) {
            throw IllegalArgumentException("Invalid sets: ${record.sets}")
        }
        if (!com.nnoidea.fitnez2.core.ValidateAndCorrect.validateReps(record.reps)) {
            throw IllegalArgumentException("Invalid reps: ${record.reps}")
        }
        if (!com.nnoidea.fitnez2.core.ValidateAndCorrect.validateWeight(record.weight)) {
            throw IllegalArgumentException("Invalid weight: ${record.weight}")
        }
        updateInternal(record)
    }

    // --- DELETE ---

    @Query("DELETE FROM record WHERE id = :recordId")
    abstract suspend fun delete(recordId: Int)
}
