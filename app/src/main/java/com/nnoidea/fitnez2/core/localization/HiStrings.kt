package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object HiStrings : EnStrings(
    appLocale = Locale.forLanguageTag("hi"),
    languageName = "हिन्दी",
) {
    override val labelSystemLanguage = "सिस्टम की भाषा"
    override val labelLanguage = "भाषा"

    override val errorExerciseNameBlank = "व्यायाम का नाम खाली नहीं हो सकता"

    override val errorIdMustBeZero = "नए व्यायाम का ID 0 होना चाहिए"
    override val errorIdMustNotBeZero = "अपडेट करने के लिए व्यायाम का ID शून्य नहीं होना चाहिए"

    override fun errorExerciseAlreadyExists(name: String) = "'$name' नाम का व्यायाम पहले से मौजूद है"
    override fun errorExerciseRenameConflict(name: String) = "व्यायाम का नाम '$name' पहले से उपयोग में है"
    override fun errorExerciseNotFoundById(id: String) = "ID $id वाला व्यायाम मौजूद नहीं है"

    override val errorWorkoutNameBlank = "कृपया एक नाम भरें"
    override fun errorWorkoutAlreadyExists(name: String) = "'$name' नाम का वर्कआउट पहले से मौजूद है"
    override val errorWorkoutNoExercises = "कृपया कम से कम एक व्यायाम जोड़ें"
    override val errorWorkoutEmpty = "वर्कआउट खाली है"

    override val labelAddExercise = "व्यायाम जोड़ें"
    override val labelCreateExercise = "व्यायाम बनाएं"
    override val labelCreateWorkout = "वर्कआउट बनाएं"
    override val labelWorkoutName = "वर्कआउट का नाम"
    override val labelWorkout = "वर्कआउट"
    override val labelExercise = "व्यायाम"
    override val labelAdd = "जोड़ें"
    override val labelExerciseName = "व्यायाम का नाम"
    override val labelSave = "सहेजें"
    override val labelCancel = "रद्द करें"
    override val labelClose = "बंद करें"
    override val labelDelete = "हटाएं"
    override val labelSwitchLanguage = "भाषा बदलें"
    override val labelAiTranslationsDisclaimer = "अनुवाद आर्टिफिशियल इंटेलिजेंस द्वारा किए गए हैं"

    override val labelTimeline = "टाइमलाइन"
    override val labelMonthly = "मासिक"
    override val labelSettings = "सेटिंग्स"

    override val labelSets = "सेट"
    override val labelReps = "रेप्स"
    override val labelWeight = "वजन"

    override fun labelEdit(target: String) = "$target संपादित करें"

    override val labelSelectExercise = "कुछ चुनें"
    override val labelWeightUnit = "वजन की इकाई"
    override val labelProgramPlaceholder = "प्रोग्राम पेज प्लेसहोल्डर"
    override val labelHistoryListPlaceholder = "इतिहास सूची प्लेसहोल्डर"
    override val labelOpenDrawer = "नेविगेशन ड्रॉअर खोलें"
    override val labelHistoryEmpty = "अभी तक कोई इतिहास नहीं है।"
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "व्यायाम संपादित करें"

    override val labelRecordDeleted = "रिकॉर्ड हटाया गया"
    override val labelRecordsDeleted = "रिकॉर्ड हटाए गए"
    override val labelUndo = "पूर्ववत करें"

    override val labelToday = "आज"
    override val labelYesterday = "कल"
    override val labelDeleteExerciseWarning = "यह क्रिया सभी रिकॉर्ड मिटा देगी और इसे पूर्ववत नहीं किया जा सकता"
    override val labelDeleteWorkoutWarning = "क्या आप वाकई इस वर्कआउट को हटाना चाहते हैं?"
    
    override val labelExerciseNamePlaceholder = "जैसे: बेंच प्रेस"
    
    override val labelDefaultExerciseValues = "डिफ़ॉल्ट व्यायाम मान"

    override val labelDefaultSets = "डिफ़ॉल्ट सेट"
    override val labelDefaultReps = "डिफ़ॉल्ट रेप्स"
    override val labelDefaultWeight = "डिफ़ॉल्ट वजन"
    
    override val labelBack = "पीछे"
    
    override val labelRotation = "ऑटो-रोटेट"
    override val labelRotationSystem = "सिस्टम का पालन करें"
    override val labelRotationOn = "चालू"
    override val labelRotationOff = "बंद"

    override val labelExportData = "डेटा निर्यात करें"
    override val labelImportData = "डेटा आयात करें"
    override val labelExportSuccess = "निर्यात सफल"
    override val labelExportFailed = "निर्यात विफल"
    override val labelImportSuccess = "आयात सफल"
    override val labelImportFailed = "आयात विफल"
    
    override val titleImportWarning = "डेटा अधिलेखित करें?"
    override val msgImportWarning = "यह आपके वर्तमान डेटाबेस को स्थायी रूप से हटा देगा और इसे आयातित डेटा से बदल देगा। इस क्रिया को पूर्ववत नहीं किया जा सकता।"
    override val labelConfirm = "पुष्टि करें"
    override val labelDeveloperOptions = "डेवलपर विकल्प"
    override val unitKg = "किग्रा"
    override val unitLb = "पाउंड"
    override val labelUnknownExercise = "अज्ञात व्यायाम"
    override val labelOlderRecords = "पुराने रिकॉर्ड"
    override val labelRestDay = "आराम का दिन"
    override fun labelExercisesCount(count: Int) = if (count == 1) "1 व्यायाम" else "$count व्यायाम"

    // Developer Options
    override val devColorPalette = "रंग पट्टिका"
    override val devViewColors = "रंग देखें"
    override val devDatabase = "डेटाबेस"
    override val devRunStressTest = "डेटा तनाव परीक्षण चलाएं"
    override val devStressTestDescription = "DB मिटाएं और 1M रिकॉर्ड डालें"
    override val devStressTestConfirmTitle = "तनाव परीक्षण चलाएं?"
    override val devStressTestConfirmMessage = "⚠️ चेतावनी: यह स्थायी रूप से सभी मौजूदा डेटा को हटा देगा और इसे ~1 मिलियन उत्पन्न रिकॉर्ड से बदल देगा।\n\nइस प्रक्रिया में एक मिनट लग सकता है।"
    override val devWipeAndGenerate = "मिटाएं और उत्पन्न करें"
    override val devGeneratingData = "डेटा उत्पन्न किया जा रहा है..."
    override val devHapticsTest = "हैप्टिक्स टेस्ट"
    override val devMoveSlider = "विभिन्न कंपनों को महसूस करने के लिए स्लाइडर को हिलाएं"

    // Validation Errors
    override val errorSetsEmpty = "सेट खाली नहीं हो सकते"
    override val errorSetsFormat = "अमान्य सेट प्रारूप"
    override val errorSetsWholeNumber = "सेट एक पूर्ण संख्या होनी चाहिए"
    override val errorSetsPositive = "सेट 0 से अधिक होने चाहिए"

    override val errorRepsEmpty = "रेप्स खाली नहीं हो सकते"
    override val errorRepsFormat = "अमान्य रेप्स प्रारूप"
    override val errorRepsWholeNumber = "रेप्स एक पूर्ण संख्या होनी चाहिए"
    override val errorRepsPositive = "रेप्स 0 से अधिक होने चाहिए"

    override val errorWeightEmpty = "वजन खाली नहीं हो सकता"
    override val errorWeightFormat = "अमान्य वजन प्रारूप"
    override val errorWeightInvalid = "अमान्य वजन मान"
    
    override val labelGoToCurrentMonth = "वर्तमान महीने पर जाएं"
    
    // Graph Screen Translations
    override val labelGraph = "ग्राफ"
    override val labelNoDataForExercise = "इस व्यायाम के लिए अभी तक कोई रिकॉर्ड इतिहास नहीं है"
    override val labelMaxWeight = "व्यक्तिगत रिकॉर्ड"
    override val labelCurrentWeight = "नवीनतम वजन"
    override val labelProgress = "प्रगति"
    override val labelNoExercises = "कोई व्यायाम नहीं मिला। पहले एक व्यायाम बनाएं!"

    // Unsaved Work Dialog
    override val titleNoName = "कोई नाम नहीं"
    override val msgNoName = "क्या आप छोड़ना चाहते हैं या नाम भरकर सहेजना चाहते हैं?"
    override val titleUnsavedWork = "असुरक्षित कार्य"
    override val msgUnsavedWork = "क्या आप छोड़ना चाहते हैं या सहेजना चाहते हैं?"
    override val labelDiscard = "खारिज करें"
    override val labelKeepEditing = "संपादन जारी रखें"
    override val labelEditAction = "संपादित करें"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "वर्कआउट"
    override val labelExercises = "व्यायाम"

    // In-App Font Settings
    override val labelInAppFont = "ऐप फ़ॉन्ट"
    override val labelFontSystemDefault = "सिस्टम डिफ़ॉल्ट"
    override val labelFontGoogleSansFlexRounded = "गूगल सेन्स फ्लेक्स राउंडेड"
}
