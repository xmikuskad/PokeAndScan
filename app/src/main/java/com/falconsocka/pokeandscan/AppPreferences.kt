package com.falconsocka.pokeandscan

import android.content.SharedPreferences
import java.util.Locale

enum class AppLanguage(val languageTag: String) {
    English("en"),
    Slovak("sk");

    companion object {
        fun forLanguageTag(languageTag: String): AppLanguage =
            if (languageTag.substringBefore('-').equals("sk", ignoreCase = true)) Slovak else English
    }
}

enum class ThemePreference {
    System,
    Light,
    Dark
}

fun initialLanguage(systemLanguageTag: String): AppLanguage =
    AppLanguage.forLanguageTag(systemLanguageTag)

class AppPreferences(private val preferences: SharedPreferences) {
    private val languageKey = "app_language"
    private val themeKey = "theme_preference"

    fun hasSavedLanguage(): Boolean = preferences.contains(languageKey)

    fun languageOrDefault(systemLanguageTag: String = Locale.getDefault().toLanguageTag()): AppLanguage =
        preferences.getString(languageKey, null)
            ?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
            ?: initialLanguage(systemLanguageTag)

    fun saveLanguage(language: AppLanguage) {
        preferences.edit().putString(languageKey, language.name).apply()
    }

    fun theme(): ThemePreference =
        preferences.getString(themeKey, null)
            ?.let { runCatching { ThemePreference.valueOf(it) }.getOrNull() }
            ?: ThemePreference.System

    fun saveTheme(theme: ThemePreference) {
        preferences.edit().putString(themeKey, theme.name).apply()
    }
}
