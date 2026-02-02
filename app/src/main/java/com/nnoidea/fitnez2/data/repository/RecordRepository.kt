package com.nnoidea.fitnez2.data.repositories

import com.nnoidea.fitnez2.data.dao.RecordDao
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import kotlinx.coroutines.flow.Flow

class RecordRepository(private val recordDao: RecordDao) {

    suspend fun createRecord(record: Record) {
        recordDao.create(record)
    }

    suspend fun updateRecord(record: Record) {
        recordDao.update(record)
    }

    suspend fun deleteRecord(recordId: Int) {
        recordDao.delete(recordId)
    }

    fun getHistoryForExercise(exerciseId: Int): Flow<List<RecordWithExercise>> {
        return recordDao.getRecordsByExerciseId(exerciseId)
    }

    fun getAllHistory(): Flow<List<RecordWithExercise>> {
        return recordDao.getAllRecordsFlow()
    }
}
