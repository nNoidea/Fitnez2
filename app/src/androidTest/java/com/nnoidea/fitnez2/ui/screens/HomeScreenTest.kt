package com.nnoidea.fitnez2.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nnoidea.fitnez2.core.localization.EnglishStrings
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.service.SettingsService
import com.nnoidea.fitnez2.ui.common.ProvideGlobalUiState
import com.nnoidea.fitnez2.ui.common.rememberGlobalUiState
import com.nnoidea.fitnez2.ui.screens.timeline.TimelineScreen
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun timelineScreen_displaysHelperText() {
        rule.setContent {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            val database: AppDatabase = remember { Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build() }
            val settingsService: SettingsService = remember { SettingsService(context) }
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

        rule.onNodeWithContentDescription(EnglishStrings.labelOpenDrawer)
            .assertIsDisplayed()
    }
}
