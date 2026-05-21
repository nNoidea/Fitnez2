package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object DeStrings : EnStrings(
    appLocale = Locale.forLanguageTag("de"),
    languageName = "Deutsch",
) {
    override val labelSystemLanguage = "Systemsprache"
    override val labelLanguage = "Sprache"

    override val errorExerciseNameBlank = "Übungsname darf nicht leer sein"

    override val errorIdMustBeZero = "Neue Übungen müssen eine ID von 0 haben"
    override val errorIdMustNotBeZero = "Zu aktualisierende Datensätze müssen eine ID ungleich Null haben"

    override fun errorExerciseAlreadyExists(name: String) = "Übung mit dem Namen '$name' existiert bereits"
    override fun errorExerciseRenameConflict(name: String) = "Übungsname '$name' wird bereits verwendet"
    override fun errorExerciseNotFoundById(id: Int) = "Übung mit der ID $id existiert nicht"

    override val errorWorkoutNameBlank = "Bitte geben Sie einen Namen ein"
    override fun errorWorkoutAlreadyExists(name: String) = "Workout mit dem Namen '$name' existiert bereits"
    override val errorWorkoutNoExercises = "Bitte fügen Sie mindestens eine Übung hinzu"
    override val errorWorkoutEmpty = "Workout ist leer"

    override val labelAddExercise = "Übung Hinzufügen"
    override val labelCreateExercise = "Übung erstellen"
    override val labelCreateWorkout = "Workout erstellen"
    override val labelWorkoutName = "Workout-Name"
    override val labelWorkout = "Workout"
    override val labelExercise = "Übung"
    override val labelAdd = "Hinzufügen"
    override val labelExerciseName = "Übungsname"
    override val labelSave = "Speichern"
    override val labelCancel = "Abbrechen"
    override val labelClose = "Schließen"
    override val labelDelete = "Löschen"
    override val labelSwitchLanguage = "Sprache Wechseln"
    override val labelAiTranslationsDisclaimer = "Übersetzungen wurden durch Künstliche Intelligenz erstellt"

    override val labelTimeline = "Timeline"
    override val labelMonthly = "Monatlich"
    override val labelSettings = "Einstellungen"

    override val labelSets = "Sätze"
    override val labelReps = "Reps"
    override val labelWeight = "Gewicht"

    override fun labelEdit(target: String) = "$target bearbeiten"

    override val labelSelectExercise = "Etwas Auswählen"
    override val labelWeightUnit = "Gewichtseinheit"
    override val labelProgramPlaceholder = "Programmseite Platzhalter"
    override val labelHistoryListPlaceholder = "Verlaufsliste Platzhalter"
    override val labelOpenDrawer = "Navigationsmenü Öffnen"
    override val labelHistoryEmpty = "Noch kein Verlauf."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "Übung Bearbeiten"

    override val labelRecordDeleted = "Datensatz gelöscht"
    override val labelUndo = "Rückgängig machen"

    override val labelToday = "Heute"
    override val labelYesterday = "Gestern"
    override val labelDeleteExerciseWarning = "Diese Aktion löscht alle Datensätze und kann nicht rückgängig gemacht werden"
    override val labelDeleteWorkoutWarning = "Sind Sie sicher, dass Sie dieses Workout löschen möchten?"
    
    override val labelExerciseNamePlaceholder = "z.B. Bankdrücken"
    
    override val labelDefaultExerciseValues = "Standard-Übungswerte"

    override val labelDefaultSets = "Standard-Sätze"
    override val labelDefaultReps = "Standard-Reps"
    override val labelDefaultWeight = "Standard-Gewicht"
    
    override val labelBack = "Zurück"
    
    override val labelRotation = "Auto-Rotation"
    override val labelRotationSystem = "System Folgen"
    override val labelRotationOn = "An"
    override val labelRotationOff = "Aus"

    override val labelExportData = "Daten Exportieren"
    override val labelImportData = "Daten Importieren"
    override val labelExportSuccess = "Export Erfolgreich"
    override val labelExportFailed = "Export Fehlgeschlagen"
    override val labelImportSuccess = "Import Erfolgreich"
    override val labelImportFailed = "Import Fehlgeschlagen"
    
    override val titleImportWarning = "Daten Überschreiben?"
    override val msgImportWarning = "Dadurch wird Ihre aktuelle Datenbank dauerhaft gelöscht und durch die importierten Daten ersetzt. Diese Aktion kann nicht rückgängig gemacht werden."
    override val labelConfirm = "Bestätigen"
    override val labelDeveloperOptions = "Entwickleroptionen"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Unbekannte Übung"
    override val labelOlderRecords = "Ältere Datensätze"
    override val labelRestDay = "Ruhetag"
    override fun labelExercisesCount(count: Int) = if (count == 1) "1 Übung" else "$count Übungen"

    // Developer Options
    override val devColorPalette = "Farbpalette"
    override val devViewColors = "Farben Anzeigen"
    override val devDatabase = "Datenbank"
    override val devRunStressTest = "Daten-Stresstest Ausführen"
    override val devStressTestDescription = "DB löschen & 1M Datensätze einfügen"
    override val devStressTestConfirmTitle = "Stresstest ausführen?"
    override val devStressTestConfirmMessage = "⚠️ WARNUNG: Dadurch werden alle vorhandenen Daten dauerhaft GELÖSCHT und durch ca. 1 Million generierte Datensätze ersetzt.\n\nDieser Vorgang kann eine Minute dauern."
    override val devWipeAndGenerate = "Löschen & Generieren"
    override val devGeneratingData = "Daten werden generiert..."
    override val devHapticsTest = "Haptik-Test"
    override val devMoveSlider = "Schieberegler bewegen, um verschiedene Vibrationen zu spüren"

    // Validation Errors
    override val errorSetsEmpty = "Sätze dürfen nicht leer sein"
    override val errorSetsFormat = "Ungültiges Sätze-Format"
    override val errorSetsWholeNumber = "Sätze müssen eine ganze Zahl sein"
    override val errorSetsPositive = "Sätze müssen größer als 0 sein"

    override val errorRepsEmpty = "Reps dürfen nicht leer sein"
    override val errorRepsFormat = "Ungültiges Reps-Format"
    override val errorRepsWholeNumber = "Reps müssen eine ganze Zahl sein"
    override val errorRepsPositive = "Reps müssen größer als 0 sein"

    override val errorWeightEmpty = "Gewicht darf nicht leer sein"
    override val errorWeightFormat = "Ungültiges Gewicht-Format"
    override val errorWeightInvalid = "Ungültiger Gewichtswert"
    
    override val labelGoToCurrentMonth = "Zum aktuellen Monat gehen"
    
    // Graph Screen Translations
    override val labelGraph = "Grafik"
    override val labelNoDataForExercise = "Noch kein Verlauf für diese Übung"
    override val labelMaxWeight = "Persönlicher Rekord"
    override val labelCurrentWeight = "Letztes Gewicht"
    override val labelProgress = "Fortschritt"
    override val labelNoExercises = "Keine Übungen gefunden. Erstellen Sie zuerst eine Übung!"

    // Unsaved Work Dialog
    override val titleNoName = "Kein Name"
    override val msgNoName = "Möchten Sie verwerfen oder einen Namen eingeben und speichern?"
    override val titleUnsavedWork = "Ungespeicherte Arbeit"
    override val msgUnsavedWork = "Möchten Sie verwerfen oder speichern?"
    override val labelDiscard = "Verwerfen"
    override val labelKeepEditing = "Weiter Bearbeiten"
    override val labelEditAction = "Bearbeiten"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Workouts"
    override val labelExercises = "Übungen"

    // In-App Font Settings
    override val labelInAppFont = "Schriftart in der App"
    override val labelFontSystemDefault = "Systemstandard"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Abgerundet"
}
