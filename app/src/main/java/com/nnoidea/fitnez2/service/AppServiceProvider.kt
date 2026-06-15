package com.nnoidea.fitnez2.service

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.nnoidea.fitnez2.data.AppDatabase

val LocalExerciseService = staticCompositionLocalOf<ExerciseService> {
    error("No ExerciseService provided")
}

val LocalRecordService = staticCompositionLocalOf<RecordService> {
    error("No RecordService provided")
}

val LocalWorkoutService = staticCompositionLocalOf<WorkoutService> {
    error("No WorkoutService provided")
}

val LocalSettingsService = staticCompositionLocalOf<SettingsService> {
    error("No SettingsService provided")
}

val LocalBackupService = staticCompositionLocalOf<BackupService> {
    error("No BackupService provided")
}

@Composable
fun ProvideAppServices(
    context: Context,
    database: AppDatabase,
    content: @Composable () -> Unit
) {
    val exerciseService = remember { ExerciseService(database) }
    val recordService = remember { RecordService(database) }
    val workoutService = remember { WorkoutService(database) }
    val settingsService = remember { SettingsService(context) }
    val backupService = remember { BackupService(context, database) }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalExerciseService provides exerciseService,
        LocalRecordService provides recordService,
        LocalWorkoutService provides workoutService,
        LocalSettingsService provides settingsService,
        LocalBackupService provides backupService
    ) {
        content()
    }
}
