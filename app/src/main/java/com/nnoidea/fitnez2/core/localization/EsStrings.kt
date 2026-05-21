package com.nnoidea.fitnez2.core.localization

import java.util.Locale

object EsStrings : EnStrings(
    appLocale = Locale.forLanguageTag("es"),
    languageName = "Español",
) {
    override val labelSystemLanguage = "Idioma del sistema"
    override val labelLanguage = "Idioma"

    override val errorExerciseNameBlank = "El nombre del ejercicio no puede estar vacío"

    override val errorIdMustBeZero = "Los nuevos ejercicios deben tener un ID de 0"
    override val errorIdMustNotBeZero = "Los ejercicios a actualizar deben tener un ID distinto de cero"

    override fun errorExerciseAlreadyExists(name: String) = "El ejercicio con nombre '$name' ya existe"
    override fun errorExerciseRenameConflict(name: String) = "El nombre del ejercicio '$name' ya está en uso"
    override fun errorExerciseNotFoundById(id: Int) = "El ejercicio con ID $id no existe"

    override val errorWorkoutNameBlank = "Por favor, introduce un nombre"
    override fun errorWorkoutAlreadyExists(name: String) = "El entrenamiento con nombre '$name' ya existe"
    override val errorWorkoutNoExercises = "Por favor, añade al menos un ejercicio"
    override val errorWorkoutEmpty = "El entrenamiento está vacío"

    override val labelAddExercise = "Añadir Ejercicio"
    override val labelCreateExercise = "Crear un ejercicio" // English is "Create an exercise" (Sentence Case).
    override val labelCreateWorkout = "Crear un entrenamiento" // English is "Create a workout" (Sentence Case).
    override val labelWorkoutName = "Nombre Del Entrenamiento"
    override val labelWorkout = "Entrenamiento"
    override val labelExercise = "Ejercicio"
    override val labelAdd = "Añadir"
    override val labelExerciseName = "Nombre Del Ejercicio"
    override val labelSave = "Guardar"
    override val labelCancel = "Cancelar"
    override val labelClose = "Cerrar"
    override val labelDelete = "Eliminar"
    override val labelSwitchLanguage = "Cambiar Idioma"
    override val labelAiTranslationsDisclaimer = "Las traducciones son realizadas por Inteligencia Artificial"

    override val labelTimeline = "Cronología"
    override val labelMonthly = "Mensual"
    override val labelSettings = "Ajustes"

    override val labelSets = "Series"
    override val labelReps = "Reps"
    override val labelWeight = "Peso"

    override fun labelEdit(target: String) = "Editar $target"

    override val labelSelectExercise = "Seleccionar Algo"
    override val labelWeightUnit = "Unidad De Peso"
    override val labelProgramPlaceholder = "Marcador De Posición De La Página Del Programa"
    override val labelHistoryListPlaceholder = "Marcador De Posición De La Lista De Historial"
    override val labelOpenDrawer = "Abrir Menú De Navegación"
    override val labelHistoryEmpty = "Aún no hay historial."
    override val labelAppName = "Fitnez2"
    override val labelVersion = "1.0.0"
    override val labelEditExercise = "Editar ejercicio"

    override val labelRecordDeleted = "Registro eliminado"
    override val labelRecordsDeleted = "Registros eliminados"
    override val labelUndo = "Deshacer"

    override val labelToday = "Hoy"
    override val labelYesterday = "Ayer"
    override val labelDeleteExerciseWarning = "Esta acción eliminará todos los registros y no se puede deshacer"
    override val labelDeleteWorkoutWarning = "¿Estás seguro de que deseas eliminar este entrenamiento?"
    
    override val labelExerciseNamePlaceholder = "ej. Press de banca"
    
    override val labelDefaultExerciseValues = "Valores predeterminados del ejercicio"

    override val labelDefaultSets = "Series predeterminadas"
    override val labelDefaultReps = "Reps predeterminadas"
    override val labelDefaultWeight = "Peso predeterminado"
    
    override val labelBack = "Atrás"
    
    override val labelRotation = "Rotación automática"
    override val labelRotationSystem = "Seguir sistema"
    override val labelRotationOn = "Activado"
    override val labelRotationOff = "Desactivado"

    override val labelExportData = "Exportar datos"
    override val labelImportData = "Importar datos"
    override val labelExportSuccess = "Exportación exitosa"
    override val labelExportFailed = "Error en la exportación"
    override val labelImportSuccess = "Importación exitosa"
    override val labelImportFailed = "Error en la importación"
    
    override val titleImportWarning = "¿Sobrescribir datos?"
    override val msgImportWarning = "Esto eliminará permanentemente tu base de datos actual y la reemplazará con los datos importados. Esta acción no se puede deshacer."
    override val labelConfirm = "Confirmar"
    override val labelDeveloperOptions = "Opciones de desarrollador"
    override val unitKg = "kg"
    override val unitLb = "lb"
    override val labelUnknownExercise = "Ejercicio desconocido"
    override val labelOlderRecords = "Registros más antiguos"
    override val labelRestDay = "Día de descanso"
    override fun labelExercisesCount(count: Int) = if (count == 1) "1 ejercicio" else "$count ejercicios"

    // Developer Options
    override val devColorPalette = "Paleta de colores"
    override val devViewColors = "Ver colores"
    override val devDatabase = "Base de datos"
    override val devRunStressTest = "Ejecutar prueba de estrés"
    override val devStressTestDescription = "Borrar BD e insertar 1M de registros"
    override val devStressTestConfirmTitle = "¿Ejecutar prueba de estrés?"
    override val devStressTestConfirmMessage = "⚠️ ADVERTENCIA: Esto eliminará permanentemente TODOS los datos existentes y los reemplazará con ~1 millón de registros generados.\n\nEste proceso puede tardar un minuto."
    override val devWipeAndGenerate = "Borrar y generar"
    override val devGeneratingData = "Generando datos..."
    override val devHapticsTest = "Prueba de háptica"
    override val devMoveSlider = "Mueve el control deslizante para sentir diferentes vibraciones"

    // Validation Errors
    override val errorSetsEmpty = "Las series no pueden estar vacías"
    override val errorSetsFormat = "Formato de series inválido"
    override val errorSetsWholeNumber = "Las series deben ser un número entero"
    override val errorSetsPositive = "Las series deben ser mayores que 0"

    override val errorRepsEmpty = "Las reps no pueden estar vacías"
    override val errorRepsFormat = "Formato de reps inválido"
    override val errorRepsWholeNumber = "Las reps deben ser un número entero"
    override val errorRepsPositive = "Las reps deben ser mayores que 0"

    override val errorWeightEmpty = "El peso no puede estar vacío"
    override val errorWeightFormat = "Formato de peso inválido"
    override val errorWeightInvalid = "Valor de peso inválido"
    
    override val labelGoToCurrentMonth = "Ir al mes actual"
    
    // Graph Screen Translations
    override val labelGraph = "Gráfico"
    override val labelNoDataForExercise = "Aún no hay historial de registros para este ejercicio"
    override val labelMaxWeight = "Récord personal"
    override val labelCurrentWeight = "Último peso"
    override val labelProgress = "Progreso"
    override val labelNoExercises = "No se encontraron ejercicios. ¡Crea un ejercicio primero!"

    // Unsaved Work Dialog
    override val titleNoName = "Sin nombre"
    override val msgNoName = "¿Quieres descartar o completar un nombre y guardar?"
    override val titleUnsavedWork = "Trabajo no guardado"
    override val msgUnsavedWork = "¿Quieres descartar o guardar?"
    override val labelDiscard = "Descartar"
    override val labelKeepEditing = "Seguir editando"
    override val labelEditAction = "Editar"

    // Exercise Selection Dialog Separators
    override val labelWorkouts = "Entrenamientos"
    override val labelExercises = "Ejercicios"

    // In-App Font Settings
    override val labelInAppFont = "Fuente de la aplicación"
    override val labelFontSystemDefault = "Predeterminado del sistema"
    override val labelFontGoogleSansFlexRounded = "Google Sans Flex Redondeado"
}
