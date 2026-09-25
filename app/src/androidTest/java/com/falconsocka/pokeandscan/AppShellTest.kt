package com.falconsocka.pokeandscan

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class AppShellTest {
    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testRule: RuleChain = RuleChain.outerRule(ClearAppPreferencesRule()).around(composeRule)

    @Test
    fun libraryOpensNewScanAndBackReturnsToLibrary() {
        dismissFirstLaunchLanguageChoiceIfPresent()

        composeRule.onNodeWithText(appString(R.string.library_title)).assertIsDisplayed()
        composeRule.onNodeWithText(appString(R.string.new_scan_action)).performClick()
        composeRule.onNodeWithText(appString(R.string.new_scan_state_title)).assertIsDisplayed()

        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNodeWithText(appString(R.string.library_title)).assertIsDisplayed()
    }

    @Test
    fun languageAndThemeSelectionsPersist() {
        dismissFirstLaunchLanguageChoiceIfPresent()
        composeRule.onNodeWithText(appString(R.string.settings_action)).performClick()

        composeRule.onNodeWithText(appString(R.string.theme_dark)).performClick()
        composeRule.onNodeWithText("Slovenčina").performClick()
        composeRule.onNodeWithText("Nastavenia").assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText(appString(R.string.settings_title)).assertIsDisplayed()

        composeRule.runOnIdle {
            val preferences = AppPreferences(
                composeRule.activity.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            )
            assertEquals(ThemePreference.Dark, preferences.theme())
            assertEquals(AppLanguage.Slovak, preferences.languageOrDefault("en-US"))
        }
    }

    @Test
    fun firstRunPreparationCanBeReopenedFromNewScan() {
        composeRule.onNodeWithText(appString(R.string.welcome_description)).assertIsDisplayed()
        composeRule.onNodeWithText(appString(R.string.language_slovak)).performClick()
        composeRule.onNodeWithText(appString(R.string.welcome_description)).assertIsDisplayed()
        composeRule.onNodeWithText(appString(R.string.action_continue)).performScrollTo().performClick()
        composeRule.onNodeWithText(appString(R.string.live_capture_title)).assertIsDisplayed()
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNodeWithText(appString(R.string.welcome_description)).assertIsDisplayed()
        composeRule.onNodeWithText(appString(R.string.action_continue)).performScrollTo().performClick()
        composeRule.onNodeWithText(appString(R.string.live_capture_title)).assertIsDisplayed()
        composeRule.onNodeWithText(appString(R.string.action_continue)).performClick()
        composeRule.onNodeWithText(appString(R.string.reference_setup_title)).assertIsDisplayed()
        composeRule.onNodeWithText(appString(R.string.action_continue)).performScrollTo().performClick()

        composeRule.onNodeWithText(appString(R.string.library_title)).assertIsDisplayed()
        composeRule.onNodeWithText(appString(R.string.new_scan_action)).performClick()
        composeRule.onNodeWithText(appString(R.string.review_preparation)).performClick()
        composeRule.onNodeWithText(appString(R.string.traversal_title)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(appString(R.string.action_back)).performScrollTo().performClick()
        composeRule.onNodeWithText(appString(R.string.new_scan_title)).assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText(appString(R.string.new_scan_title)).assertIsDisplayed()
    }

    private fun dismissFirstLaunchLanguageChoiceIfPresent() {
        if (composeRule.onAllNodesWithText(appString(R.string.welcome_description)).fetchSemanticsNodes().isNotEmpty()) {
            repeat(3) {
                composeRule.onNodeWithText(appString(R.string.action_continue)).performScrollTo().performClick()
            }
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

private class ClearAppPreferencesRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
            base.evaluate()
        }
    }
}
