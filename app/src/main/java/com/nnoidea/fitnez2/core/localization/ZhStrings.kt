package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object ZhStrings : EnStrings(
    appLocale = Locale.forLanguageTag("zh"),
    languageName = "中文",
) {
    override val labelSystemLanguage = "系统语言"
    override val labelLanguage = "语言"

    override val errorExerciseNameBlank = "动作名称不能为空"

    override val errorIdMustBeZero = "新动作的 ID 必须为 0"
    override val errorIdMustNotBeZero = "要更新的动作必须具有非零 ID"

    override fun errorExerciseAlreadyExists(name: String) = "名称为 '$name' 的动作已存在"
    override fun errorExerciseRenameConflict(name: String) = "动作名称 '$name' 已被使用"
    override fun errorExerciseNotFoundById(id: Int) = "ID 为 $id 的动作不存在"

    override val errorWorkoutNameBlank = "请输入名称"
    override fun errorWorkoutAlreadyExists(name: String) = "名称为 '$name' 的训练已存在"
    override val errorWorkoutNoExercises = "请至少添加一个动作"
    override val errorWorkoutEmpty = "训练内容为空"

    override val labelAddExercise = "添加动作"
    override val labelCreateExercise = "创建动作"
    override val labelCreateWorkout = "创建训练"
    override val labelWorkoutName = "训练名称"
    override val labelWorkout = "训练"
    override val labelExercise = "动作"
    override val labelAdd = "添加"
    override val labelExerciseName = "动作名称"
    override val labelSave = "保存"
    override val labelCancel = "取消"
    override val labelClose = "关闭"
    override val labelDelete = "删除"
    override val labelSwitchLanguage = "切换语言"
    override val labelAiTranslationsDisclaimer = "翻译由人工智能完成"

    override val labelTimeline = "时间线"
    override val labelMonthly = "每月"
    override val labelSettings = "设置"

    override val labelSets = "组数"
    override val labelReps = "次数"
    override val labelWeight = "重量"

    override fun labelEdit(target: String) = "编辑 $target"

    override val labelSelectExercise = "选择一项"
    override val labelWeightUnit = "重量单位"
    override val labelProgramPlaceholder = "计划页面占位符"
    override val labelHistoryListPlaceholder = "历史列表占位符"
    override val labelOpenDrawer = "打开导航抽屉"
    override val labelHistoryEmpty = "暂无历史记录。"
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "编辑动作"

    override val labelRecordDeleted = "记录已删除"
    override val labelUndo = "撤销"

    override val labelToday = "今天"
    override val labelYesterday = "昨天"
    override val labelDeleteExerciseWarning = "此操作将删除所有记录且无法撤销"
    override val labelDeleteWorkoutWarning = "您确定要删除此训练吗？"
    
    override val labelExerciseNamePlaceholder = "例如：卧推"
    
    override val labelDefaultExerciseValues = "默认动作值"

    override val labelDefaultSets = "默认组数"
    override val labelDefaultReps = "默认次数"
    override val labelDefaultWeight = "默认重量"
    
    override val labelBack = "返回"
    
    override val labelRotation = "自动旋转"
    override val labelRotationSystem = "跟随系统"
    override val labelRotationOn = "开启"
    override val labelRotationOff = "关闭"

    override val labelExportData = "导出数据"
    override val labelImportData = "导入数据"
    override val labelExportSuccess = "导出成功"
    override val labelExportFailed = "导出失败"
    override val labelImportSuccess = "导入成功"
    override val labelImportFailed = "导入失败"
    
    override val titleImportWarning = "覆盖数据？"
    override val msgImportWarning = "这将永久删除您当前的数据库并替换为导入的数据。此操作无法撤销。"
    override val labelConfirm = "确认"
    override val labelDeveloperOptions = "开发者选项"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "未知动作"
    override val labelOlderRecords = "更早的记录"
    override val labelRestDay = "休息日"
    override fun labelExercisesCount(count: Int) = if (count == 1) "1 个动作" else "$count 个动作"

    // Developer Options
    override val devColorPalette = "调色板"
    override val devViewColors = "查看颜色"
    override val devDatabase = "数据库"
    override val devRunStressTest = "运行数据压力测试"
    override val devStressTestDescription = "清空数据库并插入 100 万条记录"
    override val devStressTestConfirmTitle = "运行压力测试？"
    override val devStressTestConfirmMessage = "⚠️ 警告：这将永久删除所有现有数据并替换为约 100 万条生成的记录。\n\n此过程可能需要一分钟。"
    override val devWipeAndGenerate = "清空并生成"
    override val devGeneratingData = "正在生成数据..."
    override val devHapticsTest = "触觉测试"
    override val devMoveSlider = "移动滑块以感受不同的振动"

    // Validation Errors
    override val errorSetsEmpty = "组数不能为空"
    override val errorSetsFormat = "组数格式无效"
    override val errorSetsWholeNumber = "组数必须是整数"
    override val errorSetsPositive = "组数必须大于 0"

    override val errorRepsEmpty = "次数不能为空"
    override val errorRepsFormat = "次数格式无效"
    override val errorRepsWholeNumber = "次数必须是整数"
    override val errorRepsPositive = "次数必须大于 0"

    override val errorWeightEmpty = "重量不能为空"
    override val errorWeightFormat = "重量格式无效"
    override val errorWeightInvalid = "重量值无效"
    
    override val labelGoToCurrentMonth = "转到当前月份"
    
    // Graph Screen Translations
    override val labelGraph = "图表"
    override val labelNoDataForExercise = "该动作暂无记录历史"
    override val labelMaxWeight = "个人纪录"
    override val labelCurrentWeight = "最新重量"
    override val labelProgress = "进度"
    override val labelNoExercises = "未找到动作。请先创建一个动作！"

    // Unsaved Work Dialog
    override val titleNoName = "未命名"
    override val msgNoName = "您想放弃还是填写名称并保存？"
    override val titleUnsavedWork = "未保存的工作"
    override val msgUnsavedWork = "您想放弃还是保存？"
    override val labelDiscard = "放弃"
    override val labelKeepEditing = "继续编辑"
    override val labelEditAction = "编辑"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "训练"
    override val labelExercises = "动作"
}
