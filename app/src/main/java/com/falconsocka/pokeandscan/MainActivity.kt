package com.falconsocka.pokeandscan

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.falconsocka.pokeandscan.ui.illustration.ScreenIllustrationPool
import com.falconsocka.pokeandscan.ui.illustration.ScreenIllustrationSelector
import com.falconsocka.pokeandscan.ui.illustration.IllustrationArtwork
import com.falconsocka.pokeandscan.ui.illustration.IllustratedInformationState
import com.falconsocka.pokeandscan.ui.illustration.IllustratedIntro
import com.falconsocka.pokeandscan.ui.layout.ScrollableScreenColumn
import com.falconsocka.pokeandscan.ui.snapshot.ScanNameField
import com.falconsocka.pokeandscan.ui.snapshot.snapshotNameIssueMessage
import com.falconsocka.pokeandscan.ui.components.InformationCard
import com.falconsocka.pokeandscan.ui.components.InlineNotice
import com.falconsocka.pokeandscan.ui.components.NoticeTone
import com.falconsocka.pokeandscan.ui.components.SelectionOptionCard
import com.falconsocka.pokeandscan.ui.components.SectionCard
import com.falconsocka.pokeandscan.ui.components.SingleChoiceRow
import com.falconsocka.pokeandscan.ui.components.ScreenState
import com.falconsocka.pokeandscan.ui.components.LoadingState
import com.falconsocka.pokeandscan.ui.components.PrimaryActionButton
import com.falconsocka.pokeandscan.ui.components.QuietActionButton
import com.falconsocka.pokeandscan.ui.components.SecondaryActionButton
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.falconsocka.pokeandscan.ui.theme.PokeAndScanTheme
import com.falconsocka.pokeandscan.ui.theme.AppDimensions
import com.falconsocka.pokeandscan.ui.theme.AppSpacing
import com.falconsocka.pokeandscan.ui.theme.AppShapes
import com.falconsocka.pokeandscan.ui.theme.focusOutline
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = AppPreferences(getSharedPreferences("app_preferences", MODE_PRIVATE))
        setContent {
            PokeAndScanApp(preferences = preferences)
        }
    }
}

private sealed class AppDestination(
    val route: String,
    val titleRes: Int,
    val illustrationPool: ScreenIllustrationPool? = null
) {
    data object Welcome : AppDestination("welcome", R.string.welcome_title, ScreenIllustrationPool.Welcome)
    data object CaptureExplanation : AppDestination(
        "capture-explanation",
        R.string.capture_explanation_title,
        ScreenIllustrationPool.CaptureExplanation
    )
    data object OnboardingPreparation : AppDestination(
        "onboarding-preparation",
        R.string.preparation_title,
        ScreenIllustrationPool.Preparation
    )
    data object Library : AppDestination("library", R.string.library_title, ScreenIllustrationPool.ScansEmpty)
    data object SnapshotDetail : AppDestination("snapshot", R.string.library_title)
    data object NewScan : AppDestination("new-scan", R.string.new_scan_title, ScreenIllustrationPool.NewScan)
    data object Settings : AppDestination("settings", R.string.settings_title)

    companion object {
        fun fromRoute(route: String?): AppDestination = when {
            route == SnapshotDetail.route || route == "${SnapshotDetail.route}/{snapshotId}" || route?.startsWith("${SnapshotDetail.route}/") == true -> SnapshotDetail
            route == Welcome.route -> Welcome
            route == CaptureExplanation.route -> CaptureExplanation
            route == OnboardingPreparation.route -> OnboardingPreparation
            route == NewScan.route || route?.startsWith("${NewScan.route}/") == true -> NewScan
            route == Settings.route -> Settings
            else -> Library
        }
    }
}

