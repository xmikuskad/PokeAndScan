package com.falconsocka.pokeandscan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.falconsocka.pokeandscan.ui.components.PrimaryActionButton
import com.falconsocka.pokeandscan.ui.components.QuietActionButton
import com.falconsocka.pokeandscan.ui.components.SecondaryActionButton
import com.falconsocka.pokeandscan.ui.components.ScreenState
import com.falconsocka.pokeandscan.ui.components.LoadingState
import com.falconsocka.pokeandscan.ui.components.InlineNotice
import com.falconsocka.pokeandscan.ui.components.NoticeTone
import com.falconsocka.pokeandscan.ui.snapshot.DeleteSnapshotDialog
import com.falconsocka.pokeandscan.ui.snapshot.RenameSnapshotDialog
import com.falconsocka.pokeandscan.ui.snapshot.SnapshotMetric
import com.falconsocka.pokeandscan.ui.snapshot.SnapshotMetricKind
import com.falconsocka.pokeandscan.ui.snapshot.SnapshotMetricLine
import com.falconsocka.pokeandscan.ui.snapshot.SnapshotStatusLabels
import com.falconsocka.pokeandscan.ui.snapshot.presentation
import com.falconsocka.pokeandscan.ui.illustration.IllustratedInformationState
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.falconsocka.pokeandscan.ui.theme.AppShapes
import com.falconsocka.pokeandscan.ui.theme.AppSpacing
import com.falconsocka.pokeandscan.ui.theme.LocalStatusColors
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
        snapshots == null && !loadingError -> LoadingState(R.string.snapshot_loading, modifier)
        loadingError -> ScreenState(
            title = stringResource(R.string.snapshot_load_error_title),
            body = stringResource(R.string.snapshot_load_error_body),
            modifier = modifier,
            primaryAction = { PrimaryActionButton(onClick = onRetry) { Text(stringResource(R.string.snapshot_retry_action)) } }
        )
        snapshots.isNullOrEmpty() -> IllustratedInformationState(
            illustrationRes = illustrationRes,
            title = stringResource(R.string.empty_library_title),
            body = stringResource(R.string.empty_library_description),
            actionLabel = stringResource(R.string.new_scan_action),
            onAction = onNewScan,
            modifier = modifier
        )
        else -> androidx.compose.foundation.lazy.LazyColumn(
            modifier = modifier.fillMaxSize().testTag("snapshot-library-list"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = AppSpacing.screen, vertical = AppSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            val hasActiveJob = snapshots.any {
                it.lifecycle == SnapshotLifecycle.PROCESSING || it.lifecycle == SnapshotLifecycle.INCOMPLETE
            }
            val prioritizedSnapshots = snapshots.sortedBy { it.lifecycle.libraryPriority() }
            item {
                PrimaryActionButton(
                    onClick = onNewScan,
                    enabled = !hasActiveJob,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.new_scan_action))
                }
            }
            if (hasActiveJob) {
                item {
                    InlineNotice(stringResource(R.string.snapshot_active_job_notice), NoticeTone.Warning)
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
    isRenaming: Boolean,
    onBackToLibrary: () -> Unit,
    onRetry: () -> Unit,
    onContinueSetup: () -> Unit,
    onClearOperationError: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        loading && snapshot == null -> LoadingState(R.string.snapshot_loading, modifier)
        loadingError -> ScreenState(
            title = stringResource(R.string.snapshot_load_error_title),
            body = stringResource(R.string.snapshot_load_error_body),
            modifier = modifier,
            primaryAction = { PrimaryActionButton(onClick = onRetry) { Text(stringResource(R.string.snapshot_retry_action)) } },
            secondaryAction = { QuietActionButton(onClick = onBackToLibrary) { Text(stringResource(R.string.snapshot_back_to_library)) } }
        )
        snapshot == null -> ScreenState(
            title = stringResource(R.string.snapshot_missing_title),
            body = stringResource(R.string.snapshot_missing_body),
            modifier = modifier,
            primaryAction = { PrimaryActionButton(onClick = onBackToLibrary) { Text(stringResource(R.string.snapshot_back_to_library)) } }
        )
        else -> SnapshotDetailContent(
            snapshot = snapshot,
            operationError = operationError,
            isDeleting = isDeleting,
            isRenaming = isRenaming,
            onContinueSetup = onContinueSetup,
            onClearOperationError = onClearOperationError,
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
    val shape = AppShapes.card
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
                snapshot.sourceType?.let { source ->
                    Icon(
                        painter = painterResource(source.presentation().iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = AppSpacing.xSmall).size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                    Text(snapshot.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        formattedDate(snapshot.createdAtMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            SnapshotStatusLabels(
                lifecycle = snapshot.lifecycle,
                scopeCompleteness = snapshot.scopeCompleteness,
                source = snapshot.sourceType,
                scanScopeType = snapshot.scanScopeType,
                scanScopeDescription = snapshot.scanScopeDescription
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                SnapshotMetric(SnapshotMetricKind.Review, snapshot.reviewCount)
                SnapshotMetric(SnapshotMetricKind.Partial, snapshot.partialCount)
                SnapshotMetric(SnapshotMetricKind.Warning, snapshot.warningCount)
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
    isRenaming: Boolean,
    onContinueSetup: () -> Unit,
    onClearOperationError: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRenameDialog by remember(snapshot.id) { mutableStateOf(false) }
    var showDeleteDialog by remember(snapshot.id) { mutableStateOf(false) }
    var renameName by remember(snapshot.id) { mutableStateOf(snapshot.name) }

    Column(
        modifier = modifier.fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screen, vertical = AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        Card(
            shape = AppShapes.card,
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
                SnapshotStatusLabels(
                    lifecycle = snapshot.lifecycle,
                    scopeCompleteness = snapshot.scopeCompleteness,
                    source = snapshot.sourceType,
                    scanScopeType = snapshot.scanScopeType,
                    scanScopeDescription = snapshot.scanScopeDescription
                )
                Text(
                    stringResource(R.string.snapshot_detail_records, snapshot.recordCount),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    stringResource(R.string.snapshot_detail_included_records, snapshot.includedRecordCount),
                    style = MaterialTheme.typography.bodyMedium
                )
                SnapshotMetricLine(SnapshotMetricKind.Review, snapshot.reviewCount)
                SnapshotMetricLine(SnapshotMetricKind.Partial, snapshot.partialCount)
                SnapshotMetricLine(SnapshotMetricKind.Warning, snapshot.warningCount)
            }
        }

        if (snapshot.lifecycle != SnapshotLifecycle.COMPLETE ||
            snapshot.reviewCount > 0 || snapshot.warningCount > 0 || snapshot.partialCount > 0
        ) {
            InlineNotice(
                message = when (snapshot.lifecycle) {
                    SnapshotLifecycle.SETUP -> stringResource(R.string.snapshot_setup_saved_notice)
                    SnapshotLifecycle.PROCESSING -> stringResource(R.string.snapshot_processing_notice)
                    SnapshotLifecycle.INCOMPLETE -> stringResource(R.string.snapshot_not_finished_notice)
                    SnapshotLifecycle.COMPLETE -> stringResource(R.string.snapshot_review_notice)
                },
                tone = if (snapshot.lifecycle == SnapshotLifecycle.SETUP) NoticeTone.Info else NoticeTone.Warning
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
            InlineNotice(stringResource(R.string.snapshot_operation_error), NoticeTone.Error)
        }

        if (snapshot.lifecycle == SnapshotLifecycle.SETUP) {
            PrimaryActionButton(
                onClick = onContinueSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.snapshot_continue_setup))
            }
        }
        QuietActionButton(
            onClick = {
                onClearOperationError()
                renameName = snapshot.name
                showRenameDialog = true
            },
            enabled = !isDeleting && !isRenaming,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.snapshot_rename_action))
        }
        SecondaryActionButton(
            onClick = {
                onClearOperationError()
                showDeleteDialog = true
            },
            enabled = !isDeleting && !isRenaming,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.snapshot_delete_action))
        }
    }

    if (showRenameDialog) {
        RenameSnapshotDialog(
            name = renameName,
            onNameChange = { renameName = it },
            isSaving = isRenaming,
            operationError = operationError,
            onSave = onRename,
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showDeleteDialog) {
        DeleteSnapshotDialog(
            snapshotName = snapshot.name,
            isDeleting = isDeleting,
            operationError = operationError,
            onConfirm = onDelete,
            onDismiss = { showDeleteDialog = false }
        )
    }
}

private fun SnapshotLifecycle.libraryPriority(): Int = when (this) {
    SnapshotLifecycle.PROCESSING -> 0
    SnapshotLifecycle.INCOMPLETE -> 1
    SnapshotLifecycle.SETUP -> 2
    SnapshotLifecycle.COMPLETE -> 3
}

@Composable
private fun formattedDate(createdAtMillis: Long): String {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0].let { Locale.forLanguageTag(it.toLanguageTag()) }
    val formatter = remember(locale) { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale) }
    return formatter.format(Instant.ofEpochMilli(createdAtMillis).atZone(ZoneId.systemDefault()))
}
