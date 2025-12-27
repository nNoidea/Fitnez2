package com.nnoidea.fitnez2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nnoidea.fitnez2.ui.animations.PredictiveRouteContainer
import com.nnoidea.fitnez2.ui.animations.PredictiveSidePanelContainer
import com.nnoidea.fitnez2.ui.animations.routeEnterTransition
import com.nnoidea.fitnez2.ui.animations.routeExitTransition
import com.nnoidea.fitnez2.ui.animations.routePopEnterTransition
import com.nnoidea.fitnez2.ui.animations.routePopExitTransition
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
                        color =
                                Color.Black.copy(alpha = 0.5f)
                                        .compositeOver(
                                                androidx.compose.material3.MaterialTheme.colorScheme
                                                        .background
                                        )
                ) {
                    val navController = rememberNavController()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                PredictiveSidePanelContainer(
                                        drawerState = drawerState,
                                        scope = scope
                                ) {
                                    SidePanel(
                                            items = AppPage.entries,
                                            currentRoute = currentRoute,
                                            onItemClick = { route ->
                                                scope.launch { drawerState.close() }
                                                if (currentRoute != route) {
                                                    navController.navigate(route) {
                                                        popUpTo(AppPage.Home.route) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                    )
                                }
                            }
                    ) {
                        NavHost(
                                navController = navController,
                                startDestination = AppPage.Home.route
                        ) {
                            AppPage.entries.forEach { page ->
                                composable(
                                        route = page.route,
                                        enterTransition = routeEnterTransition(),
                                        exitTransition = routeExitTransition(),
                                        popEnterTransition = routePopEnterTransition(),
                                        popExitTransition = routePopExitTransition()
                                ) {
                                    // Main Content
                                    PredictiveRouteContainer(
                                            navController = navController,
                                            enabled = page != AppPage.Home && !drawerState.isOpen,
                                            backgroundContent = { HomeScreen(onOpenDrawer = {}) }
                                    ) { page.content { scope.launch { drawerState.open() } } }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
