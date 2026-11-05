package com.nnoidea.fitnez2.ui.components.dialog

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.service.ExerciseService
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExerciseSelectionDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: AppDatabase
    private lateinit var exerciseService: ExerciseService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        exerciseService = ExerciseService(database)
        LocalizationManager.setLanguage(null)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testExerciseListRow_hasNoButtons_onlyDisplaysName() {
        val exercises = listOf(Exercise(id = "1", name = "Bench Press"))

        composeTestRule.setContent {
            Fitnez2Theme {
                ExerciseSelectionDialog(
                    show = true,
                    exercises = exercises,
                    selectedExerciseId = null,
                    exerciseService = exerciseService,
                    onDismissRequest = {},
                    onExerciseSelected = {}
                )
            }
        }

        // Exercise name is shown
        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()

        // Edit button is NOT present on the row
        composeTestRule.onAllNodesWithContentDescription(globalLocalization.labelEdit("Bench Press")).assertCountEquals(0)

        // Delete button is NOT present on the row
        composeTestRule.onAllNodesWithContentDescription(globalLocalization.labelDelete).assertCountEquals(0)
    }

    @Test
    fun testExerciseListRow_tapSelectsExercise() {
        val exercises = listOf(Exercise(id = "1", name = "Bench Press"))
        var selected: Exercise? = null

        composeTestRule.setContent {
            Fitnez2Theme {
                ExerciseSelectionDialog(
                    show = true,
                    exercises = exercises,
                    selectedExerciseId = null,
                    exerciseService = exerciseService,
                    onDismissRequest = {},
                    onExerciseSelected = { selected = it }
                )
            }
        }

        // Tap the exercise item
        composeTestRule.onNodeWithText("Bench Press").performClick()
        org.junit.Assert.assertEquals("1", selected?.id)
    }

    @Test
    fun testExerciseListRow_holdOpensEditDialog() {
        val exercises = listOf(Exercise(id = "1", name = "Bench Press"))

        composeTestRule.setContent {
            Fitnez2Theme {
                ExerciseSelectionDialog(
                    show = true,
                    exercises = exercises,
                    selectedExerciseId = null,
                    exerciseService = exerciseService,
                    onDismissRequest = {},
                    onExerciseSelected = {}
                )
            }
        }

        // Long press (hold) on the exercise item
        composeTestRule.onNodeWithText("Bench Press").performTouchInput { longClick() }

        // Edit dialog appears
        composeTestRule.onNodeWithText(globalLocalization.labelEditExercise).assertIsDisplayed()

        // Verify Delete, Cancel, and Save buttons are present inside the dialog
        composeTestRule.onNodeWithText(globalLocalization.labelDelete).assertIsDisplayed()
        composeTestRule.onNodeWithText(globalLocalization.labelCancel).assertIsDisplayed()
        composeTestRule.onNodeWithText(globalLocalization.labelSave).assertIsDisplayed()
    }

    @Test
    fun testEditExerciseDialog_clickingDelete_opensConfirmationWarningDialog() {
        val exercises = listOf(Exercise(id = "1", name = "Bench Press"))

        composeTestRule.setContent {
            Fitnez2Theme {
                ExerciseSelectionDialog(
                    show = true,
                    exercises = exercises,
                    selectedExerciseId = null,
                    exerciseService = exerciseService,
                    onDismissRequest = {},
                    onExerciseSelected = {}
                )
            }
        }

        // Hold to open edit dialog
        composeTestRule.onNodeWithText("Bench Press").performTouchInput { longClick() }

        // Click Delete inside edit dialog
        composeTestRule.onNodeWithText(globalLocalization.labelDelete).performClick()

        // Warning message confirmation dialog is now displayed
        composeTestRule.onNodeWithText(globalLocalization.labelDeleteExerciseWarning).assertIsDisplayed()
    }
}
