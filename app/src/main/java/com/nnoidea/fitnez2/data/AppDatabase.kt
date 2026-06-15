package com.nnoidea.fitnez2.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nnoidea.fitnez2.data.dao.ExerciseDao
import com.nnoidea.fitnez2.data.dao.RecordDao
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.data.entities.Workout
import com.nnoidea.fitnez2.data.entities.WorkoutRecord
import com.nnoidea.fitnez2.data.dao.WorkoutDao

@Database(
    entities = [Exercise::class, Record::class, Workout::class, WorkoutRecord::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun recordDao(): RecordDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context, scope: kotlinx.coroutines.CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val seeder = DatabaseSeeder(scope)
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitnez2_database"
                )
                .addCallback(seeder)
                .fallbackToDestructiveMigration(true)
                .build()
                seeder.database = instance
                INSTANCE = instance
                instance
            }
        }
    }
}
