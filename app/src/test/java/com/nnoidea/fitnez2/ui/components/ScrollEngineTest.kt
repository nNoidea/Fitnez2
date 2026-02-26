package com.nnoidea.fitnez2.ui.components

import com.nnoidea.fitnez2.data.dao.RecordDao
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeRecordDao : RecordDao() {
    val records = mutableListOf<Record>()

    override suspend fun insertInternal(record: Record): Long {
        val id = if (record.id == 0) (records.size + 1) else record.id
        val newRecord = record.copy(id = id)
        records.add(newRecord)
        return id.toLong()
    }

    override suspend fun updateInternal(record: Record) {
        val index = records.indexOfFirst { it.id == record.id }
        if (index != -1) records[index] = record
    }

    override suspend fun deleteInternal(id: Int) {
        records.removeIf { it.id == id }
    }

    override suspend fun getRecordById(recordId: Int): Record? = records.find { it.id == recordId }

    override suspend fun getLatestRecords(limit: Int): List<Record> = 
        records.sortedByDescending { it.date }.take(limit)

    override suspend fun getRecordsByExerciseId(exerciseId: Int, limit: Int): List<Record> =
        records.filter { it.exerciseId == exerciseId }.sortedByDescending { it.date }.take(limit)

    override suspend fun getRecordsByExerciseIds(exerciseIds: List<Int>, limit: Int): List<Record> =
        records.filter { it.exerciseId in exerciseIds }.sortedByDescending { it.date }.take(limit)

    override suspend fun getLatestRecord(): RecordWithExercise? = null // Not used in ScrollEngine tests
    override suspend fun getLatestRecordByExerciseId(exerciseId: Int): RecordWithExercise? = null

    override suspend fun getOlderRecords(offset: Int, limit: Int): List<Record> =
        records.sortedByDescending { it.date }.drop(offset).take(limit)

    override suspend fun getOlderRecordsByExerciseId(exerciseId: Int, offset: Int, limit: Int): List<Record> =
        records.filter { it.exerciseId == exerciseId }.sortedByDescending { it.date }.drop(offset).take(limit)

    override suspend fun getOlderRecordsByExerciseIds(exerciseIds: List<Int>, offset: Int, limit: Int): List<Record> =
        records.filter { it.exerciseId in exerciseIds }.sortedByDescending { it.date }.drop(offset).take(limit)

    override suspend fun getTotalRecordCount(): Int = records.size
    override suspend fun getRecordCountByExerciseId(exerciseId: Int): Int = 
        records.count { it.exerciseId == exerciseId }
    override suspend fun getRecordCountByExerciseIds(exerciseIds: List<Int>): Int =
        records.count { it.exerciseId in exerciseIds }

    override suspend fun getAllRecords(): List<Record> = records
    override suspend fun getAllRecordsOrdered(): List<Record> = records.sortedBy { it.date }
    override suspend fun deleteAllRecords() { records.clear() }
    override suspend fun insertAll(records: List<Record>) { this.records.addAll(records) }
}

class ScrollEngineTest {

    private lateinit var dao: FakeRecordDao
    private lateinit var engine: ScrollEngine

    @Before
    fun setup() {
        dao = FakeRecordDao()
    }

    @Test
    fun `loadInitial loads recent records and sets hasMoreOlderRecords`() = runBlocking {
        // Prepare 150 records
        val now = System.currentTimeMillis()
        for (i in 1..150) {
            dao.insertInternal(Record(exerciseId = 1, sets = 3, reps = 10, weight = 10.0, date = now - i * 1000))
        }

        engine = ScrollEngine(dao, null)
        engine.loadInitial()

        assertEquals(ScrollEngine.RECENT_LIMIT, engine.recentRecords.size)
        assertTrue(engine.hasMoreOlderRecords)
        assertTrue(engine.initialLoadDone)
    }

    @Test
    fun `loadInitial with exercise filter only loads relevant records`() = runBlocking {
        val now = System.currentTimeMillis()
        // Create 120 records for ID 1 and 50 for ID 2
        for (i in 1..120) {
            dao.insertInternal(Record(exerciseId = 1, sets = 3, reps = 10, weight = 10.0, date = now - i * 1000))
        }
        for (i in 1..50) {
            dao.insertInternal(Record(exerciseId = 2, sets = 3, reps = 10, weight = 10.0, date = now - i * 1000 + 500))
        }

        engine = ScrollEngine(dao, listOf(1))
        engine.loadInitial()

        assertEquals(ScrollEngine.RECENT_LIMIT, engine.recentRecords.size)
        assertTrue(engine.recentRecords.all { it.exerciseId == 1 })
        assertTrue(engine.hasMoreOlderRecords)
    }

    @Test
    fun `loadNextBatchIfNeeded appends new batch and increases offset`() = runBlocking {
        val now = System.currentTimeMillis()
        for (i in 1..200) {
            dao.insertInternal(Record(exerciseId = 1, sets = 3, reps = 10, weight = 10.0, date = now - i * 1000))
        }

        engine = ScrollEngine(dao, null)
        engine.loadInitial()

        // total items = recent (100) + potential older
        // loadNextBatchIfNeeded(lastVisible, totalItems)
        engine.loadNextBatchIfNeeded(95, 100) 

        assertEquals(1, engine.olderBatches.size)
        assertEquals(ScrollEngine.OLDER_BATCH_SIZE, engine.olderBatches[0]!!.size)
        assertEquals(ScrollEngine.RECENT_LIMIT + ScrollEngine.OLDER_BATCH_SIZE, engine.totalOlderLoaded + ScrollEngine.RECENT_LIMIT)
    }

    @Test
    fun `evictAndReload evicts distant batches`() = runBlocking {
        val now = System.currentTimeMillis()
        for (i in 1..300) {
            dao.insertInternal(Record(exerciseId = 1, sets = 3, reps = 10, weight = 10.0, date = now - i * 1000))
        }

        engine = ScrollEngine(dao, null)
        engine.loadInitial()

        // Load 4 batches
        engine.loadNextBatchIfNeeded(95, 100) // Batch 0
        engine.loadNextBatchIfNeeded(145, 150) // Batch 1
        engine.loadNextBatchIfNeeded(195, 200) // Batch 2
        engine.loadNextBatchIfNeeded(245, 250) // Batch 3

        assertEquals(4, engine.olderBatches.size)

        // Evict with center at batch 0, radius 2 -> Batch 3 should be evicted
        engine.evictAndReload(0)
        
        // BATCH_WINDOW_RADIUS is 2 in ScrollEngine.kt
        // distance(3, 0) = 3 > 2 -> Evicted
        assertNull(engine.olderBatches[3])
        assertNotNull(engine.olderBatches[0])
        assertNotNull(engine.olderBatches[1])
        assertNotNull(engine.olderBatches[2])
    }

    @Test
    fun `prependNewRecord adds record to the top`() = runBlocking {
        val now = System.currentTimeMillis()
        dao.insertInternal(Record(exerciseId = 1, sets = 3, reps = 10, weight = 10.0, date = now))

        engine = ScrollEngine(dao, null)
        engine.loadInitial()
        
        val initialSize = engine.recentRecords.size
        
        val newRecordId = dao.insertInternal(Record(exerciseId = 1, sets = 4, reps = 12, weight = 15.0, date = now + 1000))
        engine.prependNewRecord(newRecordId.toInt())

        assertEquals(initialSize + 1, engine.recentRecords.size)
        assertEquals(newRecordId.toInt(), engine.recentRecords[0].id)
    }
}