private data class ScanSetupDraft(
    val snapshotId: String?,
    val name: String,
    val scopeType: ScanScopeType,
    val scopeDescription: String?,
    val sourceType: SnapshotSourceType
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PokeAndScanApp(preferences: AppPreferences) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val snapshotRepository = remember(context.applicationContext) {
        SnapshotRepository.from(context.applicationContext)
    }
    var snapshots by remember { mutableStateOf<List<SnapshotSummary>?>(null) }
    var snapshotsLoadError by remember { mutableStateOf(false) }
    var snapshotRetryCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(snapshotRepository, snapshotRetryCount) {
        snapshotsLoadError = false
        try {
            snapshotRepository.recoverPendingEvidenceDeletions()
            snapshotRepository.observeSnapshots().collect {
                snapshots = it
                snapshotsLoadError = false
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            snapshotsLoadError = true
        }
    }
    val startDestination = remember {
        if (preferences.hasCompletedOnboarding()) AppDestination.Library.route else AppDestination.Welcome.route
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = AppDestination.fromRoute(backStackEntry?.destination?.route)
    val activeSnapshotId = backStackEntry?.arguments?.getString("snapshotId")
    val activeSnapshot = snapshots?.firstOrNull { it.id == activeSnapshotId }
    LaunchedEffect(backStackEntry?.id, snapshotRepository) {
        if (currentDestination == AppDestination.Library) {
            try {
                snapshots = snapshotRepository.observeSnapshots().first()
                snapshotsLoadError = false
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                snapshotsLoadError = true
            }
        }
    }
    val activeJob = snapshots?.any {
        it.lifecycle == SnapshotLifecycle.PROCESSING || it.lifecycle == SnapshotLifecycle.INCOMPLETE
    } == true
    val illustrationSelector = remember { ScreenIllustrationSelector() }
    val selectedIllustration = remember(backStackEntry?.id, currentDestination.illustrationPool) {
        currentDestination.illustrationPool?.let(illustrationSelector::select)
    }
    var language by remember {
        mutableStateOf(preferences.languageOrDefault())
    }
    var theme by remember { mutableStateOf(preferences.theme()) }
    val finishOnboarding: () -> Unit = {
        preferences.completeOnboarding()
        navController.navigate(AppDestination.Library.route) {
            popUpTo(AppDestination.Welcome.route) { inclusive = true }
            launchSingleTop = true
        }
    }
    val saveSetup: suspend (ScanSetupDraft) -> String = { draft ->
        val id = if (draft.snapshotId == null) {
            snapshotRepository.createSetupSnapshot(
                displayName = draft.name,
                fallbackName = defaultSnapshotName(context, language),
                sourceType = draft.sourceType,
                scanScopeType = draft.scopeType,
                scanScopeDescription = draft.scopeDescription
            )
        } else {
            check(snapshotRepository.updateSetupSnapshot(
                snapshotId = draft.snapshotId,
                displayName = draft.name,
                sourceType = draft.sourceType,
                scanScopeType = draft.scopeType,
                scanScopeDescription = draft.scopeDescription
            ))
            draft.snapshotId
        }
        val refreshedSnapshots = snapshotRepository.observeSnapshots().first()
        withContext(Dispatchers.Main.immediate) { snapshots = refreshedSnapshots }
        id
    }
    val returnToLibrary: () -> Unit = {
        if (!navController.popBackStack(AppDestination.Library.route, false)) {
            navController.navigate(AppDestination.Library.route) { launchSingleTop = true }
        }
    }

    val baseConfiguration = LocalConfiguration.current
    val localizedConfiguration = remember(baseConfiguration, language) {
        Configuration(baseConfiguration).apply {
            setLocale(Locale.forLanguageTag(language.languageTag))
            setLayoutDirection(Locale.forLanguageTag(language.languageTag))
        }
    }
    val localizedContext = LocalContext.current.createConfigurationContext(localizedConfiguration)
    val view = LocalView.current
    val darkTheme = when (theme) {
        ThemePreference.System -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }
    val illustrationResource = selectedIllustration?.resourceFor(darkTheme)
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    PokeAndScanTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides localizedConfiguration
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (currentDestination == AppDestination.SnapshotDetail) {
                                    activeSnapshot?.name ?: stringResource(R.string.snapshot_missing_title)
                                } else {
                                    stringResource(currentDestination.titleRes)
                                },
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        actions = {
                            if (currentDestination == AppDestination.Library) {
                                QuietActionButton(
                                    onClick = { navController.navigate(AppDestination.Settings.route) },
                                    modifier = Modifier
                                ) {
                                    Text(stringResource(R.string.settings_action))
                                }
                            }
                        },
                        navigationIcon = {
                            if (currentDestination == AppDestination.SnapshotDetail) {
                                QuietActionButton(
                                    onClick = { navController.popBackStack() },
                                    modifier = Modifier
                                ) { Text(stringResource(R.string.action_back)) }
                            }
                        }
                    )
                }
            ) { contentPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(contentPadding)
                ) {
                    composable(AppDestination.Welcome.route) {
                        WelcomeScreen(
                            language = language,
                            illustrationRes = illustrationResource
                                ?: ScreenIllustrationPool.Welcome.options.first().resourceFor(darkTheme),
                            onLanguageSelected = {
                                language = it
                                preferences.saveLanguage(it)
                            },
                            onContinue = {
                                preferences.saveLanguage(language)
                                navController.navigate(AppDestination.CaptureExplanation.route)
                            },
                            onSkip = finishOnboarding
                        )
                    }
                    composable(AppDestination.CaptureExplanation.route) {
                        CaptureExplanationScreen(
                            illustrationRes = illustrationResource
                                ?: ScreenIllustrationPool.CaptureExplanation.options.first().resourceFor(darkTheme),
                            onBack = { navController.popBackStack() },
                            onContinue = { navController.navigate(AppDestination.OnboardingPreparation.route) }
                        )
                    }
                    composable(AppDestination.OnboardingPreparation.route) {
                        PreparationScreen(
                            illustrationRes = illustrationResource
                                ?: ScreenIllustrationPool.Preparation.options.first().resourceFor(darkTheme),
                            onBack = { navController.popBackStack() },
                            onContinue = finishOnboarding
                        )
                    }
                    composable(AppDestination.Library.route) {
                        SnapshotLibraryScreen(
                            illustrationRes = illustrationResource
                                ?: ScreenIllustrationPool.ScansEmpty.options.first().resourceFor(darkTheme),
                            snapshots = snapshots,
                            loadingError = snapshotsLoadError,
                            onRetry = { snapshotRetryCount++ },
                            onNewScan = { navController.navigate(AppDestination.NewScan.route) },
                            onOpenSnapshot = { id -> navController.navigate("${AppDestination.SnapshotDetail.route}/$id") }
                        )
                    }
                    composable(
                        route = "${AppDestination.SnapshotDetail.route}/{snapshotId}",
                        arguments = listOf(navArgument("snapshotId") { type = NavType.StringType })
                    ) { entry ->
                        val snapshotId = entry.arguments?.getString("snapshotId")
                        val snapshot = snapshots?.firstOrNull { it.id == snapshotId }
                        val detail = snapshot?.let {
                            SnapshotDetailSummary(
                                id = it.id,
                                name = it.name,
                                createdAtMillis = it.createdAtMillis,
                                sourceType = it.sourceType,
                                lifecycle = it.lifecycle,
                                scopeCompleteness = it.scopeCompleteness,
                                scanScopeType = it.scanScopeType,
                                scanScopeDescription = it.scanScopeDescription,
                                recordCount = it.recordCount,
                                includedRecordCount = it.includedRecordCount,
                                reviewCount = it.reviewCount,
                                partialCount = it.partialCount,
                                warningCount = it.warningCount
                            )
                        }
                        var screenOperationError by remember(snapshotId) { mutableStateOf(false) }
                        var screenIsDeleting by remember(snapshotId) { mutableStateOf(false) }
                        var screenIsRenaming by remember(snapshotId) { mutableStateOf(false) }
                        SnapshotDetailScreen(
                            snapshot = detail,
                            loading = snapshots == null && !snapshotsLoadError,
                            loadingError = snapshotsLoadError,
                            operationError = screenOperationError,
                            isDeleting = screenIsDeleting,
                            isRenaming = screenIsRenaming,
                            onBackToLibrary = { navController.popBackStack(AppDestination.Library.route, false) },
                            onRetry = { snapshotRetryCount++ },
                            onContinueSetup = {
                                if (snapshotId != null) {
                                    navController.navigate("${AppDestination.NewScan.route}/$snapshotId")
                                }
                            },
                            onClearOperationError = { screenOperationError = false },
                            onRename = { name ->
                                if (snapshotId != null && !screenIsRenaming && !screenIsDeleting) {
                                    screenIsRenaming = true
                                    screenOperationError = false
                                    coroutineScope.launch {
                                        try {
                                            screenOperationError = !snapshotRepository.renameSnapshot(snapshotId, name)
                                        } catch (_: Exception) {
                                            screenOperationError = true
                                        } finally {
                                            screenIsRenaming = false
                                        }
                                    }
                                }
                            },
                            onDelete = {
                                if (snapshotId != null && !screenIsDeleting) coroutineScope.launch {
                                    screenIsDeleting = true
                                    screenOperationError = false
                                    try {
                                        if (snapshotRepository.deleteSnapshot(snapshotId)) {
                                            navController.popBackStack(AppDestination.Library.route, false)
                                        } else {
                                            screenOperationError = true
                                        }
                                    } catch (_: Exception) {
                                        screenOperationError = true
                                    } finally {
                                        screenIsDeleting = false
                                    }
                                }
                            }
                        )
                    }
                    composable(AppDestination.NewScan.route) { entry ->
                        val sessionId by entry.savedStateHandle
                            .getStateFlow<String?>("setupSessionId", null)
                            .collectAsState()
                        val isPreparing by entry.savedStateHandle
                            .getStateFlow("scanPreparation", false)
                            .collectAsState()
                        val setup = snapshots?.firstOrNull {
                            it.id == sessionId && it.lifecycle == SnapshotLifecycle.SETUP
                        }
                        ScanSetupDestination(
                            illustrationRes = illustrationResource
                                ?: ScreenIllustrationPool.NewScan.options.first().resourceFor(darkTheme),
                            preparationIllustrationRes = ScreenIllustrationPool.Preparation.options
                                .first().resourceFor(darkTheme),
                            operationScope = coroutineScope,
                            sessionId = sessionId,
                            savedSetup = setup,
                            isPreparing = isPreparing,
                            onPreparingChange = { entry.savedStateHandle["scanPreparation"] = it },
                            language = language,
                            activeJob = activeJob,
                            loading = snapshots == null && !snapshotsLoadError,
                            loadingError = snapshotsLoadError,
                            onRetry = { snapshotRetryCount++ },
                            onBackToLibrary = returnToLibrary,
                            onContinue = { entry.savedStateHandle["scanPreparation"] = true },
                            onSaveSetup = { draft, openPreparation ->
                                val id = saveSetup(draft)
                                if (draft.snapshotId == null && openPreparation) {
                                    entry.savedStateHandle["setupSessionId"] = id
                                }
                                id
                            }
                        )
                    }
                    composable(
                        route = "${AppDestination.NewScan.route}/{sessionId}",
                        arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                    ) { entry ->
                        val sessionId = entry.arguments?.getString("sessionId")
                        val isPreparing by entry.savedStateHandle
                            .getStateFlow("scanPreparation", false)
                            .collectAsState()
                        val setup = snapshots?.firstOrNull {
                            it.id == sessionId && it.lifecycle == SnapshotLifecycle.SETUP
                        }
                        ScanSetupDestination(
                            illustrationRes = illustrationResource
                                ?: ScreenIllustrationPool.NewScan.options.first().resourceFor(darkTheme),
                            preparationIllustrationRes = ScreenIllustrationPool.Preparation.options
                                .first().resourceFor(darkTheme),
                            operationScope = coroutineScope,
                            sessionId = sessionId,
                            savedSetup = setup,
                            isPreparing = isPreparing,
                            onPreparingChange = { entry.savedStateHandle["scanPreparation"] = it },
                            language = language,
                            activeJob = activeJob,
                            loading = snapshots == null && !snapshotsLoadError,
                            loadingError = snapshotsLoadError,
                            onRetry = { snapshotRetryCount++ },
                            onBackToLibrary = returnToLibrary,
                            onContinue = { entry.savedStateHandle["scanPreparation"] = true },
                            onSaveSetup = { draft, _ -> saveSetup(draft) }
                        )
                    }
                    composable(AppDestination.Settings.route) {
                        SettingsScreen(
                            language = language,
                            theme = theme,
                            appName = stringResource(R.string.app_name),
                            appVersion = appVersion(context),
                            onPrivacyInformation = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl(language))))
                            },
                            onLanguageSelected = {
                                preferences.saveLanguage(it)
                                language = it
                            },
                            onThemeSelected = {
                                preferences.saveTheme(it)
                                theme = it
                            }
                        )
                    }
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> if (baseContext === this) null else baseContext.findActivity()
    else -> null
}

