package com.falconsocka.pokeandscan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.falconsocka.pokeandscan.ui.theme.LocalStatusColors
import com.falconsocka.pokeandscan.ui.components.PrimaryActionButton
import com.falconsocka.pokeandscan.ui.components.QuietActionButton
import com.falconsocka.pokeandscan.ui.components.SecondaryActionButton
import com.falconsocka.pokeandscan.ui.theme.AppSpacing
import com.falconsocka.pokeandscan.ui.theme.AppShapes
import androidx.compose.foundation.lazy.items
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun SnapshotLibraryScreen(
    illustrationRes: Int,
    snapshots: List<SnapshotSummary>?,
    loadingError: Boolean,
    onRetry: () -> Unit,
    onNewScan: () -> Unit,
    onOpenSnapshot: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        snapshots == null && !loadingError -> LoadingSnapshotState(modifier)
        loadingError -> SnapshotErrorState(onRetry = onRetry, modifier = modifier)
        snapshots.isNullOrEmpty() -> IllustratedInformationState(
            illustrationRes = illustrationRes,
            title = stringResource(R.string.empty_library_title),
            body = stringResource(R.string.empty_library_description),
            actionLabel = stringResource(R.string.new_scan_action),
            onAction = onNewScan,
            modifier = modifier
        )
        else -> androidx.compose.foundation.lazy.LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = AppSpacing.screen, vertical = AppSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            val hasUnfinishedSnapshot = snapshots.any { it.lifecycle != SnapshotLifecycle.COMPLETE }
            val prioritizedSnapshots = snapshots.sortedBy { it.lifecycle.libraryPriority() }
            item {
                PrimaryActionButton(
                    onClick = onNewScan,
                    enabled = !hasUnfinishedSnapshot,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.new_scan_action))
                }
            }
            if (hasUnfinishedSnapshot) {
                item {
                    Text(
                        stringResource(R.string.snapshot_active_job_notice),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(prioritizedSnapshots, key = SnapshotSummary::id) { snapshot ->
                SnapshotCard(
                    snapshot = snapshot,
                    onOpen = { onOpenSnapshot(snapshot.id) }
                )
            }
        }
    }
}

