package com.nnoidea.fitnez2.core.localization

import java.util.Locale

/**
 * Base class for all language implementations.
 * Using a sealed class allows for automatic language registration in LocalizationManager.
 */
sealed class EnStrings(
    open val appLocale: Locale,
    open val languageName: String,
) {
    open val labelSystemLanguage: String = "System Language" // I would've prefered to always display this system language button in the system's language, but we might not support some languages and that migh cause problems.
    open val labelLanguage: String = "Language"

    open val errorExerciseNameBlank: String = "Exercise name cannot be empty or blank"

    open val errorIdMustBeZero: String = "New exercises must have an ID of 0. Use update() for existing exercises."

    open fun errorExerciseAlreadyExists(name: String): String = "Exercise with name '$name' already exists."
    open fun errorExerciseRenameConflict(name: String): String = "Exercise name '$name' is already used by another exercise."
    open fun errorExerciseNotFoundById(id: String): String = "Exercise with ID $id does not exist."
    open fun errorRecordNotFoundById(id: String): String = "Record with ID $id not found."

    open val errorWorkoutNameBlank: String = "Please fill in a name"
    open fun errorWorkoutAlreadyExists(name: String): String = "Workout with name '$name' already exists."
    open val errorWorkoutNoExercises: String = "Please add at least one exercise"
    open val errorWorkoutEmpty: String = "Workout is empty"
    open val labelCreateExercise: String = "Create an exercise"
    open val labelWorkoutName: String = "Workout Name"
    open val labelWorkout: String = "Workout"
    open val labelExercise: String = "Exercise"
    open val labelAdd: String = "Add"
    open val labelExerciseName: String = "Exercise Name"
    open val labelSave: String = "Save"
    open val labelCancel: String = "Cancel"
    open val labelClose: String = "Close"
    open val labelDelete: String = "Delete"
    open val labelAiTranslationsDisclaimer: String = "Translations are done by Artificial Intelligence"

    open val labelTimeline: String = "Timeline"
    open val labelMonthly: String = "Monthly"
    open val labelSettings: String = "Settings"

    open val labelSets: String = "Sets"
    open val labelReps: String = "Reps"

    open fun formatDateShort(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", appLocale)
        return sdf.format(java.util.Date(timestamp))
    }

    open fun formatDayName(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("EEEE", appLocale)
        return sdf.format(java.util.Date(timestamp))
    }


    open fun labelEdit(target: String): String = "Edit $target"

    open val labelSelectExercise: String = "Select Something"
    open val labelWeightUnit: String = "Weight Unit"
    open val labelOpenDrawer: String = "Open Navigation Drawer"
    open val labelHistoryEmpty: String = "No history yet."
    open val labelAppName: String = "Fitnez2"
    open val labelEditExercise: String = "Edit Exercise"

    open val labelRecordDeleted: String = "Record deleted"
    open val labelRecordsDeleted: String = "Records deleted"
    open val labelUndo: String = "Undo"

    open val labelToday: String = "Today"
    open val labelYesterday: String = "Yesterday"
    open val labelDeleteExerciseWarning: String = "This action will delete all records and cannot be undone"
    open val labelDeleteWorkoutWarning: String = "Are you sure you want to delete this workout?"
    
    open val labelExerciseNamePlaceholder: String = "e.g. Bench Press"
    
    open val labelDefaultExerciseValues: String = "Default Exercise Values"

    open val labelDefaultSets: String = "Default Sets"
    open val labelDefaultReps: String = "Default Reps"
    open val labelDefaultWeight: String = "Default Weight"
    
    open val labelBack: String = "Back"
    
    open val labelRotation: String = "Auto-rotate"
    open val labelRotationSystem: String = "Follow System"
    open val labelRotationOn: String = "On"
    open val labelRotationOff: String = "Off"

    open val labelNightMode: String = "Night Mode"
    open val labelNightModeDescription: String = "Early morning workouts before this hour are counted as part of the previous day."
    open fun labelNightModeHour(hour: Int): String = when (hour) {
        0 -> "Off (00:00)"
        12 -> "12:00 (Noon)"
        else -> String.format(java.util.Locale.US, "%02d:00", hour)
    }

    open val labelExportData: String = "Export Data"
    open val labelImportData: String = "Import Data"
    open val labelExportSuccess: String = "Export Successful"
    open val labelExportFailed: String = "Export Failed"
    open val labelImportSuccess: String = "Import Successful"
    open val labelImportFailed: String = "Import Failed"
    
    open val titleImportWarning: String = "Overwrite Data?"
    open val msgImportWarning: String = "This will permanently delete your current database and replace it with the imported data. This action cannot be undone."
    open val labelConfirm: String = "Confirm"
    open val labelDeveloperOptions: String = "Developer Options"
    open val unitKg: String = "kg"
    open val unitLb: String = "lb"
    open val labelUnknownExercise: String = "Unknown Exercise"

    // Developer Options
    open val devColorPalette: String = "Color Palette"
    open val devViewColors: String = "View Colors"
    open val devDatabase: String = "Database"
    open val devRunStressTest: String = "Run Data Stress Test"
    open val devStressTestDescription: String = "Wipe DB & Insert 1M Records"
    open val devStressTestConfirmTitle: String = "Run Stress Test?"
    open val devStressTestConfirmMessage: String = "⚠️ WARNING: This will permanently DELETE ALL existing data (exercises & records) and replace it with ~1 million generated records (2000-2025).\n\nThis process may take a minute."
    open val devWipeAndGenerate: String = "Wipe & Generate"
    open val devGeneratingData: String = "Generating Data..."
    open val labelClearingDatabase: String = "Clearing database..."
    open val labelExportingData: String = "Exporting data..."
    open val devHapticsTest: String = "Haptics Test"
    open val devMoveSlider: String = "Move slider to feel different vibrations"

    // Validation Errors
    open val errorSetsPositive: String = "Sets must be greater than 0"
    open val errorRepsPositive: String = "Reps must be greater than 0"
    open val errorWeightInvalid: String = "Invalid weight value"
    
    open val labelGoToCurrentMonth: String = "Go to current month"
    
    // Graph Screen Translations
    open val labelGraph: String = "Graph"
    open val labelNoDataForExercise: String = "No record history for this exercise yet"
    open val labelNoExercises: String = "No exercises found. Create an exercise first!"
    open val labelCompareBy: String = "Compare by"
    open val labelGraphMaxWeight: String = "Weight"
    open val labelGraphVolume: String = "Volume"
    open val labelGraphOneRm: String = "1RM"

    // Unsaved Work Dialog
    open val titleNoName: String = "No Name"
    open val msgNoName: String = "Would you like to discard or fill in a name and save?"
    open val titleUnsavedWork: String = "Unsaved Work"
    open val msgUnsavedWork: String = "Would you like to discard or save?"
    open val labelDiscard: String = "Discard"
    open val labelEditAction: String = "Edit"

    // Exercise Selection Dialog Separators
    open val labelWorkouts: String = "Workouts"
    open val labelExercises: String = "Exercises"

    // In-App Font Settings
    open val labelInAppFont: String = "In-App Font"
    open val labelFontSystemDefault: String = "System Default"
    open val labelFontGoogleSansFlexRounded: String = "Google Sans Flex Rounded"

    // Showcase / Showcase Dialogs
    open val devLoadingIndicators: String = "Loading Indicators"
    open val devLoadingIndicatorsDescription: String = "View all M3 loading styles"
    open val devLoadingIndicatorsTitle: String = "Material 3 Loading Indicators"
    open val devExpressiveWavySection: String = "M3 Expressive (Wavy)"
    open val devClassicM3Section: String = "Classic M3"
}

/**
 * Default English implementation.
 */
object EnglishStrings : EnStrings(
    appLocale = Locale.ENGLISH,
    languageName = "English",
)