@Composable
private fun ScanSetupDestination(
    illustrationRes: Int,
    preparationIllustrationRes: Int,
    operationScope: CoroutineScope,
    sessionId: String?,
    savedSetup: SnapshotSummary?,
    isPreparing: Boolean,
    onPreparingChange: (Boolean) -> Unit,
    language: AppLanguage,
    activeJob: Boolean,
    loading: Boolean,
    loadingError: Boolean,
    onRetry: () -> Unit,
    onBackToLibrary: () -> Unit,
    onContinue: () -> Unit,
    onSaveSetup: suspend (ScanSetupDraft, openPreparation: Boolean) -> String
) {
    val context = LocalContext.current
    val formKey = sessionId ?: "new"
    var scanName by remember(formKey, savedSetup?.id) { mutableStateOf(savedSetup?.name.orEmpty()) }
    var scopeType by remember(formKey, savedSetup?.id) {
        mutableStateOf(savedSetup?.scanScopeType ?: ScanScopeType.WHOLE_COLLECTION)
    }
    var scopeDescription by remember(formKey, savedSetup?.id) {
        mutableStateOf(savedSetup?.scanScopeDescription.orEmpty())
    }
    var selectedSource by remember(formKey, savedSetup?.id) {
        mutableStateOf(savedSetup?.sourceType ?: SnapshotSourceType.LIVE)
    }
    var nameIssue by remember(formKey) { mutableStateOf<SnapshotNameIssue?>(null) }
    var scopeDescriptionIssue by remember(formKey) { mutableStateOf(false) }
    var isSaving by remember(formKey) { mutableStateOf(false) }
    var saveError by remember(formKey) { mutableStateOf<Int?>(null) }
    var hasEditedSetup by remember(formKey) { mutableStateOf(false) }

    val validateForm: (Boolean) -> Boolean = { requireCompleteScope ->
        val enteredNameIssue = snapshotNameIssue(scanName)
        val invalidScopeDescription = scopeType == ScanScopeType.FILTERED_SUBSET &&
            (scopeDescription.any(Char::isISOControl) ||
                (requireCompleteScope && scopeDescription.isBlank()))
        when {
            enteredNameIssue != null -> {
                nameIssue = enteredNameIssue
                false
            }
            invalidScopeDescription -> {
                scopeDescriptionIssue = true
                false
            }
            else -> true
        }
    }
    val draft = ScanSetupDraft(
        snapshotId = sessionId,
        name = scanName,
        scopeType = scopeType,
        scopeDescription = scopeDescription.takeIf { scopeType == ScanScopeType.FILTERED_SUBSET },
        sourceType = selectedSource
    )
    val leaveSetup: () -> Unit = {
        if (isSaving) {
            Unit
        } else if (sessionId == null && !hasEditedSetup) {
            onBackToLibrary()
        } else {
            val draftToSave = draft.copy(
                name = draft.name.takeIf { snapshotNameIssue(it) == null }.orEmpty(),
                scopeDescription = draft.scopeDescription?.takeIf { value ->
                    value.none(Char::isISOControl)
                }
            )
            operationScope.launch {
                isSaving = true
                saveError = null
                try {
                    onSaveSetup(draftToSave, false)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: ActiveScanJobException) {
                    saveError = R.string.snapshot_active_job_notice
                    return@launch
                } catch (_: Exception) {
                    saveError = R.string.scan_setup_save_error
                    return@launch
                } finally {
                    isSaving = false
                }
                withContext(Dispatchers.Main.immediate) { onBackToLibrary() }
            }
        }
    }

    BackHandler(enabled = true) {
        when {
            isPreparing -> onPreparingChange(false)
            sessionId != null && savedSetup == null -> onBackToLibrary()
            else -> leaveSetup()
        }
    }

    if (sessionId != null && savedSetup == null) {
        ScanSetupLoadingOrMissing(
            loading = loading,
            loadingError = loadingError,
            onRetry = onRetry,
            onBack = onBackToLibrary
        )
        return
    }

    if (isPreparing) {
        PreparationScreen(
            illustrationRes = preparationIllustrationRes,
            headingRes = R.string.preparation_title,
            introRes = R.string.scan_preparation_intro,
            selectedSourceLabelRes = if (selectedSource == SnapshotSourceType.LIVE) {
                R.string.live_capture_title
            } else {
                R.string.mp4_import_title
            },
            continueLabelRes = R.string.capture_source_unavailable_action,
            continueEnabled = false,
            sourceUnavailable = true,
            onBack = { onPreparingChange(false) },
            onContinue = {}
        )
        return
    }

    NewScanScreen(
        illustrationRes = illustrationRes,
        scanName = scanName,
        namePlaceholder = defaultSnapshotName(context, language),
        nameErrorRes = when (nameIssue) {
            SnapshotNameIssue.TOO_LONG -> R.string.scan_name_too_long
            SnapshotNameIssue.UNSAFE_CHARACTERS -> R.string.scan_name_unsafe_characters
            null -> null
        },
        onScanNameChange = {
            hasEditedSetup = true
            scanName = it
            nameIssue = null
            saveError = null
        },
        scopeType = scopeType,
        onScopeTypeSelected = {
            hasEditedSetup = true
            scopeType = it
            scopeDescriptionIssue = false
            saveError = null
        },
        scopeDescription = scopeDescription,
        onScopeDescriptionChange = {
            hasEditedSetup = true
            scopeDescription = it
            scopeDescriptionIssue = false
            saveError = null
        },
        scopeDescriptionError = scopeDescriptionIssue,
        selectedSource = selectedSource,
        onSourceSelected = {
            hasEditedSetup = true
            selectedSource = it
            saveError = null
        },
        activeJob = activeJob,
        isSaving = isSaving,
        saveErrorRes = saveError,
        onBackToLibrary = leaveSetup,
        onPreparation = {
            if (isSaving) return@NewScanScreen
            if (!validateForm(true)) return@NewScanScreen
            operationScope.launch {
                isSaving = true
                saveError = null
                try {
                    onSaveSetup(draft, true)
                    withContext(Dispatchers.Main.immediate) { onContinue() }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: ActiveScanJobException) {
                    saveError = R.string.snapshot_active_job_notice
                } catch (_: Exception) {
                    saveError = R.string.scan_setup_save_error
                } finally {
                    isSaving = false
                }
            }
        }
    )
}

