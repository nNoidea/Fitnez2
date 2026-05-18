package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object DaStrings : EnStrings(
    appLocale = Locale.forLanguageTag("da"),
    languageName = "Dansk",
) {
    override val labelSystemLanguage = "Systemsprog"
    override val labelLanguage = "Sprog"

    override val errorExerciseNameBlank = "Træningsnavn kan ikke være tomt"

    override val errorIdMustBeZero = "Nye øvelser skal have et ID på 0"
    override val errorIdMustNotBeZero = "Optegnelser, der skal opdateres, skal have et ikke-nul ID"

    override fun errorExerciseAlreadyExists(name: String) = "Øvelse med namnet '$name' findes allerede"
    override fun errorExerciseRenameConflict(name: String) = "Øvelsesnavnet '$name' er allerede i brug"
    override fun errorExerciseNotFoundById(id: Int) = "Øvelse med ID $id findes ikke"

    override val errorWorkoutNameBlank = "Udfyld venligst et navn"
    override fun errorWorkoutAlreadyExists(name: String) = "Træningspas med namnet '$name' findes allerede"
    override val errorWorkoutNoExercises = "Tilføj venligst mindst én øvelse"
    override val errorWorkoutEmpty = "Træningspasset er tomt"

    override val labelAddExercise = "Tilføj Øvelse"
    override val labelCreateExercise = "Opret en øvelse"
    override val labelCreateWorkout = "Opret et træningspas"
    override val labelWorkoutName = "Træningspassets Navn"
    override val labelWorkout = "Træningspas"
    override val labelExercise = "Øvelse"
    override val labelAdd = "Tilføj"
    override val labelExerciseName = "Øvelsesnavn"
    override val labelSave = "Gem"
    override val labelCancel = "Annuller"
    override val labelClose = "Luk"
    override val labelDelete = "Slet"
    override val labelSwitchLanguage = "Skift Sprog"
    override val labelAiTranslationsDisclaimer = "Oversættelser er udført af kunstig intelligens"

    override val labelTimeline = "Tidslinje"
    override val labelMonthly = "Månedlig"
    override val labelSettings = "Indstillinger"

    override val labelSets = "Sæt"
    override val labelReps = "Reps"
    override val labelWeight = "Vægt"

    override fun labelEdit(target: String) = "Rediger $target"

    override val labelSelectExercise = "Vælg Noget"
    override val labelWeightUnit = "Vægtenhed"
    override val labelProgramPlaceholder = "Pladsholder For Programside"
    override val labelHistoryListPlaceholder = "Pladsholder For Historikliste"
    override val labelOpenDrawer = "Åbn Navigationsmenu"
    override val labelHistoryEmpty = "Ingen historik endnu."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "Rediger øvelse"

    override val labelRecordDeleted = "Optegnelse slettet"
    override val labelUndo = "Fortryd"

    override val labelToday = "I dag"
    override val labelYesterday = "I går"
    override val labelDeleteExerciseWarning = "Denne handling vil slette alle optegnelser e kan ikke fortrydes"
    override val labelDeleteWorkoutWarning = "Er du sikker på, at du vil slette dette træningspas?"
    
    override val labelExerciseNamePlaceholder = "f.eks. Bænkpres"
    
    override val labelDefaultExerciseValues = "Standardværdier For Øvelse"

    override val labelDefaultSets = "Standardsæt"
    override val labelDefaultReps = "Standardreps"
    override val labelDefaultWeight = "Standardvægt"
    
    override val labelBack = "Tilbage"
    
    override val labelRotation = "Auto-roter"
    override val labelRotationSystem = "Følg Systemet"
    override val labelRotationOn = "Til"
    override val labelRotationOff = "Fra"

    override val labelExportData = "Eksporter Data"
    override val labelImportData = "Importer Data"
    override val labelExportSuccess = "Eksport Lykkedes"
    override val labelExportFailed = "Eksport Mislykkedes"
    override val labelImportSuccess = "Import Lykkedes"
    override val labelImportFailed = "Import Mislykkedes"
    
    override val titleImportWarning = "Overskriv Data?"
    override val msgImportWarning = "Dette vil slette din nuværende database permanent e erstatte den med de importerede data. Denne handling kan ikke fortrydes."
    override val labelConfirm = "Bekræft"
    override val labelDeveloperOptions = "Udviklerindstillinger"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Ukendt øvelse"
    override val labelOlderRecords = "Ældre optegnelser"
    override val labelRestDay = "Hviledag"
    override fun labelExercisesCount(count: Int) = if (count == 1) "1 øvelse" else "$count øvelser"

    // Developer Options
    override val devColorPalette = "Farvepalette"
    override val devViewColors = "Vis Farver"
    override val devDatabase = "Database"
    override val devRunStressTest = "Kør Datastresstest"
    override val devStressTestDescription = "Ryd DB & indsæt 1 mio. optegnelser"
    override val devStressTestConfirmTitle = "Kør stresstest?"
    override val devStressTestConfirmMessage = "⚠️ ADVARSEL: Dette vil slette ALLE eksisterende data permanent e erstatte dem med ca. 1 million genererede optegnelser.\n\nDenne proces kan tage et minut."
    override val devWipeAndGenerate = "Ryd & Generer"
    override val devGeneratingData = "Genererer data..."
    override val devHapticsTest = "Haptisk test"
    override val devMoveSlider = "Flyt skyderen for at mærke forskellige vibrationer"

    // Validation Errors
    override val errorSetsEmpty = "Sæt kan ikke være tomt"
    override val errorSetsFormat = "Ugyldigt sætformat"
    override val errorSetsWholeNumber = "Sæt skal være et heltal"
    override val errorSetsPositive = "Sæt skal være større end 0"

    override val errorRepsEmpty = "Reps kan ikke være tomt"
    override val errorRepsFormat = "Ugyldigt repsformat"
    override val errorRepsWholeNumber = "Reps skal være et heltal"
    override val errorRepsPositive = "Reps skal være større end 0"

    override val errorWeightEmpty = "Vægt kan ikke være tomt"
    override val errorWeightFormat = "Ugyldigt vægtformat"
    override val errorWeightInvalid = "Ugyldig vægtværdi"
    
    override val labelGoToCurrentMonth = "Gå til aktuel måned"
    
    // Graph Screen Translations
    override val labelGraph = "Graf"
    override val labelNoDataForExercise = "Ingen historik for denne øvelse endnu"
    override val labelMaxWeight = "Personlig Rekord"
    override val labelCurrentWeight = "Seneste Vægt"
    override val labelProgress = "Fremskridt"
    override val labelNoExercises = "Ingen øvelser fundet. Opret en øvelse først!"

    // Unsaved Work Dialog
    override val titleNoName = "Intet Navn"
    override val msgNoName = "Vil du kassere eller udfylde et navn og gemme?"
    override val titleUnsavedWork = "Ugemt Arbejde"
    override val msgUnsavedWork = "Gemme eller kassere?"
    override val labelDiscard = "Kasser"
    override val labelKeepEditing = "Fortsæt Med At Redigere"
    override val labelEditAction = "Rediger"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Træningspas"
    override val labelExercises = "Øvelser"
}
