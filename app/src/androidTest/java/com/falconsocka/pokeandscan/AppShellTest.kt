package com.falconsocka.pokeandscan

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
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

    @After
    fun removeSetupDraftsCreatedByUiTests() = runBlocking {
        val repository = SnapshotRepository.from(InstrumentationRegistry.getInstrumentation().targetContext)
        repository.observeSnapshots().first()
            .filter { it.lifecycle == SnapshotLifecycle.SETUP }
            .forEach { repository.deleteSnapshot(it.id) }
    }

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
        composeRule.onNodeWithText(appString(R.string.scan_scope_whole_collection)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(appString(R.string.action_continue)).performClick()
        composeRule.onNodeWithText(appString(R.string.traversal_title)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(appString(R.string.capture_source_unavailable_action))
            .performScrollTo()
            .assertIsNotEnabled()
        composeRule.onNodeWithText(appString(R.string.action_back)).performScrollTo().performClick()
        composeRule.onNodeWithText(appString(R.string.new_scan_title)).assertIsDisplayed()
        composeRule.mainClock.advanceTimeBy(1_000L)
        composeRule.waitForIdle()

        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText(appString(R.string.new_scan_title)).assertIsDisplayed()
    }

    @Test
    fun filteredSetupPersistsNameScopeAndSourceWhenResumed() {
        dismissFirstLaunchLanguageChoiceIfPresent()
        composeRule.onNodeWithText(appString(R.string.new_scan_action)).performClick()
        composeRule.onNodeWithText(appString(R.string.scan_name_optional))
            .performScrollTo()
            .performTextInput("PVP weekend")
        composeRule.onNodeWithText(appString(R.string.scan_scope_filtered_subset))
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText(appString(R.string.scan_scope_description_label))
            .performScrollTo()
            .performTextInput("Battle League tag")
        composeRule.onNodeWithText(appString(R.string.mp4_import_title))
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText(appString(R.string.action_continue)).performClick()
        composeRule.onNodeWithText(appString(R.string.capture_source_unavailable_action))
            .performScrollTo()
            .assertIsNotEnabled()
        composeRule.onNodeWithText(appString(R.string.action_back)).performScrollTo().performClick()

        composeRule.onNodeWithText("PVP weekend").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Battle League tag").performScrollTo().assertIsDisplayed()
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("PVP weekend").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Battle League tag").performScrollTo().assertIsDisplayed()

        val repository = SnapshotRepository.from(composeRule.activity)
        val setup = runBlocking {
            repository.observeSnapshots().first().single { it.name == "PVP weekend" }
        }
        assertEquals(SnapshotLifecycle.SETUP, setup.lifecycle)
        assertEquals(SnapshotSourceType.MP4, setup.sourceType)
        assertEquals(ScanScopeType.FILTERED_SUBSET, setup.scanScopeType)
        assertEquals("Battle League tag", setup.scanScopeDescription)
    }

    @Test
    fun leavingSetupBeforePreparationSavesItForLater() {
        dismissFirstLaunchLanguageChoiceIfPresent()
        composeRule.onNodeWithText(appString(R.string.new_scan_action)).performClick()
        composeRule.onNodeWithText(appString(R.string.scan_name_optional))
            .performScrollTo()
            .performTextInput("Pre-check draft")
        composeRule.onNodeWithText(appString(R.string.scan_scope_filtered_subset))
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText(appString(R.string.scan_scope_description_label))
            .performScrollTo()
            .performTextInput("Gym tag")
        composeRule.onNodeWithText(appString(R.string.snapshot_back_to_library)).performClick()
        composeRule.waitForIdle()
        val repository = SnapshotRepository.from(composeRule.activity)
        val storedSnapshots = runBlocking { repository.observeSnapshots().first() }
        assertEquals("Draft should be persisted before returning to the Library", true, storedSnapshots.any {
            it.name == "Pre-check draft" && it.lifecycle == SnapshotLifecycle.SETUP
        })
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithText(appString(R.string.library_title))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithText(appString(R.string.snapshot_lifecycle_setup))
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("snapshot-library-list").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("snapshot-library-list")
            .performScrollToNode(hasText("Pre-check draft"))
        composeRule.waitForIdle()
        val setupStateLabels = listOf(
            appString(R.string.snapshot_lifecycle_setup),
            "Setup saved",
            "Nastavenie uložené"
        ).distinct()
        val setupStateLabel = setupStateLabels.first { label ->
            composeRule.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(setupStateLabel).assertIsDisplayed()
        composeRule.onNodeWithText("Pre-check draft").assertIsDisplayed()
        val openActionLabels = listOf(
            appString(R.string.snapshot_open_action),
            "View details",
            "Zobraziť podrobnosti"
        ).distinct()
        val openActionLabel = openActionLabels.first { label ->
            composeRule.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(openActionLabel)
            .performScrollTo()
            .performClick()
        val continueSetupLabels = listOf(
            appString(R.string.snapshot_continue_setup),
            "Continue setup",
            "Pokračovať v nastavení"
        ).distinct()
        composeRule.waitUntil(5_000L) {
            continueSetupLabels.any { label ->
                composeRule.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty()
            }
        }
        val continueSetupLabel = continueSetupLabels.first { label ->
            composeRule.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(continueSetupLabel)
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("Pre-check draft").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Gym tag").performScrollTo().assertIsDisplayed()

        val setup = runBlocking {
            repository.observeSnapshots().first().single { it.name == "Pre-check draft" }
        }
        assertEquals(SnapshotLifecycle.SETUP, setup.lifecycle)
        assertEquals(ScanScopeType.FILTERED_SUBSET, setup.scanScopeType)
        assertEquals("Gym tag", setup.scanScopeDescription)
    }

    @Test
    fun invalidNameDoesNotBlockLeavingSetup() {
        dismissFirstLaunchLanguageChoiceIfPresent()
        composeRule.onNodeWithText(appString(R.string.new_scan_action)).performClick()
        val tooLongName = "A".repeat(MAX_SCAN_NAME_LENGTH + 1)
        composeRule.onNodeWithText(appString(R.string.scan_name_optional))
            .performScrollTo()
            .performTextInput(tooLongName)
        composeRule.onNodeWithText(appString(R.string.snapshot_back_to_library)).performClick()
        composeRule.waitUntil(5_000L) {
            composeRule.onAllNodesWithText(appString(R.string.library_title))
                .fetchSemanticsNodes().isNotEmpty()
        }

        val setup = runBlocking {
            SnapshotRepository.from(composeRule.activity).observeSnapshots().first()
                .single { it.lifecycle == SnapshotLifecycle.SETUP }
        }
        assertEquals(false, setup.name == tooLongName)
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