@Composable
private fun ScanSetupLoadingOrMissing(
    loading: Boolean,
    loadingError: Boolean,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    if (loading) {
        LoadingState(R.string.snapshot_loading)
    } else if (loadingError) {
        ScreenState(
            title = stringResource(R.string.snapshot_load_error_title),
            body = stringResource(R.string.snapshot_load_error_body),
            primaryAction = {
                PrimaryActionButton(onClick = onRetry) { Text(stringResource(R.string.snapshot_retry_action)) }
            },
            secondaryAction = {
                QuietActionButton(onClick = onBack) { Text(stringResource(R.string.snapshot_back_to_library)) }
            }
        )
    } else {
        ScreenState(
            title = stringResource(R.string.snapshot_missing_title),
            body = stringResource(R.string.snapshot_missing_body),
            primaryAction = {
                PrimaryActionButton(onClick = onBack) { Text(stringResource(R.string.snapshot_back_to_library)) }
            }
        )
    }
}

@Composable
private fun WelcomeScreen(
    language: AppLanguage,
    illustrationRes: Int,
    modifier: Modifier = Modifier,
    onLanguageSelected: (AppLanguage) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    ScrollableScreenColumn(modifier = modifier, verticalPadding = AppSpacing.large) {
        IllustrationArtwork(illustrationRes, height = 208.dp)
        Text(
            text = stringResource(R.string.brand_tagline),
            modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.welcome_description),
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.card,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Text(
                text = stringResource(R.string.welcome_details),
                modifier = Modifier.padding(AppSpacing.large),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SectionCard(title = stringResource(R.string.language_setting), modifier = Modifier.selectableGroup()) {
            SingleChoiceRow(
                label = stringResource(R.string.language_english),
                selected = language == AppLanguage.English,
                onClick = { onLanguageSelected(AppLanguage.English) }
            )
            SingleChoiceRow(
                label = stringResource(R.string.language_slovak),
                selected = language == AppLanguage.Slovak,
                onClick = { onLanguageSelected(AppLanguage.Slovak) }
            )
        }
        Spacer(Modifier.height(AppSpacing.small))
        PrimaryActionButton(
            onClick = onContinue,
            modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(max = 480.dp).fillMaxWidth()
        ) {
            Text(stringResource(R.string.action_continue))
        }
        QuietActionButton(
            onClick = onSkip,
            modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(max = 480.dp).fillMaxWidth()
        ) {
            Text(stringResource(R.string.skip_introduction))
        }
    }
}

@Composable
private fun CaptureExplanationScreen(
    illustrationRes: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    ScrollableScreenColumn(modifier = modifier) {
        IllustratedIntro(
            illustrationRes = illustrationRes,
            body = stringResource(R.string.capture_explanation_body)
        )
        InformationCard(stringResource(R.string.live_capture_title), stringResource(R.string.live_capture_description))
        InformationCard(stringResource(R.string.mp4_import_title), stringResource(R.string.mp4_import_description))
        InlineNotice(stringResource(R.string.manual_navigation_reminder), NoticeTone.Info)
        PrimaryActionButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.action_continue))
        }
        QuietActionButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.action_back))
        }
    }
}

