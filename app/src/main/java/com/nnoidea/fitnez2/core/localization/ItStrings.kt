package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object ItStrings : EnStrings(
    appLocale = Locale.forLanguageTag("it"),
    languageName = "Italiano",
) {
    override val labelSystemLanguage = "Lingua di sistema"
    override val labelLanguage = "Lingua"

    override val errorExerciseNameBlank = "Il nome dell'esercizio non può essere vuoto"

    override val errorIdMustBeZero = "I nuovi esercizi devono avere un ID pari a 0"
    override val errorIdMustNotBeZero = "I record da aggiornare devono avere un ID diverso da zero"

    override fun errorExerciseAlreadyExists(name: String) = "L'esercizio con il nome '$name' esiste già"
    override fun errorExerciseRenameConflict(name: String) = "Il nome dell'esercizio '$name' è già in uso"
    override fun errorExerciseNotFoundById(id: Int) = "L'esercizio con ID $id non esiste"

    override val errorWorkoutNameBlank = "Inserisci un nome"
    override fun errorWorkoutAlreadyExists(name: String) = "L'allenamento con il nome '$name' esiste già"
    override val errorWorkoutNoExercises = "Aggiungi almeno un esercizio"
    override val errorWorkoutEmpty = "L'allenamento è vuoto"

    override val labelAddExercise = "Aggiungi Esercizio"
    override val labelCreateExercise = "Crea esercizio"
    override val labelCreateWorkout = "Crea allenamento"
    override val labelWorkoutName = "Nome Allenamento"
    override val labelWorkout = "Allenamento"
    override val labelExercise = "Esercizio"
    override val labelAdd = "Aggiungi"
    override val labelExerciseName = "Nome Esercizio"
    override val labelSave = "Salva"
    override val labelCancel = "Annulla"
    override val labelClose = "Chiudi"
    override val labelDelete = "Elimina"
    override val labelSwitchLanguage = "Cambia Lingua"
    override val labelAiTranslationsDisclaimer = "Le traduzioni sono effettuate dall'Intelligenza Artificiale"

    override val labelTimeline = "Cronologia"
    override val labelMonthly = "Mensile"
    override val labelSettings = "Impostazioni"

    override val labelSets = "Serie"
    override val labelReps = "Reps"
    override val labelWeight = "Peso"

    override fun labelEdit(target: String) = "Modifica $target"

    override val labelSelectExercise = "Seleziona Qualcosa"
    override val labelWeightUnit = "Unità Di Peso"
    override val labelProgramPlaceholder = "Segnaposto Pagina Programma"
    override val labelHistoryListPlaceholder = "Segnaposto Elenco Cronologia"
    override val labelOpenDrawer = "Apri Menu Di Navigazione"
    override val labelHistoryEmpty = "Nessuna cronologia ancora."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "Modifica esercizio"

    override val labelRecordDeleted = "Record eliminato"
    override val labelUndo = "Annulla"

    override val labelToday = "Oggi"
    override val labelYesterday = "Ieri"
    override val labelDeleteExerciseWarning = "Questa azione eliminerà tutti i record e non può essere annullata"
    override val labelDeleteWorkoutWarning = "Sei sicuro di voler eliminare questo allenamento?"
    
    override val labelExerciseNamePlaceholder = "es. Panca piana"
    
    override val labelDefaultExerciseValues = "Valori Predefiniti Esercizio"

    override val labelDefaultSets = "Serie Predefinite"
    override val labelDefaultReps = "Reps Predefinite"
    override val labelDefaultWeight = "Peso Predefinito"
    
    override val labelBack = "Indietro"
    
    override val labelRotation = "Rotazione automatica"
    override val labelRotationSystem = "Segui Il Sistema"
    override val labelRotationOn = "Attivo"
    override val labelRotationOff = "Disattivo"

    override val labelExportData = "Esporta Dati"
    override val labelImportData = "Importa Dati"
    override val labelExportSuccess = "Esportazione Completata"
    override val labelExportFailed = "Esportazione Fallita"
    override val labelImportSuccess = "Importazione Completata"
    override val labelImportFailed = "Importazione Fallita"
    
    override val titleImportWarning = "Sovrascrivere I Dati?"
    override val msgImportWarning = "Questo eliminerà permanentemente il database corrente e lo sostituirà con i dati importati. Questa azione non può essere annullata."
    override val labelConfirm = "Conferma"
    override val labelDeveloperOptions = "Opzioni sviluppatore"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Esercizio sconosciuto"
    override val labelOlderRecords = "Record più vecchi"
    override val labelRestDay = "Giorno di riposo"
    override fun labelExercisesCount(count: Int) = if (count == 1) "1 esercizio" else "$count esercizi"

    // Developer Options
    override val devColorPalette = "Tavolozza colori"
    override val devViewColors = "Visualizza Colori"
    override val devDatabase = "Database"
    override val devRunStressTest = "Esegui Stress Test Dati"
    override val devStressTestDescription = "Cancella DB e inserisci 1 milione di record"
    override val devStressTestConfirmTitle = "Eseguire lo stress test?"
    override val devStressTestConfirmMessage = "⚠️ AVVERTIMENTO: Questo eliminerà permanentemente TUTTI i dati esistenti e li sostituirà con circa 1 milione di record generati.\n\nQuesto processo potrebbe richiedere un minuto."
    override val devWipeAndGenerate = "Cancella e genera"
    override val devGeneratingData = "Generazione dati..."
    override val devHapticsTest = "Test aptico"
    override val devMoveSlider = "Muovi lo slider per sentire vibrazioni diverse"

    // Validation Errors
    override val errorSetsEmpty = "Le serie non possono essere vuote"
    override val errorSetsFormat = "Formato serie non valido"
    override val errorSetsWholeNumber = "Le serie devono essere un numero intero"
    override val errorSetsPositive = "Le serie devono essere maggiori di 0"

    override val errorRepsEmpty = "Le reps non possono essere vuote"
    override val errorRepsFormat = "Formato reps non valido"
    override val errorRepsWholeNumber = "Le reps devono essere un numero intero"
    override val errorRepsPositive = "Le reps devono essere maggiori di 0"

    override val errorWeightEmpty = "Il peso non può essere vuoto"
    override val errorWeightFormat = "Formato peso non valido"
    override val errorWeightInvalid = "Valore peso non valido"
    
    override val labelGoToCurrentMonth = "Vai al mese corrente"
    
    // Graph Screen Translations
    override val labelGraph = "Grafico"
    override val labelNoDataForExercise = "Nessuna cronologia record per questo esercizio ancora"
    override val labelMaxWeight = "Record Personale"
    override val labelCurrentWeight = "Ultimo Peso"
    override val labelProgress = "Progresso"
    override val labelNoExercises = "Nessun esercizio trovato. Crea prima un esercizio!"

    // Unsaved Work Dialog
    override val titleNoName = "Senza Nome"
    override val msgNoName = "Vuoi scartare o inserire un nome e salvare?"
    override val titleUnsavedWork = "Lavoro Non Salvato"
    override val msgUnsavedWork = "Vuoi scartare o salvare?"
    override val labelDiscard = "Scarta"
    override val labelKeepEditing = "Continua A Modificare"
    override val labelEditAction = "Modifica"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Allenamenti"
    override val labelExercises = "Esercizi"
}
