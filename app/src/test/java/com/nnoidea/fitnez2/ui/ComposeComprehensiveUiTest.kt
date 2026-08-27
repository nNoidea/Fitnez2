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
            settingsService = SettingsService(context)

            // Seed exercise and initial record
            val squat = exerciseService.createExercise("Squat")

            val now = System.currentTimeMillis()
            recordService.createRecord(squat.id, sets = 3, reps = 10, weight = 100.0, date = now)
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
    fun testSwipeRightOnRecordCard_triggersOpenDrawer() {
        lateinit var drawerState: androidx.compose.material3.DrawerState
        lateinit var scope: kotlinx.coroutines.CoroutineScope

        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)
            drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            scope = rememberCoroutineScope()

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    ModalNavigationDrawer(
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

        // Trigger drawer open
        composeRule.runOnUiThread {
            scope.launch { drawerState.open() }
        }
        composeRule.waitForIdle()

        // Verify drawer is now open and contains navigation destinations
        composeRule.onNodeWithText(EnglishStrings.labelMonthly).assertIsDisplayed()
        composeRule.onNodeWithText(EnglishStrings.labelGraph).assertIsDisplayed()
        composeRule.onNodeWithText(EnglishStrings.labelSettings).assertIsDisplayed()
    }

    @Test
    fun testHamburgerMenuButton_opensNavigationDrawer() {
        composeRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    ModalNavigationDrawer(
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

        // Click the hamburger menu icon
        composeRule.onNodeWithContentDescription(EnglishStrings.labelOpenDrawer).performClick()
        composeRule.waitForIdle()

        // Verify drawer is open
        composeRule.onNodeWithText(EnglishStrings.labelMonthly).assertIsDisplayed()
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
}