@Composable
private fun PreparationScreen(
    illustrationRes: Int,
    modifier: Modifier = Modifier,
    headingRes: Int? = null,
    introRes: Int = R.string.preparation_intro,
    selectedSourceLabelRes: Int? = null,
    continueLabelRes: Int = R.string.action_continue,
    continueEnabled: Boolean = true,
    sourceUnavailable: Boolean = false,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val pokemonGoNotFoundMessage = stringResource(R.string.pokemon_go_not_found)
    ScrollableScreenColumn(modifier = modifier) {
        IllustratedIntro(
            illustrationRes = illustrationRes,
            title = headingRes?.let { stringResource(it) },
            body = stringResource(introRes)
        )
        selectedSourceLabelRes?.let { sourceLabelRes ->
            InlineNotice(
                stringResource(R.string.selected_capture_source, stringResource(sourceLabelRes)),
                NoticeTone.Info
            )
        }
        if (sourceUnavailable) {
            InlineNotice(stringResource(R.string.capture_source_unavailable_body), NoticeTone.Info)
        }
        GuidanceSection(stringResource(R.string.reference_setup_title), stringResource(R.string.reference_setup_details))
        GuidanceSection(stringResource(R.string.scan_scope_title), stringResource(R.string.scan_scope_details))
        GuidanceSection(stringResource(R.string.nickname_warning_title), stringResource(R.string.nickname_warning_details))
        GuidanceSection(stringResource(R.string.traversal_title), stringResource(R.string.traversal_details))
        SecondaryActionButton(
            onClick = {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.nianticlabs.pokemongo")
                if (launchIntent != null) context.startActivity(launchIntent)
                else Toast.makeText(context, pokemonGoNotFoundMessage, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.open_pokemon_go))
        }
        PrimaryActionButton(
            onClick = onContinue,
            enabled = continueEnabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(continueLabelRes))
        }
        QuietActionButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.action_back))
        }
    }
}

