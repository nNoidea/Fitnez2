package com.nnoidea.fitnez2.ui.components.bottomsheet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.Velocity
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.models.RecordWithExercise
import com.nnoidea.fitnez2.service.ExerciseService
import com.nnoidea.fitnez2.service.SettingsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Base implementation of [PredictiveBottomSheetState] that contains ALL shared logic:
 * - Animation state (offsetY, predictiveProgress, isExpanded, hasBeenOpened)
 * - Form fields (committedSets, committedReps, committedWeight, pendingSets, pendingReps, pendingWeight, fallbacks)
 * - Settings collection (exercises, weight unit, defaults)
 * - Sheet physics (settleSpring, nestedScrollConnection, predictive back handlers)
 * - Exercise selection (onExerciseSelected, toggleExerciseSelection)
 *
 * Concrete implementations only need to override [onAddClick] and optionally [onExerciseSelected].
 */
@Stable
abstract class PredictiveBottomSheetState(
    protected val scope: CoroutineScope,
    protected val exerciseService: ExerciseService,
    protected val settingsService: SettingsService,
    protected val keyboardController: SoftwareKeyboardController?,
    protected val focusManager: FocusManager,
    protected val context: android.content.Context,
    val maxOffset: Float,
    val minOffset: Float,
    protected val onHapticFeedback: (Int) -> Unit
) {

    // ── Form Fields ──────────────────────────────────────────────────────

    var selectedExerciseId by mutableStateOf<String?>(null)
    var overrideExerciseName by mutableStateOf<String?>(null)
    open var selectedExerciseName: String?
        get() = exercises.find { it.id == selectedExerciseId }?.name ?: overrideExerciseName
        set(value) {
            overrideExerciseName = value
        }
    var committedSets by mutableStateOf("")
    var committedReps by mutableStateOf("")
    var committedWeight by mutableStateOf("")
    var weightUnit by mutableStateOf("kg")

    var pendingSets by mutableStateOf("")
    var pendingReps by mutableStateOf("")
    var pendingWeight by mutableStateOf("")
    var showExerciseSelection by mutableStateOf(false)
    var exercises by mutableStateOf<List<Exercise>>(emptyList())

    // Clear selection if the selected exercise gets deleted from the database
    init {
        scope.launch {
            snapshotFlow { exercises }.collect { currentExercises ->
                if (selectedExerciseId != null && currentExercises.none { it.id == selectedExerciseId }) {
                    selectedExerciseId = null
                    overrideExerciseName = null
                }
            }
        }
    }

    // ── Animation State ──────────────────────────────────────────────────

    val offsetY = Animatable(maxOffset)
    var predictiveProgress by mutableFloatStateOf(0f)
    val isExpanded by derivedStateOf { offsetY.value < maxOffset / 2 }
    var hasBeenOpened by mutableStateOf(false)

    // ── Defaults & Fallbacks ─────────────────────────────────────────────

    protected var defaultSets = "3"
    protected var defaultReps = "10"
    protected var defaultWeight = "20"

    protected var setsFallback = ""
    protected var repsFallback = ""
    protected var weightFallback = ""

    // ── Init: Collect settings flows ─────────────────────────────────────

    init {
        scope.launch {
            exerciseService.getAllExercisesFlow().collect { exercises = it }
        }
        scope.launch {
            settingsService.weightUnitFlow.collect { weightUnit = it }
        }
        scope.launch {
            settingsService.defaultSetsFlow.collect { defaultSets = it }
        }
        scope.launch {
            settingsService.defaultRepsFlow.collect { defaultReps = it }
        }
        scope.launch {
            settingsService.defaultWeightFlow.collect { defaultWeight = it }
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

    // ── Exercise Selection ───────────────────────────────────────────────

    open fun onExerciseSelected(exercise: Exercise, closeDialog: Boolean) {
        selectedExerciseName = exercise.name
        selectedExerciseId = exercise.id
        if (closeDialog) {
            showExerciseSelection = false
        }
    }

    protected suspend fun loadInputsForExercise(
        exerciseId: String,
        lookup: suspend (String) -> RecordWithExercise?
    ) {
        val latestForExercise = lookup(exerciseId)
        if (latestForExercise != null) {
            committedSets = latestForExercise.record.sets.toString()
            committedReps = latestForExercise.record.reps.toString()
            committedWeight = latestForExercise.record.weight.toString()
        } else {
            committedSets = defaultSets
            committedReps = defaultReps
            committedWeight = defaultWeight
        }
    }

    abstract fun onAddClick()

    fun toggleExerciseSelection(show: Boolean) {
        showExerciseSelection = show
    }

    // ── Form Field Changes ───────────────────────────────────────────────

    fun onCommittedSetsChange(value: String) {
        committedSets = value
        if (value.isNotEmpty()) setsFallback = value
    }

    fun onCommittedRepsChange(value: String) {
        committedReps = value
        if (value.isNotEmpty()) repsFallback = value
    }

    fun onCommittedWeightChange(value: String) {
        committedWeight = value
        if (value.isNotEmpty()) weightFallback = value
    }

    // ── Validated Input Resolution ───────────────────────────────────────

    /** Resolves sets from raw → committed → fallback. */
    protected fun resolveSets(): String = pendingSets.ifEmpty { committedSets }.ifEmpty { setsFallback }

    /** Resolves reps from raw → committed → fallback. */
    protected fun resolveReps(): String = pendingReps.ifEmpty { committedReps }.ifEmpty { repsFallback }

    /** Resolves weight from raw → committed → fallback. */
    protected fun resolveWeight(): String = pendingWeight.ifEmpty { committedWeight }.ifEmpty { weightFallback }

    /** Clears focus and hides keyboard. Call after successful add. */
    protected fun dismissInput() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    // ── Sheet Physics ────────────────────────────────────────────────────

    suspend fun settleSpring(velocity: Float) {
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

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(
            available: androidx.compose.ui.geometry.Offset,
            source: NestedScrollSource
        ): androidx.compose.ui.geometry.Offset {
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

        override fun onPostScroll(
            consumed: androidx.compose.ui.geometry.Offset,
            available: androidx.compose.ui.geometry.Offset,
            source: NestedScrollSource
        ): androidx.compose.ui.geometry.Offset {
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

    // ── Predictive Back ──────────────────────────────────────────────────

    fun onPredictiveBackProgress(progress: Float) {
        predictiveProgress = progress
    }

    suspend fun onPredictiveBackCommit() {
        kotlinx.coroutines.coroutineScope {
            launch {
                offsetY.animateTo(maxOffset, spring(stiffness = Spring.StiffnessMediumLow))
            }
            launch {
                Animatable(predictiveProgress).animateTo(
                    targetValue = 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) {
                    predictiveProgress = value
                }
            }
        }
    }

    fun onPredictiveBackCancel() {
        scope.launch {
            Animatable(predictiveProgress).animateTo(0f) { predictiveProgress = value }
        }
    }
}
