package com.falconsocka.pokeandscan

import android.content.Context
import android.content.res.Configuration
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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

private enum class AppRoute { Library, NewScan, Settings }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PokeAndScanApp(preferences: AppPreferences) {
    val firstLaunch = remember { !preferences.hasSavedLanguage() }
    var language by remember {
        mutableStateOf(preferences.languageOrDefault().also(preferences::saveLanguage))
    }
    var theme by remember { mutableStateOf(preferences.theme()) }
    var route by remember { mutableStateOf(AppRoute.Library) }
    var showLanguageChoice by remember { mutableStateOf(firstLaunch) }
    var showPrivacyUnavailable by remember { mutableStateOf(false) }

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

    BackHandler(enabled = route != AppRoute.Library) {
        route = AppRoute.Library
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
                                text = when (route) {
                                    AppRoute.Library -> stringResource(R.string.library_title)
                                    AppRoute.NewScan -> stringResource(R.string.new_scan_title)
                                    AppRoute.Settings -> stringResource(R.string.settings_title)
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        actions = {
                            if (route == AppRoute.Library) {
                                TextButton(onClick = { route = AppRoute.Settings }) {
                                    Text(stringResource(R.string.settings_action))
                                }
                            }
                        }
                    )
                }
            ) { contentPadding ->
                when (route) {
                    AppRoute.Library -> LibraryScreen(
                        modifier = Modifier.padding(contentPadding),
                        onNewScan = { route = AppRoute.NewScan }
                    )
                    AppRoute.NewScan -> PlaceholderScreen(
                        title = stringResource(R.string.new_scan_title),
                        message = stringResource(R.string.new_scan_placeholder),
                        modifier = Modifier.padding(contentPadding)
                    )
                    AppRoute.Settings -> SettingsScreen(
                        language = language,
                        theme = theme,
                        appName = stringResource(R.string.app_name),
                        appVersion = appVersion(LocalContext.current),
                        modifier = Modifier.padding(contentPadding),
                        onLanguageSelected = {
                            preferences.saveLanguage(it)
                            language = it
                        },
                        onThemeSelected = {
                            preferences.saveTheme(it)
                            theme = it
                        },
                        onPrivacy = { showPrivacyUnavailable = true }
                    )
                }
            }
            if (showLanguageChoice) {
                AlertDialog(
                    onDismissRequest = {
                        preferences.saveLanguage(language)
                        showLanguageChoice = false
                    },
                    title = { Text(stringResource(R.string.choose_language_title)) },
                    text = {
                        Column {
                            Text(stringResource(R.string.choose_language_description))
                            Spacer(Modifier.height(12.dp))
                            LanguageChoiceRow(
                                label = stringResource(R.string.language_english),
                                selected = language == AppLanguage.English,
                                onClick = {
                                    preferences.saveLanguage(AppLanguage.English)
                                    language = AppLanguage.English
                                    showLanguageChoice = false
                                }
                            )
                            LanguageChoiceRow(
                                label = stringResource(R.string.language_slovak),
                                selected = language == AppLanguage.Slovak,
                                onClick = {
                                    preferences.saveLanguage(AppLanguage.Slovak)
                                    language = AppLanguage.Slovak
                                    showLanguageChoice = false
                                }
                            )
                        }
                    },
                    confirmButton = {}
                )
            }
            if (showPrivacyUnavailable) {
                AlertDialog(
                    onDismissRequest = { showPrivacyUnavailable = false },
                    title = { Text(stringResource(R.string.privacy_title)) },
                    text = { Text(stringResource(R.string.privacy_unavailable)) },
                    confirmButton = {
                        TextButton(onClick = { showPrivacyUnavailable = false }) {
                            Text(stringResource(R.string.action_close))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LibraryScreen(modifier: Modifier = Modifier, onNewScan: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        ExplorerIllustration(Modifier.size(width = 220.dp, height = 150.dp))
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.empty_library_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_library_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onNewScan,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.new_scan_action), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onThemeSelected: (ThemePreference) -> Unit,
    onPrivacy: () -> Unit
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
                    .clickable(onClick = onPrivacy)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.privacy_action), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.privacy_unavailable),
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
private fun ExplorerIllustration(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.primaryContainer
    val accent = MaterialTheme.colorScheme.secondary
    Canvas(modifier = modifier) {
        val frameWidth = size.width * .46f
        val frameHeight = size.height * .70f
        val left = (size.width - frameWidth) / 2
        val top = size.height * .08f
        drawRoundRect(
            color = surface,
            topLeft = Offset(left, top),
            size = Size(frameWidth, frameHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f)
        )
        val inset = size.width * .055f
        drawRoundRect(
            color = primary,
            topLeft = Offset(left + inset, top + inset),
            size = Size(frameWidth - inset * 2, frameHeight - inset * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f),
            style = Stroke(width = 3.dp.toPx())
        )
        val trail = Path().apply {
            moveTo(size.width * .18f, size.height * .87f)
            cubicTo(size.width * .32f, size.height * .65f, size.width * .6f, size.height * .99f, size.width * .82f, size.height * .76f)
        }
        drawPath(trail, accent, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(primary, radius = 5.dp.toPx(), center = Offset(size.width * .18f, size.height * .87f))
        drawCircle(accent, radius = 5.dp.toPx(), center = Offset(size.width * .82f, size.height * .76f))
        val cornerLength = size.width * .07f
        val corners = listOf(
            Offset(left, top) to Offset(1f, 1f),
            Offset(left + frameWidth, top) to Offset(-1f, 1f),
            Offset(left, top + frameHeight) to Offset(1f, -1f),
            Offset(left + frameWidth, top + frameHeight) to Offset(-1f, -1f)
        )
        corners.forEach { (point, direction) ->
            drawLine(primary, point, Offset(point.x + cornerLength * direction.x, point.y), 4.dp.toPx(), StrokeCap.Round)
            drawLine(primary, point, Offset(point.x, point.y + cornerLength * direction.y), 4.dp.toPx(), StrokeCap.Round)
        }
    }
}

private fun appVersion(context: Context): String =
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
