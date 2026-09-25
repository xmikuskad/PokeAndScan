package com.falconsocka.pokeandscan

import android.content.Context
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
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.falconsocka.pokeandscan.ui.theme.PokeAndScanTheme
import java.util.Locale

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

private sealed class AppDestination(val route: String, val titleRes: Int) {
    data object Welcome : AppDestination("welcome", R.string.welcome_title)
    data object CaptureExplanation : AppDestination("capture-explanation", R.string.capture_explanation_title)
    data object OnboardingPreparation : AppDestination("onboarding-preparation", R.string.preparation_title)
    data object Library : AppDestination("library", R.string.library_title)
    data object NewScan : AppDestination("new-scan", R.string.new_scan_title)
    data object ScanPreparation : AppDestination("scan-preparation", R.string.preparation_title)
    data object Settings : AppDestination("settings", R.string.settings_title)

    companion object {
        fun fromRoute(route: String?): AppDestination = when (route) {
            Welcome.route -> Welcome
            CaptureExplanation.route -> CaptureExplanation
            OnboardingPreparation.route -> OnboardingPreparation
            NewScan.route -> NewScan
            ScanPreparation.route -> ScanPreparation
            Settings.route -> Settings
            else -> Library
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PokeAndScanApp(preferences: AppPreferences) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val startDestination = remember {
        if (preferences.hasCompletedOnboarding()) AppDestination.Library.route else AppDestination.Welcome.route
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = AppDestination.fromRoute(backStackEntry?.destination?.route)
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
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
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
                                text = stringResource(currentDestination.titleRes),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        actions = {
                            if (currentDestination == AppDestination.Library) {
                                TextButton(onClick = { navController.navigate(AppDestination.Settings.route) }) {
                                    Text(stringResource(R.string.settings_action))
                                }
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
                            onBack = { navController.popBackStack() },
                            onContinue = { navController.navigate(AppDestination.OnboardingPreparation.route) }
                        )
                    }
                    composable(AppDestination.OnboardingPreparation.route) {
                        PreparationScreen(
                            onBack = { navController.popBackStack() },
                            onContinue = finishOnboarding
                        )
                    }
                    composable(AppDestination.Library.route) {
                        LibraryScreen(onNewScan = { navController.navigate(AppDestination.NewScan.route) })
                    }
                    composable(AppDestination.NewScan.route) {
                        NewScanScreen(onPreparation = { navController.navigate(AppDestination.ScanPreparation.route) })
                    }
                    composable(AppDestination.ScanPreparation.route) {
                        PreparationScreen(
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

@Composable
private fun WelcomeScreen(
    language: AppLanguage,
    modifier: Modifier = Modifier,
    onLanguageSelected: (AppLanguage) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExplorerLandscape(
            modifier = Modifier.fillMaxWidth().height(184.dp),
            contentDescription = stringResource(R.string.welcome_illustration_description)
        )
        Text(
            text = stringResource(R.string.brand_tagline),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.welcome_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Text(
                text = stringResource(R.string.welcome_details),
                modifier = Modifier.padding(16.dp),
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
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.action_continue), fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.skip_introduction))
        }
    }
}

@Composable
private fun CaptureExplanationScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExplorerLandscape(
            modifier = Modifier.fillMaxWidth().height(168.dp),
            contentDescription = stringResource(R.string.capture_illustration_description)
        )
        Text(
            stringResource(R.string.capture_explanation_body),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        CaptureMethodCard(stringResource(R.string.live_capture_title), stringResource(R.string.live_capture_description))
        CaptureMethodCard(stringResource(R.string.mp4_import_title), stringResource(R.string.mp4_import_description))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                stringResource(R.string.manual_navigation_reminder),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.action_back)) }
            Button(onClick = onContinue, modifier = Modifier.weight(1f).defaultMinSize(minHeight = 52.dp)) {
                Text(stringResource(R.string.action_continue))
            }
        }
    }
}

@Composable
private fun PreparationScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExplorerLandscape(
            modifier = Modifier.fillMaxWidth().height(112.dp),
            contentDescription = stringResource(R.string.preparation_illustration_description)
        )
        Text(stringResource(R.string.preparation_intro), style = MaterialTheme.typography.bodyLarge)
        GuidanceSection(stringResource(R.string.reference_setup_title), stringResource(R.string.reference_setup_details))
        GuidanceSection(stringResource(R.string.scan_scope_title), stringResource(R.string.scan_scope_details))
        GuidanceSection(stringResource(R.string.nickname_warning_title), stringResource(R.string.nickname_warning_details))
        GuidanceSection(stringResource(R.string.traversal_title), stringResource(R.string.traversal_details))
        OutlinedButton(
            onClick = {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.nianticlabs.pokemongo")
                if (launchIntent != null) context.startActivity(launchIntent)
                else Toast.makeText(context, context.getString(R.string.pokemon_go_not_found), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp)
        ) {
            Text(stringResource(R.string.open_pokemon_go))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.action_back)) }
            Button(onClick = onContinue, modifier = Modifier.weight(1f).defaultMinSize(minHeight = 52.dp)) {
                Text(stringResource(R.string.action_continue))
            }
        }
    }
}

@Composable
private fun NewScanScreen(modifier: Modifier = Modifier, onPreparation: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.new_scan_placeholder), style = MaterialTheme.typography.bodyLarge)
        Button(
            onClick = onPreparation,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp)
        ) {
            Text(stringResource(R.string.review_preparation))
        }
    }
}

@Composable
private fun GuidanceSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CaptureMethodCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LibraryScreen(modifier: Modifier = Modifier, onNewScan: () -> Unit) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ExplorerLandscape(
                modifier = Modifier.widthIn(max = 260.dp).fillMaxWidth().height(138.dp),
                contentDescription = stringResource(R.string.empty_library_illustration_description)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.empty_library_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.empty_library_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 340.dp)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onNewScan,
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.new_scan_action), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    language: AppLanguage,
    theme: ThemePreference,
    appName: String,
    appVersion: String,
    modifier: Modifier = Modifier,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (ThemePreference) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
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
                    .defaultMinSize(minHeight = 56.dp)
                    .clickable(onClick = onPrivacyInformation)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.privacy_action), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.privacy_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.version_footer, appName, appVersion),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
            .defaultMinSize(minHeight = 52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
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

@Composable
private fun ExplorerLandscape(modifier: Modifier = Modifier, contentDescription: String) {
    val dark = MaterialTheme.colorScheme.background.red < .2f
    Box(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                .14f to Color.Black,
                                .86f to Color.Black,
                                1f to Color.Transparent
                            )
                        ),
                        blendMode = BlendMode.DstIn
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                .1f to Color.Black,
                                .9f to Color.Black,
                                1f to Color.Transparent
                            )
                        ),
                        blendMode = BlendMode.DstIn
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to Color.Black,
                                .6f to Color.Black,
                                1f to Color.Transparent
                            ),
                            center = Offset(size.width * .5f, size.height * .45f),
                            radius = size.maxDimension * .78f
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            Image(
                painter = painterResource(R.drawable.explorer_landscape_fade),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (dark) {
                Box(Modifier.fillMaxSize().background(Color(0x480B1220)))
            }
        }
    }
}

private fun appVersion(context: Context): String =
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
