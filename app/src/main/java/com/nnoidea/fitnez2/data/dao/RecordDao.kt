package com.nnoidea.fitnez2.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: Record)

    @Update
    suspend fun updateRecord(record: Record)

    @Delete
    suspend fun deleteRecord(record: Record)

    @Query("SELECT * FROM record WHERE id = :recordId")
    suspend fun getRecordById(recordId: String): Record?

    @Query("SELECT * FROM record ORDER BY date DESC, orderNumber DESC, id DESC LIMIT :limit")
    suspend fun getLatestRecords(limit: Int = 100): List<Record>

    @Query("SELECT * FROM record WHERE exerciseId = :exerciseId ORDER BY date DESC, orderNumber DESC, id DESC LIMIT :limit")
    suspend fun getRecordsByExerciseId(exerciseId: String, limit: Int = 100): List<Record>

    @Query("SELECT * FROM record WHERE exerciseId = :exerciseId ORDER BY date ASC LIMIT 2000")
    fun getRecordsByExerciseIdFlow(exerciseId: String): Flow<List<Record>>

    @Query("SELECT * FROM record WHERE exerciseId IN (:exerciseIds) ORDER BY date DESC, orderNumber DESC, id DESC LIMIT :limit")
    suspend fun getRecordsByExerciseIds(exerciseIds: List<String>, limit: Int = 100): List<Record>

    @Query("""
        SELECT record.*, exercise.name as exerciseName 
        FROM record 
        JOIN exercise ON record.exerciseId = exercise.id 
        ORDER BY record.date DESC, record.orderNumber DESC, record.id DESC
        LIMIT 1
    """)
    suspend fun getLatestRecord(): RecordWithExercise?

    @Query("""
        SELECT record.*, exercise.name as exerciseName 
        FROM record 
        JOIN exercise ON record.exerciseId = exercise.id 
        WHERE record.exerciseId = :exerciseId 
        ORDER BY record.date DESC, record.orderNumber DESC, record.id DESC
        LIMIT 1
    """)
    suspend fun getLatestRecordByExerciseId(exerciseId: String): RecordWithExercise?

    @Query("SELECT * FROM record ORDER BY date DESC, orderNumber DESC, id DESC LIMIT :limit OFFSET :offset")
    suspend fun getOlderRecords(offset: Int, limit: Int = 50): List<Record>

    @Query("SELECT * FROM record WHERE (date < :date) OR (date = :date AND orderNumber < :orderNumber) OR (date = :date AND orderNumber = :orderNumber AND id < :id) ORDER BY date DESC, orderNumber DESC, id DESC LIMIT :limit")
    suspend fun getOlderRecordsAfter(date: Long, orderNumber: Int, id: String, limit: Int = 50): List<Record>

    @Query("SELECT * FROM record WHERE (date > :date) OR (date = :date AND orderNumber > :orderNumber) OR (date = :date AND orderNumber = :orderNumber AND id > :id) ORDER BY date ASC, orderNumber ASC, id ASC LIMIT :limit")
    suspend fun getNewerRecordsBefore(date: Long, orderNumber: Int, id: String, limit: Int = 50): List<Record>

    @Query("SELECT * FROM record WHERE exerciseId = :exerciseId ORDER BY date DESC, orderNumber DESC, id DESC LIMIT :limit OFFSET :offset")
    suspend fun getOlderRecordsByExerciseId(exerciseId: String, offset: Int, limit: Int = 50): List<Record>

    @Query("SELECT * FROM record WHERE exerciseId = :exerciseId AND ((date < :date) OR (date = :date AND orderNumber < :orderNumber) OR (date = :date AND orderNumber = :orderNumber AND id < :id)) ORDER BY date DESC, orderNumber DESC, id DESC LIMIT :limit")
    suspend fun getOlderRecordsByExerciseIdAfter(exerciseId: String, date: Long, orderNumber: Int, id: String, limit: Int = 50): List<Record>

    @Query("SELECT * FROM record WHERE exerciseId IN (:exerciseIds) ORDER BY date DESC, orderNumber DESC, id DESC LIMIT :limit OFFSET :offset")
    suspend fun getOlderRecordsByExerciseIds(exerciseIds: List<String>, offset: Int, limit: Int = 50): List<Record>

    @Query("SELECT COUNT(*) FROM record")
    suspend fun getTotalRecordCount(): Int

    @Query("SELECT COUNT(*) FROM record WHERE exerciseId = :exerciseId")
    suspend fun getRecordCountByExerciseId(exerciseId: String): Int

    @Query("SELECT COUNT(*) FROM record WHERE exerciseId IN (:exerciseIds)")
    suspend fun getRecordCountByExerciseIds(exerciseIds: List<String>): Int

    @Query("SELECT * FROM record WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, orderNumber DESC, id DESC LIMIT 50000")
    fun getRecordsByDateRangeFlow(startDate: Long, endDate: Long): Flow<List<Record>>

    @Query("SELECT COUNT(*) FROM record")
    fun getRecordCountFlow(): Flow<Int>

    @Query("SELECT * FROM record ORDER BY date ASC, orderNumber ASC, id ASC")
    suspend fun getAllRecordsOrdered(): List<Record>

    @Query("SELECT COALESCE(MAX(orderNumber), -1) FROM record WHERE exerciseId = :exerciseId AND date = :date")
    suspend fun getMaxOrderNumberForDate(exerciseId: String, date: Long): Int

    @Query("SELECT * FROM record WHERE date >= :fromDate AND date <= :toDate ORDER BY date DESC, orderNumber DESC, id DESC LIMIT 500")
    suspend fun getRecordsAroundDate(fromDate: Long, toDate: Long): List<Record>

    @Query("DELETE FROM record")
    suspend fun deleteAllRecords()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<Record>)
}
