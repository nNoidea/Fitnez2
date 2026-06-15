package com.nnoidea.fitnez2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.service.SettingsService
import com.nnoidea.fitnez2.ui.common.ProvideGlobalUiState
import com.nnoidea.fitnez2.ui.common.rememberGlobalUiState
import androidx.compose.ui.Modifier
import com.nnoidea.fitnez2.ui.components.navigation.PredictiveSidePanelContainer
import com.nnoidea.fitnez2.ui.components.navigation.PredictiveSidePanel
import com.nnoidea.fitnez2.ui.navigation.AppPage
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_PAGE_ROUTE = "extra_page_route"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Identify which page this instance should represent
        val route = intent.getStringExtra(EXTRA_PAGE_ROUTE) ?: AppPage.Timeline.route
        val currentPage = AppPage.entries.find { it.route == route } ?: AppPage.Timeline

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

                    ProvideGlobalUiState(
                        database = database,
                        settingsService = settingsService,
                        state = globalUiState
                    ) {
                        ModalNavigationDrawer(
                            drawerState = drawerState,

                            drawerContent = {
                                PredictiveSidePanelContainer(
                                    drawerState = drawerState,
                                    scope = scope
                                ) {
                                    PredictiveSidePanel(
                                        currentRoute = currentPage.route,
                                        onItemClick = { clickedRoute ->
                                            val sourceRoute = intent.getStringExtra("extra_source_route")
                                            if (clickedRoute == sourceRoute) {
                                                // If navigating to the screen we jumped from, simply finish the jumped timeline
                                                // to return back to the already running screen!
                                                scope.launch {
                                                    drawerState.snapTo(DrawerValue.Closed)
                                                    finish()
                                                }
                                            } else if (clickedRoute != currentPage.route) {
                                                if (clickedRoute == AppPage.Timeline.route) {
                                                    if (!isTaskRoot) {
                                                        scope.launch {
                                                            drawerState.snapTo(DrawerValue.Closed)
                                                            finish()
                                                        }
                                                    }
                                                } else {
                                                    scope.launch {
                                                        drawerState.snapTo(DrawerValue.Closed)

                                                        val intent =
                                                            Intent(
                                                                this@MainActivity,
                                                                MainActivity::class.java
                                                            ).apply {
                                                                putExtra(
                                                                    EXTRA_PAGE_ROUTE,
                                                                    clickedRoute
                                                                )
                                                            }

                                                        startActivity(intent)

                                                        if (!isTaskRoot) {
                                                            finish()
                                                        }
                                                    }
                                                }
                                            } else {
                                                // If we clicked the same route we are currently on, check if we are on a jumped Timeline
                                                if ((clickedRoute == AppPage.Timeline.route) && (!isTaskRoot)) {
                                                    scope.launch {
                                                        drawerState.snapTo(DrawerValue.Closed)
                                                        finish()
                                                    }
                                                } else {
                                                    scope.launch { drawerState.close() }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        ) {
                            currentPage.content { scope.launch { drawerState.open() } }
                        }
                    }

                }
            }
        }
    }
}
