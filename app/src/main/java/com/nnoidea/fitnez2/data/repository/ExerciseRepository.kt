package com.nnoidea.fitnez2.data.repositories

import com.nnoidea.fitnez2.data.dao.ExerciseDao
import com.nnoidea.fitnez2.data.entities.Exercise

import kotlinx.coroutines.flow.Flow

class ExerciseRepository(private val exerciseDao: ExerciseDao) {

    suspend fun getAllExercises(): List<Exercise> {
        return exerciseDao.getAllExercises()
    }

    fun getAllExercisesFlow(): Flow<List<Exercise>> {
        return exerciseDao.getAllExercisesFlow()
    }

    suspend fun getExerciseById(id: Int): Exercise? {
        return exerciseDao.getExerciseById(id)
    }

    suspend fun createExercise(exercise: Exercise) {
        exerciseDao.create(exercise)
    }

    suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.update(exercise)
    }

    suspend fun deleteExercise(exerciseId: Int) {
        exerciseDao.delete(exerciseId)
    }
}
