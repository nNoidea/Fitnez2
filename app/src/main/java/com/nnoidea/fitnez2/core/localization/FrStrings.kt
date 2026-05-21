package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object FrStrings : EnStrings(
    appLocale = Locale.forLanguageTag("fr"),
    languageName = "Français",
) {
    override val labelSystemLanguage = "Langue du système"
    override val labelLanguage = "Langue"

    override val errorExerciseNameBlank = "Le nom de l'exercice ne peut pas être vide"

    override val errorIdMustBeZero = "Les nouveaux exercices doivent avoir un ID de 0"
    override val errorIdMustNotBeZero = "Les exercices à mettre à jour doivent avoir un ID non nul"

    override fun errorExerciseAlreadyExists(name: String) = "L'exercice avec le nom '$name' existe déjà"
    override fun errorExerciseRenameConflict(name: String) = "Le nom de l'exercice '$name' est déjà utilisé"
    override fun errorExerciseNotFoundById(id: Int) = "L'exercice avec l'ID $id n'existe pas"

    override val errorWorkoutNameBlank = "Veuillez entrer un nom"
    override fun errorWorkoutAlreadyExists(name: String) = "L'entraînement avec le nom '$name' existe déjà"
    override val errorWorkoutNoExercises = "Veuillez ajouter au moins un exercice"
    override val errorWorkoutEmpty = "L'entraînement est vide"

    override val labelAddExercise = "Ajouter Un Exercice"
    override val labelCreateExercise = "Créer un exercice"
    override val labelCreateWorkout = "Créer un entraînement"
    override val labelWorkoutName = "Nom De L'Entraînement"
    override val labelWorkout = "Entraînement"
    override val labelExercise = "Exercice"
    override val labelAdd = "Ajouter"
    override val labelExerciseName = "Nom De L'Exercice"
    override val labelSave = "Enregistrer"
    override val labelCancel = "Annuler"
    override val labelClose = "Fermer"
    override val labelDelete = "Supprimer"
    override val labelSwitchLanguage = "Changer De Langue"
    override val labelAiTranslationsDisclaimer = "Les traductions sont effectuées par Intelligence Artificielle"

    override val labelTimeline = "Chronologie"
    override val labelMonthly = "Mensuel"
    override val labelSettings = "Paramètres"

    override val labelSets = "Séries"
    override val labelReps = "Reps"
    override val labelWeight = "Poids"

    override fun labelEdit(target: String) = "Modifier $target"

    override val labelSelectExercise = "Sélectionner Quelque Chose"
    override val labelWeightUnit = "Unité De Poids"
    override val labelProgramPlaceholder = "Espace Réservé Pour La Page Du Programme"
    override val labelHistoryListPlaceholder = "Espace Réservé Pour La Liste D'Historique"
    override val labelOpenDrawer = "Ouvrir Le Menu De Navigation"
    override val labelHistoryEmpty = "Pas encore d'historique."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "Modifier l'exercice"

    override val labelRecordDeleted = "Enregistrement supprimé"
    override val labelUndo = "Annuler"

    override val labelToday = "Aujourd'hui"
    override val labelYesterday = "Hier"
    override val labelDeleteExerciseWarning = "Cette action supprimera tous les enregistrements et ne peut pas être annulée"
    override val labelDeleteWorkoutWarning = "Êtes-vous sûr de vouloir supprimer cet entraînement ?"
    
    override val labelExerciseNamePlaceholder = "ex. Développé couché"
    
    override val labelDefaultExerciseValues = "Valeurs Par Défaut De L'Exercice"

    override val labelDefaultSets = "Séries Par Défaut"
    override val labelDefaultReps = "Reps Par Défaut"
    override val labelDefaultWeight = "Poids Par Défaut"
    
    override val labelBack = "Retour"
    
    override val labelRotation = "Rotation automatique"
    override val labelRotationSystem = "Suivre Le Système"
    override val labelRotationOn = "Activé"
    override val labelRotationOff = "Désactivé"

    override val labelExportData = "Exporter Les Données"
    override val labelImportData = "Importer Les Données"
    override val labelExportSuccess = "Exportation Réussie"
    override val labelExportFailed = "Échec De L'Exportation"
    override val labelImportSuccess = "Importation Réussie"
    override val labelImportFailed = "Échec De L'Importation"
    
    override val titleImportWarning = "Remplacer Les Données ?"
    override val msgImportWarning = "Cela supprimera définitivement votre base de données actuelle et la remplacera par les données importées. Cette action ne peut pas être annulée."
    override val labelConfirm = "Confirmer"
    override val labelDeveloperOptions = "Options de développement"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Exercice inconnu"
    override val labelOlderRecords = "Enregistrements plus anciens"
    override val labelRestDay = "Jour de repos"
    override fun labelExercisesCount(count: Int) = if (count == 1) "1 exercice" else "$count exercices"

    // Developer Options
    override val devColorPalette = "Palette de couleurs"
    override val devViewColors = "Voir Les Couleurs"
    override val devDatabase = "Base de données"
    override val devRunStressTest = "Exécuter Le Test De Stress"
    override val devStressTestDescription = "Effacer la BD et insérer 1M d'enregistrements"
    override val devStressTestConfirmTitle = "Exécuter le test de stress ?"
    override val devStressTestConfirmMessage = "⚠️ AVERTISSEMENT : Cela supprimera définitivement TOUTES les données existantes et les remplacera par ~1 million d'enregistrements générés.\n\nCe processus peut prendre une minute."
    override val devWipeAndGenerate = "Effacer et générer"
    override val devGeneratingData = "Génération des données..."
    override val devHapticsTest = "Test de retour haptique"
    override val devMoveSlider = "Déplacez le curseur pour ressentir différentes vibrations"

    // Validation Errors
    override val errorSetsEmpty = "Les séries ne peuvent pas être vides"
    override val errorSetsFormat = "Format de séries invalide"
    override val errorSetsWholeNumber = "Les séries doivent être un nombre entier"
    override val errorSetsPositive = "Les séries doivent être supérieures à 0"

    override val errorRepsEmpty = "Les reps ne peuvent pas être vides"
    override val errorRepsFormat = "Format de reps invalide"
    override val errorRepsWholeNumber = "Les reps doivent être un nombre entier"
    override val errorRepsPositive = "Les reps doivent être supérieures à 0"

    override val errorWeightEmpty = "Le poids ne peut pas être vide"
    override val errorWeightFormat = "Format de poids invalide"
    override val errorWeightInvalid = "Valeur de poids invalide"
    
    override val labelGoToCurrentMonth = "Aller au mois actuel"
    
    // Graph Screen Translations
    override val labelGraph = "Graphique"
    override val labelNoDataForExercise = "Pas encore d'historique d'enregistrement pour cet exercice"
    override val labelMaxWeight = "Record Personnel"
    override val labelCurrentWeight = "Dernier Poids"
    override val labelProgress = "Progression"
    override val labelNoExercises = "Aucun exercice trouvé. Créez d'abord un exercice !"

    // Unsaved Work Dialog
    override val titleNoName = "Sans Nom"
    override val msgNoName = "Voulez-vous abandonner ou entrer un nom et enregistrer ?"
    override val titleUnsavedWork = "Travail Non Enregistré"
    override val msgUnsavedWork = "Voulez-vous abandonner ou enregistrer ?"
    override val labelDiscard = "Abandonner"
    override val labelKeepEditing = "Continuer À Modifier"
    override val labelEditAction = "Modifier"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Entraînements"
    override val labelExercises = "Exercices"

    // In-App Font Settings
    override val labelInAppFont = "Police de l'application"
    override val labelFontSystemDefault = "Par défaut du système"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Arrondi"
}
