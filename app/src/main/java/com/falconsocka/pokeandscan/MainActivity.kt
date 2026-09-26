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
import kotlinx.coroutines.launch

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
    data object ScanPreparation : AppDestination("scan-preparation", R.string.preparation_title, ScreenIllustrationPool.Preparation)
    data object Settings : AppDestination("settings", R.string.settings_title)

    companion object {
        fun fromRoute(route: String?): AppDestination = when {
            route == SnapshotDetail.route || route == "${SnapshotDetail.route}/{snapshotId}" || route?.startsWith("${SnapshotDetail.route}/") == true -> SnapshotDetail
            route == Welcome.route -> Welcome
            route == CaptureExplanation.route -> CaptureExplanation
            route == OnboardingPreparation.route -> OnboardingPreparation
            route == NewScan.route -> NewScan
            route == ScanPreparation.route -> ScanPreparation
            route == Settings.route -> Settings
            else -> Library
        }
    }
}

private enum class CaptureSource {
    LiveCapture,
    Mp4Import
}

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
    val illustrationSelector = remember { ScreenIllustrationSelector() }
    val selectedIllustration = remember(backStackEntry?.id, currentDestination.illustrationPool) {
        currentDestination.illustrationPool?.let(illustrationSelector::select)
    }
    var language by remember {
        mutableStateOf(preferences.languageOrDefault())
    }
    var theme by remember { mutableStateOf(preferences.theme()) }
    var selectedCaptureSource by remember { mutableStateOf(CaptureSource.LiveCapture) }
    var scanName by remember(language) { mutableStateOf(defaultSnapshotName(context, language)) }
    val finishOnboarding: () -> Unit = {
        preferences.completeOnboarding()
        navController.navigate(AppDestination.Library.route) {
            popUpTo(AppDestination.Welcome.route) { inclusive = true }
            launchSingleTop = true
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
                                recordCount = it.recordCount,
                                includedRecordCount = it.includedRecordCount,
                                reviewCount = it.reviewCount,
                                partialCount = it.partialCount,
                                warningCount = it.warningCount
                            )
                        }
                        var screenOperationError by remember(snapshotId) { mutableStateOf(false) }
                        var screenIsDeleting by remember(snapshotId) { mutableStateOf(false) }
                        SnapshotDetailScreen(
                            snapshot = detail,
                            loading = snapshots == null,
                            loadingError = snapshotsLoadError,
                            operationError = screenOperationError,
                            isDeleting = screenIsDeleting,
                            onBackToLibrary = { navController.popBackStack(AppDestination.Library.route, false) },
                            onRetry = { snapshotRetryCount++ },
                            onRename = { name ->
                                if (snapshotId != null) coroutineScope.launch {
                                    screenOperationError = try {
                                        !snapshotRepository.renameSnapshot(snapshotId, name)
                                    } catch (_: Exception) {
                                        true
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
                    composable(AppDestination.NewScan.route) {
                        NewScanScreen(
                            illustrationRes = illustrationResource
                                ?: ScreenIllustrationPool.NewScan.options.first().resourceFor(darkTheme),
                            scanName = scanName,
                            onScanNameChange = { scanName = it },
                            selectedSource = selectedCaptureSource,
                            onSourceSelected = { selectedCaptureSource = it },
                            onPreparation = { navController.navigate(AppDestination.ScanPreparation.route) }
                        )
                    }
                    composable(AppDestination.ScanPreparation.route) {
                        PreparationScreen(
                            illustrationRes = illustrationResource
                                ?: ScreenIllustrationPool.Preparation.options.first().resourceFor(darkTheme),
                            introRes = R.string.scan_preparation_intro,
                            selectedSourceLabelRes = when (selectedCaptureSource) {
                                CaptureSource.LiveCapture -> R.string.live_capture_title
                                CaptureSource.Mp4Import -> R.string.mp4_import_title
                            },
                            continueLabelRes = R.string.action_im_ready,
                            onBack = { navController.popBackStack() },
                            onContinue = { navController.popBackStack() }
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
private fun WelcomeScreen(
    language: AppLanguage,
    illustrationRes: Int,
    modifier: Modifier = Modifier,
    onLanguageSelected: (AppLanguage) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    ScrollableScreenColumn(modifier = modifier, verticalPadding = AppSpacing.large) {
        IllustrationArtwork(illustrationRes, height = 188.dp)
        Text(
            text = stringResource(R.string.brand_tagline),
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.welcome_description),
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
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
        SettingsGroup(title = stringResource(R.string.language_setting)) {
            LanguageChoiceRow(
                label = stringResource(R.string.language_english),
                selected = language == AppLanguage.English,
                onClick = { onLanguageSelected(AppLanguage.English) }
            )
            LanguageChoiceRow(
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
        IllustrationArtwork(illustrationRes, height = 184.dp)
        Text(
            stringResource(R.string.capture_explanation_body),
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        CaptureMethodCard(stringResource(R.string.live_capture_title), stringResource(R.string.live_capture_description))
        CaptureMethodCard(stringResource(R.string.mp4_import_title), stringResource(R.string.mp4_import_description))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.card,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                stringResource(R.string.manual_navigation_reminder),
                modifier = Modifier.padding(AppSpacing.large),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
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
    introRes: Int = R.string.preparation_intro,
    selectedSourceLabelRes: Int? = null,
    continueLabelRes: Int = R.string.action_continue,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val pokemonGoNotFoundMessage = stringResource(R.string.pokemon_go_not_found)
    ScrollableScreenColumn(modifier = modifier) {
        IllustrationArtwork(illustrationRes, height = 184.dp)
        Text(
            stringResource(introRes),
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        selectedSourceLabelRes?.let { sourceLabelRes ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.card,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = stringResource(R.string.selected_capture_source, stringResource(sourceLabelRes)),
                    modifier = Modifier.fillMaxWidth().padding(AppSpacing.large),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
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
    onScanNameChange: (String) -> Unit,
    selectedSource: CaptureSource,
    onSourceSelected: (CaptureSource) -> Unit,
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
                IllustrationArtwork(illustrationRes, height = 176.dp)
                Column(
                    modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.new_scan_state_title),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stringResource(R.string.new_scan_intro),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                QuietActionButton(
                    onClick = onPreparation,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(stringResource(R.string.review_preparation))
                }
                OutlinedTextField(
                    value = scanName,
                    onValueChange = onScanNameChange,
                    modifier = Modifier.fillMaxWidth().focusOutline(AppShapes.control),
                    label = { Text(stringResource(R.string.scan_name_optional)) },
                    singleLine = true,
                    shape = AppShapes.control
                )
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    Text(stringResource(R.string.scan_scope_title), style = MaterialTheme.typography.titleLarge)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.large),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
                        ) {
                            Text(stringResource(R.string.appraisal_scope), style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.appraisal_scope_fields),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    Text(stringResource(R.string.capture_source_title), style = MaterialTheme.typography.titleLarge)
                    CaptureSourceCard(
                        title = stringResource(R.string.live_capture_title),
                        body = stringResource(R.string.live_capture_description),
                        recommendation = stringResource(R.string.recommended_label),
                        selected = selectedSource == CaptureSource.LiveCapture,
                        onClick = { onSourceSelected(CaptureSource.LiveCapture) }
                    )
                    CaptureSourceCard(
                        title = stringResource(R.string.mp4_import_title),
                        body = stringResource(R.string.mp4_import_description),
                        selected = selectedSource == CaptureSource.Mp4Import,
                        onClick = { onSourceSelected(CaptureSource.Mp4Import) }
                    )
                }
            }
        }
        PrimaryActionButton(
            onClick = onPreparation,
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = AppSpacing.screen, vertical = AppSpacing.medium)
        ) {
            Text(stringResource(R.string.action_continue))
        }
    }
}

@Composable
private fun CaptureSourceCard(
    title: String,
    body: String,
    selected: Boolean,
    recommendation: String? = null,
    onClick: () -> Unit
) {
    val shape = AppShapes.card
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().focusOutline(shape),
        shape = shape,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            RadioButton(selected = selected, onClick = null)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    recommendation?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
private fun CaptureMethodCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ScrollableScreenColumn(
    modifier: Modifier = Modifier,
    verticalPadding: androidx.compose.ui.unit.Dp = AppSpacing.screen,
    maxContentWidth: androidx.compose.ui.unit.Dp = 560.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screen, vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large),
            content = content
        )
    }
}

@Composable
internal fun IllustratedInformationState(
    illustrationRes: Int,
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = AppSpacing.screen, vertical = AppSpacing.xLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IllustrationArtwork(illustrationRes, height = 208.dp)
            Spacer(Modifier.height(AppSpacing.large))
            Text(
                text = title,
                modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(AppSpacing.small))
            Text(
                text = body,
                modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(AppSpacing.xLarge))
            PrimaryActionButton(
                onClick = onAction,
                modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(max = 360.dp).fillMaxWidth()
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun IllustrationArtwork(
    illustrationRes: Int,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 200.dp
) {
    Box(
        modifier = modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(illustrationRes),
            contentDescription = null,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth().fillMaxHeight(),
            contentScale = ContentScale.Fit
        )
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
        SettingsGroup(title = stringResource(R.string.language_setting)) {
            LanguageChoiceRow(
                label = stringResource(R.string.language_english),
                selected = language == AppLanguage.English,
                onClick = { onLanguageSelected(AppLanguage.English) }
            )
            LanguageChoiceRow(
                label = stringResource(R.string.language_slovak),
                selected = language == AppLanguage.Slovak,
                onClick = { onLanguageSelected(AppLanguage.Slovak) }
            )
        }
        SettingsGroup(title = stringResource(R.string.theme_setting)) {
            ThemeChoiceRow(ThemePreference.System, theme, stringResource(R.string.theme_system), onThemeSelected)
            ThemeChoiceRow(ThemePreference.Light, theme, stringResource(R.string.theme_light), onThemeSelected)
            ThemeChoiceRow(ThemePreference.Dark, theme, stringResource(R.string.theme_dark), onThemeSelected)
        }
        SettingsGroup(title = stringResource(R.string.privacy_title)) {
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

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.card,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun LanguageChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppDimensions.choiceRowMinHeight)
            .focusOutline(AppShapes.control)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.xSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
private fun ThemeChoiceRow(
    value: ThemePreference,
    selected: ThemePreference,
    label: String,
    onSelected: (ThemePreference) -> Unit
) {
    LanguageChoiceRow(label, value == selected) { onSelected(value) }
}

private fun appVersion(context: Context): String =
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
