package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object SvStrings : EnStrings(
    appLocale = Locale.forLanguageTag("sv"),
    languageName = "Svenska",
) {
    override val labelSystemLanguage = "Systemspråk"
    override val labelLanguage = "Språk"

    override val errorExerciseNameBlank = "Övningens namn kan inte vara tomt"

    override val errorIdMustBeZero = "Nya övningar måste ha ett ID på 0"
    override val errorIdMustNotBeZero = "Poster som ska uppdateras måste ha ett icke-noll ID"

    override fun errorExerciseAlreadyExists(name: String) = "Övning med namnet '$name' finns redan"
    override fun errorExerciseRenameConflict(name: String) = "Övningsnamnet '$name' används redan"
    override fun errorExerciseNotFoundById(id: Int) = "Övning med ID $id finns inte"

    override val errorWorkoutNameBlank = "Vänligen fyll i ett namn"
    override fun errorWorkoutAlreadyExists(name: String) = "Träningspass med namnet '$name' finns redan"
    override val errorWorkoutNoExercises = "Vänligen lägg till minst en övning"
    override val errorWorkoutEmpty = "Träningspasset är tomt"

    override val labelAddExercise = "Lägg Till Övning"
    override val labelCreateExercise = "Skapa en övning"
    override val labelCreateWorkout = "Skapa ett träningspass"
    override val labelWorkoutName = "Träningspassets Namn"
    override val labelWorkout = "Träningspass"
    override val labelExercise = "Övning"
    override val labelAdd = "Lägg till"
    override val labelExerciseName = "Övningsnamn"
    override val labelSave = "Spara"
    override val labelCancel = "Avbryt"
    override val labelClose = "Stäng"
    override val labelDelete = "Ta bort"
    override val labelSwitchLanguage = "Byt Språk"
    override val labelAiTranslationsDisclaimer = "Översättningar görs av artificiell intelligens"

    override val labelTimeline = "Tidslinje"
    override val labelMonthly = "Månadsvis"
    override val labelSettings = "Inställningar"

    override val labelSets = "Set"
    override val labelReps = "Reps"
    override val labelWeight = "Vikt"

    override fun labelEdit(target: String) = "Redigera $target"

    override val labelSelectExercise = "Välj Något"
    override val labelWeightUnit = "Viktenhet"
    override val labelProgramPlaceholder = "Platshållare För Programsida"
    override val labelHistoryListPlaceholder = "Platshållare För Historiklista"
    override val labelOpenDrawer = "Öppna Navigeringsmeny"
    override val labelHistoryEmpty = "Ingen historik ännu."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "Redigera övning"

    override val labelRecordDeleted = "Post borttagen"
    override val labelRecordsDeleted = "Poster borttagna"
    override val labelUndo = "Ångra"

    override val labelToday = "Idag"
    override val labelYesterday = "Igår"
    override val labelDeleteExerciseWarning = "Denna åtgärd kommer att ta bort alla poster och kan inte ångras"
    override val labelDeleteWorkoutWarning = "Är du säker på att du vill ta bort detta träningspass?"
    
    override val labelExerciseNamePlaceholder = "t.ex. Bänkpress"
    
    override val labelDefaultExerciseValues = "Standardvärden För Övning"

    override val labelDefaultSets = "Standardset"
    override val labelDefaultReps = "Standardreps"
    override val labelDefaultWeight = "Standardvikt"
    
    override val labelBack = "Bakåt"
    
    override val labelRotation = "Auto-rotera"
    override val labelRotationSystem = "Följ Systemet"
    override val labelRotationOn = "På"
    override val labelRotationOff = "Av"

    override val labelExportData = "Exportera Data"
    override val labelImportData = "Importera Data"
    override val labelExportSuccess = "Export Lyckades"
    override val labelExportFailed = "Export Misslyckades"
    override val labelImportSuccess = "Import Lyckades"
    override val labelImportFailed = "Import Misslyckades"
    
    override val titleImportWarning = "Skriv Över Data?"
    override val msgImportWarning = "Detta kommer att permanent radera din nuvarande databas och ersätta den med importerad data. Denna åtgärd kan inte ångras."
    override val labelConfirm = "Bekräfta"
    override val labelDeveloperOptions = "Utvecklaralternativ"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Okänd övning"
    override val labelOlderRecords = "Äldre poster"
    override val labelRestDay = "Vilodag"
    override fun labelExercisesCount(count: Int) = if (count == 1) "1 övning" else "$count övningar"

    // Developer Options
    override val devColorPalette = "Färgpalett"
    override val devViewColors = "Visa Färger"
    override val devDatabase = "Databas"
    override val devRunStressTest = "Kör Datastresstest"
    override val devStressTestDescription = "Rensa DB & infoga 1 miljon poster"
    override val devStressTestConfirmTitle = "Kör stresstest?"
    override val devStressTestConfirmMessage = "⚠️ VARNING: Detta kommer att permanent RADERA ALLA befintliga data och ersätta dem med ca 1 miljon genererade poster.\n\nDenna process kan ta en minut."
    override val devWipeAndGenerate = "Rensa & Generera"
    override val devGeneratingData = "Genererar data..."
    override val devHapticsTest = "Haptiktest"
    override val devMoveSlider = "Flytta skjutreglaget för att känna olika vibrationer"

    // Validation Errors
    override val errorSetsEmpty = "Set kan inte vara tomt"
    override val errorSetsFormat = "Ogiltigt setformat"
    override val errorSetsWholeNumber = "Set måste vara ett heltal"
    override val errorSetsPositive = "Set måste vara större än 0"

    override val errorRepsEmpty = "Reps kan inte vara tomt"
    override val errorRepsFormat = "Ogiltigt repsformat"
    override val errorRepsWholeNumber = "Reps måste vara ett heltal"
    override val errorRepsPositive = "Reps måste vara större än 0"

    override val errorWeightEmpty = "Vikt kan inte vara tomt"
    override val errorWeightFormat = "Ogiltigt viktformat"
    override val errorWeightInvalid = "Ogiltigt viktvärde"
    
    override val labelGoToCurrentMonth = "Gå till aktuell månad"
    
    // Graph Screen Translations
    override val labelGraph = "Graf"
    override val labelNoDataForExercise = "Ingen historik för denna övning ännu"
    override val labelMaxWeight = "Personligt Rekord"
    override val labelCurrentWeight = "Senaste Vikt"
    override val labelProgress = "Framsteg"
    override val labelNoExercises = "Inga övningar hittades. Skapa en övning först!"

    // Unsaved Work Dialog
    override val titleNoName = "Inget Namn"
    override val msgNoName = "Vill du kasta bort eller fylla i ett namn och spara?"
    override val titleUnsavedWork = "Osparat Arbete"
    override val msgUnsavedWork = "Vill du kasta bort eller spara?"
    override val labelDiscard = "Kasta bort"
    override val labelKeepEditing = "Fortsätt Redigera"
    override val labelEditAction = "Redigera"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Träningspass"
    override val labelExercises = "Övningar"

    // In-App Font Settings
    override val labelInAppFont = "Teckensnitt i appen"
    override val labelFontSystemDefault = "Systemstandard"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Rundad"
}
