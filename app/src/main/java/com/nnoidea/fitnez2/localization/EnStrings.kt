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
    open val errorExerciseAlreadyExists: String = "Exercise with this name already exists."
    open val errorExerciseRenameConflict: String = "This name is already used by another exercise."
    open val errorExerciseNotFound: String = "Exercise not found."

    open val errorIdMustBeZero: String = "New exercises must have an ID of 0. Use update() for existing exercises."
    open val errorIdMustNotBeZero: String = "Records to update must have a non-zero ID. Use create() for new exercises."

    open val errorSetsInputInvalid: String = "Sets must be non-negative and limited to 2 decimal places."
    open val errorRepsInputInvalid: String = "Reps must be non-negative and limited to 2 decimal places."

    open fun errorExerciseAlreadyExists(name: String): String = "Exercise with name '$name' already exists."
    open fun errorExerciseRenameConflict(name: String): String = "Exercise name '$name' is already used by another exercise."
    open fun errorExerciseNotFoundById(id: Int): String = "Exercise with ID $id does not exist."

    open val labelAddExercise: String = "Add Exercise"
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

    // UI - BottomSheet
    open val labelBottomSheetTitle: String = "Custom Draggable Sheet"
    open val labelBottomSheetDesc: String = "Full control over animation."
    open val labelSayHello: String = "Say Hello"
    open val labelHelloTitle: String = "Hello"
    open val labelHelloText: String = "Hello there!"
    open val labelOkay: String = "Okay"
    open fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy - EEEE", appLocale)
        return sdf.format(java.util.Date(timestamp)).lowercase()
    }


    open fun labelEdit(target: String): String = "Edit $target"

    open val labelSelectExercise: String = "Exercise Name"
    open val labelWeightUnit: String = "Weight Unit"
}

/**
 * Default English implementation.
 */
object EnglishStrings : EnStrings(
    appLocale = Locale.ENGLISH,
    languageName = "English",
)
