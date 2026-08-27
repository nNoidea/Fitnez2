package com.nnoidea.fitnez2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.service.SettingsService
import com.nnoidea.fitnez2.ui.common.ProvideGlobalUiState
import com.nnoidea.fitnez2.ui.common.rememberGlobalUiState
import com.nnoidea.fitnez2.ui.components.navigation.PredictiveSidePanel
import com.nnoidea.fitnez2.ui.navigation.AppPage
import com.nnoidea.fitnez2.ui.screens.developer.DeveloperOptionsScreen
import com.nnoidea.fitnez2.ui.screens.graph.GraphScreen
import com.nnoidea.fitnez2.ui.screens.monthly.MonthlyScreen
import com.nnoidea.fitnez2.ui.screens.settings.SettingsScreen
import com.nnoidea.fitnez2.ui.screens.timeline.TimelineScreen
import com.nnoidea.fitnez2.ui.screens.workout.WorkoutScreen
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_PAGE_ROUTE = "extra_page_route"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val scope = rememberCoroutineScope()
            val database = remember { AppDatabase.getDatabase(this@MainActivity, scope) }
            val settingsService = remember { SettingsService(this@MainActivity) }
            val globalUiState = rememberGlobalUiState(settingsService)

            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                    // Sync Drawer State to Global UI State
                    LaunchedEffect(drawerState.isOpen) {
                        globalUiState.isOverlayOpen = drawerState.isOpen
                    }

                    // Handle Rotation Mode
                    val rotationMode = globalUiState.rotationMode
                    LaunchedEffect(rotationMode) {
                        requestedOrientation = when (rotationMode) {
                            com.nnoidea.fitnez2.core.RotationMode.ON -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                            com.nnoidea.fitnez2.core.RotationMode.OFF -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            else -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination?.route?.substringBefore("?") ?: AppPage.Timeline.route

                    ProvideGlobalUiState(
                        database = database,
                        settingsService = settingsService,
                        state = globalUiState
                    ) {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            gesturesEnabled = currentDestination in listOf(
                                AppPage.Timeline.route,
                                AppPage.Monthly.route,
                                AppPage.Graph.route,
                                AppPage.Settings.route
                            ),
                            drawerContent = {
                                PredictiveSidePanel(
                                    currentRoute = currentDestination,
                                    onItemClick = { clickedRoute ->
                                        scope.launch {
                                            drawerState.close()
                                        }
                                        if (clickedRoute != currentDestination) {
                                            navController.navigate(clickedRoute) {
                                                popUpTo(AppPage.Timeline.route) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = AppPage.Timeline.route,
                                enterTransition = { slideInHorizontally { it / 3 } + fadeIn() },
                                exitTransition = { slideOutHorizontally { -it / 3 } + fadeOut() },
                                popEnterTransition = { slideInHorizontally { -it / 3 } + fadeIn() },
                                popExitTransition = { slideOutHorizontally { it / 3 } + fadeOut() }
                            ) {
                                composable(
                                    route = AppPage.Timeline.route,
                                    arguments = listOf(
                                        navArgument("targetDate") {
                                            type = NavType.LongType
                                            defaultValue = -1L
                                        }
                                    )
                                ) { backStackEntry ->
                                    val targetDate = backStackEntry.arguments?.getLong("targetDate")?.takeIf { it != -1L }
                                    TimelineScreen(
                                        targetDate = targetDate,
                                        onOpenDrawer = { scope.launch { drawerState.open() } },
                                        onNavigateToWorkout = { workoutId ->
                                            if (workoutId != null) {
                                                navController.navigate("workout?workoutId=$workoutId")
                                            } else {
                                                navController.navigate(AppPage.Workout.route)
                                            }
                                        }
                                    )
                                }

                                composable(
                                    route = "${AppPage.Timeline.route}?targetDate={targetDate}",
                                    arguments = listOf(
                                        navArgument("targetDate") {
                                            type = NavType.LongType
                                            defaultValue = -1L
                                        }
                                    )
                                ) { backStackEntry ->
                                    val targetDate = backStackEntry.arguments?.getLong("targetDate")?.takeIf { it != -1L }
                                    TimelineScreen(
                                        targetDate = targetDate,
                                        onOpenDrawer = { scope.launch { drawerState.open() } },
                                        onNavigateToWorkout = { workoutId ->
                                            if (workoutId != null) {
                                                navController.navigate("workout?workoutId=$workoutId")
                                            } else {
                                                navController.navigate(AppPage.Workout.route)
                                            }
                                        }
                                    )
                                }

                                composable(AppPage.Monthly.route) {
                                    MonthlyScreen(
                                        onOpenDrawer = { scope.launch { drawerState.open() } },
                                        onNavigateToTimelineDate = { epochMillis ->
                                            navController.navigate("${AppPage.Timeline.route}?targetDate=$epochMillis") {
                                                popUpTo(AppPage.Timeline.route) {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }

                                composable(AppPage.Graph.route) {
                                    GraphScreen(
                                        onOpenDrawer = { scope.launch { drawerState.open() } }
                                    )
                                }

                                composable(AppPage.Settings.route) {
                                    SettingsScreen(
                                        onOpenDrawer = { scope.launch { drawerState.open() } },
                                        onNavigateToDeveloper = {
                                            navController.navigate(AppPage.Developer.route)
                                        }
                                    )
                                }

                                composable(
                                    route = "workout?workoutId={workoutId}",
                                    arguments = listOf(
                                        navArgument("workoutId") {
                                            type = NavType.StringType
                                            nullable = true
                                            defaultValue = null
                                        }
                                    )
                                ) { backStackEntry ->
                                    val workoutId = backStackEntry.arguments?.getString("workoutId")
                                    WorkoutScreen(
                                        workoutId = workoutId,
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable(AppPage.Workout.route) {
                                    WorkoutScreen(
                                        workoutId = null,
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable(AppPage.Developer.route) {
                                    DeveloperOptionsScreen(
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
