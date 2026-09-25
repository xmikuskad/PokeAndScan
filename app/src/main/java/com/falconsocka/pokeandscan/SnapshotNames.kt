package com.falconsocka.pokeandscan

import android.content.Context
import android.content.res.Configuration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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
