package com.nnoidea.fitnez2.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nnoidea.fitnez2.core.localization.EnglishStrings
import com.nnoidea.fitnez2.ui.common.ProvideGlobalUiState
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun homeScreen_displaysHelperText() {
        // Start the app with the HomeScreen
        rule.setContent {
            // We need to provide the GlobalUiState so the screen can access
            // things like settings, language, etc.
            ProvideGlobalUiState {
                Fitnez2Theme {
                    HomeScreen(onOpenDrawer = {})
                }
            }
        }

        // Use the testing robot to look for the hamburger menu by its content description
        // This confirms the screen is actually rendered and the top bar area is visible.
        rule.onNodeWithContentDescription(EnglishStrings.labelOpenDrawer)
            .assertIsDisplayed()
    }
}
