package com.nnoidea.fitnez2.service

import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.verification.RecordVerifier
import kotlinx.coroutines.flow.Flow

class RecordService(private val database: AppDatabase) {

    private val dao get() = database.recordDao()

    suspend fun getRecordById(recordId: String): Record? = dao.getRecordById(recordId)

    suspend fun getLatestRecords(limit: Int = 100): List<Record> = dao.getLatestRecords(limit)

    suspend fun getRecordsByExerciseId(exerciseId: String, limit: Int = 100): List<Record> =
        dao.getRecordsByExerciseId(exerciseId, limit)

    fun getRecordsByExerciseIdFlow(exerciseId: String): Flow<List<Record>> =
        dao.getRecordsByExerciseIdFlow(exerciseId)

    suspend fun getRecordsByExerciseIds(exerciseIds: List<String>, limit: Int = 100): List<Record> =
        dao.getRecordsByExerciseIds(exerciseIds, limit)

    suspend fun getLatestRecord(): RecordWithExercise? = dao.getLatestRecord()

    suspend fun getLatestRecordByExerciseId(exerciseId: String): RecordWithExercise? =
        dao.getLatestRecordByExerciseId(exerciseId)

    suspend fun getOlderRecords(offset: Int, limit: Int = 50): List<Record> =
        dao.getOlderRecords(offset, limit)

    suspend fun getOlderRecordsAfter(date: Long, orderNumber: Int, id: String, limit: Int = 50): List<Record> =
        dao.getOlderRecordsAfter(date, orderNumber, id, limit)

    suspend fun getNewerRecordsBefore(date: Long, orderNumber: Int, id: String, limit: Int = 50): List<Record> =
        dao.getNewerRecordsBefore(date, orderNumber, id, limit)

    suspend fun getOlderRecordsByExerciseId(exerciseId: String, offset: Int, limit: Int = 50): List<Record> =
        dao.getOlderRecordsByExerciseId(exerciseId, offset, limit)

    suspend fun getOlderRecordsByExerciseIdAfter(exerciseId: String, date: Long, orderNumber: Int, id: String, limit: Int = 50): List<Record> =
        dao.getOlderRecordsByExerciseIdAfter(exerciseId, date, orderNumber, id, limit)

    suspend fun getOlderRecordsByExerciseIds(exerciseIds: List<String>, offset: Int, limit: Int = 50): List<Record> =
        dao.getOlderRecordsByExerciseIds(exerciseIds, offset, limit)

    suspend fun getTotalRecordCount(): Int = dao.getTotalRecordCount()

    suspend fun getRecordCountByExerciseId(exerciseId: String): Int =
        dao.getRecordCountByExerciseId(exerciseId)

    suspend fun getRecordCountByExerciseIds(exerciseIds: List<String>): Int =
        dao.getRecordCountByExerciseIds(exerciseIds)

    fun getRecordsByDateRangeFlow(startDate: Long, endDate: Long): Flow<List<Record>> =
        dao.getRecordsByDateRangeFlow(startDate, endDate)

    suspend fun getRecordsAroundDate(fromDate: Long, toDate: Long): List<Record> =
        dao.getRecordsAroundDate(fromDate, toDate)

    fun getRecordCountFlow(): Flow<Int> = dao.getRecordCountFlow()

    suspend fun createRecord(exerciseId: String, sets: Int, reps: Int, weight: Double, date: Long): Record {
        RecordVerifier.validateRecord(sets, reps, weight)
        val maxOrderNumber = dao.getMaxOrderNumberForDate(exerciseId, date)
        val record = Record(
            exerciseId = exerciseId,
            sets = sets,
            reps = reps,
            weight = weight,
            date = date,
            orderNumber = maxOrderNumber + 1
        )
        dao.insertRecord(record)
        return record
    }

    suspend fun updateRecord(id: String, sets: Int, reps: Int, weight: Double): Record {
        RecordVerifier.validateRecord(sets, reps, weight)
        val existing = dao.getRecordById(id)
            ?: throw IllegalArgumentException(globalLocalization.errorRecordNotFoundById(id))
        val record = existing.copy(sets = sets, reps = reps, weight = weight)
        dao.updateRecord(record)
        return record
    }

    suspend fun deleteRecord(id: String) {
        val record = dao.getRecordById(id)
            ?: throw IllegalArgumentException(globalLocalization.errorRecordNotFoundById(id))
        dao.deleteRecord(record)
    }
}
