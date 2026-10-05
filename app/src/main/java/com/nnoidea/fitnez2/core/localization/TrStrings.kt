package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object TrStrings : EnStrings(
    appLocale = Locale.forLanguageTag("tr"),
    languageName = "Türkçe",
) {
    override val labelSystemLanguage = "Sistem Dili" 
    override val labelLanguage = "Dil"

    override val errorExerciseNameBlank = "Egzersiz adı boş olamaz"

    override val errorIdMustBeZero = "Yeni egzersizler için ID 0 olmalıdır. Varolan egzersizler için update() kullanın."

    override fun errorExerciseAlreadyExists(name: String) = "'$name' adında bir egzersiz zaten mevcut."
    override fun errorExerciseRenameConflict(name: String) = "'$name' ismi başka bir egzersiz tarafından kullanılıyor."
    override fun errorExerciseNotFoundById(id: String) = "$id ID'li egzersiz bulunamadı."

    override val errorWorkoutNameBlank = "Lütfen bir isim girin"
    override fun errorWorkoutAlreadyExists(name: String) = "'$name' adında bir antrenman zaten mevcut."
    override val errorWorkoutNoExercises = "Lütfen en az bir egzersiz ekleyin"
    override val errorWorkoutEmpty = "Antrenman boş"
    override val labelCreateExercise = "Egzersiz Oluştur"
    override val labelWorkoutName = "Antrenman Adı"
    override val labelWorkout = "Antrenman"
    override val labelExercise = "Egzersiz"
    override val labelAdd = "Ekle"
    override val labelExerciseName = "Egzersiz Adı"
    override val labelSave = "Kaydet"
    override val labelCancel = "İptal"
    override val labelClose = "Kapat"
    override val labelDelete = "Sil"
    override val labelAiTranslationsDisclaimer = "Çeviriler Yapay Zeka tarafından yapılmıştır"

    override val labelTimeline = "Zaman Tüneli"
    override val labelMonthly = "Aylık"
    override val labelSettings = "Ayarlar"

    override val labelSets = "Setler"
    override val labelReps = "Tekrar"

    override fun labelEdit(target: String): String = "$target Düzenle"

    override val labelSelectExercise: String = "Bir Şey Seçin"
    override val labelWeightUnit: String = "Ağırlık Birimi"
    override val labelOpenDrawer: String = "Navigasyon Menüsünü Aç"
    override val labelHistoryEmpty: String = "Henüz geçmiş yok."
    override val labelAppName: String = "Fitnez2" // Usually brand names don't change, but good to have control
    override val labelEditExercise: String = "Egzersizi Düzenle"

    override val labelRecordDeleted: String = "Kayıt silindi"
    override val labelRecordsDeleted: String = "Kayıtlar silindi"
    override val labelUndo: String = "Geri Al"

    override val labelToday: String = "Bugün"
    override val labelYesterday: String = "Dün"
    override val labelDeleteExerciseWarning = "Bu işlem tüm kayıtları silecek ve geri alınamaz"
    override val labelDeleteWorkoutWarning = "Bu antrenmanı silmek istediğinizden emin misiniz?"

    override val labelExerciseNamePlaceholder: String = "örn. Bench Press"

    override val labelDefaultExerciseValues: String = "Varsayılan Egzersiz Değerleri"

    override val labelDefaultSets: String = "Varsayılan Set"
    override val labelDefaultReps: String = "Varsayılan Tekrar"
    override val labelDefaultWeight: String = "Varsayılan Ağırlık"
    
    override val labelBack: String = "Geri"

    override val labelRotation: String = "Otomatik Döndürme"
    override val labelRotationSystem: String = "Sistemi İzle"
    override val labelRotationOn: String = "Açık"
    override val labelRotationOff: String = "Kapalı"

    override val labelExportData = "Veriyi Dışa Aktar"
    override val labelImportData = "Veriyi İçe Aktar"
    override val labelExportSuccess = "Dışa Aktarma Başarılı"
    override val labelExportFailed = "Dışa Aktarma Başarısız"
    override val labelImportSuccess = "İçe Aktarma Başarılı"
    override val labelImportFailed = "İçe Aktarma Başarısız"

    override val titleImportWarning = "Verilerin Üzerine Yazılsın Mı?"
    override val msgImportWarning = "Bu işlem mevcut veritabanınızı kalıcı olarak silip yerine içe aktarılan verileri koyacaktır. Bu işlem geri alınamaz."
    override val labelConfirm = "Onayla"
    override val labelDeveloperOptions = "Geliştirici Seçenekleri"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Bilinmeyen Egzersiz"

    // Developer Options
    override val devColorPalette = "Renk Paleti"
    override val devViewColors = "Renkleri Görüntüle"
    override val devDatabase = "Veritabanı"
    override val devRunStressTest = "Stres Testi Çalıştır"
    override val devStressTestDescription = "DB'yi Sil & 1M Kayıt Ekle"
    override val devStressTestConfirmTitle = "Stres Testi Çalıştırılsın Mı?"
    override val devStressTestConfirmMessage = "⚠️ UYARI: Bu işlem mevcut tüm verileri (egzersizler ve kayıtlar) kalıcı olarak SİLECEK ve yerine ~1 milyon rastgele kayıt (2000-2025) ekleyecektir.\n\nBu işlem bir dakika sürebilir."
    override val devWipeAndGenerate = "Sil ve Oluştur"
    override val devGeneratingData = "Veri Oluşturuluyor..."
    override val devHapticsTest = "Dokunsal Test"
    override val devMoveSlider = "Farklı titreşimleri hissetmek için kaydırıcıyı hareket ettirin"

    // Validation Errors
    override val errorSetsPositive = "Setler 0'dan büyük olmalıdır"
    override val errorRepsPositive = "Tekrarlar 0'dan büyük olmalıdır"
    override val errorWeightInvalid = "Geçersiz ağırlık değeri"
    
    override val labelGoToCurrentMonth = "Mevcut aya git"
    
    // Graph Screen Translations
    override val labelGraph = "Grafik"
    override val labelNoDataForExercise = "Bu egzersiz için henüz kayıt geçmişi yok"
    override val labelNoExercises = "Egzersiz bulunamadı. Önce bir egzersiz oluşturun!"
    override val labelCompareBy = "Karşılaştırma ölçütü"
    override val labelGraphMaxWeight = "Ağırlık"
    override val labelGraphVolume = "Hacim"

    // Unsaved Work Dialog
    override val titleNoName = "İsim Yok"
    override val msgNoName = "Vazgeçmek mi yoksa bir isim girip kaydetmek mi istersiniz?"
    override val titleUnsavedWork = "Kaydedilmemiş Çalışma"
    override val msgUnsavedWork = "Vazgeçmek mi yoksa kaydetmek mi istersiniz?"
    override val labelDiscard = "Vazgeç"
    override val labelEditAction = "Düzenle"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Antrenmanlar"
    override val labelExercises = "Egzersizler"

    // In-App Font Settings
    override val labelInAppFont = "Uygulama İçi Yazı Tipi"
    override val labelFontSystemDefault = "Sistem Varsayılanı"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Oval"
}
