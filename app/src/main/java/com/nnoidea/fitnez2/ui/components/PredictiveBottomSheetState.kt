package com.nnoidea.fitnez2.ui.components

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.unit.Velocity
import com.nnoidea.fitnez2.core.ValidateAndCorrect
import com.nnoidea.fitnez2.data.LocalAppDatabase
import com.nnoidea.fitnez2.data.LocalSettingsRepository
import com.nnoidea.fitnez2.data.SettingsRepository
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.common.UiSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Stable
interface PredictiveBottomSheetState {
    // Form data
    val selectedExerciseId: Int?
    val selectedExerciseName: String?
    val sets: String
    val reps: String
    val weight: String
    val weightUnit: String
    
    // UI Local State
    var setsRaw: String
    var repsRaw: String
    var weightRaw: String
    var showExerciseSelection: Boolean
    val exercises: List<Exercise>

    // Animation & Sheet State
    val offsetY: Animatable<Float, AnimationVector1D>
    val predictiveProgress: Float
    val isExpanded: Boolean
    val hasBeenOpened: Boolean
    val maxOffset: Float
    val minOffset: Float

    // Actions
    fun onExerciseSelected(exercise: Exercise)
    fun onAddClick()
    fun onSetsChange(value: String)
    fun onRepsChange(value: String)
    fun onWeightChange(value: String)
    suspend fun settleSpring(velocity: Float)
    val nestedScrollConnection: NestedScrollConnection
    fun onPredictiveBackProgress(progress: Float)
    suspend fun onPredictiveBackCommit()
    fun onPredictiveBackCancel()
    fun toggleExerciseSelection(show: Boolean)
}

