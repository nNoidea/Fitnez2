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
    override val errorIdMustNotBeZero = "Güncellenecek kayıtların ID'si sıfırdan farklı olmalıdır. Yeni egzersizler için create() kullanın."

    override fun errorExerciseAlreadyExists(name: String) = "'$name' adında bir egzersiz zaten mevcut."
    override fun errorExerciseRenameConflict(name: String) = "'$name' ismi başka bir egzersiz tarafından kullanılıyor."
    override fun errorExerciseNotFoundById(id: Int) = "$id ID'li egzersiz bulunamadı."

    override val labelAddExercise = "Egzersiz Ekle"
    override val labelCreateExercise = "Egzersiz Oluştur"
    override val labelAdd = "Ekle"
    override val labelExerciseName = "Egzersiz Adı"
    override val labelSave = "Kaydet"
    override val labelCancel = "İptal"
    override val labelDelete = "Sil"
    override val labelSwitchLanguage = "Dili Değiştir"

    override val labelHome = "Ana Sayfa"
    override val labelProgram = "Programlar"
    override val labelSettings = "Ayarlar"

    override val labelSets = "Setler"
    override val labelReps = "Tekrar"
    override val labelWeight = "Ağırlık"

    override fun labelEdit(target: String): String = "$target Düzenle"

    override val labelSelectExercise: String = "Egzersiz Adı"
    override val labelWeightUnit: String = "Ağırlık Birimi"
    override val labelProgramPlaceholder: String = "Program Sayfası Yer Tutucusu"
    override val labelHistoryListPlaceholder: String = "Geçmiş Listesi Yer Tutucusu"
    override val labelOpenDrawer: String = "Navigasyon Menüsünü Aç"
    override val labelHistoryEmpty: String = "Henüz geçmiş yok."
    override val labelAppName: String = "Fitnez2" // Usually brand names don't change, but good to have control
    override val labelVersion: String = "1.0.0"
    override val labelEditExercise: String = "Egzersizi Düzenle"

    override fun labelWeightWithUnit(unit: String): String = "$labelWeight ($unit)"

    override val labelRecordDeleted: String = "Kayıt silindi"
    override val labelUndo: String = "Geri Al"

    override val labelToday: String = "Bugün"
    override val labelYesterday: String = "Dün"
    override val labelDeleteExerciseWarning = "Bu işlem tüm kayıtları silecek ve geri alınamaz"

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

    // Validation Errors
    override val errorSetsEmpty = "Setler boş olamaz"
    override val errorSetsFormat = "Geçersiz set formatı"
    override val errorSetsWholeNumber = "Setler tam sayı olmalıdır"
    override val errorSetsPositive = "Setler 0'dan büyük olmalıdır"

    override val errorRepsEmpty = "Tekrarlar boş olamaz"
    override val errorRepsFormat = "Geçersiz tekrar formatı"
    override val errorRepsWholeNumber = "Tekrarlar tam sayı olmalıdır"
    override val errorRepsPositive = "Tekrarlar 0'dan büyük olmalıdır"

    override val errorWeightEmpty = "Ağırlık boş olamaz"
    override val errorWeightFormat = "Geçersiz ağırlık formatı"
    override val errorWeightInvalid = "Geçersiz ağırlık değeri"
}
