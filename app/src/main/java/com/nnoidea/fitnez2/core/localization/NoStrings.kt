package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object NoStrings : EnStrings(
    appLocale = Locale.forLanguageTag("no"),
    languageName = "Norsk",
) {
    override val labelSystemLanguage = "Systemspråk"
    override val labelLanguage = "Språk"

    override val errorExerciseNameBlank = "Navn på øvelse kan ikke være tomt"

    override val errorIdMustBeZero = "Nye øvelser må ha en ID på 0"
    override val errorIdMustNotBeZero = "Oppføringer som skal oppdateres må ha en ikke-null ID"

    override fun errorExerciseAlreadyExists(name: String) = "Øvelse med navn '$name' finnes allerede"
    override fun errorExerciseRenameConflict(name: String) = "Navn på øvelse '$name' er allerede i bruk"
    override fun errorExerciseNotFoundById(id: Int) = "Øvelse med ID $id finnes ikke"

    override val errorWorkoutNameBlank = "Vennligst fyll ut et navn"
    override fun errorWorkoutAlreadyExists(name: String) = "Treningsøkt med navn '$name' finnes allerede"
    override val errorWorkoutNoExercises = "Vennligst legg til minst én øvelse"
    override val errorWorkoutEmpty = "Treningsøkten er tom"

    override val labelAddExercise = "Legg Til Øvelse"
    override val labelCreateExercise = "Opprett en øvelse"
    override val labelCreateWorkout = "Opprett en treningsøkt"
    override val labelWorkoutName = "Navn På Treningsøkt"
    override val labelWorkout = "Treningsøkt"
    override val labelExercise = "Øvelse"
    override val labelAdd = "Legg til"
    override val labelExerciseName = "Navn på øvelse"
    override val labelSave = "Lagre"
    override val labelCancel = "Avbryt"
    override val labelClose = "Lukk"
    override val labelDelete = "Slett"
    override val labelSwitchLanguage = "Bytt Språk"
    override val labelAiTranslationsDisclaimer = "Oversettelser er gjort av kunstig intelligens"

    override val labelTimeline = "Tidslinje"
    override val labelMonthly = "Månedlig"
    override val labelSettings = "Innstillinger"

    override val labelSets = "Sett"
    override val labelReps = "Reps"
    override val labelWeight = "Vekt"

    override fun labelEdit(target: String) = "Rediger $target"

    override val labelSelectExercise = "Velg Noe"
    override val labelWeightUnit = "Vektenhet"
    override val labelProgramPlaceholder = "Plassholder For Programside"
    override val labelHistoryListPlaceholder = "Plassholder For Historikliste"
    override val labelOpenDrawer = "Åpne Navigasjonsskuff"
    override val labelHistoryEmpty = "Ingen historikk ennå."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "Rediger øvelse"

    override val labelRecordDeleted = "Oppføring slettet"
    override val labelUndo = "Angre"

    override val labelToday = "I dag"
    override val labelYesterday = "I går"
    override val labelDeleteExerciseWarning = "Denne handlingen vil slette alle oppføringer og kan ikke angres"
    override val labelDeleteWorkoutWarning = "Er du sikker på at du vil slette denne treningsøkten?"
    
    override val labelExerciseNamePlaceholder = "f.eks. Benkpress"
    
    override val labelDefaultExerciseValues = "Standardverdier For Øvelse"

    override val labelDefaultSets = "Standardsett"
    override val labelDefaultReps = "Standardreps"
    override val labelDefaultWeight = "Standardvikt"
    
    override val labelBack = "Tilbake"
    
    override val labelRotation = "Auto-roter"
    override val labelRotationSystem = "Følg Systemet"
    override val labelRotationOn = "På"
    override val labelRotationOff = "Av"

    override val labelExportData = "Eksporter Data"
    override val labelImportData = "Importer Data"
    override val labelExportSuccess = "Eksport Vellykket"
    override val labelExportFailed = "Eksport Mislyktes"
    override val labelImportSuccess = "Import Vellykket"
    override val labelImportFailed = "Import Mislyktes"
    
    override val titleImportWarning = "Overskriv Data?"
    override val msgImportWarning = "Dette vil slette din nåværende database permanent og erstatte den med de importerte dataene. Denne handlingen kan ikke angres."
    override val labelConfirm = "Bekreft"
    override val labelDeveloperOptions = "Utvikleralternativer" // "Utvikleralternativer" in Norwegian!
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Ukjent øvelse"
    override val labelOlderRecords = "Eldre oppføringer"
    override val labelRestDay = "Hviledag"
    override fun labelExercisesCount(count: Int) = if (count == 1) "1 øvelse" else "$count øvelser"

    // Developer Options
    override val devColorPalette = "Fargepalett"
    override val devViewColors = "Vis Farger"
    override val devDatabase = "Database"
    override val devRunStressTest = "Kjør Datastresstest"
    override val devStressTestDescription = "Tøm DB & sett inn 1 mill. oppføringer"
    override val devStressTestConfirmTitle = "Kjøre stresstest?"
    override val devStressTestConfirmMessage = "⚠️ ADVARSEL: Dette vil slette ALLE eksisterende data permanent og erstatte dem med ca. 1 million genererede oppføringer.\n\nDenne prosessen kan ta et minutt."
    override val devWipeAndGenerate = "Tøm & Generer"
    override val devGeneratingData = "Genererer data..."
    override val devHapticsTest = "Haptisk test"
    override val devMoveSlider = "Flytt glidebryteren for å føle forskjellige vibrasjoner"

    // Validation Errors
    override val errorSetsEmpty = "Sett kan ikke være tomt"
    override val errorSetsFormat = "Ugyldig settformat"
    override val errorSetsWholeNumber = "Sett må være et heltall"
    override val errorSetsPositive = "Sett må være større enn 0"

    override val errorRepsEmpty = "Reps kan ikke være tomt"
    override val errorRepsFormat = "Ugyldig repsformat"
    override val errorRepsWholeNumber = "Reps må være et heltall"
    override val errorRepsPositive = "Reps må være større enn 0"

    override val errorWeightEmpty = "Vekt kan ikke være tomt"
    override val errorWeightFormat = "Ugyldig vektformat"
    override val errorWeightInvalid = "Ugyldig vektverdi"
    
    override val labelGoToCurrentMonth = "Gå til gjeldende måned"
    
    // Graph Screen Translations
    override val labelGraph = "Graf"
    override val labelNoDataForExercise = "Ingen historikk for denne øvelsen ennå"
    override val labelMaxWeight = "Personlig Rekord"
    override val labelCurrentWeight = "Siste Vekt"
    override val labelProgress = "Fremgang"
    override val labelNoExercises = "Ingen øvelser funnet. Opprett en øvelse først!"

    // Unsaved Work Dialog
    override val titleNoName = "Uten Navn"
    override val msgNoName = "Vil du forkaste eller fylle ut et navn og lagre?"
    override val titleUnsavedWork = "Ulagret Arbeid"
    override val msgUnsavedWork = "Vil du forkaste eller lagre?"
    override val labelDiscard = "Forkast"
    override val labelKeepEditing = "Fortsett Å Redigere"
    override val labelEditAction = "Rediger"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Treningsøkter"
    override val labelExercises = "Øvelser"

    // In-App Font Settings
    override val labelInAppFont = "Skrifttype i appen"
    override val labelFontSystemDefault = "Systemstandard"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Avrundet"
}
