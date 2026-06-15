package com.nnoidea.fitnez2.service

import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.verification.ExerciseVerifier
import kotlinx.coroutines.flow.Flow

class ExerciseService(private val database: AppDatabase) {

    private val dao get() = database.exerciseDao()

    suspend fun getAllExercises(): List<Exercise> = dao.getAllExercises()

    fun getAllExercisesFlow(): Flow<List<Exercise>> = dao.getAllExercisesFlow()

    suspend fun getExerciseById(id: String): Exercise? = dao.getExerciseById(id)

    suspend fun getExerciseByName(name: String): Exercise? = dao.getExerciseByName(name)

    suspend fun createExercise(name: String): Exercise {
        val trimmedName = ExerciseVerifier.validateName(name)
        val existing = dao.getExerciseByName(trimmedName)
        if (existing != null) {
            throw IllegalArgumentException(globalLocalization.errorExerciseAlreadyExists(trimmedName))
        }
        val exercise = Exercise(name = trimmedName)
        dao.insertExercise(exercise)
        return exercise
    }

    suspend fun updateExercise(id: String, name: String): Exercise {
        val trimmedName = ExerciseVerifier.validateName(name)
        val existing = dao.getExerciseByName(trimmedName)
        if (existing != null && existing.id != id) {
            throw IllegalArgumentException(globalLocalization.errorExerciseRenameConflict(trimmedName))
        }
        val exercise = Exercise(id = id, name = trimmedName)
        dao.updateExercise(exercise)
        return exercise
    }

    suspend fun deleteExercise(id: String) {
        val exercise = dao.getExerciseById(id)
            ?: throw IllegalArgumentException(globalLocalization.errorExerciseNotFoundById(id))
        dao.deleteExercise(exercise)
    }
}
