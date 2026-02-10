package com.nnoidea.fitnez2.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nnoidea.fitnez2.data.dao.ExerciseDao
import com.nnoidea.fitnez2.data.dao.RecordDao
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record

@Database(
    entities = [Exercise::class, Record::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context, scope: kotlinx.coroutines.CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitnez2_database"
                )
                .addCallback(DatabaseSeeder({ INSTANCE!! }, scope))
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        // 1. Add Column
        db.execSQL("ALTER TABLE record ADD COLUMN groupIndex INTEGER NOT NULL DEFAULT 0")

        // 2. Calculate Groups
        val cursor = db.query("SELECT id, exerciseId FROM record ORDER BY date ASC, id ASC")
        
        var currentGroupIndex = 0
        var lastExerciseId = -1
        
        try {
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex("id")
                val exerciseIdIndex = cursor.getColumnIndex("exerciseId")
                
                do {
                    val id = cursor.getInt(idIndex)
                    val exerciseId = cursor.getInt(exerciseIdIndex)
                    
                    if (lastExerciseId != -1) {
                         if (exerciseId != lastExerciseId) {
                             currentGroupIndex++
                         }
                    }
                    lastExerciseId = exerciseId
                    
                    // Update
                    db.execSQL("UPDATE record SET groupIndex = $currentGroupIndex WHERE id = $id")
                    
                } while (cursor.moveToNext())
            }
        } finally {
            cursor.close()
        }
    }
}

val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_record_exerciseId_date_id` ON `record` (`exerciseId`, `date`, `id`)")
        db.execSQL("DROP INDEX IF EXISTS `index_record_exerciseId`")
    }
}

val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_record_groupIndex` ON `record` (`groupIndex`)")
    }
}
