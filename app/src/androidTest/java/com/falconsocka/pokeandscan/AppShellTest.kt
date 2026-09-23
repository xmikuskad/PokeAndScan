package com.falconsocka.pokeandscan

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.res.Configuration
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class AppShellTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun libraryOpensNewScanAndBackReturnsToLibrary() {
        dismissFirstLaunchLanguageChoiceIfPresent()

        composeRule.onNodeWithText(appString(R.string.library_title)).assertIsDisplayed()
        composeRule.onNodeWithText(appString(R.string.new_scan_action)).performClick()
        composeRule.onNodeWithText(appString(R.string.new_scan_placeholder)).assertIsDisplayed()

        composeRule.activity.onBackPressedDispatcher.onBackPressed()
        composeRule.onNodeWithText(appString(R.string.library_title)).assertIsDisplayed()
    }

    @Test
    fun languageAndThemeSelectionsPersist() {
        dismissFirstLaunchLanguageChoiceIfPresent()
        composeRule.onNodeWithText(appString(R.string.settings_action)).performClick()

        composeRule.onNodeWithText(appString(R.string.theme_dark)).performClick()
        composeRule.onNodeWithText("Slovenčina").performClick()
        composeRule.onNodeWithText("Nastavenia").assertIsDisplayed()

        composeRule.runOnIdle {
            val preferences = AppPreferences(
                composeRule.activity.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            )
            assertEquals(ThemePreference.Dark, preferences.theme())
            assertEquals(AppLanguage.Slovak, preferences.languageOrDefault("en-US"))
        }
    }

    private fun dismissFirstLaunchLanguageChoiceIfPresent() {
        if (composeRule.onAllNodesWithText("English").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("English").performClick()
        }
    }

    private fun appString(resourceId: Int): String {
        val activity = composeRule.activity
        val language = AppPreferences(
            activity.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        ).languageOrDefault()
        val configuration = Configuration(activity.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(language.languageTag))
        }
        return activity.createConfigurationContext(configuration).getString(resourceId)
    }
}
