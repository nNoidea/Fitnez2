package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object NlStrings : EnStrings(
    appLocale = Locale.forLanguageTag("nl"),
    languageName = "Nederlands",
) {
    override val labelSystemLanguage = "Systeemtaal"
    override val labelLanguage = "Taal"

    override val errorExerciseNameBlank = "Oefeningsnaam mag niet leeg zijn"

    override val errorIdMustBeZero = "Nieuwe oefeningen moeten een ID van 0 hebben"

    override fun errorExerciseAlreadyExists(name: String) = "Oefening met naam '$name' bestaat al"
    override fun errorExerciseRenameConflict(name: String) = "Oefeningsnaam '$name' is al in gebruik"
    override fun errorExerciseNotFoundById(id: String) = "Oefening met ID $id bestaat niet"

    override val errorWorkoutNameBlank = "Vul a.u.b. een naam in"
    override fun errorWorkoutAlreadyExists(name: String) = "Workout met naam '$name' bestaat al"
    override val errorWorkoutNoExercises = "Voeg a.u.b. ten minste één oefening toe"
    override val errorWorkoutEmpty = "Workout is leeg"
    override val labelCreateExercise = "Oefening maken" // English is "Create an exercise" (Sentence Case). So this is fine!
    override val labelWorkoutName = "Workout Naam"
    override val labelWorkout = "Workout"
    override val labelExercise = "Oefening"
    override val labelAdd = "Toevoegen"
    override val labelExerciseName = "Oefeningsnaam"
    override val labelSave = "Opslaan"
    override val labelCancel = "Annuleren"
    override val labelClose = "Sluiten"
    override val labelDelete = "Verwijderen"
    override val labelAiTranslationsDisclaimer = "Vertalingen zijn gedaan door Kunstmatige Intelligentie"

    override val labelTimeline = "Tijdlijn"
    override val labelMonthly = "Maandelijks"
    override val labelSettings = "Instellingen"

    override val labelSets = "Sets"
    override val labelReps = "Reps"

    override fun labelEdit(target: String) = "Bewerk $target"

    override val labelSelectExercise = "Selecteer Iets"
    override val labelWeightUnit = "Gewichtseenheid"
    override val labelOpenDrawer = "Navigatiemenu Openen"
    override val labelHistoryEmpty = "Nog geen geschiedenis."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "Oefening bewerken"

    override val labelRecordDeleted = "Record verwijderd"
    override val labelRecordsDeleted = "Records verwijderd"
    override val labelUndo = "Ongedaan maken"

    override val labelToday = "Vandaag"
    override val labelYesterday = "Gisteren"
    override val labelDeleteExerciseWarning = "Deze actie verwijdert alle records en kan niet ongedaan worden gemaakt"
    override val labelDeleteWorkoutWarning = "Weet je zeker dat je deze workout wilt verwijderen?"
    
    override val labelExerciseNamePlaceholder = "bijv. Bench Press"
    
    override val labelDefaultExerciseValues = "Standaard Oefeningswaarden"

    override val labelDefaultSets = "Standaard Sets"
    override val labelDefaultReps = "Standaard Reps"
    override val labelDefaultWeight = "Standaard Gewicht"
    
    override val labelBack = "Terug"
    
    override val labelRotation = "Automatisch draaien"
    override val labelRotationSystem = "Systeem Volgen"
    override val labelRotationOn = "Aan"
    override val labelRotationOff = "Uit"

    override val labelExportData = "Gegevens Exporteren"
    override val labelImportData = "Gegevens Importeren"
    override val labelExportSuccess = "Exporteren Geslaagd"
    override val labelExportFailed = "Exporteren Mislukt"
    override val labelImportSuccess = "Importeren Geslaagd"
    override val labelImportFailed = "Importeren Mislukt"
    
    override val titleImportWarning = "Gegevens Overschrijven?"
    override val msgImportWarning = "Dit zal je huidige database permanent verwijderen en vervangen door de geïmporteerde gegevens. Deze actie kan niet ongedaan worden gemaakt."
    override val labelConfirm = "Bevestigen"
    override val labelDeveloperOptions = "Ontwikkelaarsopties"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Onbekende oefening"

    // Developer Options
    override val devColorPalette = "Kleurenpalet"
    override val devViewColors = "Kleuren Bekijken"
    override val devDatabase = "Database"
    override val devRunStressTest = "Gegevensstresstest Uitvoeren"
    override val devStressTestDescription = "DB wissen & 1M records invoegen"
    override val devStressTestConfirmTitle = "Stresstest uitvoeren?"
    override val devStressTestConfirmMessage = "⚠️ WAARSCHUWING: Dit zal permanent ALLE bestaande gegevens verwijderen en vervangen door ~1 miljoen gegenereerde records.\n\nDit proces kan een minuut duren."
    override val devWipeAndGenerate = "Wissen & genereren"
    override val devGeneratingData = "Gegevens genereren..."
    override val devHapticsTest = "Haptics-test"
    override val devMoveSlider = "Beweeg de schuifregelaar om verschillende trillingen te voelen"

    // Validation Errors
    override val errorSetsPositive = "Sets moeten groter zijn dan 0"
    override val errorRepsPositive = "Reps moeten groter zijn dan 0"
    override val errorWeightInvalid = "Ongeldige gewichtswaarde"
    
    override val labelGoToCurrentMonth = "Ga naar huidige maand"
    
    // Graph Screen Translations
    override val labelGraph = "Grafiek"
    override val labelNoDataForExercise = "Nog geen geschiedenis voor deze oefening"
    override val labelNoExercises = "Geen oefeningen gevonden. Maak eerst een oefening!"
    override val labelCompareBy = "Vergelijken op"
    override val labelGraphMaxWeight = "Gewicht"
    override val labelGraphVolume = "Volume"

    // Unsaved Work Dialog
    override val titleNoName = "Geen Naam"
    override val msgNoName = "Wil je negeren of een naam invullen en opslaan?"
    override val titleUnsavedWork = "Niet-Opgeslagen Werk"
    override val msgUnsavedWork = "Wil je negeren of opslaan?"
    override val labelDiscard = "Negeren"
    override val labelEditAction = "Bewerken"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Workouts"
    override val labelExercises = "Oefeningen"

    // In-App Font Settings
    override val labelInAppFont = "Lettertype in-app"
    override val labelFontSystemDefault = "Systeemstandaard"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Afgerond"
}
