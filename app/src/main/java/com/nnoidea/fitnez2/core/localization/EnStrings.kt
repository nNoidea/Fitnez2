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
    open val errorIdMustNotBeZero: String = "Records to update must have a non-zero ID. Use create() for new exercises."

    open fun errorExerciseAlreadyExists(name: String): String = "Exercise with name '$name' already exists."
    open fun errorExerciseRenameConflict(name: String): String = "Exercise name '$name' is already used by another exercise."
    open fun errorExerciseNotFoundById(id: Int): String = "Exercise with ID $id does not exist."

    open val labelAddExercise: String = "Add Exercise"
    open val labelCreateExercise: String = "Create an exercise"
    open val labelCreateWorkout: String = "Create a workout"
    open val labelAdd: String = "Add"
    open val labelExerciseName: String = "Exercise Name"
    open val labelSave: String = "Save"
    open val labelCancel: String = "Cancel"
    open val labelDelete: String = "Delete"
    open val labelSwitchLanguage: String = "Switch Language"

    open val labelHome: String = "Home"
    open val labelProgram: String = "Programs"
    open val labelSettings: String = "Settings"

    open val labelSets: String = "Sets"
    open val labelReps: String = "Reps"
    open val labelWeight: String = "Weight"

    open fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy - EEEE", appLocale)
        return sdf.format(java.util.Date(timestamp)).lowercase()
    }

    open fun formatDateShort(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", appLocale)
        return sdf.format(java.util.Date(timestamp))
    }

    open fun formatDayName(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("EEEE", appLocale)
        return sdf.format(java.util.Date(timestamp))
    }


    open fun labelEdit(target: String): String = "Edit $target"

    open val labelSelectExercise: String = "Select Exercise or Workout"
    open val labelWeightUnit: String = "Weight Unit"
    open val labelProgramPlaceholder: String = "Program Page Placeholder"
    open val labelHistoryListPlaceholder: String = "History List Placeholder"
    open val labelOpenDrawer: String = "Open Navigation Drawer"
    open val labelHistoryEmpty: String = "No history yet."
    open val labelAppName: String = "Fitnez2"
    open val labelVersion: String = "1.0.0"
    open val labelEditExercise: String = "Edit Exercise"

    open fun labelWeightWithUnit(unit: String): String = "$labelWeight ($unit)"

    open val labelRecordDeleted: String = "Record deleted"
    open val labelUndo: String = "Undo"

    open val labelToday: String = "Today"
    open val labelYesterday: String = "Yesterday"
    open val labelDeleteExerciseWarning: String = "This action will delete all records and cannot be undone"
    
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
    open val labelOlderRecords: String = "Older Records"

    // Validation Errors
    open val errorSetsEmpty: String = "Sets cannot be empty"
    open val errorSetsFormat: String = "Invalid sets format"
    open val errorSetsWholeNumber: String = "Sets must be a whole number"
    open val errorSetsPositive: String = "Sets must be greater than 0"

    open val errorRepsEmpty: String = "Reps cannot be empty"
    open val errorRepsFormat: String = "Invalid reps format"
    open val errorRepsWholeNumber: String = "Reps must be a whole number"
    open val errorRepsPositive: String = "Reps must be greater than 0"

    open val errorWeightEmpty: String = "Weight cannot be empty"
    open val errorWeightFormat: String = "Invalid weight format"
    open val errorWeightInvalid: String = "Invalid weight value"
}

/**
 * Default English implementation.
 */
object EnglishStrings : EnStrings(
    appLocale = Locale.ENGLISH,
    languageName = "English",
)
