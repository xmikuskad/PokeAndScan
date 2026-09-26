package com.falconsocka.pokeandscan.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.falconsocka.pokeandscan.ui.theme.AppShapes
import com.falconsocka.pokeandscan.ui.theme.AppSpacing
import com.falconsocka.pokeandscan.ui.theme.LocalStatusColors

enum class NoticeTone { Info, Warning, Error, Success }

data class NoticeAction(val labelRes: Int, val onClick: () -> Unit)

/** A localized message with a semantic tone and an optional action supplied as one value. */
@Composable
fun InlineNotice(
    message: String,
    tone: NoticeTone,
    modifier: Modifier = Modifier,
    title: String? = null,
    action: NoticeAction? = null
) {
    val statusColors = LocalStatusColors.current
    val (container, content, iconRes) = when (tone) {
        NoticeTone.Info -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, AppIcons.NoticeInfo)
        NoticeTone.Warning -> Triple(statusColors.needsReview.container, statusColors.needsReview.foreground, AppIcons.StatusWarning)
        NoticeTone.Error -> Triple(statusColors.error.container, statusColors.error.foreground, AppIcons.NoticeError)
        NoticeTone.Success -> Triple(statusColors.ready.container, statusColors.ready.foreground, AppIcons.StatusComplete)
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.card,
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.large),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(20.dp).semantics { hideFromAccessibility() }
            )
            Spacer(Modifier.width(AppSpacing.medium))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)) {
                title?.let { Text(it, style = MaterialTheme.typography.titleMedium, color = content) }
                Text(message, style = MaterialTheme.typography.bodyMedium, color = content)
                action?.let {
                    QuietActionButton(onClick = it.onClick) { Text(stringResource(it.labelRes)) }
                }
            }
        }
    }
}
