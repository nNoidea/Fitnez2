package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object ArStrings : EnStrings(
    appLocale = Locale.forLanguageTag("ar"),
    languageName = "العربية",
) {
    override val labelSystemLanguage = "لغة النظام"
    override val labelLanguage = "اللغة"

    override val errorExerciseNameBlank = "لا يمكن أن يكون اسم التمرين فارغًا"

    override val errorIdMustBeZero = "يجب أن يكون معرف التمرين الجديد 0"
    override val errorIdMustNotBeZero = "يجب ألا يكون معرف التمرين للتحديث صفرًا"

    override fun errorExerciseAlreadyExists(name: String) = "التمرين بالاسم '$name' موجود بالفعل"
    override fun errorExerciseRenameConflict(name: String) = "اسم التمرين '$name' قيد الاستخدام بالفعل"
    override fun errorExerciseNotFoundById(id: Int) = "التمرين ذو المعرف $id غير موجود"

    override val errorWorkoutNameBlank = "يرجى إدخال اسم"
    override fun errorWorkoutAlreadyExists(name: String) = "التمرين بالاسم '$name' موجود بالفعل"
    override val errorWorkoutNoExercises = "يرجى إضافة تمرين واحد على الأقل"
    override val errorWorkoutEmpty = "التمرين فارغ"

    override val labelAddExercise = "إضافة تمرين"
    override val labelCreateExercise = "إنشاء تمرين"
    override val labelCreateWorkout = "إنشاء تدريب"
    override val labelWorkoutName = "اسم التدريب"
    override val labelWorkout = "تدريب"
    override val labelExercise = "تمرين"
    override val labelAdd = "إضافة"
    override val labelExerciseName = "اسم التمرين"
    override val labelSave = "حفظ"
    override val labelCancel = "إلغاء"
    override val labelClose = "إغلاق"
    override val labelDelete = "حذف"
    override val labelSwitchLanguage = "تغيير اللغة"
    override val labelAiTranslationsDisclaimer = "الترجمات تمت بواسطة الذكاء الاصطناعي"

    override val labelTimeline = "الجدول الزمني"
    override val labelMonthly = "شهريًا"
    override val labelSettings = "الإعدادات"

    override val labelSets = "مجموعات"
    override val labelReps = "تكرارات"
    override val labelWeight = "الوزن"

    override fun labelEdit(target: String) = "تعديل $target"

    override val labelSelectExercise = "اختر شيئًا"
    override val labelWeightUnit = "وحدة الوزن"
    override val labelProgramPlaceholder = "نائب صفحة البرنامج"
    override val labelHistoryListPlaceholder = "نائب قائمة السجل"
    override val labelOpenDrawer = "افتح درج التنقل"
    override val labelHistoryEmpty = "لا يوجد سجل بعد."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "تعديل التمرين"

    override val labelRecordDeleted = "تم حذف السجل"
    override val labelRecordsDeleted = "تم حذف السجلات"
    override val labelUndo = "تراجع"

    override val labelToday = "اليوم"
    override val labelYesterday = "أمس"
    override val labelDeleteExerciseWarning = "سيؤدي هذا الإجراء إلى حذف جميع السجلات ولا يمكن التراجع عنه"
    override val labelDeleteWorkoutWarning = "هل أنت متأكد أنك تريد حذف هذا التدريب؟"
    
    override val labelExerciseNamePlaceholder = "مثال: بنش برس"
    
    override val labelDefaultExerciseValues = "القيم الافتراضية للتمرين"

    override val labelDefaultSets = "المجموعات الافتراضية"
    override val labelDefaultReps = "التكرارات الافتراضية"
    override val labelDefaultWeight = "الوزن الافتراضي"
    
    override val labelBack = "رجوع"
    
    override val labelRotation = "تدوير تلقائي"
    override val labelRotationSystem = "اتبع النظام"
    override val labelRotationOn = "تشغيل"
    override val labelRotationOff = "إيقاف"

    override val labelExportData = "تصدير البيانات"
    override val labelImportData = "استيراد البيانات"
    override val labelExportSuccess = "تم التصدير بنجاح"
    override val labelExportFailed = "فشل التصدير"
    override val labelImportSuccess = "تم الاستيراد بنجاح"
    override val labelImportFailed = "فشل الاستيراد"
    
    override val titleImportWarning = "استبدال البيانات؟"
    override val msgImportWarning = "سيؤدي هذا إلى حذف قاعدة البيانات الحالية نهائيًا واستبدالها بالبيانات المستوردة. لا يمكن التراجع عن هذا الإجراء."
    override val labelConfirm = "تأكيد"
    override val labelDeveloperOptions = "خيارات المطور"
    override val unitKg = "كجم"
    override val unitLb = "رطل"
    override val labelUnknownExercise = "تمرين غير معروف"
    override val labelOlderRecords = "السجلات القديمة"
    override val labelRestDay = "يوم راحة"
    
    override fun labelExercisesCount(count: Int) = when {
        count == 1 -> "تمرين واحد"
        count == 2 -> "تمرينان"
        count in 3..10 -> "$count تمارين"
        else -> "$count تمرينًا"
    }

    // Developer Options
    override val devColorPalette = "لوحة الألوان"
    override val devViewColors = "عرض الألوان"
    override val devDatabase = "قاعدة البيانات"
    override val devRunStressTest = "تشغيل اختبار إجهاد البيانات"
    override val devStressTestDescription = "مسح قاعدة البيانات وإدخال 1 مليون سجل"
    override val devStressTestConfirmTitle = "تشغيل اختبار الإجهاد؟"
    override val devStressTestConfirmMessage = "⚠️ تحذير: سيؤدي هذا إلى حذف جميع البيانات الحالية نهائيًا واستبدالها بحوالي مليون سجل تم إنشاؤه.\n\nقد تستغرق هذه العملية دقيقة."
    override val devWipeAndGenerate = "مسح وإنشاء"
    override val devGeneratingData = "جاري إنشاء البيانات..."
    override val devHapticsTest = "اختبار اللمس"
    override val devMoveSlider = "حرك شريط التمرير للشعور بالاهتزازات المختلفة"

    // Validation Errors
    override val errorSetsEmpty = "لا يمكن أن تكون المجموعات فارغة"
    override val errorSetsFormat = "تنسيق مجموعات غير صالح"
    override val errorSetsWholeNumber = "يجب أن تكون المجموعات عددًا صحيحًا"
    override val errorSetsPositive = "يجب أن تكون المجموعات أكبر من 0"

    override val errorRepsEmpty = "لا يمكن أن تكون التكرارات فارغة"
    override val errorRepsFormat = "تنسيق تكرارات غير صالح"
    override val errorRepsWholeNumber = "يجب أن تكون التكرارات عددًا صحيحًا"
    override val errorRepsPositive = "يجب أن تكون التكرارات أكبر من 0"

    override val errorWeightEmpty = "لا يمكن أن يكون الوزن فارغًا"
    override val errorWeightFormat = "تنسيق وزن غير صالح"
    override val errorWeightInvalid = "قيمة وزن غير صالحة"
    
    override val labelGoToCurrentMonth = "الانتقال إلى الشهر الحالي"
    
    // Graph Screen Translations
    override val labelGraph = "الرسم البياني"
    override val labelNoDataForExercise = "لا يوجد سجل سجلات لهذا التمرين بعد"
    override val labelMaxWeight = "الرقم القياسي الشخصي"
    override val labelCurrentWeight = "الوزن الأخير"
    override val labelProgress = "التقدم"
    override val labelNoExercises = "لم يتم العثور على تمارين. أنشئ تمرينًا أولاً!"

    // Unsaved Work Dialog
    override val titleNoName = "بدون اسم"
    override val msgNoName = "هل تريد التجاهل أم ملء الاسم والحفظ؟"
    override val titleUnsavedWork = "عمل غير محفوظ"
    override val msgUnsavedWork = "هل تريد التجاهل أم الحفظ؟"
    override val labelDiscard = "تجاهل"
    override val labelKeepEditing = "مواصلة التعديل"
    override val labelEditAction = "تعديل"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "التدريبات"
    override val labelExercises = "التمارين"

    // In-App Font Settings
    override val labelInAppFont = "خط التطبيق"
    override val labelFontSystemDefault = "افتراضي النظام"
    override val labelFontGoogleSansFlexRounded = "جوجل سانس فليكس المستدير"
}
