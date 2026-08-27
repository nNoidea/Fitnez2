package com.nnoidea.fitnez2.ui

import android.content.Context
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.core.localization.EnglishStrings
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.service.ExerciseService
import com.nnoidea.fitnez2.service.RecordService
import com.nnoidea.fitnez2.service.SettingsService
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.common.ProvideGlobalUiState
import com.nnoidea.fitnez2.ui.common.UiSignal
import com.nnoidea.fitnez2.ui.common.rememberGlobalUiState
import com.nnoidea.fitnez2.ui.components.navigation.PredictiveSidePanel
import com.nnoidea.fitnez2.ui.navigation.AppPage
import com.nnoidea.fitnez2.ui.screens.monthly.MonthlyScreen
import com.nnoidea.fitnez2.ui.screens.timeline.TimelineScreen
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ComposeComprehensiveUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var recordService: RecordService
    private lateinit var exerciseService: ExerciseService
    private lateinit var workoutService: com.nnoidea.fitnez2.service.WorkoutService
    private lateinit var settingsService: SettingsService

    @Before
    fun setUp() {
        runBlocking {
            context = ApplicationProvider.getApplicationContext()
            database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
            recordService = RecordService(database)
            exerciseService = ExerciseService(database)
            workoutService = com.nnoidea.fitnez2.service.WorkoutService(database)
            settingsService = SettingsService(context)

            // Seed exercise and initial record
            val squat = exerciseService.createExercise("Squat")

            val now = System.currentTimeMillis()
            recordService.createRecord(squat.id, sets = 3, reps = 10, weight = 100.0, date = now)

            com.nnoidea.fitnez2.core.localization.LocalizationManager.setLanguage(null)
            androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        }
    }

    @Test
    fun testRecordSwipeToDeleteAndUndo_restoresSingleItemWithoutDuplicates() {
        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)
            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    TimelineScreen(onOpenDrawer = {})
                }
            }
        }

        composeRule.waitForIdle()

        // Verify initial state: exactly 1 Squat record in the timeline list
        val timelineSquatMatcher = hasTestTag("record_card_Squat") and hasAnyAncestor(hasTestTag("main_timeline_record_list"))

        composeRule.onAllNodes(timelineSquatMatcher).assertCountEquals(1)

        // Swipe left on the Squat record card to delete
        composeRule.onNode(timelineSquatMatcher).performTouchInput {
            swipeLeft()
        }
        composeRule.waitForIdle()

        // Verify Squat record is deleted from the timeline
        composeRule.onAllNodes(timelineSquatMatcher).assertCountEquals(0)

        // Verify Undo snackbar is visible
        composeRule.onNodeWithText(EnglishStrings.labelUndo).assertIsDisplayed()

        // Tap Undo
        composeRule.onNodeWithText(EnglishStrings.labelUndo).performClick()
        composeRule.waitForIdle()

        // Verify Squat is restored and there is EXACTLY 1 instance (no duplicates!)
        composeRule.onAllNodes(timelineSquatMatcher).assertCountEquals(1)
    }

    @Test
    fun testSwipeRightToOpenAndSwipeLeftToCloseDrawer() {
        lateinit var drawerState: com.nnoidea.fitnez2.ui.components.navigation.PredictiveDrawerState
        lateinit var scope: kotlinx.coroutines.CoroutineScope

        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    drawerState = com.nnoidea.fitnez2.ui.components.navigation.rememberPredictiveDrawerState(initialValue = DrawerValue.Closed)
                    scope = rememberCoroutineScope()

                    com.nnoidea.fitnez2.ui.components.navigation.PredictiveNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            PredictiveSidePanel(
                                currentRoute = AppPage.Timeline.route,
                                onItemClick = {}
                            )
                        }
                    ) {
                        TimelineScreen(
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                }
            }
        }

        composeRule.waitForIdle()

        // 1. Swipe right directly on top of the "Squat" record card in the timeline to OPEN drawer
        val timelineSquatMatcher = hasTestTag("record_card_Squat") and hasAnyAncestor(hasTestTag("main_timeline_record_list"))
        composeRule.onNode(timelineSquatMatcher).performTouchInput {
            swipeRight(startX = centerX, endX = centerX + 400f)
        }
        composeRule.waitForIdle()

        // Verify drawer is now open and contains navigation destinations
        composeRule.onNodeWithText(EnglishStrings.labelMonthly).assertIsDisplayed()
        composeRule.onNodeWithText(EnglishStrings.labelGraph).assertIsDisplayed()
        composeRule.onNodeWithText(EnglishStrings.labelSettings).assertIsDisplayed()

        // 2. Swipe LEFT on the open side panel to CLOSE the drawer
        composeRule.onNodeWithText(EnglishStrings.labelMonthly).performTouchInput {
            swipeLeft(startX = centerX, endX = centerX - 400f)
        }
        composeRule.waitForIdle()

        // Verify drawer is closed
        assert(drawerState.isClosed)
        assert(drawerState.progress == 0f)
    }

    @Test
    fun testHamburgerMenuButton_opensNavigationDrawer() {
        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    val drawerState = com.nnoidea.fitnez2.ui.components.navigation.rememberPredictiveDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    com.nnoidea.fitnez2.ui.components.navigation.PredictiveNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            PredictiveSidePanel(
                                currentRoute = AppPage.Timeline.route,
                                onItemClick = {}
                            )
                        }
                    ) {
                        TimelineScreen(
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                }
            }
        }

        composeRule.waitForIdle()

        // Tap the hamburger menu button
        composeRule.onNodeWithContentDescription("Open Navigation Drawer").performClick()
        composeRule.waitForIdle()

        // Verify drawer opened
        composeRule.onNodeWithText(EnglishStrings.labelMonthly).assertIsDisplayed()
        composeRule.onNodeWithText(EnglishStrings.labelGraph).assertIsDisplayed()
        composeRule.onNodeWithText(EnglishStrings.labelSettings).assertIsDisplayed()
    }

    @Test
    fun testDrawerScrimClick_closesDrawer() {
        lateinit var drawerState: com.nnoidea.fitnez2.ui.components.navigation.PredictiveDrawerState
        lateinit var scope: kotlinx.coroutines.CoroutineScope

        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    drawerState = com.nnoidea.fitnez2.ui.components.navigation.rememberPredictiveDrawerState(initialValue = DrawerValue.Open)
                    scope = rememberCoroutineScope()

                    com.nnoidea.fitnez2.ui.components.navigation.PredictiveNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            PredictiveSidePanel(
                                currentRoute = AppPage.Timeline.route,
                                onItemClick = {}
                            )
                        }
                    ) {
                        TimelineScreen(onOpenDrawer = {})
                    }
                }
            }
        }

        composeRule.waitForIdle()

        // Verify drawer is initially open
        assert(drawerState.isOpen)
        composeRule.onNodeWithText(EnglishStrings.labelMonthly).assertIsDisplayed()

        // Tap the scrim overlay outside the drawer
        composeRule.onNodeWithTag("drawer_scrim").performClick()
        composeRule.waitForIdle()

        // Verify drawer is closed
        assert(drawerState.isClosed)
        assert(drawerState.progress == 0f)
    }

    @Test
    fun testAddingRecordsBackToBack_updatesTimelineCorrectly() {
        val squatId = runBlocking { exerciseService.getAllExercises().first().id }

        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)
            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    TimelineScreen(onOpenDrawer = {})
                }
            }
        }

        val timelineSquatMatcher = hasTestTag("record_card_Squat") and hasAnyAncestor(hasTestTag("main_timeline_record_list"))

        // Wait for initial records to load in main timeline
        composeRule.onAllNodes(timelineSquatMatcher).assertCountEquals(1)

        // Insert first back-to-back record
        runBlocking {
            val rec2 = recordService.createRecord(
                exerciseId = squatId,
                sets = 3,
                reps = 10,
                weight = 100.0,
                date = System.currentTimeMillis()
            )
            GlobalUiState.emitToAll(UiSignal.RecordInserted(rec2.id))
        }
        composeRule.waitForIdle()

        // Verify that Squat records count in main timeline increased to 2
        composeRule.waitUntil(5000) {
            composeRule.onAllNodes(timelineSquatMatcher).fetchSemanticsNodes().size == 2
        }

        // Insert second back-to-back record
        runBlocking {
            val rec3 = recordService.createRecord(
                exerciseId = squatId,
                sets = 3,
                reps = 10,
                weight = 100.0,
                date = System.currentTimeMillis() + 100
            )
            GlobalUiState.emitToAll(UiSignal.RecordInserted(rec3.id))
        }
        composeRule.waitForIdle()

        // Verify that Squat records count in main timeline increased to 3
        composeRule.waitUntil(5000) {
            composeRule.onAllNodes(timelineSquatMatcher).fetchSemanticsNodes().size == 3
        }
    }

    @Test
    fun testSwipingUpOnBottomSheet_expandsSheet() {
        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)
            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    TimelineScreen(onOpenDrawer = {})
                }
            }
        }

        composeRule.waitForIdle()

        // Swipe up on the bottom sheet exercise selector button area
        composeRule.onNodeWithTag("exercise_selector_button").performTouchInput {
            swipeUp(startY = centerY, endY = centerY - 150f)
        }
        composeRule.waitForIdle()

        // Verify bottom sheet form elements remain responsive and accessible
        composeRule.onNodeWithTag("exercise_selector_button").assertExists()
        composeRule.onNodeWithTag("add_record_button").assertExists()
    }

    @Test
    fun testMonthlyToTimelineDate_backGestureReturnsToMonthly() {
        lateinit var navController: androidx.navigation.NavHostController

        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)
            navController = rememberNavController()

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = AppPage.Monthly.route
                    ) {
                        composable(AppPage.Monthly.route) {
                            MonthlyScreen(
                                onOpenDrawer = {},
                                onNavigateToTimelineDate = { epochMillis: Long ->
                                    navController.navigate("${AppPage.Timeline.route}?targetDate=$epochMillis")
                                }
                            )
                        }

                        composable(
                            route = "${AppPage.Timeline.route}?targetDate={targetDate}",
                            arguments = listOf(
                                androidx.navigation.navArgument("targetDate") {
                                    type = androidx.navigation.NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) {
                            TimelineScreen(onOpenDrawer = {})
                        }
                    }
                }
            }
        }

        composeRule.waitForIdle()

        // Verify currently on Monthly screen
        org.junit.Assert.assertEquals(AppPage.Monthly.route, navController.currentBackStackEntry?.destination?.route)

        // Navigate to Timeline with target date (simulating tapping a day on the monthly calendar)
        val targetDayMillis = System.currentTimeMillis()
        composeRule.runOnUiThread {
            navController.navigate("${AppPage.Timeline.route}?targetDate=$targetDayMillis")
        }
        composeRule.waitForIdle()

        // Verify navigated to Timeline destination
        org.junit.Assert.assertEquals(
            "${AppPage.Timeline.route}?targetDate={targetDate}",
            navController.currentBackStackEntry?.destination?.route
        )

        // Perform back navigation
        composeRule.runOnUiThread {
            navController.popBackStack()
        }
        composeRule.waitForIdle()

        // Verify popped back and currently on Monthly screen again!
        org.junit.Assert.assertEquals(AppPage.Monthly.route, navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun testBottomSheet_filtersTimelineBySelectedExercise() {
        lateinit var bench: com.nnoidea.fitnez2.data.entities.Exercise
        runBlocking {
            bench = exerciseService.createExercise("Bench Press")
            recordService.createRecord(bench.id, sets = 4, reps = 8, weight = 80.0, date = System.currentTimeMillis() - 500)
        }

        lateinit var sheetState: com.nnoidea.fitnez2.ui.screens.timeline.HomeBottomSheetState

        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    sheetState = com.nnoidea.fitnez2.ui.screens.timeline.rememberHomeBottomSheetState()
                    TimelineScreen(
                        onOpenDrawer = {},
                        bottomSheetState = sheetState
                    )
                }
            }
        }

        composeRule.waitForIdle()

        val bsSquatMatcher = hasTestTag("record_card_Squat") and hasAnyAncestor(hasTestTag("bottom_sheet_record_list"))
        val bsBenchMatcher = hasTestTag("record_card_Bench Press") and hasAnyAncestor(hasTestTag("bottom_sheet_record_list"))

        // When Squat is selected: bottom sheet timeline only displays Squat (0 Bench Press)
        composeRule.onAllNodes(bsSquatMatcher).assertCountEquals(1)
        composeRule.onAllNodes(bsBenchMatcher).assertCountEquals(0)

        // Select Bench Press exercise
        composeRule.runOnUiThread {
            sheetState.onExerciseSelected(bench, closeDialog = true)
        }
        composeRule.waitForIdle()

        // Verify bottom sheet timeline updates to show ONLY Bench Press records and 0 Squat records
        composeRule.onAllNodes(bsBenchMatcher).assertCountEquals(1)
        composeRule.onAllNodes(bsSquatMatcher).assertCountEquals(0)
    }

    @Test
    fun testBottomSheet_filtersTimelineBySelectedWorkout() {
        lateinit var squat: com.nnoidea.fitnez2.data.entities.Exercise
        lateinit var bench: com.nnoidea.fitnez2.data.entities.Exercise
        lateinit var deadlift: com.nnoidea.fitnez2.data.entities.Exercise
        lateinit var workout: com.nnoidea.fitnez2.data.entities.Workout

        runBlocking {
            bench = exerciseService.createExercise("Bench Press")
            deadlift = exerciseService.createExercise("Deadlift")
            squat = exerciseService.getExerciseByName("Squat")!!

            recordService.createRecord(bench.id, sets = 3, reps = 10, weight = 80.0, date = System.currentTimeMillis() - 400)
            recordService.createRecord(deadlift.id, sets = 1, reps = 5, weight = 150.0, date = System.currentTimeMillis() - 300)

            // Create a workout containing only Squat and Bench Press (NOT Deadlift)
            workout = workoutService.createWorkout("Strength Workout")
            workoutService.addRecordToWorkout(workout.id, squat.id, sets = 3, reps = 10, weight = 100.0)
            workoutService.addRecordToWorkout(workout.id, bench.id, sets = 3, reps = 10, weight = 80.0)
        }

        lateinit var sheetState: com.nnoidea.fitnez2.ui.screens.timeline.HomeBottomSheetState

        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    sheetState = com.nnoidea.fitnez2.ui.screens.timeline.rememberHomeBottomSheetState()
                    TimelineScreen(
                        onOpenDrawer = {},
                        bottomSheetState = sheetState
                    )
                }
            }
        }

        composeRule.waitForIdle()

        val bsSquatMatcher = hasTestTag("record_card_Squat") and hasAnyAncestor(hasTestTag("bottom_sheet_record_list"))
        val bsBenchMatcher = hasTestTag("record_card_Bench Press") and hasAnyAncestor(hasTestTag("bottom_sheet_record_list"))
        val bsDeadliftMatcher = hasTestTag("record_card_Deadlift") and hasAnyAncestor(hasTestTag("bottom_sheet_record_list"))

        // Select the workout
        composeRule.runOnUiThread {
            sheetState.onWorkoutSelected(workout, closeDialog = true)
        }
        composeRule.waitForIdle()

        // Verify bottom sheet timeline includes records from the workout (Squat and Bench Press), but EXCLUDES Deadlift
        composeRule.onAllNodes(bsSquatMatcher).assertCountEquals(1)
        composeRule.onAllNodes(bsBenchMatcher).assertCountEquals(1)
        composeRule.onAllNodes(bsDeadliftMatcher).assertCountEquals(0)
    }

    @Test
    fun testRecordDeleteAndUndo_restoresExactOriginalOrderWithoutMergingAtTop() {
        val today = System.currentTimeMillis()
        lateinit var squat: com.nnoidea.fitnez2.data.entities.Exercise
        lateinit var rec1: com.nnoidea.fitnez2.data.entities.Record
        lateinit var rec2: com.nnoidea.fitnez2.data.entities.Record
        lateinit var rec3: com.nnoidea.fitnez2.data.entities.Record

        runBlocking {
            squat = exerciseService.getExerciseByName("Squat")!!
            val initialRecords = recordService.getLatestRecords()
            initialRecords.forEach { recordService.deleteRecord(it.id) }

            rec1 = recordService.createRecord(squat.id, sets = 1, reps = 5, weight = 60.0, date = today)
            rec2 = recordService.createRecord(squat.id, sets = 1, reps = 5, weight = 80.0, date = today)
            rec3 = recordService.createRecord(squat.id, sets = 1, reps = 5, weight = 100.0, date = today)
        }

        lateinit var globalUiState: com.nnoidea.fitnez2.ui.common.GlobalUiState

        composeRule.setContent {
            globalUiState = rememberGlobalUiState(settingsService)

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    TimelineScreen(onOpenDrawer = {})
                }
            }
        }

        composeRule.waitForIdle()

        val timelineSquatMatcher = hasTestTag("record_card_Squat") and hasAnyAncestor(hasTestTag("main_timeline_record_list"))
        composeRule.onAllNodes(timelineSquatMatcher).assertCountEquals(3)

        // Delete the bottom record (rec1 = 60kg) and show undo snackbar
        runBlocking {
            val snapshot = recordService.getRecordById(rec1.id)!!
            recordService.deleteRecord(rec1.id)
            com.nnoidea.fitnez2.ui.common.GlobalUiState.emitToAll(com.nnoidea.fitnez2.ui.common.UiSignal.RecordDeleted(rec1.id))
            globalUiState.showSnackbar(
                message = EnglishStrings.labelRecordDeleted,
                actionLabel = EnglishStrings.labelUndo,
                onActionPerformed = {
                    kotlinx.coroutines.runBlocking {
                        val restored = recordService.restoreRecord(snapshot)
                        com.nnoidea.fitnez2.ui.common.GlobalUiState.emitToAll(com.nnoidea.fitnez2.ui.common.UiSignal.RecordInserted(restored.id))
                    }
                }
            )
        }

        composeRule.waitForIdle()
        composeRule.onAllNodes(timelineSquatMatcher).assertCountEquals(2)

        // Click Undo
        composeRule.onNodeWithText(EnglishStrings.labelUndo).performClick()
        composeRule.waitForIdle()

        // Verify all 3 records are back in timeline
        composeRule.onAllNodes(timelineSquatMatcher).assertCountEquals(3)

        // Verify that in the database, records maintain exact ordering: rec3 (100kg, order 3), rec2 (80kg, order 2), rec1 (60kg, order 1)
        val allRecordsAfterUndo = runBlocking { recordService.getLatestRecords() }
        org.junit.Assert.assertEquals(3, allRecordsAfterUndo.size)
        org.junit.Assert.assertEquals(100.0, allRecordsAfterUndo[0].weight, 0.01) // Top record is still 100kg
        org.junit.Assert.assertEquals(80.0, allRecordsAfterUndo[1].weight, 0.01)  // Middle record is still 80kg
        org.junit.Assert.assertEquals(60.0, allRecordsAfterUndo[2].weight, 0.01)  // Restored record goes back to the bottom (60kg) - NOT top!
        org.junit.Assert.assertEquals(rec1.id, allRecordsAfterUndo[2].id)
        org.junit.Assert.assertEquals(rec1.orderNumber, allRecordsAfterUndo[2].orderNumber)
    }

    @Test
    fun testClosingDrawer_allowsImmediateContentInteractionsWithoutWaitingForAnimation() {
        lateinit var drawerState: com.nnoidea.fitnez2.ui.components.navigation.PredictiveDrawerState
        lateinit var scope: kotlinx.coroutines.CoroutineScope
        lateinit var sheetState: com.nnoidea.fitnez2.ui.screens.timeline.HomeBottomSheetState

        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    drawerState = com.nnoidea.fitnez2.ui.components.navigation.rememberPredictiveDrawerState(initialValue = DrawerValue.Open)
                    scope = rememberCoroutineScope()
                    sheetState = com.nnoidea.fitnez2.ui.screens.timeline.rememberHomeBottomSheetState()

                    com.nnoidea.fitnez2.ui.components.navigation.PredictiveNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            PredictiveSidePanel(
                                currentRoute = AppPage.Timeline.route,
                                onItemClick = {}
                            )
                        }
                    ) {
                        TimelineScreen(
                            onOpenDrawer = {},
                            bottomSheetState = sheetState
                        )
                    }
                }
            }
        }

        composeRule.waitForIdle()

        // 1. Drawer is initially open
        assert(drawerState.isOpen)

        // 2. Initiate closing the drawer
        composeRule.runOnUiThread {
            scope.launch { drawerState.close() }
        }

        // 3. Immediately swipe left on the Squat record card to delete it on the main screen
        val timelineSquatMatcher = hasTestTag("record_card_Squat") and hasAnyAncestor(hasTestTag("main_timeline_record_list"))
        composeRule.onNode(timelineSquatMatcher).performTouchInput {
            swipeLeft()
        }
        composeRule.waitForIdle()

        // Verify that the record was deleted and Undo snackbar appeared immediately
        composeRule.onAllNodes(timelineSquatMatcher).assertCountEquals(0)
        composeRule.onNodeWithText(EnglishStrings.labelUndo).assertIsDisplayed()
    }

    @Test
    fun testClosingDrawer_allowsImmediateBottomSheetSwipeUp() {
        lateinit var drawerState: com.nnoidea.fitnez2.ui.components.navigation.PredictiveDrawerState
        lateinit var scope: kotlinx.coroutines.CoroutineScope
        lateinit var sheetState: com.nnoidea.fitnez2.ui.screens.timeline.HomeBottomSheetState

        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    drawerState = com.nnoidea.fitnez2.ui.components.navigation.rememberPredictiveDrawerState(initialValue = DrawerValue.Open)
                    scope = rememberCoroutineScope()
                    sheetState = com.nnoidea.fitnez2.ui.screens.timeline.rememberHomeBottomSheetState()

                    com.nnoidea.fitnez2.ui.components.navigation.PredictiveNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            PredictiveSidePanel(
                                currentRoute = AppPage.Timeline.route,
                                onItemClick = {}
                            )
                        }
                    ) {
                        TimelineScreen(
                            onOpenDrawer = {},
                            bottomSheetState = sheetState
                        )
                    }
                }
            }
        }

        composeRule.waitForIdle()

        // 1. Drawer is initially open, bottom sheet is collapsed
        assert(drawerState.isOpen)
        assert(!sheetState.isExpanded)

        // 2. Initiate closing the drawer
        composeRule.runOnUiThread {
            scope.launch { drawerState.close() }
        }

        // 3. Immediately swipe up on the bottom sheet exercise selector button area
        composeRule.onNodeWithTag("exercise_selector_button").performTouchInput {
            swipeUp(startY = centerY, endY = centerY - 500f)
        }
        composeRule.waitForIdle()

        // 4. Verify bottom sheet elements are visible and accessible
        composeRule.onNodeWithTag("exercise_selector_button").assertExists()
        composeRule.onNodeWithTag("add_record_button").assertExists()
    }
}
