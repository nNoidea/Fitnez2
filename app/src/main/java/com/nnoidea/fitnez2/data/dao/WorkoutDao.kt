package com.nnoidea.fitnez2.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nnoidea.fitnez2.data.entities.Workout
import com.nnoidea.fitnez2.data.entities.WorkoutRecord
import com.nnoidea.fitnez2.data.models.WorkoutRecordWithExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // --- Workout Queries ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Query("SELECT * FROM workout WHERE id = :workoutId LIMIT 1")
    suspend fun getWorkoutById(workoutId: String): Workout?

    @Query("SELECT * FROM workout ORDER BY id ASC")
    fun getAllWorkoutsFlow(): Flow<List<Workout>>

    // --- WorkoutRecord Queries ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutRecord(record: WorkoutRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWorkoutRecords(records: List<WorkoutRecord>)

    @Update
    suspend fun updateWorkoutRecord(record: WorkoutRecord)

    @Delete
    suspend fun deleteWorkoutRecord(record: WorkoutRecord)

    @Query("DELETE FROM workout_record WHERE workoutId = :workoutId")
    suspend fun deleteRecordsByWorkoutId(workoutId: String)

    @Query("""
        SELECT wr.*, e.name as exerciseName 
        FROM workout_record wr 
        INNER JOIN exercise e ON wr.exerciseId = e.id 
        WHERE wr.workoutId = :workoutId 
        ORDER BY wr.id ASC
        LIMIT 500
    """)
    fun getRecordsForWorkoutFlow(workoutId: String): Flow<List<WorkoutRecordWithExercise>>

    @Query("""
        SELECT wr.*, e.name as exerciseName 
        FROM workout_record wr 
        INNER JOIN exercise e ON wr.exerciseId = e.id 
        WHERE wr.workoutId = :workoutId 
        ORDER BY wr.id ASC
        LIMIT 500
    """)
    suspend fun getRecordsForWorkout(workoutId: String): List<WorkoutRecordWithExercise>

    @Query("SELECT * FROM workout ORDER BY id ASC")
    suspend fun getAllWorkouts(): List<Workout>

    @Query("SELECT * FROM workout WHERE name = :name LIMIT 1")
    suspend fun getWorkoutByName(name: String): Workout?

    @Query("SELECT * FROM workout_record ORDER BY id ASC")
    suspend fun getAllWorkoutRecords(): List<WorkoutRecord>

    @Query("DELETE FROM workout")
    suspend fun deleteAllWorkouts()
}