@Composable
internal fun SnapshotDetailScreen(
    snapshot: SnapshotDetailSummary?,
    loading: Boolean,
    loadingError: Boolean,
    operationError: Boolean,
    isDeleting: Boolean,
    onBackToLibrary: () -> Unit,
    onRetry: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        loading && snapshot == null -> LoadingSnapshotState(modifier)
        loadingError -> SnapshotErrorState(onRetry = onRetry, modifier = modifier)
        snapshot == null -> SnapshotMissingState(onBackToLibrary, modifier)
        else -> SnapshotDetailContent(
            snapshot = snapshot,
            operationError = operationError,
            isDeleting = isDeleting,
            onRename = onRename,
            onDelete = onDelete,
            modifier = modifier
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun SnapshotCard(
    snapshot: SnapshotSummary,
    onOpen: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                SnapshotGlyph(
                    kind = sourceGlyph(snapshot.sourceType),
                    contentDescription = sourceDescription(snapshot.sourceType),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(snapshot.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        formattedDate(snapshot.createdAtMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                SnapshotStateLabel(snapshot.lifecycle)
                snapshot.scopeCompleteness?.let { SnapshotScopeLabel(it) }
                snapshot.sourceType?.let { SnapshotSourceLabel(it) }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                SnapshotMetric(R.string.snapshot_metric_review, snapshot.reviewCount, SnapshotGlyphKind.REVIEW)
                SnapshotMetric(R.string.snapshot_metric_partial, snapshot.partialCount, SnapshotGlyphKind.PARTIAL)
                SnapshotMetric(R.string.snapshot_metric_warnings, snapshot.warningCount, SnapshotGlyphKind.WARNING)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall), verticalAlignment = Alignment.CenterVertically) {
                QuietActionButton(onClick = onOpen) { Text(stringResource(R.string.snapshot_open_action)) }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun SnapshotDetailContent(
    snapshot: SnapshotDetailSummary,
    operationError: Boolean,
    isDeleting: Boolean,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRenameDialog by remember(snapshot.id) { mutableStateOf(false) }
    var showDeleteDialog by remember(snapshot.id) { mutableStateOf(false) }
    var name by remember(snapshot.id, snapshot.name) { mutableStateOf(snapshot.name) }

    Column(
        modifier = modifier.fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screen, vertical = AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
            ) {
                Text(stringResource(R.string.snapshot_summary_heading), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.snapshot_created_at, formattedDate(snapshot.createdAtMillis)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    SnapshotStateLabel(snapshot.lifecycle)
                    snapshot.scopeCompleteness?.let { SnapshotScopeLabel(it) }
                    snapshot.sourceType?.let { SnapshotSourceLabel(it) }
                }
                SummaryLine(R.string.snapshot_detail_records, snapshot.recordCount)
                SummaryLine(R.string.snapshot_detail_included_records, snapshot.includedRecordCount)
                SummaryLine(R.string.snapshot_metric_review, snapshot.reviewCount, SnapshotGlyphKind.REVIEW)
                SummaryLine(R.string.snapshot_metric_partial, snapshot.partialCount, SnapshotGlyphKind.PARTIAL)
                SummaryLine(R.string.snapshot_metric_warnings, snapshot.warningCount, SnapshotGlyphKind.WARNING)
            }
        }

        if (snapshot.lifecycle != SnapshotLifecycle.COMPLETE ||
            snapshot.reviewCount > 0 || snapshot.warningCount > 0 || snapshot.partialCount > 0
        ) {
            Text(
                text = when (snapshot.lifecycle) {
                    SnapshotLifecycle.PROCESSING -> stringResource(R.string.snapshot_processing_notice)
                    SnapshotLifecycle.INCOMPLETE -> stringResource(R.string.snapshot_not_finished_notice)
                    SnapshotLifecycle.COMPLETE -> stringResource(R.string.snapshot_review_notice)
                },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).padding(14.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (snapshot.recordCount == 0 && snapshot.warningCount == 0) {
            Text(
                stringResource(R.string.snapshot_no_records),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (operationError) {
            Text(
                stringResource(R.string.snapshot_operation_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        PrimaryActionButton(
            onClick = { showRenameDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.snapshot_rename_action))
        }
        SecondaryActionButton(
            onClick = { showDeleteDialog = true },
            enabled = !isDeleting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.snapshot_delete_action))
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(R.string.snapshot_rename_title)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.snapshot_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.control
                )
            },
            confirmButton = {
                QuietActionButton(
                    onClick = {
                        showRenameDialog = false
                        onRename(name)
                    },
                    enabled = name.isNotBlank() && !isDeleting
                ) { Text(stringResource(R.string.snapshot_save_name_action)) }
            },
            dismissButton = {
                QuietActionButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.action_back))
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text(stringResource(R.string.snapshot_delete_confirmation_title, snapshot.name)) },
            text = { Text(stringResource(R.string.snapshot_delete_confirmation_body)) },
            confirmButton = {
                QuietActionButton(onClick = onDelete, enabled = !isDeleting) {
                    Text(stringResource(if (isDeleting) R.string.snapshot_deleting else R.string.snapshot_delete_action))
                }
            },
            dismissButton = {
                QuietActionButton(onClick = { showDeleteDialog = false }, enabled = !isDeleting) {
                    Text(stringResource(R.string.action_back))
                }
            }
        )
    }
}

private fun SnapshotLifecycle.libraryPriority(): Int = when (this) {
    SnapshotLifecycle.PROCESSING -> 0
    SnapshotLifecycle.INCOMPLETE -> 1
    SnapshotLifecycle.COMPLETE -> 2
}

@Composable
private fun SnapshotStateLabel(lifecycle: SnapshotLifecycle) {
    val palette = LocalStatusColors.current
    val state = when (lifecycle) {
        SnapshotLifecycle.PROCESSING -> Triple(R.string.snapshot_lifecycle_processing, SnapshotGlyphKind.PROCESSING, palette.needsReview)
        SnapshotLifecycle.INCOMPLETE -> Triple(R.string.snapshot_lifecycle_incomplete, SnapshotGlyphKind.WARNING, palette.needsReview)
        SnapshotLifecycle.COMPLETE -> Triple(R.string.snapshot_lifecycle_complete, SnapshotGlyphKind.COMPLETE, palette.ready)
    }
    SemanticLabel(state.first, state.second, state.third.foreground, state.third.container)
}

@Composable
private fun SnapshotScopeLabel(scope: SnapshotScopeCompleteness) {
    val palette = LocalStatusColors.current
    val state = if (scope == SnapshotScopeCompleteness.INTENDED_RANGE) {
        Triple(R.string.snapshot_scope_intended, SnapshotGlyphKind.COMPLETE, palette.ready)
    } else {
        Triple(R.string.snapshot_scope_partial, SnapshotGlyphKind.PARTIAL, palette.partial)
    }
    SemanticLabel(state.first, state.second, state.third.foreground, state.third.container)
}

@Composable
private fun SnapshotSourceLabel(source: SnapshotSourceType) {
    val sourceRes = if (source == SnapshotSourceType.LIVE) R.string.snapshot_source_live else R.string.snapshot_source_mp4
    SemanticLabel(sourceRes, sourceGlyph(source), MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surface)
}

@Composable
private fun SemanticLabel(labelRes: Int, kind: SnapshotGlyphKind, foreground: Color, background: Color) {
    val label = stringResource(labelRes)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = background,
        contentColor = foreground,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SnapshotGlyph(kind, label, foreground, Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SnapshotMetric(labelRes: Int, count: Int, kind: SnapshotGlyphKind) {
    val palette = LocalStatusColors.current
    val tint = when (kind) {
        SnapshotGlyphKind.PARTIAL -> palette.partial.foreground
        SnapshotGlyphKind.WARNING, SnapshotGlyphKind.REVIEW -> palette.needsReview.foreground
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        SnapshotGlyph(kind, stringResource(labelRes, count), tint, Modifier.size(16.dp))
        Text(
            stringResource(labelRes, count),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SummaryLine(labelRes: Int, count: Int, kind: SnapshotGlyphKind? = null) {
    val palette = LocalStatusColors.current
    val tint = when (kind) {
        SnapshotGlyphKind.PARTIAL -> palette.partial.foreground
        SnapshotGlyphKind.WARNING, SnapshotGlyphKind.REVIEW -> palette.needsReview.foreground
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        kind?.let { SnapshotGlyph(it, stringResource(labelRes, count), tint, Modifier.size(18.dp)) }
        Text(
            text = stringResource(labelRes, count),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SnapshotGlyph(
    kind: SnapshotGlyphKind,
    contentDescription: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp).semantics { this.contentDescription = contentDescription }) {
        val stroke = 1.8.dp.toPx()
        val inset = stroke / 2
        val x = size.width / 2
        val y = size.height / 2
        when (kind) {
            SnapshotGlyphKind.COMPLETE -> {
                drawCircle(tint, size.minDimension / 2 - inset, style = Stroke(stroke))
                drawLine(tint, androidx.compose.ui.geometry.Offset(size.width * .27f, size.height * .53f), androidx.compose.ui.geometry.Offset(size.width * .43f, size.height * .68f), stroke, StrokeCap.Round)
                drawLine(tint, androidx.compose.ui.geometry.Offset(size.width * .43f, size.height * .68f), androidx.compose.ui.geometry.Offset(size.width * .75f, size.height * .34f), stroke, StrokeCap.Round)
            }
            SnapshotGlyphKind.PROCESSING -> {
                drawCircle(tint, size.minDimension / 2 - inset, style = Stroke(stroke))
                drawLine(tint, androidx.compose.ui.geometry.Offset(x, size.height * .22f), androidx.compose.ui.geometry.Offset(x, y), stroke, StrokeCap.Round)
                drawLine(tint, androidx.compose.ui.geometry.Offset(x, y), androidx.compose.ui.geometry.Offset(size.width * .7f, size.height * .63f), stroke, StrokeCap.Round)
            }
            SnapshotGlyphKind.WARNING, SnapshotGlyphKind.REVIEW -> {
                val path = Path().apply {
                    moveTo(x, inset)
                    lineTo(size.width - inset, size.height - inset)
                    lineTo(inset, size.height - inset)
                    close()
                }
                drawPath(path, tint, style = Stroke(stroke, cap = StrokeCap.Round))
                drawLine(tint, androidx.compose.ui.geometry.Offset(x, size.height * .37f), androidx.compose.ui.geometry.Offset(x, size.height * .61f), stroke, StrokeCap.Round)
                drawCircle(tint, stroke / 1.5f, androidx.compose.ui.geometry.Offset(x, size.height * .77f))
            }
            SnapshotGlyphKind.PARTIAL -> {
                drawCircle(tint, size.minDimension / 2 - inset, style = Stroke(stroke))
                drawLine(tint, androidx.compose.ui.geometry.Offset(size.width * .28f, y), androidx.compose.ui.geometry.Offset(size.width * .72f, y), stroke, StrokeCap.Round)
            }
            SnapshotGlyphKind.LIVE -> {
                drawRoundRect(tint, topLeft = androidx.compose.ui.geometry.Offset(size.width * .08f, size.height * .22f), size = androidx.compose.ui.geometry.Size(size.width * .84f, size.height * .64f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(stroke * 2), style = Stroke(stroke))
                drawCircle(tint, size.width * .12f, androidx.compose.ui.geometry.Offset(x, y))
            }
            SnapshotGlyphKind.MP4 -> {
                drawRoundRect(tint, topLeft = androidx.compose.ui.geometry.Offset(size.width * .08f, size.height * .15f), size = androidx.compose.ui.geometry.Size(size.width * .84f, size.height * .7f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(stroke * 2), style = Stroke(stroke))
                val play = Path().apply {
                    moveTo(size.width * .43f, size.height * .34f)
                    lineTo(size.width * .65f, y)
                    lineTo(size.width * .43f, size.height * .66f)
                    close()
                }
                drawPath(play, tint)
            }
            SnapshotGlyphKind.UNKNOWN -> {
                drawCircle(tint, size.minDimension / 2 - inset, style = Stroke(stroke))
                drawLine(tint, androidx.compose.ui.geometry.Offset(x, size.height * .33f), androidx.compose.ui.geometry.Offset(x, size.height * .57f), stroke, StrokeCap.Round)
                drawCircle(tint, stroke / 1.5f, androidx.compose.ui.geometry.Offset(x, size.height * .75f))
            }
        }
    }
}

private enum class SnapshotGlyphKind { COMPLETE, PROCESSING, WARNING, REVIEW, PARTIAL, LIVE, MP4, UNKNOWN }

private fun sourceGlyph(source: SnapshotSourceType?): SnapshotGlyphKind = when (source) {
    SnapshotSourceType.LIVE -> SnapshotGlyphKind.LIVE
    SnapshotSourceType.MP4 -> SnapshotGlyphKind.MP4
    null -> SnapshotGlyphKind.UNKNOWN
}

@Composable
private fun sourceDescription(source: SnapshotSourceType?): String = when (source) {
    SnapshotSourceType.LIVE -> stringResource(R.string.snapshot_source_live)
    SnapshotSourceType.MP4 -> stringResource(R.string.snapshot_source_mp4)
    null -> stringResource(R.string.snapshot_source_unknown)
}

@Composable
private fun LoadingSnapshotState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
            CircularProgressIndicator()
            Text(stringResource(R.string.snapshot_loading), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SnapshotErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.xLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.snapshot_load_error_title), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(AppSpacing.small))
        Text(stringResource(R.string.snapshot_load_error_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(AppSpacing.screen))
        PrimaryActionButton(onClick = onRetry) {
            Text(stringResource(R.string.snapshot_retry_action))
        }
    }
}

@Composable
private fun SnapshotMissingState(onBackToLibrary: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.xLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.snapshot_missing_title), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(AppSpacing.small))
        Text(stringResource(R.string.snapshot_missing_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(AppSpacing.screen))
        PrimaryActionButton(onClick = onBackToLibrary) {
            Text(stringResource(R.string.snapshot_back_to_library))
        }
    }
}

@Composable
private fun formattedDate(createdAtMillis: Long): String {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0].let { Locale.forLanguageTag(it.toLanguageTag()) }
    val formatter = remember(locale) { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale) }
    return formatter.format(Instant.ofEpochMilli(createdAtMillis).atZone(ZoneId.systemDefault()))
}
