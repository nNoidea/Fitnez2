package com.nnoidea.fitnez2.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.nnoidea.fitnez2.ui.components.SettingsCardGroup
import com.nnoidea.fitnez2.ui.components.SettingsDivider
import com.nnoidea.fitnez2.ui.components.SettingsGroup
import com.nnoidea.fitnez2.ui.components.SettingsItem
import com.nnoidea.fitnez2.ui.components.SettingsPageScaffold
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSettingsCardGroup_andSettingsItem_renderAndClick() {
        var clicked = false

        composeTestRule.setContent {
            Fitnez2Theme {
                SettingsCardGroup {
                    SettingsItem(
                        label = "Language",
                        value = "English",
                        icon = Icons.Default.Language,
                        onClick = { clicked = true }
                    )
                    SettingsDivider()
                    SettingsItem(
                        label = "Auto-rotate",
                        value = "Off",
                        onClick = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Language").assertIsDisplayed()
        composeTestRule.onNodeWithText("English").assertIsDisplayed()
        composeTestRule.onNodeWithText("Auto-rotate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Off").assertIsDisplayed()

        composeTestRule.onNodeWithText("Language").performClick()
        assertTrue(clicked)
    }

    @Test
    fun testSettingsPageScaffold_andSettingsGroupDsl_rendersAllItems() {
        var switchChecked by mutableStateOf(false)
        var actionClicked = false

        composeTestRule.setContent {
            Fitnez2Theme {
                SettingsPageScaffold(
                    title = "Developer Options",
                    onBack = {}
                ) {
                    SettingsGroup(title = "General") {
                        item(
                            label = "Color Palette",
                            value = "View theme colors",
                            icon = Icons.Default.Palette,
                            onClick = { actionClicked = true }
                        )
                        switchItem(
                            label = "Dark Mode Override",
                            checked = switchChecked,
                            onCheckedChange = { switchChecked = it }
                        )
                    }

                    SettingsGroup(title = "Custom Section") {
                        custom {
                            Text("Custom Slider Content")
                        }
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Developer Options").assertIsDisplayed()
        composeTestRule.onNodeWithText("General").assertIsDisplayed()
        composeTestRule.onNodeWithText("Color Palette").assertIsDisplayed()
        composeTestRule.onNodeWithText("View theme colors").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark Mode Override").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom Section").assertIsDisplayed()
        composeTestRule.onNodeWithText("Custom Slider Content").assertIsDisplayed()

        composeTestRule.onNodeWithText("Color Palette").performClick()
        assertTrue(actionClicked)

        composeTestRule.onNodeWithText("Dark Mode Override").performClick()
        assertTrue(switchChecked)
    }
}
