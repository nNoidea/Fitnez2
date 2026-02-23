package com.nnoidea.fitnez2.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.core.localization.LocalizationManager

@Dao
abstract class ExerciseDao {

    // ============================================================================================
    // INTERNAL & HELPER METHODS
    // ============================================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(exercises: List<Exercise>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertInternal(exercise: Exercise): Long

    @Update
    protected abstract suspend fun updateInternal(exercise: Exercise)

    @Query("SELECT * FROM exercise WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    protected abstract suspend fun getExerciseByName(name: String): Exercise?

    @Query("DELETE FROM exercise WHERE id = :exerciseId")
    abstract suspend fun deleteExerciseInternal(exerciseId: Int)

    private fun validateAndPrepare(exercise: Exercise): Exercise {
        val trimmedName = exercise.name.trim()
        if (trimmedName.isBlank()) {
           throw IllegalArgumentException(LocalizationManager.strings.errorExerciseNameBlank)
        }
        return exercise.copy(name = trimmedName)
    }

    // ============================================================================================
    // PUBLIC CRUD OPERATIONS
    // ============================================================================================

    // --- CREATE ---

    @Transaction
    open suspend fun create(exercise: Exercise) {
        if (exercise.id != 0) {
            throw IllegalArgumentException(LocalizationManager.strings.errorIdMustBeZero)
        }
        val sanitized = validateAndPrepare(exercise)
        
        // Strictly check for duplicates for new items
        val existing = getExerciseByName(sanitized.name)
        if (existing != null) {
            throw IllegalArgumentException(LocalizationManager.strings.errorExerciseAlreadyExists(sanitized.name))
        }
        
        insertInternal(sanitized)
    }

    // --- READ ---

    @Query("SELECT * FROM exercise ORDER BY name ASC")
    abstract suspend fun getAllExercises(): List<Exercise>

    @Query("SELECT * FROM exercise ORDER BY name ASC")
    abstract fun getAllExercisesFlow(): kotlinx.coroutines.flow.Flow<List<Exercise>>

    @Query("SELECT * FROM exercise WHERE id = :id")
    abstract suspend fun getExerciseById(id: Int): Exercise?

    // --- UPDATE ---

    @Transaction
    open suspend fun update(exercise: Exercise) {
        if (exercise.id == 0) {
            throw IllegalArgumentException(LocalizationManager.strings.errorIdMustNotBeZero)
        }
        val sanitized = validateAndPrepare(exercise)

        // Check for duplicates (allowing same ID)
        val existing = getExerciseByName(sanitized.name)
        if (existing != null && existing.id != sanitized.id) {
            throw IllegalArgumentException(LocalizationManager.strings.errorExerciseRenameConflict(sanitized.name))
        }

        updateInternal(sanitized)
    }

    // --- DELETE ---

    /**
     * Permanently deletes an exercise and all its associated records (CASCADE).
     */
    open suspend fun delete(exerciseId: Int) {
        deleteExerciseInternal(exerciseId)
    }

    @Query("DELETE FROM exercise")
    abstract suspend fun deleteAllExercises()
}
