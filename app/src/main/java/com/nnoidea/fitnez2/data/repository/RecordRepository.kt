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

    suspend fun getHistoryForExercise(exerciseId: Int): List<RecordWithExercise> {
        return recordDao.getSortedOne(exerciseId)
    }

    fun getAllHistory(): Flow<List<RecordWithExercise>> {
        return recordDao.getSortedAll()
    }
}
