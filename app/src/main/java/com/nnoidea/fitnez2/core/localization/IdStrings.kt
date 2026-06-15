package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object IdStrings : EnStrings(
    appLocale = Locale.forLanguageTag("id"),
    languageName = "Bahasa Indonesia",
) {
    override val labelSystemLanguage = "Bahasa Sistem"
    override val labelLanguage = "Bahasa"

    override val errorExerciseNameBlank = "Nama latihan tidak boleh kosong"

    override val errorIdMustBeZero = "Latihan baru harus memiliki ID 0"
    override val errorIdMustNotBeZero = "Rekor yang akan diperbarui harus memiliki ID bukan nol"

    override fun errorExerciseAlreadyExists(name: String) = "Latihan dengan nama '$name' sudah ada"
    override fun errorExerciseRenameConflict(name: String) = "Nama latihan '$name' sudah digunakan"
    override fun errorExerciseNotFoundById(id: String) = "Latihan dengan ID $id tidak ada"

    override val errorWorkoutNameBlank = "Silakan isi nama"
    override fun errorWorkoutAlreadyExists(name: String) = "Latihan dengan nama '$name' sudah ada"
    override val errorWorkoutNoExercises = "Silakan tambahkan setidaknya satu latihan"
    override val errorWorkoutEmpty = "Latihan kosong"

    override val labelAddExercise = "Tambah Latihan"
    override val labelCreateExercise = "Buat Latihan"
    override val labelCreateWorkout = "Buat Program Latihan"
    override val labelWorkoutName = "Nama Program"
    override val labelWorkout = "Program"
    override val labelExercise = "Latihan"
    override val labelAdd = "Tambah"
    override val labelExerciseName = "Nama Latihan"
    override val labelSave = "Simpan"
    override val labelCancel = "Batal"
    override val labelClose = "Tutup"
    override val labelDelete = "Hapus"
    override val labelSwitchLanguage = "Ganti Bahasa"
    override val labelAiTranslationsDisclaimer = "Terjemahan dilakukan oleh Kecerdasan Buatan"

    override val labelTimeline = "Linimasa"
    override val labelMonthly = "Bulanan"
    override val labelSettings = "Pengaturan"

    override val labelSets = "Set"
    override val labelReps = "Rep"
    override val labelWeight = "Beban"

    override fun labelEdit(target: String) = "Edit $target"

    override val labelSelectExercise = "Pilih Sesuatu"
    override val labelWeightUnit = "Unit Beban"
    override val labelProgramPlaceholder = "Placeholder Halaman Program"
    override val labelHistoryListPlaceholder = "Placeholder Daftar Riwayat"
    override val labelOpenDrawer = "Buka Menu Navigasi"
    override val labelHistoryEmpty = "Belum ada riwayat."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "Edit Latihan"

    override val labelRecordDeleted = "Rekor dihapus"
    override val labelRecordsDeleted = "Rekor dihapus"
    override val labelUndo = "Urungkan"

    override val labelToday = "Hari Ini"
    override val labelYesterday = "Kemarin"
    override val labelDeleteExerciseWarning = "Tindakan ini akan menghapus semua rekor dan tidak dapat dibatalkan"
    override val labelDeleteWorkoutWarning = "Apakah Anda yakin ingin menghapus program latihan ini?"
    
    override val labelExerciseNamePlaceholder = "misal: Bench Press"
    
    override val labelDefaultExerciseValues = "Nilai Latihan Default"

    override val labelDefaultSets = "Set Default"
    override val labelDefaultReps = "Rep Default"
    override val labelDefaultWeight = "Beban Default"
    
    override val labelBack = "Kembali"
    
    override val labelRotation = "Putar Otomatis"
    override val labelRotationSystem = "Ikuti Sistem"
    override val labelRotationOn = "Aktif"
    override val labelRotationOff = "Nonaktif"

    override val labelExportData = "Ekspor Data"
    override val labelImportData = "Impor Data"
    override val labelExportSuccess = "Ekspor Berhasil"
    override val labelExportFailed = "Ekspor Gagal"
    override val labelImportSuccess = "Impor Berhasil"
    override val labelImportFailed = "Impor Gagal"
    
    override val titleImportWarning = "Timpa Data?"
    override val msgImportWarning = "Ini akan menghapus database Anda saat ini secara permanen e menggantinya dengan data yang diimpor. Tindakan ini tidak dapat dibatalkan."
    override val labelConfirm = "Konfirmasi"
    override val labelDeveloperOptions = "Opsi Pengembang"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Latihan Tidak Dikenal"
    override val labelOlderRecords = "Rekor Lama"
    override val labelRestDay = "Hari Istirahat"
    override fun labelExercisesCount(count: Int) = "$count latihan"

    // Developer Options
    override val devColorPalette = "Palet Warna"
    override val devViewColors = "Lihat Warna"
    override val devDatabase = "Database"
    override val devRunStressTest = "Jalankan Tes Stres Data"
    override val devStressTestDescription = "Hapus DB & Masukkan 1 Juta Rekor"
    override val devStressTestConfirmTitle = "Jalankan Tes Stres?"
    override val devStressTestConfirmMessage = "⚠️ PERINGATAN: Ini akan MENGHAPUS SEMUA data yang ada secara permanen dan menggantinya dengan sekitar 1 juta rekor yang dihasilkan.\n\nProses ini mungkin memakan waktu satu menit."
    override val devWipeAndGenerate = "Hapus & Hasilkan"
    override val devGeneratingData = "Menghasilkan Data..."
    override val devHapticsTest = "Tes Haptik"
    override val devMoveSlider = "Gerakkan slider untuk merasakan getaran yang berbeda"

    // Validation Errors
    override val errorSetsEmpty = "Set tidak boleh kosong"
    override val errorSetsFormat = "Format set tidak valid"
    override val errorSetsWholeNumber = "Set harus berupa bilangan bulat"
    override val errorSetsPositive = "Set harus lebih besar dari 0"

    override val errorRepsEmpty = "Rep tidak boleh kosong"
    override val errorRepsFormat = "Format rep tidak valid"
    override val errorRepsWholeNumber = "Rep harus berupa bilangan bulat"
    override val errorRepsPositive = "Rep harus lebih besar dari 0"

    override val errorWeightEmpty = "Beban tidak boleh kosong"
    override val errorWeightFormat = "Format beban tidak valid"
    override val errorWeightInvalid = "Nilai beban tidak valid"
    
    override val labelGoToCurrentMonth = "Buka bulan saat ini"
    
    // Graph Screen Translations
    override val labelGraph = "Grafik"
    override val labelNoDataForExercise = "Belum ada riwayat rekor untuk latihan ini"
    override val labelMaxWeight = "Rekor Pribadi"
    override val labelCurrentWeight = "Beban Terakhir"
    override val labelProgress = "Kemajuan"
    override val labelNoExercises = "Tidak ada latihan yang ditemukan. Buat latihan terlebih dahulu!"

    // Unsaved Work Dialog
    override val titleNoName = "Tanpa Nama"
    override val msgNoName = "Apakah Anda ingin mengabaikan atau mengisi nama dan menyimpan?"
    override val titleUnsavedWork = "Pekerjaan Belum Disimpan"
    override val msgUnsavedWork = "Apakah Anda ingin mengabaikan atau menyimpan?"
    override val labelDiscard = "Abaikan"
    override val labelKeepEditing = "Lanjutkan Mengedit"
    override val labelEditAction = "Edit"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Program"
    override val labelExercises = "Latihan"

    // In-App Font Settings
    override val labelInAppFont = "Font Aplikasi"
    override val labelFontSystemDefault = "Default Sistem"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Bulat"
}
