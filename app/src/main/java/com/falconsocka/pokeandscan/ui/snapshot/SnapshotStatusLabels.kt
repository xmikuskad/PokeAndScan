package com.falconsocka.pokeandscan.ui.snapshot

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.falconsocka.pokeandscan.R
import com.falconsocka.pokeandscan.ScanScopeType
import com.falconsocka.pokeandscan.SnapshotLifecycle
import com.falconsocka.pokeandscan.SnapshotScopeCompleteness
import com.falconsocka.pokeandscan.SnapshotSourceType
import com.falconsocka.pokeandscan.ui.components.AppIcons
import com.falconsocka.pokeandscan.ui.components.BadgeTone
import com.falconsocka.pokeandscan.ui.components.StatusBadge
import com.falconsocka.pokeandscan.ui.theme.AppSpacing

internal data class SnapshotSourcePresentation(
    @field:StringRes val labelRes: Int,
    @field:DrawableRes val iconRes: Int
)

internal fun SnapshotSourceType.presentation(): SnapshotSourcePresentation = when (this) {
    SnapshotSourceType.LIVE -> SnapshotSourcePresentation(R.string.snapshot_source_live, AppIcons.SourceLive)
    SnapshotSourceType.MP4 -> SnapshotSourcePresentation(R.string.snapshot_source_mp4, AppIcons.SourceMp4)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SnapshotStatusLabels(
    lifecycle: SnapshotLifecycle,
    scopeCompleteness: SnapshotScopeCompleteness?,
    source: SnapshotSourceType?,
    scanScopeType: ScanScopeType?,
    scanScopeDescription: String?,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        SnapshotLifecycleBadge(lifecycle)
        scopeCompleteness?.let { SnapshotScopeBadge(it) }
        scanScopeType?.let { ScanIntentScopeBadge(it, scanScopeDescription) }
        source?.let { SnapshotSourceBadge(it) }
    }
}

@Composable
private fun SnapshotLifecycleBadge(lifecycle: SnapshotLifecycle) {
    val (labelRes, iconRes, tone) = when (lifecycle) {
        SnapshotLifecycle.SETUP -> Triple(R.string.snapshot_lifecycle_setup, AppIcons.StatusPartial, BadgeTone.Partial)
        SnapshotLifecycle.PROCESSING -> Triple(R.string.snapshot_lifecycle_processing, AppIcons.StatusProcessing, BadgeTone.Review)
        SnapshotLifecycle.INCOMPLETE -> Triple(R.string.snapshot_lifecycle_incomplete, AppIcons.StatusWarning, BadgeTone.Review)
        SnapshotLifecycle.COMPLETE -> Triple(R.string.snapshot_lifecycle_complete, AppIcons.StatusComplete, BadgeTone.Neutral)
    }
    StatusBadge(stringResource(labelRes), iconRes, tone)
}

@Composable
private fun SnapshotScopeBadge(scope: SnapshotScopeCompleteness) {
    val isIntended = scope == SnapshotScopeCompleteness.INTENDED_RANGE
    StatusBadge(
        label = stringResource(if (isIntended) R.string.snapshot_scope_intended else R.string.snapshot_scope_partial),
        iconRes = if (isIntended) AppIcons.StatusComplete else AppIcons.StatusPartial,
        tone = if (isIntended) BadgeTone.Neutral else BadgeTone.Partial
    )
}

@Composable
private fun ScanIntentScopeBadge(scopeType: ScanScopeType, description: String?) {
    val label = when (scopeType) {
        ScanScopeType.WHOLE_COLLECTION -> stringResource(R.string.scan_scope_whole_collection)
        ScanScopeType.FILTERED_SUBSET -> description?.takeIf(String::isNotBlank)?.let {
            stringResource(R.string.scan_scope_filtered_summary, it)
        } ?: stringResource(R.string.scan_scope_filtered_subset)
    }
    StatusBadge(label)
}

@Composable
private fun SnapshotSourceBadge(source: SnapshotSourceType) {
    val presentation = source.presentation()
    StatusBadge(
        label = stringResource(presentation.labelRes),
        iconRes = presentation.iconRes,
        tone = BadgeTone.Neutral
    )
}
