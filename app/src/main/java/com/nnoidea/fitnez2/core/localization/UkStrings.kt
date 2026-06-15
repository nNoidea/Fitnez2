package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object UkStrings : EnStrings(
    appLocale = Locale.forLanguageTag("uk"),
    languageName = "Українська",
) {
    override val labelSystemLanguage = "Системна мова"
    override val labelLanguage = "Мова"

    override val errorExerciseNameBlank = "Назва вправи не може бути порожньою"

    override val errorIdMustBeZero = "Нові вправи повинні мати ID 0"
    override val errorIdMustNotBeZero = "Записи для оновлення повинні мати ненульовий ID"

    override fun errorExerciseAlreadyExists(name: String) = "Вправа з назвою '$name' вже існує"
    override fun errorExerciseRenameConflict(name: String) = "Назва вправи '$name' вже використовується"
    override fun errorExerciseNotFoundById(id: String) = "Вправа з ID $id не існує"

    override val errorWorkoutNameBlank = "Будь ласка, введіть назву"
    override fun errorWorkoutAlreadyExists(name: String) = "Тренування з назвою '$name' вже існує"
    override val errorWorkoutNoExercises = "Будь ласка, додайте хоча б одну вправу"
    override val errorWorkoutEmpty = "Тренування порожнє"

    override val labelAddExercise = "Додати Вправу"
    override val labelCreateExercise = "Створити вправу"
    override val labelCreateWorkout = "Створити тренування"
    override val labelWorkoutName = "Назва Тренування"
    override val labelWorkout = "Тренування"
    override val labelExercise = "Вправа"
    override val labelAdd = "Додати"
    override val labelExerciseName = "Назва Вправи"
    override val labelSave = "Зберегти"
    override val labelCancel = "Скасувати"
    override val labelClose = "Закрити"
    override val labelDelete = "Видалити"
    override val labelSwitchLanguage = "Змінити Мову"
    override val labelAiTranslationsDisclaimer = "Переклади виконані штучним інтелектом"
    
    override val labelTimeline = "Хронологія"
    override val labelMonthly = "Щомісяця"
    override val labelSettings = "Налаштування"

    override val labelSets = "Підходи"
    override val labelReps = "Повтори"
    override val labelWeight = "Вага"

    override fun labelEdit(target: String) = "Редагувати $target"

    override val labelSelectExercise = "Виберіть Що-небудь"
    override val labelWeightUnit = "Одиниця Ваги"
    override val labelProgramPlaceholder = "Заглушка Сторінки Програми"
    override val labelHistoryListPlaceholder = "Заглушка Списку Історії"
    override val labelOpenDrawer = "Відкрити Панель Навігації"
    override val labelHistoryEmpty = "Історії ще немає."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "Редагувати вправу"

    override val labelRecordDeleted = "Запис видалено"
    override val labelRecordsDeleted = "Записи видалено"
    override val labelUndo = "Скасувати"

    override val labelToday = "Сьогодні"
    override val labelYesterday = "Вчора"
    override val labelDeleteExerciseWarning = "Ця дія видалить всі записи і її не можна скасувати"
    override val labelDeleteWorkoutWarning = "Ви впевнені, що хочете видалити це тренування?"
    
    override val labelExerciseNamePlaceholder = "наприклад, Жим лежачи"
    
    override val labelDefaultExerciseValues = "Значення Вправи За Замовчуванням"

    override val labelDefaultSets = "Підходи За Замовчуванням"
    override val labelDefaultReps = "Повтори За Замовчуванням"
    override val labelDefaultWeight = "Вага За Замовчуванням"
    
    override val labelBack = "Назад"
    
    override val labelRotation = "Автоповорот"
    override val labelRotationSystem = "Як У Системі"
    override val labelRotationOn = "Увімк"
    override val labelRotationOff = "Вимк"

    override val labelExportData = "Експорт Даних"
    override val labelImportData = "Імпорт Даних"
    override val labelExportSuccess = "Експорт Успішний"
    override val labelExportFailed = "Помилка Експорту"
    override val labelImportSuccess = "Імпорт Успішний"
    override val labelImportFailed = "Помилка Імпорту"
    
    override val titleImportWarning = "Перезаписати Дані?"
    override val msgImportWarning = "Це назавжди видалить вашу поточну базу даних і замінить її імпортованими даними. Цю дію не можна скасувати."
    override val labelConfirm = "Підтвердити"
    override val labelDeveloperOptions = "Параметри розробника"
    override val unitKg = "кг"
    override val unitLb = "фунт"
    override val labelUnknownExercise = "Невідома вправа"
    override val labelOlderRecords = "Старіші записи"
    override val labelRestDay = "День відпочинку"
    
    override fun labelExercisesCount(count: Int) = when {
        count % 100 in 11..14 -> "$count вправ"
        count % 10 == 1 -> "$count вправа"
        count % 10 in 2..4 -> "$count вправи"
        else -> "$count вправ"
    }

    // Developer Options
    override val devColorPalette = "Колірна палітра"
    override val devViewColors = "Перегляд Кольорів"
    override val devDatabase = "База даних"
    override val devRunStressTest = "Запустити Стрес-Тест Даних"
    override val devStressTestDescription = "Очистити БД і вставити 1 млн записів"
    override val devStressTestConfirmTitle = "Запустити стрес-тест?"
    override val devStressTestConfirmMessage = "⚠️ ПОПЕРЕДЖЕННЯ: Це назавжди ВИДАЛИТЬ ВСІ існуючі дані і замінить їх приблизно 1 мільйоном згенерованих записів.\n\nЦей процес може зайняти хвилину."
    override val devWipeAndGenerate = "Очистити і згенерувати"
    override val devGeneratingData = "Генерація даних..."
    override val devHapticsTest = "Тест вібровідгуку"
    override val devMoveSlider = "Переміщуйте повзунок, щоб відчути різні вібрації"

    // Validation Errors
    override val errorSetsEmpty = "Підходи не можуть бути порожніми"
    override val errorSetsFormat = "Невірний формат підходів"
    override val errorSetsWholeNumber = "Підходи повинні бути цілим числом"
    override val errorSetsPositive = "Підходи повинні бути більшими за 0"

    override val errorRepsEmpty = "Повтори не можуть бути порожніми"
    override val errorRepsFormat = "Невірний формат повторів"
    override val errorRepsWholeNumber = "Повтори повинні бути цілим числом"
    override val errorRepsPositive = "Повтори повинні бути більшими за 0"

    override val errorWeightEmpty = "Вага не може бути порожньою"
    override val errorWeightFormat = "Невірний формат ваги"
    override val errorWeightInvalid = "Невірне значення ваги"
    
    override val labelGoToCurrentMonth = "Перейти до поточного місяця"
    
    // Graph Screen Translations
    override val labelGraph = "Графік"
    override val labelNoDataForExercise = "Історії записів для цієї вправи ще немає"
    override val labelMaxWeight = "Особистий Рекорд"
    override val labelCurrentWeight = "Остання Вага"
    override val labelProgress = "Прогрес"
    override val labelNoExercises = "Вправи не знайдені. Спочатку створіть вправу!"

    // Unsaved Work Dialog
    override val titleNoName = "Без Назви"
    override val msgNoName = "Хочете закрити чи ввести назву і зберегти?"
    override val titleUnsavedWork = "Незбережена Робота"
    override val msgUnsavedWork = "Хочете закрити чи зберегти?"
    override val labelDiscard = "Скасувати"
    override val labelKeepEditing = "Продовжити Редагування"
    override val labelEditAction = "Редагувати"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Тренування"
    override val labelExercises = "Вправи"

    // In-App Font Settings
    override val labelInAppFont = "Шрифт програми"
    override val labelFontSystemDefault = "Системний за замовчуванням"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Закруглений"
}
