package com.falconsocka.pokeandscan.ui.snapshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.falconsocka.pokeandscan.R
import com.falconsocka.pokeandscan.ui.components.BadgeTone
import com.falconsocka.pokeandscan.ui.components.AppIcons
import com.falconsocka.pokeandscan.ui.components.palette
import com.falconsocka.pokeandscan.ui.theme.AppSpacing

internal enum class SnapshotMetricKind { Review, Partial, Warning }

private data class MetricPresentation(val labelRes: Int, val iconRes: Int, val tone: BadgeTone)

private fun SnapshotMetricKind.presentation(): MetricPresentation = when (this) {
    SnapshotMetricKind.Review -> MetricPresentation(R.string.snapshot_metric_review, AppIcons.StatusWarning, BadgeTone.Review)
    SnapshotMetricKind.Partial -> MetricPresentation(R.string.snapshot_metric_partial, AppIcons.StatusPartial, BadgeTone.Partial)
    SnapshotMetricKind.Warning -> MetricPresentation(R.string.snapshot_metric_warnings, AppIcons.StatusWarning, BadgeTone.Review)
}

/** Compact metric used on a library card. */
@Composable
internal fun SnapshotMetric(kind: SnapshotMetricKind, count: Int, modifier: Modifier = Modifier) {
    val presentation = kind.presentation()
    val tint = presentation.tone.palette().foreground
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
        Icon(painterResource(presentation.iconRes), null, modifier = Modifier.size(16.dp), tint = tint)
        Text(metricLabel(presentation, count), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Wider detail line keeps the icon, text, and number aligned while allowing localized text to wrap. */
@Composable
internal fun SnapshotMetricLine(kind: SnapshotMetricKind, count: Int, modifier: Modifier = Modifier) {
    val presentation = kind.presentation()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(presentation.iconRes), null, modifier = Modifier.size(18.dp), tint = presentation.tone.palette().foreground)
        Text(
            text = metricLabel(presentation, count),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun metricLabel(presentation: MetricPresentation, count: Int): String =
    stringResource(presentation.labelRes, count)
