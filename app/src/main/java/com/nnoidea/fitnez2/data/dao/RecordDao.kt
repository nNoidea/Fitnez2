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
import com.nnoidea.fitnez2.core.ValidateAndCorrect

@Dao
abstract class RecordDao {

    // ============================================================================================
    // INTERNAL & HELPER METHODS
    // ============================================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertInternal(record: Record): Long

    @Update
    protected abstract suspend fun updateInternal(record: Record)

    @Query("DELETE FROM record WHERE id = :id")
    protected abstract suspend fun deleteInternal(id: Int)

    // ============================================================================================
    // PUBLIC CRUD OPERATIONS
    // ============================================================================================

    // --- CREATE ---

    @Transaction
    open suspend fun create(record: Record): Long {
        validateRecord(record)
        return insertInternal(record)
    }

    // --- READ ---

    @Query("SELECT * FROM record WHERE id = :recordId")
    abstract suspend fun getRecordById(recordId: Int): Record?

    @Query("SELECT * FROM record ORDER BY date DESC, id DESC LIMIT :limit")
    abstract suspend fun getLatestRecords(limit: Int = 100): List<Record>

    @Query("SELECT * FROM record WHERE exerciseId = :exerciseId ORDER BY date DESC, id DESC LIMIT :limit")
    abstract suspend fun getRecordsByExerciseId(exerciseId: Int, limit: Int = 100): List<Record>

    @Query("SELECT * FROM record WHERE exerciseId IN (:exerciseIds) ORDER BY date DESC, id DESC LIMIT :limit")
    abstract suspend fun getRecordsByExerciseIds(exerciseIds: List<Int>, limit: Int = 100): List<Record>

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

    open suspend fun delete(recordId: Int) {
        deleteInternal(recordId)
    }

    // --- OLDER RECORDS (beyond the latest 100) ---

    @Query("SELECT * FROM record ORDER BY date DESC, id DESC LIMIT :limit OFFSET :offset")
    abstract suspend fun getOlderRecords(offset: Int, limit: Int = 50): List<Record>

    @Query("SELECT * FROM record WHERE exerciseId = :exerciseId ORDER BY date DESC, id DESC LIMIT :limit OFFSET :offset")
    abstract suspend fun getOlderRecordsByExerciseId(exerciseId: Int, offset: Int, limit: Int = 50): List<Record>

    @Query("SELECT * FROM record WHERE exerciseId IN (:exerciseIds) ORDER BY date DESC, id DESC LIMIT :limit OFFSET :offset")
    abstract suspend fun getOlderRecordsByExerciseIds(exerciseIds: List<Int>, offset: Int, limit: Int = 50): List<Record>

    @Query("SELECT COUNT(*) FROM record")
    abstract suspend fun getTotalRecordCount(): Int

    @Query("SELECT COUNT(*) FROM record WHERE exerciseId = :exerciseId")
    abstract suspend fun getRecordCountByExerciseId(exerciseId: Int): Int

    @Query("SELECT COUNT(*) FROM record WHERE exerciseId IN (:exerciseIds)")
    abstract suspend fun getRecordCountByExerciseIds(exerciseIds: List<Int>): Int

    @Query("SELECT * FROM record")
    abstract suspend fun getAllRecords(): List<Record>

    @Query("SELECT * FROM record ORDER BY date ASC, id ASC")
    abstract suspend fun getAllRecordsOrdered(): List<Record>

    @Query("DELETE FROM record")
    abstract suspend fun deleteAllRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(records: List<Record>)
}
