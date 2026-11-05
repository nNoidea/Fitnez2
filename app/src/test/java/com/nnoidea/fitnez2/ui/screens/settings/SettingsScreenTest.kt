package com.nnoidea.fitnez2.ui.screens.settings

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.data.AppDatabase
import com.nnoidea.fitnez2.service.SettingsService
import com.nnoidea.fitnez2.ui.common.ProvideGlobalUiState
import com.nnoidea.fitnez2.ui.common.rememberGlobalUiState
import com.nnoidea.fitnez2.ui.components.SettingsGroup
import com.nnoidea.fitnez2.ui.components.SettingsPageScaffold
import com.nnoidea.fitnez2.ui.theme.Fitnez2Theme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSettingsItem_renderAndClick() {
        var clicked = false

        composeTestRule.setContent {
            Fitnez2Theme {
                SettingsGroup {
                    item(
                        label = "Language",
                        value = "English",
                        icon = Icons.Default.Language,
                        onClick = { clicked = true }
                    )
                    item(
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

    @Test
    fun testSettingsGroup_radioItem_opensDialogAndSelects() {
        var selectedWeight by mutableStateOf("kg")

        composeTestRule.setContent {
            Fitnez2Theme {
                SettingsGroup {
                    radioItem(
                        label = "Weight Unit",
                        value = selectedWeight,
                        options = listOf("kg", "lb"),
                        selected = selectedWeight,
                        onSelected = { selectedWeight = it }
                    )
                }
            }
        }

        // Verify initial render
        composeTestRule.onNodeWithText("Weight Unit").assertIsDisplayed()
        composeTestRule.onNodeWithText("kg").assertIsDisplayed()

        // Click to open dialog
        composeTestRule.onNodeWithText("Weight Unit").performClick()

        // Verify dialog options displayed
        composeTestRule.onNodeWithText("lb").assertIsDisplayed()

        // Select lb
        composeTestRule.onNodeWithText("lb").performClick()
        assertEquals("lb", selectedWeight)
    }

    @Test
    fun testSettingsGroup_expressiveStyling_rendersAndFunctions() {
        var clicked = false

        composeTestRule.setContent {
            Fitnez2Theme {
                SettingsGroup(
                    title = "Expressive Group"
                ) {
                    item(
                        label = "Item One",
                        value = "Value One",
                        icon = Icons.Default.Language,
                        iconContainerColor = androidx.compose.ui.graphics.Color.Cyan,
                        onClick = { clicked = true }
                    )
                    item(
                        label = "Item Two",
                        value = "Value Two",
                        showChevron = true,
                        onClick = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Expressive Group").assertIsDisplayed()
        composeTestRule.onNodeWithText("Item One").assertIsDisplayed()
        composeTestRule.onNodeWithText("Value One").assertIsDisplayed()
        composeTestRule.onNodeWithText("Item Two").assertIsDisplayed()
        composeTestRule.onNodeWithText("Value Two").assertIsDisplayed()

        composeTestRule.onNodeWithText("Item One").performClick()
        assertTrue(clicked)
    }

    private lateinit var database: AppDatabase
    private lateinit var settingsService: SettingsService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        settingsService = SettingsService(context)
        LocalizationManager.setLanguage(null)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSettingsGroup_nightModeRadioItem_rendersAndSelects() {
        var selectedHour by mutableStateOf(0)

        composeTestRule.setContent {
            Fitnez2Theme {
                SettingsGroup {
                    radioItem(
                        label = "Night Owl Mode",
                        value = if (selectedHour == 0) "Off (00:00)" else String.format(java.util.Locale.US, "%02d:00", selectedHour),
                        options = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                        selected = selectedHour,
                        onSelected = { selectedHour = it },
                        labelProvider = { if (it == 0) "Off (00:00)" else String.format(java.util.Locale.US, "%02d:00", it) }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Night Owl Mode").assertIsDisplayed()
        composeTestRule.onNodeWithText("Off (00:00)").assertIsDisplayed()

        // Click to open dialog
        composeTestRule.onNodeWithText("Night Owl Mode").performClick()

        // Select 01:00
        composeTestRule.onNodeWithText("01:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("01:00").performClick()
        assertEquals(1, selectedHour)
    }

    @Test
    fun testSettingsScreen_rendersInExpectedGroupsAndOrder() {
        composeTestRule.setContent {
            val globalUiState = rememberGlobalUiState(settingsService)
            Fitnez2Theme(fontMode = globalUiState.fontMode) {
                ProvideGlobalUiState(
                    database = database,
                    settingsService = settingsService,
                    state = globalUiState
                ) {
                    SettingsScreen(onOpenDrawer = {})
                }
            }
        }

        // Group 1: Language, In-App Fonts, Auto-rotate
        composeTestRule.onNodeWithText("Language").assertIsDisplayed()
        composeTestRule.onNodeWithText("In-App Font").assertIsDisplayed()
        composeTestRule.onNodeWithText("Auto-rotate").assertIsDisplayed()

        // Group 2: Weight Unit, Default Exercise Values, Night Owl Mode
        composeTestRule.onNodeWithText("Weight Unit").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Default Exercise Values").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Night Owl Mode").performScrollTo().assertIsDisplayed()

        // Group 3: Export Data, Import Data
        composeTestRule.onNodeWithText("Export Data").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Import Data").performScrollTo().assertIsDisplayed()
    }
}
