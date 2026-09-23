package com.falconsocka.pokeandscan.ui.theme

import androidx.compose.ui.graphics.Color

val LightPrimary = Color(0xFF0067D6)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFDCEBFF)
val LightOnPrimaryContainer = Color(0xFF0B3A73)
val LightSecondary = Color(0xFF7C3AED)
val LightBackground = Color(0xFFEEF4FF)
val LightSurface = Color(0xFFF8FAFF)
val LightRaisedSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF64748B)
val LightOutline = Color(0xFFC9D7EA)

val DarkPrimary = Color(0xFF60A5FA)
val DarkOnPrimary = Color(0xFF0B1220)
val DarkPrimaryContainer = Color(0xFF16355B)
val DarkOnPrimaryContainer = Color(0xFFDCEBFF)
val DarkSecondary = Color(0xFFA78BFA)
val DarkBackground = Color(0xFF0B1220)
val DarkSurface = Color(0xFF111827)
val DarkRaisedSurface = Color(0xFF172033)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFFCBD5E1)
val DarkOutline = Color(0xFF334155)

data class SemanticStatusColor(val foreground: Color, val container: Color)

data class SemanticStatusPalette(
    val ready: SemanticStatusColor,
    val needsReview: SemanticStatusColor,
    val partial: SemanticStatusColor,
    val excluded: SemanticStatusColor,
    val error: SemanticStatusColor
)

val LightStatusColors = SemanticStatusPalette(
    ready = SemanticStatusColor(Color(0xFF15803D), Color(0xFFDCFCE7)),
    needsReview = SemanticStatusColor(Color(0xFFB45309), Color(0xFFFEF3C7)),
    partial = SemanticStatusColor(Color(0xFF1D4ED8), Color(0xFFDBEAFE)),
    excluded = SemanticStatusColor(Color(0xFF475569), Color(0xFFE2E8F0)),
    error = SemanticStatusColor(Color(0xFFBE123C), Color(0xFFFFE4E6))
)

val DarkStatusColors = SemanticStatusPalette(
    ready = SemanticStatusColor(Color(0xFF4ADE80), Color(0xFF123522)),
    needsReview = SemanticStatusColor(Color(0xFFFBBF24), Color(0xFF3B2A08)),
    partial = SemanticStatusColor(Color(0xFF93C5FD), Color(0xFF153255)),
    excluded = SemanticStatusColor(Color(0xFFCBD5E1), Color(0xFF243041)),
    error = SemanticStatusColor(Color(0xFFFB7185), Color(0xFF4A1724))
)