@Composable
private fun NewScanScreen(
    illustrationRes: Int,
    scanName: String,
    namePlaceholder: String,
    nameErrorRes: Int?,
    onScanNameChange: (String) -> Unit,
    scopeType: ScanScopeType,
    onScopeTypeSelected: (ScanScopeType) -> Unit,
    scopeDescription: String,
    onScopeDescriptionChange: (String) -> Unit,
    scopeDescriptionError: Boolean,
    selectedSource: SnapshotSourceType,
    onSourceSelected: (SnapshotSourceType) -> Unit,
    activeJob: Boolean,
    isSaving: Boolean,
    saveErrorRes: Int?,
    onBackToLibrary: () -> Unit,
    modifier: Modifier = Modifier,
    onPreparation: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.screen, vertical = AppSpacing.screen),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.screen)
            ) {
                IllustratedIntro(
                    illustrationRes = illustrationRes,
                    title = stringResource(R.string.new_scan_state_title),
                    body = stringResource(R.string.new_scan_intro)
                )
                ScanNameField(
                    value = scanName,
                    onValueChange = onScanNameChange,
                    label = stringResource(R.string.scan_name_optional),
                    placeholder = namePlaceholder,
                    supportingText = stringResource(nameErrorRes ?: R.string.scan_name_helper),
                    isError = nameErrorRes != null
                )
                Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    Text(stringResource(R.string.scan_scope_title), style = MaterialTheme.typography.titleLarge)
                    SelectionOptionCard(
                        title = stringResource(R.string.scan_scope_whole_collection),
                        body = stringResource(R.string.scan_scope_whole_collection_description),
                        selected = scopeType == ScanScopeType.WHOLE_COLLECTION,
                        onClick = { onScopeTypeSelected(ScanScopeType.WHOLE_COLLECTION) }
                    )
                    SelectionOptionCard(
                        title = stringResource(R.string.scan_scope_filtered_subset),
                        body = stringResource(R.string.scan_scope_filtered_subset_description),
                        selected = scopeType == ScanScopeType.FILTERED_SUBSET,
                        onClick = { onScopeTypeSelected(ScanScopeType.FILTERED_SUBSET) }
                    )
                    if (scopeType == ScanScopeType.FILTERED_SUBSET) {
                        OutlinedTextField(
                            value = scopeDescription,
                            onValueChange = onScopeDescriptionChange,
                            modifier = Modifier.fillMaxWidth().focusOutline(AppShapes.control),
                            label = { Text(stringResource(R.string.scan_scope_description_label)) },
                            supportingText = {
                                Text(stringResource(
                                    if (scopeDescriptionError) R.string.scan_scope_description_required
                                    else R.string.scan_scope_description_helper
                                ))
                            },
                            isError = scopeDescriptionError,
                            singleLine = true,
                            shape = AppShapes.control
                        )
                    }
                }
                Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    Text(stringResource(R.string.capture_source_title), style = MaterialTheme.typography.titleLarge)
                    SelectionOptionCard(
                        title = stringResource(R.string.live_capture_title),
                        body = stringResource(R.string.live_capture_description),
                        recommendation = stringResource(R.string.recommended_label),
                        statusLabel = stringResource(R.string.capture_source_not_available),
                        selected = selectedSource == SnapshotSourceType.LIVE,
                        onClick = { onSourceSelected(SnapshotSourceType.LIVE) }
                    )
                    SelectionOptionCard(
                        title = stringResource(R.string.mp4_import_title),
                        body = stringResource(R.string.mp4_import_description),
                        statusLabel = stringResource(R.string.capture_source_not_available),
                        selected = selectedSource == SnapshotSourceType.MP4,
                        onClick = { onSourceSelected(SnapshotSourceType.MP4) }
                    )
                }
            }
        }
        if (activeJob) {
            InlineNotice(
                stringResource(R.string.snapshot_active_job_notice),
                NoticeTone.Warning,
                modifier = Modifier.widthIn(max = 560.dp).padding(horizontal = AppSpacing.screen)
            )
        }
        saveErrorRes?.let {
            InlineNotice(
                stringResource(it),
                NoticeTone.Error,
                modifier = Modifier.widthIn(max = 560.dp).padding(horizontal = AppSpacing.screen)
            )
        }
        PrimaryActionButton(
            onClick = onPreparation,
            enabled = !activeJob,
            loading = isSaving,
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = AppSpacing.screen, vertical = AppSpacing.medium)
        ) {
            Text(stringResource(if (isSaving) R.string.scan_setup_saving else R.string.action_continue))
        }
        QuietActionButton(
            onClick = onBackToLibrary,
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = AppSpacing.screen).padding(bottom = AppSpacing.small)
        ) {
            Text(stringResource(R.string.snapshot_back_to_library))
        }
    }
}

