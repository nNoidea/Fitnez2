package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object PtStrings : EnStrings(
    appLocale = Locale.forLanguageTag("pt"),
    languageName = "Português",
) {
    override val labelSystemLanguage = "Idioma do sistema"
    override val labelLanguage = "Idioma"

    override val errorExerciseNameBlank = "O nome do exercício não pode estar vazio"

    override val errorIdMustBeZero = "Novos exercícios devem ter um ID de 0"

    override fun errorExerciseAlreadyExists(name: String) = "O exercício com o nome '$name' já existe"
    override fun errorExerciseRenameConflict(name: String) = "O nome do exercício '$name' já está em uso"
    override fun errorExerciseNotFoundById(id: String) = "O exercício com o ID $id não existe"

    override val errorWorkoutNameBlank = "Por favor, preencha um nome"
    override fun errorWorkoutAlreadyExists(name: String) = "O treino com o nome '$name' já existe"
    override val errorWorkoutNoExercises = "Por favor, adicione pelo menos um exercício"
    override val errorWorkoutEmpty = "O treino está vazio"
    override val labelCreateExercise = "Criar um exercício"
    override val labelWorkoutName = "Nome do Treino"
    override val labelWorkout = "Treino"
    override val labelExercise = "Exercício"
    override val labelAdd = "Adicionar"
    override val labelExerciseName = "Nome do Exercício"
    override val labelSave = "Salvar"
    override val labelCancel = "Cancelar"
    override val labelClose = "Fechar"
    override val labelDelete = "Excluir"
    override val labelAiTranslationsDisclaimer = "As traduções são realizadas por Inteligência Artificial"

    override val labelTimeline = "Linha do Tempo"
    override val labelMonthly = "Mensal"
    override val labelSettings = "Configurações"

    override val labelSets = "Séries"
    override val labelReps = "Reps"

    override fun labelEdit(target: String) = "Editar $target"

    override val labelSelectExercise = "Selecionar Algo"
    override val labelWeightUnit = "Unidade de Peso"
    override val labelOpenDrawer = "Abrir menu de navegação"
    override val labelHistoryEmpty = "Nenhum histórico ainda."
    override val labelAppName = "Fitnez2"
    override val labelEditExercise = "Editar Exercício"

    override val labelRecordDeleted = "Registro excluído"
    override val labelRecordsDeleted = "Registros excluídos"
    override val labelUndo = "Desfazer"

    override val labelToday = "Hoje"
    override val labelYesterday = "Ontem"
    override val labelDeleteExerciseWarning = "Esta ação excluirá todos os registros e não pode ser desfeita"
    override val labelDeleteWorkoutWarning = "Tem certeza de que deseja excluir este treino?"
    
    override val labelExerciseNamePlaceholder = "ex. Supino Reto"
    
    override val labelDefaultExerciseValues = "Valores Padrão Do Exercício"

    override val labelDefaultSets = "Séries Padrão"
    override val labelDefaultReps = "Reps Padrão"
    override val labelDefaultWeight = "Peso Padrão"
    
    override val labelBack = "Voltar"
    
    override val labelRotation = "Auto-rotacionar"
    override val labelRotationSystem = "Seguir Sistema"
    override val labelRotationOn = "Ligado"
    override val labelRotationOff = "Desligado"

    override val labelExportData = "Exportar Dados"
    override val labelImportData = "Importar Dados"
    override val labelExportSuccess = "Exportação Bem-Sucedida"
    override val labelExportFailed = "Falha Na Exportação"
    override val labelImportSuccess = "Importação Bem-Sucedida"
    override val labelImportFailed = "Falha Na Importação"
    
    override val titleImportWarning = "Substituir Dados?"
    override val msgImportWarning = "Isso excluirá permanentemente seu banco de dados atual e o substituirá pelos dados importados. Esta ação não pode ser desfeita."
    override val labelConfirm = "Confirmar"
    override val labelDeveloperOptions = "Opções do Desenvolvedor"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Exercício Desconhecido"

    // Developer Options
    override val devColorPalette = "Paleta de Cores"
    override val devViewColors = "Visualizar Cores"
    override val devDatabase = "Banco de Dados"
    override val devRunStressTest = "Executar Teste de Estresse de Dados"
    override val devStressTestDescription = "Limpar BD e inserir 1M de registros"
    override val devStressTestConfirmTitle = "Executar Teste de Estresse?"
    override val devStressTestConfirmMessage = "⚠️ AVISO: Isso excluirá permanentemente TODOS os dados existentes e os substituirá por cerca de 1 milhão de registros gerados.\n\nEste processo pode levar um minuto."
    override val devWipeAndGenerate = "Limpar e Gerar"
    override val devGeneratingData = "Gerando Dados..."
    override val devHapticsTest = "Teste de Vibração"
    override val devMoveSlider = "Mova o controle deslizante para sentir vibrações diferentes"

    // Validation Errors
    override val errorSetsPositive = "As séries devem ser maiores que 0"
    override val errorRepsPositive = "As reps devem ser maiores que 0"
    override val errorWeightInvalid = "Valor de peso inválido"
    
    override val labelGoToCurrentMonth = "Ir para o mês atual"
    
    // Graph Screen Translations
    override val labelGraph = "Gráfico"
    override val labelNoDataForExercise = "Nenhum histórico de registros para este exercício ainda"
    override val labelNoExercises = "Nenhum exercício encontrado. Crie um exercício primeiro!"
    override val labelCompareBy = "Comparar por"
    override val labelGraphMaxWeight = "Peso"
    override val labelGraphVolume = "Volume"

    // Unsaved Work Dialog
    override val titleNoName = "Sem Nome"
    override val msgNoName = "Deseja descartar ou preencher um nome e salvar?"
    override val titleUnsavedWork = "Trabalho Não Salvo"
    override val msgUnsavedWork = "Deseja descartar ou salvar?"
    override val labelDiscard = "Descartar"
    override val labelEditAction = "Editar"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Treinos"
    override val labelExercises = "Exercícios"

    // In-App Font Settings
    override val labelInAppFont = "Fonte do aplicativo"
    override val labelFontSystemDefault = "Padrão do sistema"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Arredondado"
}
