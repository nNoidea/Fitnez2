package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object PlStrings : EnStrings(
    appLocale = Locale.forLanguageTag("pl"),
    languageName = "Polski",
) {
    override val labelSystemLanguage = "Język systemowy"
    override val labelLanguage = "Język"

    override val errorExerciseNameBlank = "Nazwa ćwiczenia nie może być pusta"

    override val errorIdMustBeZero = "Nowe ćwiczenia muszą mieć ID 0"

    override fun errorExerciseAlreadyExists(name: String) = "Ćwiczenie o nazwie '$name' już istnieje"
    override fun errorExerciseRenameConflict(name: String) = "Nazwa ćwiczenia '$name' jest już w użyciu"
    override fun errorExerciseNotFoundById(id: String) = "Ćwiczenie o ID $id nie istnieje"

    override val errorWorkoutNameBlank = "Proszę podać nazwę"
    override fun errorWorkoutAlreadyExists(name: String) = "Trening o nazwie '$name' już istnieje"
    override val errorWorkoutNoExercises = "Proszę dodać przynajmniej jedno ćwiczenie"
    override val errorWorkoutEmpty = "Trening jest pusty"
    override val labelCreateExercise = "Utwórz ćwiczenie"
    override val labelWorkoutName = "Nazwa Treningu"
    override val labelWorkout = "Trening"
    override val labelExercise = "Ćwiczenie"
    override val labelAdd = "Dodaj"
    override val labelExerciseName = "Nazwa Ćwiczenia"
    override val labelSave = "Zapisz"
    override val labelCancel = "Anuluj"
    override val labelClose = "Zamknij"
    override val labelDelete = "Usuń"
    override val labelAiTranslationsDisclaimer = "Tłumaczenia zostały wykonane przez Sztuczną Inteligencję"

    override val labelTimeline = "Oś czasu"
    override val labelMonthly = "Miesięcznie"
    override val labelSettings = "Ustawienia"

    override val labelSets = "Serie"
    override val labelReps = "Powtórzenia"

    override fun labelEdit(target: String) = "Edytuj $target"

    override val labelSelectExercise = "Wybierz Coś"
    override val labelWeightUnit = "Jednostka Ciężaru"
    override val labelOpenDrawer = "Otwórz Menu Nawigacji"
    override val labelHistoryEmpty = "Brak historii."
    override val labelAppName = "Fitnez2"
    override val labelEditExercise = "Edytuj ćwiczenie"

    override val labelRecordDeleted = "Rekord usunięty"
    override val labelRecordsDeleted = "Rekordy usunięte"
    override val labelUndo = "Cofnij"

    override val labelToday = "Dzisiaj"
    override val labelYesterday = "Wczoraj"
    override val labelDeleteExerciseWarning = "Ta akcja usunie wszystkie rekordy i nie można jej cofnąć"
    override val labelDeleteWorkoutWarning = "Czy na pewno chcesz usunąć ten trening?"
    
    override val labelExerciseNamePlaceholder = "np. Wyciskanie sztangi"
    
    override val labelDefaultExerciseValues = "Domyślne Wartości Ćwiczenia"

    override val labelDefaultSets = "Domyślne Serie"
    override val labelDefaultReps = "Domyślne Powtórzenia"
    override val labelDefaultWeight = "Domyślny Ciężar"
    
    override val labelBack = "Wstecz"
    
    override val labelRotation = "Auto-obrót"
    override val labelRotationSystem = "Zgodnie Z Systemem"
    override val labelRotationOn = "Wł."
    override val labelRotationOff = "Wył."

    override val labelExportData = "Eksportuj Dane"
    override val labelImportData = "Importuj Dane"
    override val labelExportSuccess = "Eksport Zakończony Sukcesem"
    override val labelExportFailed = "Eksport Nie Powiódł Się"
    override val labelImportSuccess = "Import Zakończony Sukcesem"
    override val labelImportFailed = "Import Nie Powiódł Się"
    
    override val titleImportWarning = "Nadpisać Dane?"
    override val msgImportWarning = "To trwale usunie bieżącą bazę danych i zastąpi ją zaimportowanymi danymi. Tej akcji nie można cofnąć."
    override val labelConfirm = "Potwierdź"
    override val labelDeveloperOptions = "Opcje programisty"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Nieznane ćwiczenie"

    // Developer Options
    override val devColorPalette = "Paleta kolorów"
    override val devViewColors = "Zobacz Kolory"
    override val devDatabase = "Baza danych"
    override val devRunStressTest = "Uruchom Test Obciążeniowy Danych"
    override val devStressTestDescription = "Wyczyść bazę danych i wstaw 1 mln rekordów"
    override val devStressTestConfirmTitle = "Uruchomić test obciążeniowy?"
    override val devStressTestConfirmMessage = "⚠️ OSTRZEŻENIE: To trwale USUNIE WSZYSTKIE istniejące dane i zastąpi je około 1 milionem wygenerowanych rekordów.\n\nTen proces może potrwać minutę."
    override val devWipeAndGenerate = "Wyczyść i wygeneruj"
    override val devGeneratingData = "Generowanie danych..."
    override val devHapticsTest = "Test haptyki"
    override val devMoveSlider = "Przesuń suwak, aby poczuć różne wibracje"
    override val errorSetsPositive = "Serie muszą być większe niż 0"
    override val errorRepsPositive = "Powtórzenia muszą być większe niż 0"
    override val errorWeightInvalid = "Nieprawidłowa wartość ciężaru"
    
    override val labelGoToCurrentMonth = "Idź do bieżącego miesiąca"
    
    // Graph Screen Translations
    override val labelGraph = "Wykres"
    override val labelNoDataForExercise = "Brak historii rekordów dla tego ćwiczenia"
    override val labelNoExercises = "Nie znaleziono ćwiczeń. Najpierw utwórz ćwiczenie!"
    override val labelCompareBy = "Porównaj według"
    override val labelGraphMaxWeight = "Ciężar"
    override val labelGraphVolume = "Objętość"

    // Unsaved Work Dialog
    override val titleNoName = "Brak Nazwy"
    override val msgNoName = "Czy chcesz odrzucić, czy wpisać nazwę i zapisać?"
    override val titleUnsavedWork = "Niezapisane Zmiany"
    override val msgUnsavedWork = "Czy chcesz odrzucić, czy zapisać?"
    override val labelDiscard = "Odrzuć"
    override val labelEditAction = "Edytuj"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Treningi"
    override val labelExercises = "Ćwiczenia"

    // In-App Font Settings
    override val labelInAppFont = "Czcionka w aplikacji"
    override val labelFontSystemDefault = "Domyślna systemu"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Zaokrąglona"
}
