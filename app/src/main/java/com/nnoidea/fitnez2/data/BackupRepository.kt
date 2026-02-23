package com.nnoidea.fitnez2.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.BackupData
import com.nnoidea.fitnez2.data.models.ExportedExercise
import com.nnoidea.fitnez2.data.models.ExportedRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import androidx.room.withTransaction

class BackupRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val gson = Gson()

    suspend fun exportDatabase(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val exercises = database.exerciseDao().getAllExercises()
            val records = database.recordDao().getAllRecordsOrdered()

            val recordsByExercise = records.groupBy { it.exerciseId }

            // Group records by exercise, preserving IDs for ordering
            val exportedData = exercises.map { exercise ->
                val exerciseRecords = (recordsByExercise[exercise.id] ?: emptyList())
                    .map { ExportedRecord(it.id, it.sets, it.reps, it.weight, it.date) }
                ExportedExercise(exercise.id, exercise.name, exerciseRecords)
            }

            val backupData = BackupData(data = exportedData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    gson.toJson(backupData, writer)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BackupRepository", "Export failed", e)
            Result.failure(e)
        }
    }

    suspend fun importDatabase(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            var backupData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    gson.fromJson(reader, BackupData::class.java)
                }
            }

            // FALLBACK: If the file was exported while R8 was obfuscating (keys "a", "b", etc.)
            // we try to parse it into a Map and convert it.
            if (backupData == null || backupData.data == null) {
                Log.d("BackupRepository", "Attempting legacy/minified import fallback...")
                try {
                    val rawMap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            gson.fromJson(reader, Map::class.java)
                        }
                    } as? Map<String, Any>
                    
                    val minifiedData = rawMap?.get("a") as? List<Map<String, Any>>
                    if (minifiedData != null) {
                        val convertedExercises = minifiedData.map { exMap ->
                            val records = (exMap["b"] as? List<Map<String, Any>>)?.map { rMap ->
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
                    Log.e("BackupRepository", "Fallback failed", e)
                }
            }

            if (backupData == null || backupData.data == null) {
                throw Exception("Invalid backup file: data is missing")
            }

            Log.d("BackupRepository", "Importing ${backupData.data.size} exercises...")

            database.withTransaction {
                // 1. Wipe everything for a clean start
                // Delete records first, then exercises (though CASCADE would handle it)
                database.recordDao().deleteAllRecords()
                database.exerciseDao().deleteAllExercises()

                // 2. Recreate with original IDs to preserve ordering
                backupData.data.forEach { exported ->
                    val exerciseId = if (exported.id != 0) exported.id else 0
                    val exercise = Exercise(id = exerciseId, name = exported.name)
                    val newExerciseId = database.exerciseDao().insertInternal(exercise).toInt()

                    exported.records?.forEach { r ->
                        val record = Record(
                            id = if (r.id != 0) r.id else 0,
                            exerciseId = newExerciseId,
                            sets = r.sets,
                            reps = r.reps,
                            weight = r.weight,
                            date = r.date
                        )
                        database.recordDao().insertInternal(record)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BackupRepository", "Import failed", e)
            Result.failure(e)
        }
    }
}
