package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object RuStrings : EnStrings(
    appLocale = Locale.forLanguageTag("ru"),
    languageName = "Русский",
) {
    override val labelSystemLanguage = "Системный язык"
    override val labelLanguage = "Язык"

    override val errorExerciseNameBlank = "Название упражнения не может быть пустым"

    override val errorIdMustBeZero = "Новые упражнения должны иметь ID 0"

    override fun errorExerciseAlreadyExists(name: String) = "Упражнение с именем '$name' уже существует"
    override fun errorExerciseRenameConflict(name: String) = "Название упражнения '$name' уже используется"
    override fun errorExerciseNotFoundById(id: String) = "Упражнение с ID $id не существует"

    override val errorWorkoutNameBlank = "Пожалуйста, введите название"
    override fun errorWorkoutAlreadyExists(name: String) = "Тренировка с именем '$name' уже существует"
    override val errorWorkoutNoExercises = "Пожалуйста, добавьте хотя бы одно упражнение"
    override val errorWorkoutEmpty = "Тренировка пуста"
    override val labelCreateExercise = "Создать упражнение"
    override val labelWorkoutName = "Название Тренировки"
    override val labelWorkout = "Тренировка"
    override val labelExercise = "Упражнение"
    override val labelAdd = "Добавить"
    override val labelExerciseName = "Название Упражнения"
    override val labelSave = "Сохранить"
    override val labelCancel = "Отмена"
    override val labelClose = "Закрыть"
    override val labelDelete = "Удалить"
    override val labelAiTranslationsDisclaimer = "Переводы выполнены искусственным интеллектом"

    override val labelTimeline = "Хронология"
    override val labelMonthly = "Ежемесячно"
    override val labelSettings = "Настройки"

    override val labelSets = "Подходы"
    override val labelReps = "Повторы"

    override fun labelEdit(target: String) = "Редактировать $target"

    override val labelSelectExercise = "Выберите Что-нибудь"
    override val labelWeightUnit = "Единица Веса"
    override val labelOpenDrawer = "Открыть Панель Навигации"
    override val labelHistoryEmpty = "Истории пока нет."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "Редактировать упражнение"

    override val labelRecordDeleted = "Запись удалена"
    override val labelRecordsDeleted = "Записи удалены"
    override val labelUndo = "Отменить"

    override val labelToday = "Сегодня"
    override val labelYesterday = "Вчера"
    override val labelDeleteExerciseWarning = "Это действие удалит все записи и его нельзя отменить"
    override val labelDeleteWorkoutWarning = "Вы уверены, что хотите удалить эту тренировку?"
    
    override val labelExerciseNamePlaceholder = "например, Жим лежа"
    
    override val labelDefaultExerciseValues = "Значения Упражнения По Умолчанию"

    override val labelDefaultSets = "Подходы По Умолчанию"
    override val labelDefaultReps = "Повторы По Умолчанию"
    override val labelDefaultWeight = "Вес По Умолчанию"
    
    override val labelBack = "Назад"
    
    override val labelRotation = "Автоповорот"
    override val labelRotationSystem = "Как В Системе"
    override val labelRotationOn = "Вкл"
    override val labelRotationOff = "Выкл"

    override val labelExportData = "Экспорт Данных"
    override val labelImportData = "Импорт Данных"
    override val labelExportSuccess = "Экспорт Успешен"
    override val labelExportFailed = "Ошибка Экспорта"
    override val labelImportSuccess = "Импорт Успешен"
    override val labelImportFailed = "Ошибка Импорта"
    
    override val titleImportWarning = "Перезаписать Данные?"
    override val msgImportWarning = "Это навсегда удалит вашу текущую базу данных и заменит ее импортированными данными. Это действие нельзя отменить."
    override val labelConfirm = "Подтвердить"
    override val labelDeveloperOptions = "Параметры разработчика"
    override val unitKg = "кг"
    override val unitLb = "фунт"
    override val labelUnknownExercise = "Неизвестное упражнение"
    override val labelOlderRecords = "Более старые записи"

    // Developer Options
    override val devColorPalette = "Цветовая палитра"
    override val devViewColors = "Просмотр Цветов"
    override val devDatabase = "База данных"
    override val devRunStressTest = "Запустить Стресс-Тест Данных"
    override val devStressTestDescription = "Очистить БД и вставить 1 млн записей"
    override val devStressTestConfirmTitle = "Запустить стресс-тест?"
    override val devStressTestConfirmMessage = "⚠️ ПРЕДУПРЕЖДЕНИЕ: Это навсегда УДАЛИТ ВСЕ существующие данные и заменит их примерно 1 миллионом сгенерированных записей.\n\nЭтот процесс может занять минуту."
    override val devWipeAndGenerate = "Очистить и сгенерировать"
    override val devGeneratingData = "Генерация данных..."
    override val devHapticsTest = "Тест виброотклика"
    override val devMoveSlider = "Перемещайте ползунок, чтобы почувствовать разные вибрации"

    // Validation Errors
    override val errorSetsPositive = "Подходы должны быть больше 0"
    override val errorRepsPositive = "Повторы должны быть больше 0"
    override val errorWeightInvalid = "Неверное значение веса"
    
    override val labelGoToCurrentMonth = "Перейти к текущему месяцу"
    
    // Graph Screen Translations
    override val labelGraph = "График"
    override val labelNoDataForExercise = "Истории записей для этого упражнения пока нет"
    override val labelNoExercises = "Упражнения не найдены. Сначала создайте упражнение!"
    override val labelCompareBy = "Сравнить по"
    override val labelGraphMaxWeight = "Вес"
    override val labelGraphVolume = "Объём"

    // Unsaved Work Dialog
    override val titleNoName = "Без Названия"
    override val msgNoName = "Хотите закрыть или ввести название и сохранить?"
    override val titleUnsavedWork = "Несохраненная Работа"
    override val msgUnsavedWork = "Хотите закрыть или сохранить?"
    override val labelDiscard = "Отменить"
    override val labelEditAction = "Редактировать"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Тренировки"
    override val labelExercises = "Упражнения"

    // In-App Font Settings
    override val labelInAppFont = "Шрифт приложения"
    override val labelFontSystemDefault = "Системный по умолчанию"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Округлый"
}
