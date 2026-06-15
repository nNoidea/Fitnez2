package com.nnoidea.fitnez2.service

import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.data.entities.Workout
import com.nnoidea.fitnez2.data.entities.WorkoutRecord
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.verification.WorkoutVerifier
import kotlinx.coroutines.flow.Flow

class WorkoutService(private val database: AppDatabase) {

    private val dao get() = database.workoutDao()

    suspend fun createWorkout(name: String): Workout {
        val trimmedName = WorkoutVerifier.validateName(name)
        val existing = dao.getWorkoutByName(trimmedName)
        if (existing != null) {
            throw IllegalArgumentException(globalLocalization.errorWorkoutAlreadyExists(trimmedName))
        }
        val workout = Workout(name = trimmedName)
        dao.insertWorkout(workout)
        return workout
    }

    suspend fun updateWorkout(workout: Workout) {
        WorkoutVerifier.validateName(workout.name)
        dao.updateWorkout(workout)
    }

    suspend fun deleteWorkout(workout: Workout) {
        dao.deleteWorkout(workout)
    }

    suspend fun getWorkoutById(workoutId: String): Workout? = dao.getWorkoutById(workoutId)

    fun getAllWorkoutsFlow(): Flow<List<Workout>> = dao.getAllWorkoutsFlow()

    suspend fun getAllWorkouts(): List<Workout> = dao.getAllWorkouts()

    suspend fun getWorkoutByName(name: String): Workout? = dao.getWorkoutByName(name)

    suspend fun addRecordToWorkout(
        workoutId: String,
        exerciseId: String,
        sets: Int,
        reps: Int,
        weight: Double
    ): WorkoutRecord {
        WorkoutVerifier.validateWorkoutRecord(sets, reps, weight)
        val workoutRecord = WorkoutRecord(
            workoutId = workoutId,
            exerciseId = exerciseId,
            sets = sets,
            reps = reps,
            weight = weight
        )
        dao.insertWorkoutRecord(workoutRecord)
        return workoutRecord
    }

    suspend fun updateWorkoutRecord(record: WorkoutRecord) {
        WorkoutVerifier.validateWorkoutRecord(record.sets, record.reps, record.weight)
        dao.updateWorkoutRecord(record)
    }

    suspend fun deleteWorkoutRecord(record: WorkoutRecord) {
        dao.deleteWorkoutRecord(record)
    }

    suspend fun deleteRecordsByWorkoutId(workoutId: String) {
        dao.deleteRecordsByWorkoutId(workoutId)
    }

    fun getRecordsForWorkoutFlow(workoutId: String): Flow<List<WorkoutRecordWithExercise>> =
        dao.getRecordsForWorkoutFlow(workoutId)

    suspend fun getRecordsForWorkout(workoutId: String): List<WorkoutRecordWithExercise> =
        dao.getRecordsForWorkout(workoutId)

    suspend fun getAllWorkoutRecords(): List<WorkoutRecord> = dao.getAllWorkoutRecords()

    suspend fun deleteAllWorkouts() {
        dao.deleteAllWorkouts()
    }
}
