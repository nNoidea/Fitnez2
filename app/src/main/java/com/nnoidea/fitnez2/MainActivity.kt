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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.nnoidea.fitnez2.ui.common.GlobalUiState
import com.nnoidea.fitnez2.ui.common.LocalGlobalUiState
import com.nnoidea.fitnez2.ui.common.ProvideGlobalUiState
import com.nnoidea.fitnez2.ui.common.rememberGlobalUiState
import androidx.compose.ui.Modifier
import com.nnoidea.fitnez2.ui.animations.predictiveSidePanelContainer
import com.nnoidea.fitnez2.ui.components.PredictiveSidePanel
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
        val route = intent.getStringExtra(EXTRA_PAGE_ROUTE) ?: AppPage.Home.route
        val currentPage = AppPage.entries.find { it.route == route } ?: AppPage.Home

        setContent {
            Fitnez2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val globalUiState = rememberGlobalUiState()

                    // Sync Drawer State to Global UI State
                    LaunchedEffect(drawerState.isOpen) {
                        globalUiState.isOverlayOpen = drawerState.isOpen
                    }

                    ProvideGlobalUiState(globalUiState) {
                        ModalNavigationDrawer(
                            drawerState = drawerState,

                        drawerContent = {
                            predictiveSidePanelContainer(
                                drawerState = drawerState,
                                scope = scope
                            ) {
                                PredictiveSidePanel(
                                    currentRoute = currentPage.route,
                                    onItemClick = { clickedRoute ->
                                        if (clickedRoute != currentPage.route) {
                                            if (clickedRoute == AppPage.Home.route) {
                                                if (currentPage != AppPage.Home) {
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

                                                    if (currentPage != AppPage.Home) {
                                                        finish()
                                                    }
                                                }
                                            }
                                        } else {
                                            scope.launch { drawerState.close() }
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
