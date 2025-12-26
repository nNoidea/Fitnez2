package com.nnoidea.fitnez2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nnoidea.fitnez2.ui.animations.PredictiveRouteBackground
import com.nnoidea.fitnez2.ui.animations.RoutePredictiveBackHandler
import com.nnoidea.fitnez2.ui.animations.SidePanelPredictiveBackHandler
import com.nnoidea.fitnez2.ui.animations.predictiveRouteAnimation
import com.nnoidea.fitnez2.ui.animations.predictiveSidePanelAnimation
import com.nnoidea.fitnez2.ui.animations.rememberPredictiveRouteState
import com.nnoidea.fitnez2.ui.animations.rememberPredictiveSidePanelState
import com.nnoidea.fitnez2.ui.components.SidePanel
import com.nnoidea.fitnez2.ui.navigation.AppPage
import com.nnoidea.fitnez2.ui.screens.HomeScreen
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Fitnez2Theme {
                androidx.compose.material3.Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = androidx.compose.ui.graphics.Color.Black
                ) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    // Predictive Back Handler state
                    val predictiveSidePanelState = rememberPredictiveSidePanelState()

                    ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                SidePanel(
                                        items = AppPage.entries,
                                        currentRoute = currentRoute,
                                        onItemClick = { route ->
                                            scope.launch { drawerState.close() }
                                            if (currentRoute != route) {
                                                navController.navigate(route) {
                                                    popUpTo(AppPage.Home.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        modifier =
                                                Modifier.predictiveSidePanelAnimation(
                                                        predictiveSidePanelState
                                                )
                                )
                            }
                    ) {
                        NavHost(
                                navController = navController,
                                startDestination = AppPage.Home.route
                        ) {
                            AppPage.entries.forEach { page ->
                                composable(
                                        route = page.route,
                                        enterTransition = {
                                            androidx.compose.animation.EnterTransition.None
                                        },
                                        exitTransition = {
                                            androidx.compose.animation.ExitTransition.None
                                        },
                                        popEnterTransition = {
                                            androidx.compose.animation.EnterTransition.None
                                        },
                                        popExitTransition = {
                                            androidx.compose.animation.ExitTransition.None
                                        }
                                ) {
                                    // Predictive Back Handler for the Drawer
                                    SidePanelPredictiveBackHandler(
                                            predictiveState = predictiveSidePanelState,
                                            drawerState = drawerState,
                                            scope = scope
                                    )

                                    // Main Content
                                    val predictiveRouteState = rememberPredictiveRouteState()

                                    // Only apply predictive back for routes other than Home
                                    if (page != AppPage.Home) {
                                        RoutePredictiveBackHandler(
                                                predictiveState = predictiveRouteState,
                                                navController = navController,
                                                enabled = !drawerState.isOpen
                                        )
                                    }

                                    // Static Container Box
                                    Box {
                                        // Background Layer (Home Screen)
                                        // This sits behind the foreground and handles its own
                                        // scale/fade
                                        if (page != AppPage.Home) {
                                            PredictiveRouteBackground(
                                                    state = predictiveRouteState,
                                                    modifier = Modifier.matchParentSize()
                                            ) {
                                                HomeScreen(
                                                        onOpenDrawer = {}
                                                ) // Interaction disabled in background
                                            }
                                        }

                                        // Foreground Content
                                        // We apply the slide/shrink animation ONLY to this layer
                                        val foregroundModifier =
                                                if (page != AppPage.Home) {
                                                    Modifier.predictiveRouteAnimation(
                                                            predictiveRouteState
                                                    )
                                                } else {
                                                    Modifier
                                                }

                                        Box(modifier = foregroundModifier) {
                                            page.content { scope.launch { drawerState.open() } }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
