package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object KoStrings : EnStrings(
    appLocale = Locale.forLanguageTag("ko"),
    languageName = "한국어",
) {
    override val labelSystemLanguage = "시스템 언어"
    override val labelLanguage = "언어"

    override val errorExerciseNameBlank = "운동 이름은 비워둘 수 없습니다"

    override val errorIdMustBeZero = "새 운동의 ID는 0이어야 합니다"
    override val errorIdMustNotBeZero = "업데이트할 레코드의 ID는 0이 아니어야 합니다"

    override fun errorExerciseAlreadyExists(name: String) = "'$name' 이라는 이름의 운동이 이미 존재합니다"
    override fun errorExerciseRenameConflict(name: String) = "운동 이름 '$name'은(는) 이미 사용 중입니다"
    override fun errorExerciseNotFoundById(id: String) = "ID가 ${id}인 운동이 존재하지 않습니다"

    override val errorWorkoutNameBlank = "이름을 입력해주세요"
    override fun errorWorkoutAlreadyExists(name: String) = "'$name' 이라는 이름의 운동 프로그램이 이미 존재합니다"
    override val errorWorkoutNoExercises = "최소 하나의 운동을 추가해주세요"
    override val errorWorkoutEmpty = "운동 프로그램이 비어 있습니다"

    override val labelAddExercise = "운동 추가"
    override val labelCreateExercise = "운동 생성"
    override val labelCreateWorkout = "운동 프로그램 생성"
    override val labelWorkoutName = "프로그램 이름"
    override val labelWorkout = "프로그램"
    override val labelExercise = "운동"
    override val labelAdd = "추가"
    override val labelExerciseName = "운동 이름"
    override val labelSave = "저장"
    override val labelCancel = "취소"
    override val labelClose = "닫기"
    override val labelDelete = "삭제"
    override val labelSwitchLanguage = "언어 전환"
    override val labelAiTranslationsDisclaimer = "번역은 인공지능에 의해 수행됩니다"

    override val labelTimeline = "타임라인"
    override val labelMonthly = "월별"
    override val labelSettings = "설정"

    override val labelSets = "세트"
    override val labelReps = "횟수"
    override val labelWeight = "무게"

    override fun labelEdit(target: String) = "$target 수정"

    override val labelSelectExercise = "항목 선택"
    override val labelWeightUnit = "무게 단위"
    override val labelProgramPlaceholder = "프로그램 페이지 플레이스홀더"
    override val labelHistoryListPlaceholder = "기록 목록 플레이스홀더"
    override val labelOpenDrawer = "탐색 창 열기"
    override val labelHistoryEmpty = "아직 기록이 없습니다."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "운동 수정"

    override val labelRecordDeleted = "기록이 삭제되었습니다"
    override val labelRecordsDeleted = "기록이 삭제되었습니다"
    override val labelUndo = "실행 취소"

    override val labelToday = "오늘"
    override val labelYesterday = "어제"
    override val labelDeleteExerciseWarning = "이 작업은 모든 기록을 삭제하며 되돌릴 수 없습니다"
    override val labelDeleteWorkoutWarning = "이 운동 프로그램을 삭제하시겠습니까?"
    
    override val labelExerciseNamePlaceholder = "예: 벤치 프레스"
    
    override val labelDefaultExerciseValues = "기본 운동 값"

    override val labelDefaultSets = "기본 세트"
    override val labelDefaultReps = "기본 횟수"
    override val labelDefaultWeight = "기본 무게"
    
    override val labelBack = "뒤로"
    
    override val labelRotation = "자동 회전"
    override val labelRotationSystem = "시스템 따르기"
    override val labelRotationOn = "켜짐"
    override val labelRotationOff = "꺼짐"

    override val labelExportData = "데이터 내보내기"
    override val labelImportData = "데이터 가져오기"
    override val labelExportSuccess = "내보내기 성공"
    override val labelExportFailed = "내보내기 실패"
    override val labelImportSuccess = "가져오기 성공"
    override val labelImportFailed = "가져오기 실패"
    
    override val titleImportWarning = "데이터를 덮어쓰시겠습니까?"
    override val msgImportWarning = "이렇게 하면 현재 데이터베이스가 영구적으로 삭제되고 가져온 데이터로 대체됩니다. 이 작업은 되돌릴 수 없습니다."
    override val labelConfirm = "확인"
    override val labelDeveloperOptions = "개발자 옵션"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "알 수 없는 운동"
    override val labelOlderRecords = "이전 기록"
    override val labelRestDay = "휴식일"
    override fun labelExercisesCount(count: Int) = "$count 개의 운동"

    // Developer Options
    override val devColorPalette = "색상 팔레트"
    override val devViewColors = "색상 보기"
    override val devDatabase = "데이터베이스"
    override val devRunStressTest = "데이터 스트레스 테스트 실행"
    override val devStressTestDescription = "DB 초기화 및 100만 개 레코드 삽입"
    override val devStressTestConfirmTitle = "스트레스 테스트를 실행하시겠습니까?"
    override val devStressTestConfirmMessage = "⚠️ 경고: 이렇게 하면 모든 기존 데이터가 영구적으로 삭제되고 생성된 약 100만 개의 레코드로 대체됩니다.\n\n이 프로세스는 1분 정도 걸릴 수 있습니다."
    override val devWipeAndGenerate = "초기화 및 생성"
    override val devGeneratingData = "데이터 생성 중..."
    override val devHapticsTest = "햅틱 테스트"
    override val devMoveSlider = "슬라이더를 움직여 다양한 진동을 느껴보세요"

    // Validation Errors
    override val errorSetsEmpty = "세트는 비워둘 수 없습니다"
    override val errorSetsFormat = "잘못된 세트 형식"
    override val errorSetsWholeNumber = "세트는 정수여야 합니다"
    override val errorSetsPositive = "세트는 0보다 커야 합니다"

    override val errorRepsEmpty = "횟수는 비워둘 수 없습니다"
    override val errorRepsFormat = "잘못된 횟수 형식"
    override val errorRepsWholeNumber = "횟수는 정수여야 합니다"
    override val errorRepsPositive = "횟수는 0보다 커야 합니다"

    override val errorWeightEmpty = "무게는 비워둘 수 없습니다"
    override val errorWeightFormat = "잘못된 무게 형식"
    override val errorWeightInvalid = "잘못된 무게 값"
    
    override val labelGoToCurrentMonth = "현재 달로 이동"
    
    // Graph Screen Translations
    override val labelGraph = "그래프"
    override val labelNoDataForExercise = "이 운동에 대한 기록이 아직 없습니다"
    override val labelMaxWeight = "개인 최고 기록"
    override val labelCurrentWeight = "최근 무게"
    override val labelProgress = "진행 상황"
    override val labelNoExercises = "운동을 찾을 수 없습니다. 먼저 운동을 생성하세요!"

    // Unsaved Work Dialog
    override val titleNoName = "이름 없음"
    override val msgNoName = "취소하시겠습니까, 아니면 이름을 입력하고 저장하시겠습니까?"
    override val titleUnsavedWork = "저장되지 않은 작업"
    override val msgUnsavedWork = "취소하시겠습니까, 아니면 저장하시겠습니까?"
    override val labelDiscard = "취소"
    override val labelKeepEditing = "계속 편집"
    override val labelEditAction = "편집"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "프로그램"
    override val labelExercises = "운동"

    // In-App Font Settings
    override val labelInAppFont = "앱 글꼴"
    override val labelFontSystemDefault = "시스템 기본값"
    override val labelFontGoogleSansFlexRounded = "구글 산스 플렉스 라운드"
}
