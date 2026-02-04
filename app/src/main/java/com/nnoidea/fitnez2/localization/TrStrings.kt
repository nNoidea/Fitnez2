package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object TrStrings : EnStrings(
    appLocale = Locale.forLanguageTag("tr"),
    languageName = "Türkçe",
) {
    override val labelSystemLanguage = "Sistem Dili" 
    override val labelLanguage = "Dil"

    override val errorExerciseNameBlank = "Egzersiz adı boş olamaz"
    override val errorExerciseAlreadyExists = "Bu isimde bir egzersiz zaten mevcut."
    override val errorExerciseRenameConflict = "Bu isim başka bir egzersiz tarafından kullanılıyor."
    override val errorExerciseNotFound = "Egzersiz bulunamadı."

    override val errorIdMustBeZero = "Yeni egzersizler için ID 0 olmalıdır. Varolan egzersizler için update() kullanın."
    override val errorIdMustNotBeZero = "Güncellenecek kayıtların ID'si sıfırdan farklı olmalıdır. Yeni egzersizler için create() kullanın."

    override val errorSetsInputInvalid = "Setler negatif olamaz ve en fazla 2 ondalık basamaklı olabilir."
    override val errorRepsInputInvalid = "Tekrarlar negatif olamaz ve en fazla 2 ondalık basamaklı olabilir."

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
    override val labelReps = "Tekrarlar"
    override val labelWeight = "Ağırlık"

    override val labelBottomSheetTitle = "Özel Kaydırılabilir Sayfa"
    override val labelBottomSheetDesc = "Animasyon üzerinde tam kontrol."
    override val labelSayHello = "Merhaba De"
    override val labelHelloTitle = "Merhaba"
    override val labelHelloText = "Selamlar!"
    override val labelOkay = "Tamam"

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
}