@Composable
private fun GuidanceSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsScreen(
    language: AppLanguage,
    theme: ThemePreference,
    appName: String,
    appVersion: String,
    modifier: Modifier = Modifier,
    onPrivacyInformation: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (ThemePreference) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screen, vertical = AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.screen)
    ) {
        SectionCard(title = stringResource(R.string.language_setting), modifier = Modifier.selectableGroup()) {
            SingleChoiceRow(
                label = stringResource(R.string.language_english),
                selected = language == AppLanguage.English,
                onClick = { onLanguageSelected(AppLanguage.English) }
            )
            SingleChoiceRow(
                label = stringResource(R.string.language_slovak),
                selected = language == AppLanguage.Slovak,
                onClick = { onLanguageSelected(AppLanguage.Slovak) }
            )
        }
        SectionCard(title = stringResource(R.string.theme_setting), modifier = Modifier.selectableGroup()) {
            SingleChoiceRow(stringResource(R.string.theme_system), theme == ThemePreference.System) { onThemeSelected(ThemePreference.System) }
            SingleChoiceRow(stringResource(R.string.theme_light), theme == ThemePreference.Light) { onThemeSelected(ThemePreference.Light) }
            SingleChoiceRow(stringResource(R.string.theme_dark), theme == ThemePreference.Dark) { onThemeSelected(ThemePreference.Dark) }
        }
        SectionCard(title = stringResource(R.string.privacy_title)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = AppDimensions.settingsActionRowMinHeight)
                    .focusOutline(AppShapes.control)
                    .clickable(onClick = onPrivacyInformation)
                    .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.privacy_action), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.privacy_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(AppSpacing.xSmall))
        Text(
            text = stringResource(R.string.version_footer, appName, appVersion),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(AppSpacing.small))
    }
}

private fun appVersion(context: Context): String =
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
