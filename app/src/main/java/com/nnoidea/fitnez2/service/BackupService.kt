package com.nnoidea.fitnez2.service

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.entities.Workout
import com.nnoidea.fitnez2.data.entities.WorkoutRecord
import com.nnoidea.fitnez2.data.models.BackupData
import com.nnoidea.fitnez2.data.models.ExportedExercise
import com.nnoidea.fitnez2.data.models.ExportedRecord
import com.nnoidea.fitnez2.data.models.ExportedWorkout
import com.nnoidea.fitnez2.data.models.ExportedWorkoutRecord
import com.nnoidea.fitnez2.verification.ExerciseVerifier
import com.nnoidea.fitnez2.verification.RecordVerifier
import com.nnoidea.fitnez2.verification.WorkoutVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupService(
    private val context: Context,
    private val database: AppDatabase
) {
    private val gson = Gson()

    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val exercises = database.exerciseDao().getAllExercises()
            // TODO: Paginate for large datasets — loads entire table into memory
            val records = database.recordDao().getAllRecordsOrdered()
            val workouts = database.workoutDao().getAllWorkouts()
            val workoutRecords = database.workoutDao().getAllWorkoutRecords()

            val recordsByExercise = records.groupBy { it.exerciseId }
            val workoutRecordsByWorkout = workoutRecords.groupBy { it.workoutId }

            val exportedData = exercises.map { exercise ->
                val exerciseRecords = (recordsByExercise[exercise.id] ?: emptyList())
                    .map { ExportedRecord(it.id, it.sets, it.reps, it.weight, it.date) }
                ExportedExercise(exercise.id, exercise.name, exerciseRecords)
            }

            val exportedWorkouts = workouts.map { workout ->
                val wr = (workoutRecordsByWorkout[workout.id] ?: emptyList()).map {
                    ExportedWorkoutRecord(
                        id = it.id,
                        exerciseId = it.exerciseId,
                        sets = it.sets,
                        reps = it.reps,
                        weight = it.weight,
                        date = it.date
                    )
                }
                ExportedWorkout(
            id = workout.id,
                    name = workout.name,
                    records = wr
                )
            }

            val backupData = BackupData(data = exportedData, workouts = exportedWorkouts)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    gson.toJson(backupData, writer)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BackupService", "Export failed", e)
            Result.failure(e)
        }
    }

    suspend fun importData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            var rawJsonString = ""
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    rawJsonString = reader.readText()
                }
            }

            var backupData: BackupData? = gson.fromJson(rawJsonString, BackupData::class.java)

            if (backupData == null || backupData.data.isEmpty()) {
                Log.d("BackupService", "Attempting legacy/minified import fallback...")
                try {
                    val mapType = object : TypeToken<Map<String, Any>>() {}.type
                    val rawMap: Map<String, Any>? = gson.fromJson(rawJsonString, mapType)
                    val minifiedData = (rawMap?.get("a") as? List<*>)
                        ?.mapNotNull { it as? Map<*, *> }
                    if (!minifiedData.isNullOrEmpty()) {
                        val convertedExercises = minifiedData.map { exMap ->
                            val records = (exMap["b"] as? List<*>)
                                ?.mapNotNull { it as? Map<*, *> }
                                ?.map { rMap ->
                                    ExportedRecord(
                                        sets = (rMap["a"] as? Double)?.toInt() ?: 0,
                                        reps = (rMap["b"] as? Double)?.toInt() ?: 0,
                                        weight = (rMap["c"] as? Double) ?: 0.0,
                                        date = (rMap["d"] as? Double)?.toLong() ?: 0L
                                    )
                                } ?: emptyList()
                            ExportedExercise(name = exMap["a"] as? String ?: "Unknown", records = records)
                        }
                        backupData = BackupData(version = 1, data = convertedExercises)
                    }
                } catch (e: Exception) {
                    Log.e("BackupService", "Fallback failed", e)
                }
            }

            if (backupData == null || backupData.data.isEmpty()) {
                throw Exception("Invalid backup file: data is missing")
            }

            Log.d("BackupService", "Importing ${backupData.data.size} exercises...")

            database.withTransaction {
                database.recordDao().deleteAllRecords()
                database.exerciseDao().deleteAllExercises()
                database.workoutDao().deleteAllWorkouts()

                val exerciseIdMap = mutableMapOf<String, String>()

                backupData.data.forEach { exported ->
                    ExerciseVerifier.validateName(exported.name)
                    val exerciseId = if (exported.id.isNotEmpty()) exported.id else java.util.UUID.randomUUID().toString()
                    val exercise = Exercise( id = exerciseId, name = exported.name.trim())
                    database.exerciseDao().insertExercise(exercise)
                    exerciseIdMap[exported.id] = exerciseId

                    val recordBatch = mutableListOf<Record>()
                    exported.records?.forEach { r ->
                        RecordVerifier.validateRecord(r.sets, r.reps, r.weight)
                        recordBatch.add(Record(
            id = if (r.id.isNotEmpty()) r.id else java.util.UUID.randomUUID().toString(),
                            exerciseId = exerciseIdMap[exported.id] ?: java.util.UUID.randomUUID().toString(),
                            sets = r.sets,
                            reps = r.reps,
                            weight = r.weight,
                            date = r.date
                        ))
                    }
                    if (recordBatch.isNotEmpty()) {
                        database.recordDao().insertAll(recordBatch)
                    }
                }

                backupData.workouts?.forEach { exportedWorkout ->
                    WorkoutVerifier.validateName(exportedWorkout.name)
                    val workoutId = if (exportedWorkout.id.isNotEmpty()) exportedWorkout.id else java.util.UUID.randomUUID().toString()
                    val workout = Workout( id = workoutId, name = exportedWorkout.name.trim())
                    database.workoutDao().insertWorkout(workout)

                    val workoutRecordBatch = mutableListOf<WorkoutRecord>()
                    exportedWorkout.records?.forEach { r ->
                        val resolvedExerciseId = exerciseIdMap[r.exerciseId] ?: r.exerciseId
                        workoutRecordBatch.add(WorkoutRecord(
            id = if (r.id.isNotEmpty()) r.id else java.util.UUID.randomUUID().toString(),
                            workoutId = workoutId,
                            exerciseId = resolvedExerciseId,
                            sets = r.sets,
                            reps = r.reps,
                            weight = r.weight,
                            date = r.date
                        ))
                    }
                    if (workoutRecordBatch.isNotEmpty()) {
                        database.workoutDao().insertAllWorkoutRecords(workoutRecordBatch)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BackupService", "Import failed", e)
            Result.failure(e)
        }
    }
}
