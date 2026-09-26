package com.falconsocka.pokeandscan

import android.content.Context
import android.content.res.Configuration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

internal const val MAX_SCAN_NAME_LENGTH = 80

internal enum class SnapshotNameIssue {
    TOO_LONG,
    UNSAFE_CHARACTERS
}

internal fun snapshotNameIssue(value: String): SnapshotNameIssue? {
    val trimmed = value.trim { it.isWhitespace() }
    if (trimmed.isEmpty()) return null
    if (trimmed.codePointCount(0, trimmed.length) > MAX_SCAN_NAME_LENGTH) return SnapshotNameIssue.TOO_LONG
    if (trimmed.any(Char::isISOControl)) return SnapshotNameIssue.UNSAFE_CHARACTERS
    return null
}

internal fun normalizedSnapshotName(value: String): String? =
    value.trim { it.isWhitespace() }.takeIf(String::isNotEmpty)

internal fun defaultSnapshotName(
    context: Context,
    language: AppLanguage,
    createdAtMillis: Long = System.currentTimeMillis()
): String {
    val locale = Locale.forLanguageTag(language.languageTag)
    val localizedContext = context.createConfigurationContext(
        Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
    )
    val date = Instant.ofEpochMilli(createdAtMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    return localizedContext.getString(R.string.default_scan_name, date)
}
