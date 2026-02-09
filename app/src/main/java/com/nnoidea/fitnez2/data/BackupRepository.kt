package com.nnoidea.fitnez2.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.nnoidea.fitnez2.data.models.BackupData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import androidx.room.RoomDatabase
import androidx.room.withTransaction

class BackupRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val gson = Gson()

    suspend fun exportDatabase(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val exercises = database.exerciseDao().getAllExercises()
            val records = database.recordDao().getAllRecords()

            val backupData = BackupData(
                exercises = exercises,
                records = records
            )

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    gson.toJson(backupData, writer)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importDatabase(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    gson.fromJson(reader, BackupData::class.java)
                }
            } ?: throw Exception("Could not open file")

            database.withTransaction {
                // Clear existing data
                database.recordDao().deleteAllRecords()
                database.exerciseDao().deleteAllExercises()

                // Insert new data
                // We need to insert exercises first due to foreign key constraints
                database.exerciseDao().insertAll(backupData.exercises)
                database.recordDao().insertAll(backupData.records)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
