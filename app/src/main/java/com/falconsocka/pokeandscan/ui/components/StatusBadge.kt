package com.falconsocka.pokeandscan.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.falconsocka.pokeandscan.ui.theme.AppSpacing
import com.falconsocka.pokeandscan.ui.theme.AppDimensions
import com.falconsocka.pokeandscan.ui.theme.LocalStatusColors

enum class BadgeTone { Ready, Review, Partial, Neutral, Error }

internal data class BadgePalette(val foreground: Color, val background: Color)

@Composable
internal fun BadgeTone.palette(): BadgePalette {
    val status = LocalStatusColors.current
    return when (this) {
        BadgeTone.Ready -> BadgePalette(status.ready.foreground, status.ready.container)
        BadgeTone.Review -> BadgePalette(status.needsReview.foreground, status.needsReview.container)
        BadgeTone.Partial -> BadgePalette(status.partial.foreground, status.partial.container)
        BadgeTone.Error -> BadgePalette(status.error.foreground, status.error.container)
        BadgeTone.Neutral -> BadgePalette(MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surface)
    }
}

/** Non-interactive status chip. Its icon is decorative because the label supplies the meaning. */
@Composable
fun StatusBadge(
    label: String,
    @DrawableRes iconRes: Int? = null,
    tone: BadgeTone = BadgeTone.Neutral,
    modifier: Modifier = Modifier
) {
    val palette = tone.palette()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AppDimensions.statusBadgeCornerRadius),
        color = palette.background,
        contentColor = palette.foreground,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.medium, vertical = AppSpacing.xSmall),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconRes?.let {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