@Stable
class RecordPredictiveBottomSheetState(
    private val scope: CoroutineScope,
    private val dao: com.nnoidea.fitnez2.data.dao.RecordDao,
    private val exerciseDao: com.nnoidea.fitnez2.data.dao.ExerciseDao,
    private val settingsRepository: SettingsRepository,
    private val globalUiState: GlobalUiState,
    private val keyboardController: SoftwareKeyboardController?,
    private val focusManager: FocusManager,
    private val context: android.content.Context,
    override val maxOffset: Float,
    override val minOffset: Float,
    private val onHapticFeedback: (Int) -> Unit
) : PredictiveBottomSheetState {

    override var selectedExerciseId by mutableStateOf<Int?>(null)
    override var selectedExerciseName by mutableStateOf<String?>(null)
    override var sets by mutableStateOf("")
    override var reps by mutableStateOf("")
    override var weight by mutableStateOf("")
    override var weightUnit by mutableStateOf("kg")

    override var setsRaw by mutableStateOf("")
    override var repsRaw by mutableStateOf("")
    override var weightRaw by mutableStateOf("")
    override var showExerciseSelection by mutableStateOf(false)
    override var exercises by mutableStateOf<List<Exercise>>(emptyList())

    override val offsetY = Animatable(maxOffset)
    override var predictiveProgress by mutableFloatStateOf(0f)
    override val isExpanded by derivedStateOf { offsetY.value < maxOffset / 2 }
    override var hasBeenOpened by mutableStateOf(false)

    private var defaultSets = "3"
    private var defaultReps = "10"
    private var defaultWeight = "20"

    // Fallbacks
    private var setsFallback = ""
    private var repsFallback = ""
    private var weightFallback = ""

    init {
        scope.launch {
            exerciseDao.getAllExercisesFlow().collect { exercises = it }
        }
        scope.launch {
            settingsRepository.weightUnitFlow.collect { weightUnit = it }
        }
        scope.launch {
            settingsRepository.defaultSetsFlow.collect { defaultSets = it }
        }
        scope.launch {
            settingsRepository.defaultRepsFlow.collect { defaultReps = it }
        }
        scope.launch {
            settingsRepository.defaultWeightFlow.collect { defaultWeight = it }
        }
        scope.launch {
            initializeSession()
        }
        
        // Track hasBeenOpened
        scope.launch {
            snapshotFlow { offsetY.value }.collect { currentOffset ->
                if (!hasBeenOpened && currentOffset < maxOffset - 10f) {
                    hasBeenOpened = true
                }
            }
        }
    }

    private suspend fun initializeSession() {
        val latest = dao.getLatestRecord()
        if (latest != null) {
            selectedExerciseName = latest.exerciseName
            selectedExerciseId = latest.record.exerciseId
            loadInputsForExercise(latest.record.exerciseId)
        }
    }

    private suspend fun loadInputsForExercise(exerciseId: Int) {
        val latestForExercise = dao.getLatestRecordByExerciseId(exerciseId)
        if (latestForExercise != null) {
            sets = latestForExercise.record.sets.toString()
            reps = latestForExercise.record.reps.toString()
            weight = latestForExercise.record.weight.toString()
        } else {
            sets = defaultSets
            reps = defaultReps
            weight = defaultWeight
        }
    }

    override fun onExerciseSelected(exercise: Exercise) {
        selectedExerciseName = exercise.name
        selectedExerciseId = exercise.id
        showExerciseSelection = false
        scope.launch { loadInputsForExercise(exercise.id) }
    }

    override fun onAddClick() {
        scope.launch {
            try {
                val exerciseId = selectedExerciseId
                if (exerciseId == null) {
                    Toast.makeText(context, com.nnoidea.fitnez2.core.localization.globalLocalization.labelSelectExercise, Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val inputSets = setsRaw.ifEmpty { sets }.ifEmpty { setsFallback }
                val validatedSets = ValidateAndCorrect.sets(inputSets) ?: return@launch

                val inputReps = repsRaw.ifEmpty { reps }.ifEmpty { repsFallback }
                val validatedReps = ValidateAndCorrect.reps(inputReps) ?: return@launch

                val inputWeight = weightRaw.ifEmpty { weight }.ifEmpty { weightFallback }
                val validatedWeight = ValidateAndCorrect.weight(inputWeight) ?: return@launch

                focusManager.clearFocus()
                keyboardController?.hide()

                val record = Record(
                    exerciseId = exerciseId,
                    sets = validatedSets,
                    reps = validatedReps,
                    weight = validatedWeight,
                    date = System.currentTimeMillis()
                )

                val newId = dao.create(record)
                globalUiState.emitSignal(UiSignal.ScrollToTop(newId.toInt()))
                
                onHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_END)

            } catch (_: Exception) { }
        }
    }

    override fun onSetsChange(value: String) { 
        sets = value 
        if (value.isNotEmpty()) setsFallback = value
    }
    
    override fun onRepsChange(value: String) { 
        reps = value 
        if (value.isNotEmpty()) repsFallback = value
    }
    
    override fun onWeightChange(value: String) { 
        weight = value 
        if (value.isNotEmpty()) weightFallback = value
    }

    override suspend fun settleSpring(velocity: Float) {
        val targetOffset = if (velocity > 1000f || (velocity >= 0 && offsetY.value > maxOffset / 2)) {
            maxOffset // Collapse
        } else {
            onHapticFeedback(android.view.HapticFeedbackConstants.GESTURE_START)
            minOffset // Expand
        }
        offsetY.animateTo(
            targetValue = targetOffset,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
        )
    }

    override val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: NestedScrollSource): androidx.compose.ui.geometry.Offset {
            val delta = available.y
            if (delta < 0 && offsetY.value > minOffset + 1f) {
                scope.launch {
                    val newOffset = (offsetY.value + delta).coerceIn(minOffset, maxOffset)
                    offsetY.snapTo(newOffset)
                }
                return available
            }
            return androidx.compose.ui.geometry.Offset.Zero
        }

        override fun onPostScroll(consumed: androidx.compose.ui.geometry.Offset, available: androidx.compose.ui.geometry.Offset, source: NestedScrollSource): androidx.compose.ui.geometry.Offset {
            val delta = available.y
            if (delta > 0 && source == NestedScrollSource.UserInput) {
                scope.launch {
                    val newOffset = (offsetY.value + delta).coerceIn(minOffset, maxOffset)
                    offsetY.snapTo(newOffset)
                }
                return available
            }
            return androidx.compose.ui.geometry.Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            if (offsetY.value > minOffset + 1f && offsetY.value < maxOffset - 1f) {
                settleSpring(available.y)
                return available
            }
            return super.onPreFling(available)
        }
    }

    override fun onPredictiveBackProgress(progress: Float) {
        predictiveProgress = progress
    }

    override suspend fun onPredictiveBackCommit() {
        offsetY.animateTo(maxOffset, spring(stiffness = Spring.StiffnessMediumLow))
        predictiveProgress = 0f
    }

    override fun onPredictiveBackCancel() {
        scope.launch {
            Animatable(predictiveProgress).animateTo(0f) { predictiveProgress = value }
        }
    }

    override fun toggleExerciseSelection(show: Boolean) {
        showExerciseSelection = show
    }
}

@Composable
fun rememberPredictiveBottomSheetState(): PredictiveBottomSheetState {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val globalUiState = LocalGlobalUiState.current
    val database = LocalAppDatabase.current
    val settingsRepository = LocalSettingsRepository.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val view = androidx.compose.ui.platform.LocalView.current

    // Layout constants calculation (moved from UI to helper)
    val peekHeight = PREDICTIVE_BOTTOM_SHEET_PEEK_HEIGHT_DP.dp
    val peekHeightPx = with(density) { peekHeight.toPx() }
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val topPaddingPx = with(density) { WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx() }
    
    val maxOffset = screenHeightPx - topPaddingPx - peekHeightPx
    val minOffset = 0f

    return remember(maxOffset, minOffset) {
        RecordPredictiveBottomSheetState(
            scope = scope,
            dao = database.recordDao(),
            exerciseDao = database.exerciseDao(),
            settingsRepository = settingsRepository,
            globalUiState = globalUiState,
            keyboardController = keyboardController,
            focusManager = focusManager,
            context = context,
            maxOffset = maxOffset,
            minOffset = minOffset,
            onHapticFeedback = { view.performHapticFeedback(it) }
        )
    }
}
