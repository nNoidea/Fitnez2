package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object JaStrings : EnStrings(
    appLocale = Locale.forLanguageTag("ja"),
    languageName = "日本語",
) {
    override val labelSystemLanguage = "システム言語"
    override val labelLanguage = "言語"

    override val errorExerciseNameBlank = "種目名は空にできません"

    override val errorIdMustBeZero = "新しい種目のIDは0である必要があります"
    override val errorIdMustNotBeZero = "更新するレコードのIDはゼロ以外である必要があります"

    override fun errorExerciseAlreadyExists(name: String) = "'$name' という名前の種目は既に存在します"
    override fun errorExerciseRenameConflict(name: String) = "種目名 '$name' は既に使用されています"
    override fun errorExerciseNotFoundById(id: Int) = "ID $id の種目は存在しません"

    override val errorWorkoutNameBlank = "名前を入力してください"
    override fun errorWorkoutAlreadyExists(name: String) = "'$name' という名前のワークアウトは既に存在します"
    override val errorWorkoutNoExercises = "少なくとも1つの種目を追加してください"
    override val errorWorkoutEmpty = "ワークアウトが空です"

    override val labelAddExercise = "種目を追加"
    override val labelCreateExercise = "種目を作成"
    override val labelCreateWorkout = "ワークアウトを作成"
    override val labelWorkoutName = "ワークアウト名"
    override val labelWorkout = "ワークアウト"
    override val labelExercise = "種目"
    override val labelAdd = "追加"
    override val labelExerciseName = "種目名"
    override val labelSave = "保存"
    override val labelCancel = "キャンセル"
    override val labelClose = "閉じる"
    override val labelDelete = "削除"
    override val labelSwitchLanguage = "言語を切り替え"
    override val labelAiTranslationsDisclaimer = "翻訳は人工知能によって行われます"

    override val labelTimeline = "タイムライン"
    override val labelMonthly = "月別"
    override val labelSettings = "設定"

    override val labelSets = "セット数"
    override val labelReps = "レップ数"
    override val labelWeight = "重量"

    override fun labelEdit(target: String) = "$target を編集"

    override val labelSelectExercise = "何かを選択"
    override val labelWeightUnit = "重量単位"
    override val labelProgramPlaceholder = "プログラムページのプレースホルダー"
    override val labelHistoryListPlaceholder = "履歴リストのプレースホルダー"
    override val labelOpenDrawer = "ナビゲーションドロワーを開く"
    override val labelHistoryEmpty = "履歴はまだありません。"
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "種目を編集"

    override val labelRecordDeleted = "レコードが削除されました"
    override val labelUndo = "元に戻す"

    override val labelToday = "今日"
    override val labelYesterday = "昨日"
    override val labelDeleteExerciseWarning = "この操作はすべてのレコードを削除し、元に戻すことはできません"
    override val labelDeleteWorkoutWarning = "このワークアウトを削除してもよろしいですか？"
    
    override val labelExerciseNamePlaceholder = "例: ベンチプレス"
    
    override val labelDefaultExerciseValues = "デフォルトの種目値"

    override val labelDefaultSets = "デフォルトのセット数"
    override val labelDefaultReps = "デフォルトのレップ数"
    override val labelDefaultWeight = "デフォルトの重量"
    
    override val labelBack = "戻る"
    
    override val labelRotation = "自動回転"
    override val labelRotationSystem = "システムに従う"
    override val labelRotationOn = "オン"
    override val labelRotationOff = "オフ"

    override val labelExportData = "データをエクスポート"
    override val labelImportData = "データをインポート"
    override val labelExportSuccess = "エクスポートに成功しました"
    override val labelExportFailed = "エクスポートに失敗しました"
    override val labelImportSuccess = "インポートに成功しました"
    override val labelImportFailed = "インポートに失敗しました"
    
    override val titleImportWarning = "データを上書きしますか？"
    override val msgImportWarning = "これにより、現在のデータベースが完全に削除され、インポートされたデータに置き換えられます。この操作は元に戻せません。"
    override val labelConfirm = "確認"
    override val labelDeveloperOptions = "開発者向けオプション"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "不明な種目"
    override val labelOlderRecords = "過去のレコード"
    override val labelRestDay = "休息日"
    override fun labelExercisesCount(count: Int) = "$count 個の種目"

    // Developer Options
    override val devColorPalette = "カラーパレット"
    override val devViewColors = "カラーを表示"
    override val devDatabase = "データベース"
    override val devRunStressTest = "データストレステストを実行"
    override val devStressTestDescription = "DBをクリアして100万件のレコードを挿入"
    override val devStressTestConfirmTitle = "ストレステストを実行しますか？"
    override val devStressTestConfirmMessage = "⚠️ 警告: これにより、既存のデータがすべて完全に削除され、生成された約100万件のレコードに置き換えられます。\n\nこのプロセスには1分ほどかかる場合があります。"
    override val devWipeAndGenerate = "クリアして生成"
    override val devGeneratingData = "データを生成中..."
    override val devHapticsTest = "触覚テスト"
    override val devMoveSlider = "スライダーを動かして異なる振動を感じてください"

    // Validation Errors
    override val errorSetsEmpty = "セット数は空にできません"
    override val errorSetsFormat = "セット数の形式が無効です"
    override val errorSetsWholeNumber = "セット数は整数である必要があります"
    override val errorSetsPositive = "セット数は0より大きい必要があります"

    override val errorRepsEmpty = "レップ数は空にできません"
    override val errorRepsFormat = "レップ数の形式が無効です"
    override val errorRepsWholeNumber = "レップ数は整数である必要があります"
    override val errorRepsPositive = "レップ数は0より大きい必要があります"

    override val errorWeightEmpty = "重量は空にできません"
    override val errorWeightFormat = "重量の形式が無効です"
    override val errorWeightInvalid = "重量の値が無効です"
    
    override val labelGoToCurrentMonth = "現在の月に移動"
    
    // Graph Screen Translations
    override val labelGraph = "グラフ"
    override val labelNoDataForExercise = "この種目の記録履歴はまだありません"
    override val labelMaxWeight = "自己ベスト"
    override val labelCurrentWeight = "最新の重量"
    override val labelProgress = "進捗"
    override val labelNoExercises = "種目が見つかりません。まず種目を作成してください！"

    // Unsaved Work Dialog
    override val titleNoName = "名前なし"
    override val msgNoName = "破棄しますか、それとも名前を入力して保存しますか？"
    override val titleUnsavedWork = "未保存の作業"
    override val msgUnsavedWork = "破棄しますか、それとも保存しますか？"
    override val labelDiscard = "破棄"
    override val labelKeepEditing = "編集を続ける"
    override val labelEditAction = "編集"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "ワークアウト"
    override val labelExercises = "種目"
}
